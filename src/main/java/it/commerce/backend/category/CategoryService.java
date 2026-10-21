package it.commerce.backend.category;

import it.commerce.backend.category.dto.CategoryResponse;
import it.commerce.backend.category.dto.CreateCategoryRequest;
import it.commerce.backend.category.dto.UpdateCategoryRequest;
import it.commerce.backend.product.Product;
import it.commerce.backend.product.ProductNotFoundException;
import it.commerce.backend.product.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    private final ProductRepository productRepository;

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }

    public CategoryService(CategoryRepository categoryRepository,
                           ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public CategoryResponse findById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
        return toResponse(category);
    }

    public CategoryResponse create(CreateCategoryRequest request) {
        Category category = new Category();

        category.setName(request.name());
        category.setDescription(request.description());

        Category savedCategory = categoryRepository.save(category);

        return toResponse(savedCategory);
    }

    public CategoryResponse update(Long id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(()-> new CategoryNotFoundException("Category not found"));

        category.setName(request.name());
        category.setDescription(request.description());

        Category updatedCategory = categoryRepository.save(category);

        return toResponse(updatedCategory);
    }

    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new CategoryNotFoundException("Category not found"));

        if (productRepository.existsByCategory_Id(id)) {
            throw new CategoryInUseException(
                    "Cannot delete a category associated with products"
            );
        }

        categoryRepository.delete(category);
    }
}
