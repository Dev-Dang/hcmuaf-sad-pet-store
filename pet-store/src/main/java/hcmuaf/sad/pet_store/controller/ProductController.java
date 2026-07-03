package hcmuaf.sad.pet_store.controller;

import hcmuaf.sad.pet_store.dto.response.ApiResponse;
import hcmuaf.sad.pet_store.dto.response.ProductResponse;
import hcmuaf.sad.pet_store.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ─── Thymeleaf Page ───────────────────────────────────────────────────────

    @GetMapping("/products")
    public String productsPage(Model model) {
        return "product/products";
    }

    @GetMapping("/products/{id}")
    public String productDetailPage(@PathVariable Long id, Model model) {
        model.addAttribute("productId", id);
        return "product/product-detail";
    }

    // ─── REST API ─────────────────────────────────────────────────────────────

    @ResponseBody
    @GetMapping("/api/v1/products")
    public ApiResponse<Page<ProductResponse>> getProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            Pageable pageable) {

        Page<ProductResponse> products;
        if (keyword != null && !keyword.trim().isEmpty()) {
            products = productService.searchActiveProducts(keyword, pageable);
        } else {
            products = productService.getAllActiveProducts(categoryId, pageable);
        }
        return ApiResponse.success(products);
    }

    @ResponseBody
    @GetMapping("/api/v1/products/{id}")
    public ApiResponse<ProductResponse> getProductDetails(@PathVariable Long id) {
        return ApiResponse.success(productService.getActiveProductDetails(id));
    }
}
