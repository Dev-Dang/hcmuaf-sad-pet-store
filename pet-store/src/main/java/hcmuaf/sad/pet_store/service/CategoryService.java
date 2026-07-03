package hcmuaf.sad.pet_store.service;

import hcmuaf.sad.pet_store.entity.Category;

import java.util.List;

public interface CategoryService {
    List<Category> getActiveCategories();
}
