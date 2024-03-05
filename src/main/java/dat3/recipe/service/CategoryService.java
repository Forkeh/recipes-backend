package dat3.recipe.service;

import dat3.recipe.dto.CategoryDto;
import dat3.recipe.entity.Category;
import dat3.recipe.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Service
public class CategoryService {

    CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<String> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        //Convert from list of Categories to DTO-type, list of Strings
        return categories.stream()
                .map((c) -> new String(c.getName()))
                .toList();
    }

    public CategoryDto addCategory(CategoryDto categoryDto) {
        Category category = new Category(categoryDto.getName());
        categoryRepository.save(category);
        return new CategoryDto(category);
    }
}
