package it.commerce.backend.category;

import it.commerce.backend.category.dto.CategoryResponse;
import it.commerce.backend.category.dto.CreateCategoryRequest;
import it.commerce.backend.category.dto.UpdateCategoryRequest;
import it.commerce.backend.product.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void findAllShouldReturnCategories() {
        // Arrange
        Category category = new Category();
        ReflectionTestUtils.setField(category, "id", 1L);
        category.setName("Illuminazione");
        category.setDescription("Illuminazione per interni");

        List<Category> categories = List.of(category);

        when(categoryRepository.findAll()).thenReturn(categories);

        // Act
        List<CategoryResponse> categoryResponses = categoryService.findAll();

        // Assert

        assertEquals(1, categoryResponses.size());
        assertEquals(1L, categoryResponses.getFirst().id());
        assertEquals("Illuminazione", categoryResponses.getFirst().name());
        assertEquals("Illuminazione per interni", categoryResponses.getFirst().description());

        verify(categoryRepository).findAll();
    }
    @Test
    void findByIdShouldReturnCategoryWhenCategoryExists() {
        // Arrange
        Category category = new Category();
        ReflectionTestUtils.setField(category, "id", 1L);
        category.setName("Illuminazione");
        category.setDescription("Illuminazione per interni");

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        // Act
        CategoryResponse response = categoryService.findById(1L);

        // Assert
        assertEquals(1L, response.id());
        assertEquals("Illuminazione", response.name());
        assertEquals("Illuminazione per interni", response.description());

        verify(categoryRepository).findById(1L);
    }

    @Test
    void findByIdShouldThrowNotFoundWhenCategoryDoesNotExist() {
        //Arrange
        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());
        //Act and Assert
        CategoryNotFoundException exception = assertThrows(
                CategoryNotFoundException.class,
                () -> categoryService.findById(99L)
        );

        assertEquals("Category not found", exception.getMessage());
        verify(categoryRepository).findById(99L);
    }

    @Test
    void createShouldReturnCategoryWhenCategoryIsCreated() {
        // Arrange
        CreateCategoryRequest request = new CreateCategoryRequest(
                "Illuminazione",
                "Illuminazione per interni"
        );

        Category category = new Category();
        ReflectionTestUtils.setField(category, "id", 1L);
        category.setName("Illuminazione");
        category.setDescription("Illuminazione per interni");

        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> {
                    Category categoryToSave = invocation.getArgument(0);

                    ReflectionTestUtils.setField(categoryToSave, "id", 1L);

                    return categoryToSave;
                });

        // Act
        CategoryResponse createdCategory = categoryService.create(request);

        // Assert
        assertEquals(1L, createdCategory.id());
        assertEquals("Illuminazione", createdCategory.name());
        assertEquals("Illuminazione per interni", createdCategory.description());

        verify(categoryRepository).save(any(Category.class));
    }


    @Test
    void updateShouldReturnCategoryWhenCategoryIsUpdated() {
        // Arrange
        UpdateCategoryRequest request = new UpdateCategoryRequest(
                "Illuminazione",
                "Illuminazione per interni"
        );

        Category categoryToUpdate = new Category();
        ReflectionTestUtils.setField(categoryToUpdate, "id", 1L);
        categoryToUpdate.setName("Vecchio nome");
        categoryToUpdate.setDescription("Vecchia descrizione");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoryToUpdate));
        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> {
                    Category categoryToSave = invocation.getArgument(0);

                    ReflectionTestUtils.setField(categoryToSave, "id", 1L);

                    return categoryToSave;
                });

        // Act
        CategoryResponse updatedCategory = categoryService.update(1L, request);

        // Assert
        assertEquals(1L, updatedCategory.id());
        assertEquals("Illuminazione", updatedCategory.name());
        assertEquals("Illuminazione per interni", updatedCategory.description());

        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateShouldThrowNotFoundWhenCategoryDoesNotExists() {
        // Arrange
        UpdateCategoryRequest request = new UpdateCategoryRequest(
                "Illuminazione",
                "Illuminazione per interni"
        );

        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        CategoryNotFoundException exception = assertThrows(
                CategoryNotFoundException.class,
                () -> categoryService.update(99L, request)
        );

        // Assert
        assertEquals("Category not found", exception.getMessage());

        verify(categoryRepository).findById(99L);
        verify(categoryRepository, never()).save(any(Category.class));
    }
    @Test
    void deleteShouldDeleteCategoryWhenCategoryIsNotInUse() {
        // Arrange
        Category category = new Category();
        ReflectionTestUtils.setField(category, "id", 1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.existsByCategory_Id(1L)).thenReturn(false);

        // Act
        categoryService.delete(1L);

        // Assert
        verify(categoryRepository).findById(1L);
        verify(productRepository).existsByCategory_Id(1L);
        verify(categoryRepository).delete(category);
    }
    @Test
    void deleteShouldThrowCategoryInUseWhenCategoryHasProducts() {
        // Arrange
        Category category = new Category();
        ReflectionTestUtils.setField(category, "id", 1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.existsByCategory_Id(1L)).thenReturn(true);

        // Act
        CategoryInUseException exception = assertThrows(
                CategoryInUseException.class,
                () -> categoryService.delete(1L)
        );

        assertEquals("Cannot delete a category associated with products", exception.getMessage());

        // Assert
        verify(categoryRepository).findById(1L);
        verify(productRepository).existsByCategory_Id(1L);
        verify(categoryRepository, never()).delete(category);
    }
    @Test
    void deleteShouldThrowNotFoundWhenCategoryDoesNotExist() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        CategoryNotFoundException exception = assertThrows(
                CategoryNotFoundException.class,
                () -> categoryService.delete(1L)
        );

        assertEquals("Category not found", exception.getMessage());

        // Assert
        verify(categoryRepository).findById(1L);
        verify(productRepository, never()).existsByCategory_Id(1L);
        verify(categoryRepository, never()).delete(any(Category.class));
    }

}
