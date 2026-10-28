package it.commerce.backend.product;

import it.commerce.backend.category.Category;
import it.commerce.backend.category.CategoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class ProductRepositoryIntegrationTest {

    private Product createProduct(
            String name,
            String description,
            BigDecimal price,
            int stockQuantity,
            Category category
    ) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStockQuantity(stockQuantity);
        product.setCreatedAt(LocalDateTime.now());
        product.setCategory(category);

        return productRepository.save(product);
    }

    private Category createCategory(String name, String description) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);

        return categoryRepository.save(category);
    }

    private void assertProductEquals(
            Product product,
            String expectedName,
            String expectedDescription,
            BigDecimal expectedPrice,
            int expectedStockQuantity,
            String expectedCategoryName,
            String expectedCategoryDescription
    ) {
        assertEquals(expectedName, product.getName());
        assertEquals(expectedDescription, product.getDescription());
        assertEquals(expectedPrice, product.getPrice());
        assertEquals(expectedStockQuantity, product.getStockQuantity());
        assertEquals(expectedCategoryName, product.getCategory().getName());
        assertEquals(expectedCategoryDescription, product.getCategory().getDescription());
    }

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DataSource dataSource;

    @PersistenceContext
    private EntityManager entityManager;

    @Container
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:18-alpine")
                    .withDatabaseName("ecommerce_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void contextLoads() {
    }

    @Test
    void printDatabaseUrl() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            System.out.println(connection.getMetaData().getURL());
        }
    }

    @Test
    void saveAndFindByIdShouldReturnProductWithCategory() {

        Category category = createCategory("Illuminazione Test", "Categoria di test");

        Product product = createProduct(
                "Lampada Led",
                "Lampada da interno con montaggio a parete",
                new BigDecimal("30.00"),
                50,
                category);

        assertNotNull(product.getId());

        productRepository.flush();
        entityManager.clear();

        Product savedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertProductEquals(
                savedProduct,
                "Lampada Led",
                "Lampada da interno con montaggio a parete",
                new BigDecimal("30.00"),
                50,
                "Illuminazione Test",
                "Categoria di test"
        );
    }
    @Test
    void findByIdShouldReturnEmptyWhenProductDoesNotExist() {
        assertEquals(Optional.empty(), productRepository.findById(999L));
    }
    @Test
    void findAllShouldReturnProductsWithCategories() {

        Category category = createCategory("Illuminazione Test", "Categoria di test");

        Product product1 = createProduct(
                "Prodotto 1 Test",
                "Descrizione 1 Test",
                new BigDecimal("30.00"),
                50,
                category);

        Product product2 = createProduct(
                "Prodotto 2 Test",
                "Descrizione 2 Test",
                new BigDecimal("45.00"),
                70,
                category);

        assertNotNull(product1.getId());
        assertNotNull(product2.getId());

        productRepository.flush();
        entityManager.clear();

        List<Product> products = productRepository.findAll();

        assertTrue(products.size() >= 2);

        Product savedProduct1 = products.stream()
                .filter(product -> product.getId().equals(product1.getId()))
                .findFirst()
                .orElseThrow();

        Product savedProduct2 = products.stream()
                .filter(product -> product.getId().equals(product2.getId()))
                .findFirst()
                .orElseThrow();

        assertProductEquals(
                savedProduct1,
                "Prodotto 1 Test",
                "Descrizione 1 Test",
                new BigDecimal("30.00"),
                50,
                "Illuminazione Test",
                "Categoria di test"
        );
        assertProductEquals(
                savedProduct2,
                "Prodotto 2 Test",
                "Descrizione 2 Test",
                new BigDecimal("45.00"),
                70,
                "Illuminazione Test",
                "Categoria di test"
        );

    }
    @Test
    void findAllShouldRespectPagination() {

        Category category = createCategory("Illuminazione Test", "Categoria di test");

        Product product1 = createProduct(
                "Prodotto 1 Test",
                "Descrizione 1 Test",
                new BigDecimal("30.00"),
                50,
                category);

        Product product2 = createProduct(
                "Prodotto 2 Test",
                "Descrizione 2 Test",
                new BigDecimal("45.00"),
                70,
                category);

        Product product3 = createProduct(
                "Prodotto 3 Test",
                "Descrizione 3 Test",
                new BigDecimal("10.00"),
                30,
                category);

        Product product4 = createProduct(
                "Prodotto 4 Test",
                "Descrizione 4 Test",
                new BigDecimal("95.00"),
                400,
                category);

        Product product5 = createProduct(
                "Prodotto 5 Test",
                "Descrizione 5 Test",
                new BigDecimal("130.00"),
                500,
                category);

        assertNotNull(product1.getId());
        assertNotNull(product2.getId());
        assertNotNull(product3.getId());
        assertNotNull(product4.getId());
        assertNotNull(product5.getId());

        productRepository.flush();
        entityManager.clear();

        Pageable pageable = PageRequest.of(0, 2);

        Page<Product> productsPage = productRepository.findAll(pageable);

        assertEquals(2, productsPage.getContent().size());
        assertEquals(2, productsPage.getSize());
        assertEquals(0, productsPage.getNumber());
        assertEquals(5, productsPage.getTotalElements());
        assertEquals(3, productsPage.getTotalPages());
    }
    @Test
    void existsByCategory_IdShouldReturnTrueWhenCategoryIsAssociatedWithProduct() {

        Category category = createCategory("Attrezzatura Test", "Categoria di test");

        Product product = createProduct(
                "Prodotto Test Esistenza Categoria",
                "Prodotto Test Esistenza Categoria Descrizione",
                new BigDecimal("30.00"),
                50,
                category);

        assertNotNull(product.getId());
        productRepository.flush();
        entityManager.clear();

        assertTrue(productRepository.existsByCategory_Id(category.getId()));
    }
    @Test
    void existsByCategory_IdShouldReturnFalseWhenCategoryIsNotAssociatedWithProduct() {
        Category category = createCategory("Attrezzatura Test", "Categoria di test");

        categoryRepository.flush();
        entityManager.clear();

        assertFalse(productRepository.existsByCategory_Id(category.getId()));
    }
    @Test
    void saveShouldFailWhenCategoryNameAlreadyExists() {

        createCategory("Attrezzatura Test", "Categoria di test");

        Category repeatedCategory = new Category();
        repeatedCategory.setName("Attrezzatura Test");
        repeatedCategory.setDescription("Categoria di test");

        assertThrows(DataIntegrityViolationException.class, ()-> categoryRepository.save(repeatedCategory));
    }
}






















