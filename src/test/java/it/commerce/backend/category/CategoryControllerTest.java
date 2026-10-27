package it.commerce.backend.category;

import it.commerce.backend.category.dto.CategoryResponse;
import it.commerce.backend.category.dto.CreateCategoryRequest;
import it.commerce.backend.category.dto.UpdateCategoryRequest;
import it.commerce.backend.product.ProductController;
import it.commerce.backend.product.ProductService;
import it.commerce.backend.product.dto.CreateProductRequest;
import it.commerce.backend.product.dto.ProductResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    public void findAllShouldReturnCategories() throws Exception {

        List<CategoryResponse> categories = List.of(new CategoryResponse(
                1L,
                "Illuminazione",
                "Illuminazione per interni ed esterni"
        ));

        when(categoryService.findAll()).thenReturn(categories);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Illuminazione"))
                .andExpect(jsonPath("$[0].description").value("Illuminazione per interni ed esterni"));

        verify(categoryService).findAll();

    }
    @Test
    public void findByIdShouldReturnCategoryWhenCategoryExists() throws Exception {

        CategoryResponse category = new CategoryResponse(
                1L,
                "Illuminazione",
                "Illuminazione per interni ed esterni"
        );

        when(categoryService.findById(1L)).thenReturn(category);

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Illuminazione"))
                .andExpect(jsonPath("$.description").value("Illuminazione per interni ed esterni"));

        verify(categoryService).findById(1L);

    }
    @Test
    public void findByIdShouldReturnNotFoundWhenCategoryDoesNotExist() throws Exception {

        when(categoryService.findById(99L)).thenThrow(new CategoryNotFoundException("Category not found"));

        mockMvc.perform(get("/api/categories/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Category not found"));

        verify(categoryService).findById(99L);
    }
    @Test
    public void createShouldReturnCreatedCategory() throws Exception {

        CategoryResponse mockCategory = new CategoryResponse(
                1L,
                "Illuminazione",
                "Illuminazione per interni ed esterni"
        );

        when(categoryService.create(any(CreateCategoryRequest.class))).thenReturn(mockCategory);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Illuminazione",
                                    "description": "Illuminazione per interni ed esterni"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Illuminazione"))
                .andExpect(jsonPath("$.description").value("Illuminazione per interni ed esterni"));

        verify(categoryService).create(any(CreateCategoryRequest.class));

    }
    @Test
    public void createShouldReturnBadRequestWhenNameIsBlank() throws Exception {

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "",
                                    "description": "Illuminazione per interni ed esterni"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).create(any(CreateCategoryRequest.class));
    }
    @Test
    public void updateShouldReturnUpdatedCategory() throws Exception {

        CategoryResponse newCategory = new CategoryResponse(
                1L,
                "Illuminazione",
                "Illuminazione per interni ed esterni"
        );

        when(categoryService.update(eq(1L), any(UpdateCategoryRequest.class))).thenReturn(newCategory);

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Illuminazione",
                                    "description": "Illuminazione per interni ed esterni"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Illuminazione"))
                .andExpect(jsonPath("$.description").value("Illuminazione per interni ed esterni"));

        verify(categoryService).update(eq(1L), any(UpdateCategoryRequest.class));
    }
    @Test
    public void updateShouldReturnNotFoundWhenCategoryDoesNotExist() throws Exception {

        when(categoryService.update(eq(1L), any(UpdateCategoryRequest.class))).thenThrow(new CategoryNotFoundException("Category not found"));

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Illuminazione",
                                    "description": "Illuminazione per interni ed esterni"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Category not found"));

        verify(categoryService).update(eq(1L), any(UpdateCategoryRequest.class));
    }
    @Test
    public void updateShouldReturnBadRequestWhenNameIsBlank() throws Exception {

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "",
                                    "description": "Illuminazione per interni ed esterni"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).update(eq(1L),any(UpdateCategoryRequest.class));
    }
    @Test
    public void deleteShouldReturnNoContentWhenCategoryIsDeleted() throws Exception {

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent());

        verify(categoryService).delete(1L);
    }
    @Test
    public void deleteShouldReturnNotFoundWhenCategoryDoesNotExist() throws Exception {

        doThrow(new CategoryNotFoundException("Category not found")).when(categoryService).delete(1L);

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Category not found"));

        verify(categoryService).delete(1L);
    }
    @Test
    public void deleteShouldReturnConflictWhenCategoryIsInUse() throws Exception {

        doThrow(new CategoryInUseException("Cannot delete a category associated with products")).when(categoryService).delete(1L);

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$").value("Cannot delete a category associated with products"));

        verify(categoryService).delete(1L);
    }
}
