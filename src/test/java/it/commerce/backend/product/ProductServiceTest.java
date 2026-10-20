package it.commerce.backend.product;

import it.commerce.backend.product.dto.CreateProductRequest;
import it.commerce.backend.product.dto.ProductResponse;
import it.commerce.backend.product.dto.UpdateProductRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void findAllShouldReturnPage() {
        // Arrange
        Product product1 = new Product();
        product1.setName("Lampada LED 20W");
        product1.setDescription("Lampada da 20W");
        product1.setPrice(new BigDecimal("12.50"));
        product1.setStockQuantity(30);
        product1.setCreatedAt(LocalDateTime.now());

        Product product2 = new Product();
        product2.setName("Lampada LED 30W");
        product2.setDescription("Lampada da 30W");
        product2.setPrice(new BigDecimal("14.50"));
        product2.setStockQuantity(30);
        product2.setCreatedAt(LocalDateTime.now());

        List<Product> products = List.of(product1, product2);

        Pageable pageable = PageRequest.of(
                0, 2, Sort.by("id").ascending()
        );

        Page<Product> productPages = new PageImpl<>(products, pageable, 3);

        when(productRepository.findAll(pageable)).thenReturn(productPages);

        // Act
        Page<ProductResponse> pageResult = productService.findAll(pageable);

        // Assert
        assertEquals(3, pageResult.getTotalElements());
        assertEquals(2, pageResult.getTotalPages());
        assertEquals(2, pageResult.getNumberOfElements());
        assertEquals(pageable, pageResult.getPageable());

        assertEquals("Lampada LED 20W",
                pageResult.getContent().get(0).name());
        assertEquals("Lampada LED 30W",
                pageResult.getContent().get(1).name());

        verify(productRepository).findAll(pageable);
    }

    @Test
    void findByIdShouldReturnProductWhenProductExists() {
        //Arrange
        Product product = new Product();
        product.setName("Lampada LED");
        product.setDescription("Lampada da 20W");
        product.setPrice(new BigDecimal("12.50"));
        product.setStockQuantity(30);
        product.setCreatedAt(LocalDateTime.now());

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));
        //Act
        ProductResponse response = productService.findById(1L);
        //Assert
        assertEquals("Lampada LED", response.name());
        assertEquals(new BigDecimal("12.50"), response.price());
        assertEquals(30, response.stockQuantity());
    }

    @Test
    void findByIdShouldThrowNotFoundWhenProductDoesNotExist() {
        //Arrange
        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());
        //Act and Assert
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productService.findById(99L)
        );

        assertEquals("Product not found", exception.getMessage());
    }


    @Test
    void createShouldReturnProductWhenProductIsCreated() {
        // Arrange
        CreateProductRequest request = new CreateProductRequest(
                "Striscia LED",
                "Striscia da 20m a led, 4000k",
                new BigDecimal("15.00"),
                50
        );

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> {
                    Product productToSave = invocation.getArgument(0);

                    try {
                        var idField = Product.class.getDeclaredField("id");
                        idField.setAccessible(true);
                        idField.set(productToSave, 1L);
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }

                    return productToSave;
                });

        // Act
        ProductResponse createdProduct = productService.create(request);

        // Assert
        assertEquals(1L, createdProduct.id());
        assertEquals("Striscia LED", createdProduct.name());
        assertEquals("Striscia da 20m a led, 4000k", createdProduct.description());
        assertEquals(new BigDecimal("15.00"), createdProduct.price());
        assertEquals(50, createdProduct.stockQuantity());

        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateShouldReturnProductWhenProductExists() {
        // Arrange
        Product existingProduct = new Product();
        existingProduct.setName("Vecchia lampada");
        existingProduct.setDescription("Descrizione precedente");
        existingProduct.setPrice(new BigDecimal("10.00"));
        existingProduct.setStockQuantity(10);
        existingProduct.setCreatedAt(LocalDateTime.now());

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));

        UpdateProductRequest request = new UpdateProductRequest(
                "Striscia LED",
                "Striscia da 20m a led, 4000k",
                new BigDecimal("15.00"),
                50
        );

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ProductResponse updatedProduct = productService.update(1L, request);

        // Assert
        assertEquals("Striscia LED", updatedProduct.name());
        assertEquals("Striscia da 20m a led, 4000k", updatedProduct.description());
        assertEquals(new BigDecimal("15.00"), updatedProduct.price());
        assertEquals(50, updatedProduct.stockQuantity());

        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateShouldThrowNotFoundWhenProductDoesNotExist() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        UpdateProductRequest request = new UpdateProductRequest(
                "Striscia LED",
                "Striscia da 20m a led, 4000k",
                new BigDecimal("15.00"),
                50
        );
        // Act and Assert
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class, () -> productService.update(1L, request));

        assertEquals("Product not found", exception.getMessage());
    }

    @Test
    void deleteShouldDeleteProductWhenProductExists() {
        // Arrange
        Product product = new Product();

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));
        // Act
        productService.delete(1L);
        // Assert
        verify(productRepository).findById(1L);
        verify(productRepository).delete(product);
    }

    @Test
    void deleteShouldThrowNotFoundWhenProductDoesNotExist() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        // Act and Assert
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class, () -> productService.delete(1L));
        assertEquals("Product not found", exception.getMessage());

        verify(productRepository).findById(1L);
        verify(productRepository, never()).delete(any(Product.class));
    }

}
