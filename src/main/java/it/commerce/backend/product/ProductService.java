package it.commerce.backend.product;

import it.commerce.backend.product.dto.CreateProductRequest;
import it.commerce.backend.product.dto.ProductResponse;
import it.commerce.backend.product.dto.UpdateProductRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getCreatedAt()
        );
    }

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponse> findAll() {
        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ProductResponse findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        return toResponse(product);
    }

    public ProductResponse create(CreateProductRequest request) {
        Product product = new Product();

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());
        product.setCreatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);

        return toResponse(savedProduct);
    }

    public ProductResponse update(Long id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(()-> new ProductNotFoundException("Product not found"));

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());

        Product updatedProduct = productRepository.save(product);

        return toResponse(updatedProduct);
    }

    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(()-> new ProductNotFoundException("Product not found"));
        productRepository.delete(product);
    }

}
