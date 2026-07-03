package hcmuaf.sad.pet_store.service.impl;

import hcmuaf.sad.pet_store.entity.Category;
import hcmuaf.sad.pet_store.repository.CategoryRepository;
import hcmuaf.sad.pet_store.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<Category> getActiveCategories() {
        return categoryRepository.findAllByIsActiveTrueAndDeletedAtIsNull();
    }
}
