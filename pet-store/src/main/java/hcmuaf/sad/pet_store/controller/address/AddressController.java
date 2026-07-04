package hcmuaf.sad.pet_store.controller.address;

import hcmuaf.sad.pet_store.dto.request.AddressRequest;
import hcmuaf.sad.pet_store.exception.BusinessException;
import hcmuaf.sad.pet_store.exception.ErrorCode;
import hcmuaf.sad.pet_store.mapper.AddressMapper;
import hcmuaf.sad.pet_store.model.ShippingAddress;
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
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/account/addresses")
public class AddressController {
    private final MapProvider mapProvider;

    public AddressController(MapProvider mapProvider) {
        this.mapProvider = mapProvider;
    }

    @GetMapping
    public String listAddresses(HttpSession session, Model model) {
        String userCode = (String) session.getAttribute("userCode");

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
        return Map.of("predictions", mapProvider.autocomplete(keyword));
    }

    @GetMapping("/place-details")
    @ResponseBody
    public MapPlaceResult placeDetails(@RequestParam("placeId") String placeId) {
        // [8.6.6] Lấy địa chỉ đầy đủ và tọa độ theo mã địa điểm
        return mapProvider.placeDetails(placeId);
    }

    @GetMapping("/reverse-geocode")
    @ResponseBody
    public Map<String, Object> reverseGeocode(@RequestParam("lat") BigDecimal latitude,
                                              @RequestParam("lng") BigDecimal longitude) {
        // [8.7.4] Gửi tọa độ lấy địa chỉ tương ứng
        MapPlaceResult result = mapProvider.reverseGeocode(latitude, longitude);
        Map<String, Object> response = new HashMap<>();
        response.put("result", result);
        return response;
    }

    @PostMapping
    public String createAddress(@Valid @ModelAttribute("addressRequest") AddressRequest request,
                                BindingResult bindingResult,
                                HttpSession session,
                                Model model) {
        String userCode = (String) session.getAttribute("userCode");

        // [8.2.3] Kiểm tra dữ liệu hợp lệ
        if (bindingResult.hasErrors()) {
            prepareForm(model, request, false, null);
            return "account/address-form";
        }

        MapPlaceResult placeResult;
        try {
            // [8.2.3] Xác định lại tọa độ theo placeId
            placeResult = mapProvider.placeDetails(request.getPlaceId());
        } catch (BusinessException e) {
            bindingResult.rejectValue("placeId", "address.coords", e.getErrorCode().getMessage());
            prepareForm(model, request, false, null);
            return "account/address-form";
        }

        // [8.2.4] Chuẩn bị dữ liệu địa chỉ mới
        ShippingAddress address = AddressMapper.toModel(request, placeResult);

        // [8.2.4] Lưu địa chỉ giao hàng mới
        address.createForUser(userCode);
        return "redirect:/account/addresses";
    }

    @GetMapping("/{addressId}/edit")
    public String showEditForm(@PathVariable String addressId,
                               HttpSession session,
                               Model model) {
        String userCode = (String) session.getAttribute("userCode");

        // [8.3.1] Truy xuất chi tiết địa chỉ được chọn
        ShippingAddress address = ShippingAddress.findByAddressIdAndUserCode(addressId, userCode);

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
        String userCode = (String) session.getAttribute("userCode");
        ShippingAddress current = ShippingAddress.findByAddressIdAndUserCode(addressId, userCode);

        // [8.3.4] Kiểm tra dữ liệu hợp lệ
        if (bindingResult.hasErrors()) {
            prepareForm(model, request, true, addressId);
            return "account/address-form";
        }

        MapPlaceResult placeResult;
        try {
            // [8.3.4] Xác định lại tọa độ theo placeId
            placeResult = getPlaceResult(request, current);
        } catch (BusinessException e) {
            bindingResult.rejectValue("placeId", "address.coords", e.getErrorCode().getMessage());
            prepareForm(model, request, true, addressId);
            return "account/address-form";
        }

        ShippingAddress updated = AddressMapper.toModel(request, placeResult);
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
        String userCode = (String) session.getAttribute("userCode");

        // [8.4.3] Kiểm tra địa chỉ được chọn có phải mặc định không
        ShippingAddress address = ShippingAddress.findByAddressIdAndUserCode(addressId, userCode);
        try {
            // [8.4.4] Xóa mềm địa chỉ được chọn
            address.softDelete();
        } catch (BusinessException e) {
            model.addAttribute("addresses", AddressMapper.toDtoList(ShippingAddress.findAllByUserCode(userCode)));
            model.addAttribute("error", e.getErrorCode().getMessage());
            return "account/addresses";
        }

        return "redirect:/account/addresses";
    }

    @PostMapping("/{addressId}/default")
    public String setDefaultAddress(@PathVariable String addressId,
                                    HttpSession session,
                                    Model model) {
        String userCode = (String) session.getAttribute("userCode");
        ShippingAddress address = ShippingAddress.findByAddressIdAndUserCode(addressId, userCode);

        try {
            // [8.5.3] Cập nhật cờ mặc định để chỉ có đúng một địa chỉ mặc định
            address.setAsDefault();
        } catch (BusinessException e) {
            model.addAttribute("addresses", AddressMapper.toDtoList(ShippingAddress.findAllByUserCode(userCode)));
            model.addAttribute("error", e.getErrorCode().getMessage());
            return "account/addresses";
        }
        return "redirect:/account/addresses";
    }

    private MapPlaceResult getPlaceResult(AddressRequest request, ShippingAddress current) {
        if (current.getPlaceId().equals(request.getPlaceId())) {
            return new MapPlaceResult(
                    current.getPlaceId(),
                    current.getFullAddress(),
                    current.getLatitude(),
                    current.getLongitude());
        }
        return mapProvider.placeDetails(request.getPlaceId());
    }

    private void prepareForm(Model model, AddressRequest request, boolean editMode, String addressId) {
        model.addAttribute("addressRequest", request);
        model.addAttribute("editMode", editMode);
        model.addAttribute("addressId", addressId);
    }
}
