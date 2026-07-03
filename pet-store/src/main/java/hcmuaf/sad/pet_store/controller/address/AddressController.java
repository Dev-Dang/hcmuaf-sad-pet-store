package hcmuaf.sad.pet_store.controller.address;

import hcmuaf.sad.pet_store.dto.request.AddressRequest;
import hcmuaf.sad.pet_store.exception.BusinessException;
import hcmuaf.sad.pet_store.exception.ErrorCode;
import hcmuaf.sad.pet_store.mapper.AddressMapper;
import hcmuaf.sad.pet_store.model.ShippingAddress;
import hcmuaf.sad.pet_store.model.enums.EntityType;
import hcmuaf.sad.pet_store.util.BusinessKeyGenerator;
import hcmuaf.sad.pet_store.util.DBUtils;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

@Controller
@RequestMapping("/account/addresses")
public class AddressController {
    private final GoongMap goongMap;

    public AddressController() {
        this.goongMap = new GoongMap();
    }

    @GetMapping
    public String listAddresses(HttpSession session, Model model) {
        String userCode = currentUserCode(session);

        // [8.1.2] Truy xuất danh sách địa chỉ của Customer
        var addresses = ShippingAddress.findAllByUserCode(userCode);

        // [8.1.3-8.1.5] Chuẩn bị dữ liệu hiển thị
        model.addAttribute("addresses", AddressMapper.toDtoList(addresses));
        return "account/addresses";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        // [8.6.1] Trả form nhập thông tin nhận hàng
        prepareForm(model, new AddressRequest(), false, null);
        return "account/address-form";
    }

    @GetMapping("/autocomplete")
    @ResponseBody
    public Map<String, Object> autocomplete(@RequestParam("keyword") String keyword) {
        // [8.6.4] Gửi từ khóa lấy gợi ý địa chỉ
        return Map.of("predictions", goongMap.autocomplete(keyword));
    }

    @GetMapping("/place-details")
    @ResponseBody
    public GoongMap.PlaceResult placeDetails(@RequestParam("placeId") String placeId) {
        // [8.6.6] Lấy địa chỉ đầy đủ và tọa độ theo mã địa điểm
        return goongMap.placeDetails(placeId);
    }

    @GetMapping("/reverse-geocode")
    @ResponseBody
    public Map<String, Object> reverseGeocode(@RequestParam("lat") BigDecimal latitude,
                                              @RequestParam("lng") BigDecimal longitude) {
        // [8.7.4] Gửi tọa độ lấy địa chỉ tương ứng
        return Collections.singletonMap("result", goongMap.reverseGeocode(latitude, longitude));
    }

    @PostMapping
    public String createAddress(@Valid @ModelAttribute("addressRequest") AddressRequest request,
                                BindingResult bindingResult,
                                HttpSession session,
                                Model model) {
        String userCode = currentUserCode(session);

        // [8.2.3] Kiểm tra dữ liệu hợp lệ
        if (bindingResult.hasErrors()) {
            prepareForm(model, request, false, null);
            return "account/address-form";
        }

        GoongMap.PlaceResult placeResult;
        try {
            // [8.2.3] Xác định lại tọa độ theo placeId
            placeResult = goongMap.placeDetails(request.getPlaceId());
        } catch (BusinessException e) {
            bindingResult.rejectValue("placeId", "address.coords", e.getErrorCode().getMessage());
            prepareForm(model, request, false, null);
            return "account/address-form";
        }

        // [8.2.4] Kiểm tra Customer đã có địa chỉ nào chưa
        boolean isDefault = ShippingAddress.findAllByUserCode(userCode).isEmpty();

        // [8.2.4] Chuẩn bị dữ liệu địa chỉ mới
        ShippingAddress address = buildAddress(request, placeResult);
        address.setAddressId(BusinessKeyGenerator.next(EntityType.ADDRESS));
        address.setUserCode(userCode);
        address.setDefault(isDefault);

        // [8.2.4] Lưu địa chỉ giao hàng mới
        DBUtils.tx().executeWithoutResult(status -> address.insert());
        return "redirect:/account/addresses";
    }

    @GetMapping("/{addressId}/edit")
    public String showEditForm(@PathVariable String addressId,
                               HttpSession session,
                               Model model) {
        String userCode = currentUserCode(session);

        // [8.3.1] Truy xuất chi tiết địa chỉ được chọn
        ShippingAddress address = requireAddressOwnedByUser(addressId, userCode);

        // Chuẩn bị dữ liệu form
        prepareForm(model, AddressMapper.toRequest(address), true, addressId);

        // [8.3.2 / 8.6.1] Trả form đã điền sẵn dữ liệu hiện tại
        return "account/address-form";
    }

    @PostMapping("/{addressId}")
    public String updateAddress(@PathVariable String addressId,
                                @Valid @ModelAttribute("addressRequest") AddressRequest request,
                                BindingResult bindingResult,
                                HttpSession session,
                                Model model) {
        String userCode = currentUserCode(session);
        ShippingAddress current = requireAddressOwnedByUser(addressId, userCode);

        // [8.3.4] Kiểm tra dữ liệu hợp lệ
        if (bindingResult.hasErrors()) {
            prepareForm(model, request, true, addressId);
            return "account/address-form";
        }

        GoongMap.PlaceResult placeResult;
        try {
            // [8.3.4] Xác định lại tọa độ theo placeId
            placeResult = goongMap.placeDetails(request.getPlaceId());
        } catch (BusinessException e) {
            bindingResult.rejectValue("placeId", "address.coords", e.getErrorCode().getMessage());
            prepareForm(model, request, true, addressId);
            return "account/address-form";
        }

        ShippingAddress updated = buildAddress(request, placeResult);
        updated.setAddressId(addressId);
        updated.setUserCode(userCode);
        updated.setDefault(current.isDefault());

        // [8.3.5] Cập nhật địa chỉ theo SCD Type 2
        updated.updateDetails();
        return "redirect:/account/addresses";
    }

    @PostMapping("/{addressId}/delete")
    public String deleteAddress(@PathVariable String addressId,
                                HttpSession session,
                                Model model) {
        String userCode = currentUserCode(session);

        // [8.4.3] Kiểm tra địa chỉ được chọn có phải mặc định không
        ShippingAddress address = requireAddressOwnedByUser(addressId, userCode);
        if (address.isDefault()) {
            model.addAttribute("addresses", AddressMapper.toDtoList(ShippingAddress.findAllByUserCode(userCode)));
            model.addAttribute("error", ErrorCode.ADDRESS_IS_DEFAULT.getMessage());
            return "account/addresses";
        }

        // [8.4.4] Xóa mềm địa chỉ được chọn
        address.softDelete();
        return "redirect:/account/addresses";
    }

    @PostMapping("/{addressId}/default")
    public String setDefaultAddress(@PathVariable String addressId,
                                    HttpSession session) {
        String userCode = currentUserCode(session);
        requireAddressOwnedByUser(addressId, userCode);

        // [8.5.3] Cập nhật cờ mặc định để chỉ có đúng một địa chỉ mặc định
        ShippingAddress.updateDefaultAddress(userCode, addressId);
        return "redirect:/account/addresses";
    }

    private ShippingAddress buildAddress(AddressRequest request, GoongMap.PlaceResult placeResult) {
        ShippingAddress address = new ShippingAddress();
        address.setRecipientName(request.getRecipientName());
        address.setPhone(request.getPhone());
        address.setPlaceId(placeResult.placeId());
        address.setFullAddress(placeResult.fullAddress());
        address.setAddressDetail(request.getAddressDetail());
        address.setLatitude(placeResult.latitude());
        address.setLongitude(placeResult.longitude());
        return address;
    }

    private ShippingAddress requireAddressOwnedByUser(String addressId, String userCode) {
        ShippingAddress address = ShippingAddress.findByAddressId(addressId);
        if (address == null || !address.belongsTo(userCode)) {
            throw new BusinessException(ErrorCode.ADDRESS_NOT_FOUND);
        }
        return address;
    }

    private String currentUserCode(HttpSession session) {
        return (String) session.getAttribute("userCode");
    }

    private void prepareForm(Model model, AddressRequest request, boolean editMode, String addressId) {
        model.addAttribute("addressRequest", request);
        model.addAttribute("editMode", editMode);
        model.addAttribute("addressId", addressId);
    }
}
