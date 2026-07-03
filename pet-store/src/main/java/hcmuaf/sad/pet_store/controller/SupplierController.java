package hcmuaf.sad.pet_store.controller;

import hcmuaf.sad.pet_store.dto.request.SupplierRequest;
import hcmuaf.sad.pet_store.exception.BusinessException;
import hcmuaf.sad.pet_store.exception.ErrorCode;
import hcmuaf.sad.pet_store.model.Supplier;
import hcmuaf.sad.pet_store.model.SupplierProduct;
import hcmuaf.sad.pet_store.model.enums.EntityType;
import hcmuaf.sad.pet_store.util.BusinessKeyGenerator;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/suppliers")
public class SupplierController {
    private static final int PAGE_SIZE = 10;

    @GetMapping
    public String listSuppliers(@RequestParam(required = false) String keyword,
                                @RequestParam(defaultValue = "1") int page,
                                Model model) {
        int safePage = normalizePage(page);
        String normalizedKeyword = normalizeKeyword(keyword);

        List<Supplier> suppliers;
        int totalItems;
        if (normalizedKeyword == null) {
            // Truy xuất danh sách nhà cung cấp
            suppliers = Supplier.findAll(safePage, PAGE_SIZE);
            totalItems = Supplier.countAll();
        } else {
            // Tìm nhà cung cấp khớp với từ khóa
            suppliers = Supplier.search(normalizedKeyword, safePage, PAGE_SIZE);
            totalItems = Supplier.countByKeyword(normalizedKeyword);
        }

        if (normalizedKeyword != null && suppliers.isEmpty()) {
            model.addAttribute("emptyResult", true);
        }

        // Hiển thị danh sách nhà cung cấp, có phân trang — dùng thẳng Model, không qua Mapper
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("keyword", keyword);
        model.addAttribute("page", safePage);
        model.addAttribute("totalPages", totalPages(totalItems));
        return "admin/supplier/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        prepareForm(model, new SupplierRequest(), false, null);
        return "admin/supplier/form";
    }

    @PostMapping
    public String createSupplier(@Valid @ModelAttribute("supplierRequest") SupplierRequest request,
                                 BindingResult bindingResult,
                                 Model model) {
        // Kiểm tra dữ liệu hợp lệ
        if (bindingResult.hasErrors()) {
            prepareForm(model, request, false, null);
            return "admin/supplier/form";
        }

        // Kiểm tra trùng tên nhà cung cấp
        if (Supplier.existsByName(request.getName())) {
            bindingResult.rejectValue("name", "supplier.name.exists", ErrorCode.SUPPLIER_NAME_EXISTS.getMessage());
            prepareForm(model, request, false, null);
            return "admin/supplier/form";
        }

        // Chuẩn bị và lưu nhà cung cấp mới
        Supplier supplier = new Supplier();
        supplier.setSupplierCode(BusinessKeyGenerator.next(EntityType.SUPPLIER));
        supplier.setName(request.getName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        supplier.setActive(request.isActive());
        supplier.insert();
        return "redirect:/admin/suppliers";
    }

    @GetMapping("/{id}")
    public String supplierDetail(@PathVariable Long id, Model model) {
        Supplier supplier = requireSupplier(id);
        List<SupplierProduct> products = SupplierProduct.findBySupplierId(id);

        // Hiển thị chi tiết nhà cung cấp và danh sách sản phẩm liên kết
        model.addAttribute("supplier", supplier);
        model.addAttribute("products", products);
        return "admin/supplier/detail";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Supplier supplier = requireSupplier(id);
        prepareForm(model, toRequest(supplier), true, id);
        return "admin/supplier/form";
    }

    @PostMapping("/{id}")
    public String updateSupplier(@PathVariable Long id,
                                 @Valid @ModelAttribute("supplierRequest") SupplierRequest request,
                                 BindingResult bindingResult,
                                 Model model) {
        Supplier supplier = requireSupplier(id);

        if (bindingResult.hasErrors()) {
            prepareForm(model, request, true, id);
            return "admin/supplier/form";
        }

        if (Supplier.existsByNameExcludingId(request.getName(), id)) {
            bindingResult.rejectValue("name", "supplier.name.exists", ErrorCode.SUPPLIER_NAME_EXISTS.getMessage());
            prepareForm(model, request, true, id);
            return "admin/supplier/form";
        }

        // Cập nhật nhà cung cấp (fact table — UPDATE trực tiếp)
        supplier.setName(request.getName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        supplier.setActive(request.isActive());
        supplier.update();
        return "redirect:/admin/suppliers/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteSupplier(@PathVariable Long id, Model model) {
        Supplier supplier = requireSupplier(id);

        // Không cho xóa nếu NCC đang có sản phẩm liên kết
        if (Supplier.countActiveProducts(id) > 0) {
            model.addAttribute("suppliers", Supplier.findAll(1, PAGE_SIZE));
            model.addAttribute("page", 1);
            model.addAttribute("totalPages", totalPages(Supplier.countAll()));
            model.addAttribute("error", ErrorCode.SUPPLIER_HAS_PRODUCTS.getMessage());
            return "admin/supplier/list";
        }

        // Xóa mềm nhà cung cấp
        supplier.softDelete();
        return "redirect:/admin/suppliers";
    }

    private Supplier requireSupplier(Long id) {
        Supplier supplier = Supplier.findById(id);
        if (supplier == null) {
            throw new BusinessException(ErrorCode.SUPPLIER_NOT_FOUND);
        }
        return supplier;
    }

    private SupplierRequest toRequest(Supplier supplier) {
        SupplierRequest request = new SupplierRequest();
        request.setName(supplier.getName());
        request.setContactPerson(supplier.getContactPerson());
        request.setPhone(supplier.getPhone());
        request.setEmail(supplier.getEmail());
        request.setAddress(supplier.getAddress());
        request.setActive(supplier.isActive());
        return request;
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim().toLowerCase();
    }

    private int normalizePage(int page) {
        return Math.max(page, 1);
    }

    private int totalPages(int totalItems) {
        return Math.max(1, (int) Math.ceil((double) totalItems / PAGE_SIZE));
    }

    private void prepareForm(Model model, SupplierRequest request, boolean editMode, Long id) {
        model.addAttribute("supplierRequest", request);
        model.addAttribute("editMode", editMode);
        model.addAttribute("supplierId", id);
    }
}