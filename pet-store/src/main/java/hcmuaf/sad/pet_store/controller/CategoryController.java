package hcmuaf.sad.pet_store.controller;

import hcmuaf.sad.pet_store.dto.response.ApiResponse;
import hcmuaf.sad.pet_store.entity.Category;
import hcmuaf.sad.pet_store.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ApiResponse<List<Category>> getActiveCategories() {
        return ApiResponse.success(categoryService.getActiveCategories());
    }
}
