package it.commerce.backend.product;

import it.commerce.backend.product.dto.CreateProductRequest;
import it.commerce.backend.product.dto.ProductResponse;

import it.commerce.backend.product.dto.UpdateProductRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    public void createShouldReturnCreated() throws Exception {

        ProductResponse mockProduct = new ProductResponse(
                1L,
                "Piantana LED",
                "Piantana LED 30W, 6000k",
                new BigDecimal("29.99"),
                30,
                LocalDateTime.now());
        when(productService.create(any(CreateProductRequest.class))).thenReturn(mockProduct);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Piantana LED",
                                    "description": "Piantana LED 30W, 6000k",
                                    "price": 29.99,
                                    "stockQuantity": 30
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Piantana LED"))
                .andExpect(jsonPath("$.price").value(29.99))
                .andExpect(jsonPath("$.stockQuantity").value(30));

    }

    @Test
    public void createShouldReturnBadRequestWhenNameIsBlank() throws Exception {

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "",
                                    "description": "Piantana LED 30W, 6000k",
                                    "price": 29.99,
                                    "stockQuantity": 30
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(productService, never())
                .create(any(CreateProductRequest.class));
    }
    @Test
    public void findAllShouldReturnProducts() throws Exception {

        List<ProductResponse> products = List.of(
                new ProductResponse(
                        1L,
                        "Piantana LED",
                        "Piantana LED 30W, 6000k",
                        new BigDecimal("29.99"),
                        30,
                        LocalDateTime.now()));

        when(productService.findAll()).thenReturn(products);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Piantana LED"))
                .andExpect(jsonPath("$[0].price").value(29.99))
                .andExpect(jsonPath("$[0].stockQuantity").value(30));

        verify(productService).findAll();
    }
    @Test
    public void findByIdShouldReturnProduct() throws Exception {

        ProductResponse product = new ProductResponse(
                1L,
                "Piantana LED",
                "Piantana LED 30W, 6000k",
                new BigDecimal("29.99"),
                30,
                LocalDateTime.now());

        when(productService.findById(1L)).thenReturn(product);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Piantana LED"))
                .andExpect(jsonPath("$.price").value(29.99))
                .andExpect(jsonPath("$.stockQuantity").value(30));

        verify(productService).findById(1L);
    }
    @Test
    public void findByIdShouldReturnNotFoundWhenProductDoesNotExist() throws Exception {

        when(productService.findById(99L)).thenThrow(new ProductNotFoundException("Product not found"));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound());

        verify(productService).findById(99L);
    }
    @Test
    public void updateShouldReturnUpdatedProduct() throws Exception {

        ProductResponse updatedProduct = new ProductResponse(
                1L,
                "Piantana LED Aggiornata",
                "Piantana LED 30W, 6000k",
                new BigDecimal("29.99"),
                30,
                LocalDateTime.now());

        when(productService.update(eq(1L), any(UpdateProductRequest.class)))
                .thenReturn(updatedProduct);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Piantana LED Aggiornata",
                                    "description": "Piantana LED 30W, 6000k",
                                    "price": 29.99,
                                    "stockQuantity": 30
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Piantana LED Aggiornata"))
                .andExpect(jsonPath("$.price").value(29.99))
                .andExpect(jsonPath("$.stockQuantity").value(30));

        verify(productService).update(eq(1L), any(UpdateProductRequest.class));
    }
    @Test
    void updateShouldReturnNotFoundWhenProductDoesNotExist() throws Exception {

        when(productService.update(eq(99L), any(UpdateProductRequest.class)))
                .thenThrow(new ProductNotFoundException("Product not found"));

        mockMvc.perform(put("/api/products/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {
                                    "name": "Piantana LED Aggiornata",
                                    "description": "Piantana LED 30W, 6000k",
                                    "price": 29.99,
                                    "stockQuantity": 30
                                }
                                """))
                .andExpect(status().isNotFound());

        verify(productService).update(eq(99L), any(UpdateProductRequest.class));
    }
    @Test
    void deleteShouldReturnNoContent() throws Exception {

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());

        verify(productService).delete(1L);
    }
    @Test
    void deleteShouldReturnNotFoundWhenProductDoesNotExist() throws Exception {

        doThrow(new ProductNotFoundException("Product not found"))
                .when(productService).delete(99L);

        mockMvc.perform(delete("/api/products/99"))
                .andExpect(status().isNotFound());

        verify(productService).delete(99L);
    }
}
