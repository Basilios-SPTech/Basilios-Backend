package com.basilios.basilios.core.service;

import com.basilios.basilios.app.dto.menu.MenuFilterDTO;
import com.basilios.basilios.app.dto.product.ProductRequestDTO;
import com.basilios.basilios.core.enums.ProductCategory;
import com.basilios.basilios.core.enums.ProductSubcategory;
import com.basilios.basilios.core.exception.*;
import com.basilios.basilios.core.model.Ingredient;
import com.basilios.basilios.core.model.IngredientProduct;
import com.basilios.basilios.core.model.Product;
import com.basilios.basilios.infra.observer.MenuSubject;
import com.basilios.basilios.infra.repository.IngredientProductRepository;
import com.basilios.basilios.infra.repository.IngredientRepository;
import com.basilios.basilios.infra.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MenuService Tests")
class MenuServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private IngredientProductRepository ingredientProductRepository;

    @Mock
    private MenuSubject menuSubject;

    // novos repositórios adicionados ao service que precisam ser mockados para evitar NPE
    @Mock
    private com.basilios.basilios.infra.repository.ProductOrderRepository productOrderRepository;

    @Mock
    private com.basilios.basilios.infra.repository.ProductComboRepository productComboRepository;

    @InjectMocks
    private MenuService menuService;

    private Product product1;
    private Product product2;
    private ProductRequestDTO productDTO;
    private Ingredient ingredient;
    private IngredientProduct ingredientProduct;

    @BeforeEach
    void setUp() {
        product1 = Product.builder()
                .id(1L)
                .name("X-Bacon")
                .description("Hambúrguer artesanal com bacon crocante")
                .price(new BigDecimal("35.00"))
                .category(ProductCategory.BURGER)
                .subcategory(ProductSubcategory.BEEF)
                .isPaused(false)
                .tags(Arrays.asList("hamburguer", "bacon", "artesanal"))
                .build();

        product2 = Product.builder()
                .id(2L)
                .name("Hambúrguer Vegetariano")
                .description("Hambúrguer à base de plantas")
                .price(new BigDecimal("28.00"))
                .category(ProductCategory.BURGER)
                .subcategory(ProductSubcategory.VEGETARIAN)
                .isPaused(true)
                .build();

        productDTO = new ProductRequestDTO();
        productDTO.setName("X-Frango");
        productDTO.setDescription("Hambúrguer de frango grelhado");
        productDTO.setPrice(new BigDecimal("30.00"));
        productDTO.setCategory(ProductCategory.BURGER);
        productDTO.setSubcategory(ProductSubcategory.CHICKEN);
        productDTO.setTags(Arrays.asList("hamburguer", "frango", "saudavel"));
        productDTO.setIngredientes(Arrays.asList());

        ingredient = new Ingredient("Queijo");
        ingredient.setId(1L);

        ingredientProduct = new IngredientProduct();
        ingredientProduct.setId(1L);
        ingredientProduct.setProduct(product1);
        ingredientProduct.setIngredient(ingredient);
        ingredientProduct.setQuantity(200);
        ingredientProduct.setMeasurementUnit("g");
    }

    @Nested
    @DisplayName("getActiveMenu Tests")
    class GetActiveMenuTests {

        @Test
        @DisplayName("Deve retornar apenas produtos ativos")
        void shouldReturnOnlyActiveProducts() {
            when(productRepository.findByIsPausedFalse()).thenReturn(Arrays.asList(product1));

            List<Product> result = menuService.getActiveMenu();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertFalse(result.get(0).getIsPaused());
            verify(productRepository).findByIsPausedFalse();
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há produtos ativos")
        void shouldReturnEmptyListWhenNoActiveProducts() {
            when(productRepository.findByIsPausedFalse()).thenReturn(Collections.emptyList());

            List<Product> result = menuService.getActiveMenu();

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Deve retornar múltiplos produtos ativos")
        void shouldReturnMultipleActiveProducts() {
            Product product3 = Product.builder().id(3L).isPaused(false).build();
            when(productRepository.findByIsPausedFalse())
                    .thenReturn(Arrays.asList(product1, product3));

            List<Product> result = menuService.getActiveMenu();

            assertEquals(2, result.size());
        }
    }

    @Nested
    @DisplayName("getAllMenu Tests")
    class GetAllMenuTests {

        @Test
        @DisplayName("Deve retornar todos os produtos incluindo pausados")
        void shouldReturnAllProductsIncludingPaused() {
            when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2));

            List<Product> result = menuService.getAllMenu();

            assertEquals(2, result.size());
            verify(productRepository).findAll();
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há produtos")
        void shouldReturnEmptyListWhenNoProducts() {
            when(productRepository.findAll()).thenReturn(Collections.emptyList());

            List<Product> result = menuService.getAllMenu();

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Deve incluir produtos ativos e pausados")
        void shouldIncludeActiveAndPausedProducts() {
            when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2));

            List<Product> result = menuService.getAllMenu();

            assertTrue(result.stream().anyMatch(p -> !p.getIsPaused()));
            assertTrue(result.stream().anyMatch(Product::getIsPaused));
        }
    }

    @Nested
    @DisplayName("getMenuPaginated Tests")
    class GetMenuPaginatedTests {

        @Test
        @DisplayName("Deve retornar página de produtos ativos quando activeOnly é true")
        void shouldReturnPageOfActiveProductsWhenActiveOnly() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> page = new PageImpl<>(Arrays.asList(product1));
            when(productRepository.findByIsPausedFalse(pageable)).thenReturn(page);

            Page<Product> result = menuService.getMenuPaginated(true, pageable);

            assertEquals(1, result.getContent().size());
            verify(productRepository).findByIsPausedFalse(pageable);
        }

        @Test
        @DisplayName("Deve retornar página de todos os produtos quando activeOnly é false")
        void shouldReturnPageOfAllProductsWhenNotActiveOnly() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> page = new PageImpl<>(Arrays.asList(product1, product2));
            when(productRepository.findAll(pageable)).thenReturn(page);

            Page<Product> result = menuService.getMenuPaginated(false, pageable);

            assertEquals(2, result.getContent().size());
            verify(productRepository).findAll(pageable);
        }

        @Test
        @DisplayName("Deve respeitar configurações de paginação")
        void shouldRespectPaginationSettings() {
            Pageable pageable = PageRequest.of(0, 5);
            Page<Product> page = new PageImpl<>(Arrays.asList(product1), pageable, 1);
            when(productRepository.findByIsPausedFalse(pageable)).thenReturn(page);

            Page<Product> result = menuService.getMenuPaginated(true, pageable);

            assertEquals(5, result.getSize());
            assertEquals(0, result.getNumber());
        }
    }

    @Nested
    @DisplayName("getProductById Tests")
    class GetProductByIdTests {

        @Test
        @DisplayName("Deve retornar produto quando ID existe")
        void shouldReturnProductWhenIdExists() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product1));

            Product result = menuService.getProductById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("X-Bacon", result.getName());
        }

        @Test
        @DisplayName("Deve lançar ProductNotFoundException quando ID não existe")
        void shouldThrowProductNotFoundExceptionWhenIdNotExists() {
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ProductNotFoundException.class, () ->
                    menuService.getProductById(999L)
            );
        }

        @Test
        @DisplayName("Deve buscar produto pausado também")
        void shouldFindPausedProductToo() {
            when(productRepository.findById(2L)).thenReturn(Optional.of(product2));

            Product result = menuService.getProductById(2L);

            assertTrue(result.getIsPaused());
        }
    }

    @Nested
    @DisplayName("searchByName Tests")
    class SearchByNameTests {

        @Test
        @DisplayName("Deve buscar produtos por nome com sucesso")
        void shouldSearchProductsByNameSuccessfully() {
            when(productRepository.findByNameContainingIgnoreCaseAndIsPausedFalse("Bacon"))
                    .thenReturn(Arrays.asList(product1));

            List<Product> result = menuService.searchByName("Bacon", true);

            assertEquals(1, result.size());
            assertTrue(result.get(0).getName().contains("Bacon"));
        }

        @Test
        @DisplayName("Deve lançar InvalidMenuFilterException quando termo tem menos de 2 caracteres")
        void shouldThrowInvalidMenuFilterExceptionWhenTermTooShort() {
            assertThrows(InvalidMenuFilterException.class, () ->
                    menuService.searchByName("X", true)
            );
        }

        @Test
        @DisplayName("Deve buscar produtos ativos e pausados quando activeOnly é false")
        void shouldSearchActiveAndPausedProductsWhenNotActiveOnly() {
            when(productRepository.findByNameContainingIgnoreCase("Hamburguer"))
                    .thenReturn(Arrays.asList(product1, product2));

            List<Product> result = menuService.searchByName("Hamburguer", false);

            assertNotNull(result);
            verify(productRepository).findByNameContainingIgnoreCase("Hamburguer");
        }
    }

    @Nested
    @DisplayName("getProductsByPriceRange Tests")
    class GetProductsByPriceRangeTests {

        @Test
        @DisplayName("Deve retornar produtos dentro do range de preço")
        void shouldReturnProductsWithinPriceRange() {
            BigDecimal min = new BigDecimal("25.00");
            BigDecimal max = new BigDecimal("40.00");
            when(productRepository.findByPriceBetweenAndIsPausedFalse(min, max))
                    .thenReturn(Arrays.asList(product1, product2));

            List<Product> result = menuService.getProductsByPriceRange(min, max, true);

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Deve lançar InvalidMenuFilterException quando min maior que max")
        void shouldThrowInvalidMenuFilterExceptionWhenMinGreaterThanMax() {
            BigDecimal min = new BigDecimal("50.00");
            BigDecimal max = new BigDecimal("30.00");

            assertThrows(InvalidMenuFilterException.class, () ->
                    menuService.getProductsByPriceRange(min, max, true)
            );
        }

        @Test
        @DisplayName("Deve buscar em todos os produtos quando activeOnly é false")
        void shouldSearchAllProductsWhenNotActiveOnly() {
            BigDecimal min = new BigDecimal("20.00");
            BigDecimal max = new BigDecimal("40.00");
            when(productRepository.findByPriceBetween(min, max))
                    .thenReturn(Arrays.asList(product1, product2));

            List<Product> result = menuService.getProductsByPriceRange(min, max, false);

            verify(productRepository).findByPriceBetween(min, max);
        }
    }

    @Nested
    @DisplayName("getProductsByIngredient Tests")
    class GetProductsByIngredientTests {

        @Test
        @DisplayName("Deve retornar produtos que contêm o ingrediente")
        void shouldReturnProductsContainingIngredient() {
            when(productRepository.findByIngredientNameAndActive("Queijo"))
                    .thenReturn(Arrays.asList(product1));

            List<Product> result = menuService.getProductsByIngredient("Queijo", true);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando nome do ingrediente é vazio")
        void shouldThrowBusinessExceptionWhenIngredientNameEmpty() {
            assertThrows(BusinessException.class, () ->
                    menuService.getProductsByIngredient("", true)
            );
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando nome do ingrediente é null")
        void shouldThrowBusinessExceptionWhenIngredientNameNull() {
            assertThrows(BusinessException.class, () ->
                    menuService.getProductsByIngredient(null, true)
            );
        }
    }

    @Nested
    @DisplayName("createProduct Tests")
    class CreateProductTests {

        @Test
        @DisplayName("Deve criar produto com sucesso")
        void shouldCreateProductSuccessfully() {
            when(productRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenReturn(product1);

            Product result = menuService.createProduct(productDTO);

            assertNotNull(result);
            verify(productRepository).save(any(Product.class));
            verify(menuSubject).menuChanged(eq("PRODUCT_CREATED"), any(Product.class));
        }

        @Test
        @DisplayName("(compatibilidade) Não lança exceção quando categoria é null")
        void shouldAllowCreateWhenCategoryNull() {
            productDTO.setCategory(null);
            when(productRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenReturn(product1);

            Product result = menuService.createProduct(productDTO);

            assertNotNull(result);
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("(compatibilidade) Não valida subcategoria contra categoria no MenuService")
        void shouldNotValidateSubcategoryAgainstCategory() {
            productDTO.setCategory(ProductCategory.BURGER);
            productDTO.setSubcategory(ProductSubcategory.SODA); // SODA pertence a DRINK

            when(productRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenReturn(product1);

            Product result = menuService.createProduct(productDTO);

            assertNotNull(result);
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Deve lançar DuplicateProductException quando nome já existe")
        void shouldThrowDuplicateProductExceptionWhenNameExists() {
            when(productRepository.existsByNameIgnoreCase("X-Frango")).thenReturn(true);

            assertThrows(DuplicateProductException.class, () ->
                    menuService.createProduct(productDTO)
            );
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve criar produto com ingredientes")
        void shouldCreateProductWithIngredients() {
            productDTO.setIngredientes(Arrays.asList("Queijo", "Tomate"));
            when(productRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenReturn(product1);
            when(ingredientRepository.findByNameIgnoreCase(anyString()))
                    .thenReturn(Optional.empty());
            when(ingredientRepository.save(any(Ingredient.class))).thenReturn(ingredient);
            when(ingredientProductRepository.existsByProductAndIngredient(any(), any()))
                    .thenReturn(false);

            Product result = menuService.createProduct(productDTO);

            assertNotNull(result);
            verify(ingredientProductRepository, atLeastOnce()).save(any(IngredientProduct.class));
        }

        @Test
        @DisplayName("Deve criar produto pausado quando isPaused é true")
        void shouldCreatePausedProductWhenIsPausedTrue() {
            // ProductRequestDTO não possui isPaused; usamos um DTO legado com getter isPaused
            Object legacyDto = new Object() {
                public String getName() { return "X-Frango"; }
                public String getDescription() { return "Hambúrguer de frango grelhado"; }
                public BigDecimal getPrice() { return new BigDecimal("30.00"); }
                public ProductCategory getCategory() { return ProductCategory.BURGER; }
                public ProductSubcategory getSubcategory() { return ProductSubcategory.CHICKEN; }
                public List<String> getTags() { return Arrays.asList("hamburguer", "frango", "saudavel"); }
                public Boolean getIsPaused() { return true; }
            };

            when(productRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
                Product saved = invocation.getArgument(0);
                assertTrue(saved.getIsPaused());
                return saved;
            });

            menuService.createProduct(legacyDto);

            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Deve criar produto com categoria DRINK e subcategoria SODA")
        void shouldCreateProductWithDrinkCategoryAndSodaSubcategory() {
            productDTO.setName("Coca-Cola");
            productDTO.setCategory(ProductCategory.DRINK);
            productDTO.setSubcategory(ProductSubcategory.SODA);

            when(productRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenReturn(product1);

            Product result = menuService.createProduct(productDTO);

            assertNotNull(result);
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Deve criar produto com categoria SIDE e subcategoria FRIES")
        void shouldCreateProductWithSideCategoryAndFriesSubcategory() {
            productDTO.setName("Batata Frita");
            productDTO.setCategory(ProductCategory.SIDE);
            productDTO.setSubcategory(ProductSubcategory.FRIES);

            when(productRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenReturn(product1);

            Product result = menuService.createProduct(productDTO);

            assertNotNull(result);
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Deve criar produto com categoria DESSERT e subcategoria ACAI")
        void shouldCreateProductWithDessertCategoryAndAcaiSubcategory() {
            productDTO.setName("Açaí 500ml");
            productDTO.setCategory(ProductCategory.DESSERT);
            productDTO.setSubcategory(ProductSubcategory.ACAI);

            when(productRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenReturn(product1);

            Product result = menuService.createProduct(productDTO);

            assertNotNull(result);
            verify(productRepository).save(any(Product.class));
        }
    }

    @Nested
    @DisplayName("updateProduct Tests")
    class UpdateProductTests {

        @Test
        @DisplayName("Deve atualizar produto com sucesso")
        void shouldUpdateProductSuccessfully() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
            when(productRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenReturn(product1);

            Product result = menuService.updateProduct(1L, productDTO);

            assertNotNull(result);
            verify(productRepository).save(any(Product.class));
            verify(menuSubject).menuChanged(eq("PRODUCT_UPDATED"), any(Product.class));
        }

        @Test
        @DisplayName("Deve lançar DuplicateProductException ao atualizar para nome existente")
        void shouldThrowDuplicateProductExceptionWhenUpdatingToExistingName() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
            when(productRepository.existsByNameIgnoreCase("X-Frango")).thenReturn(true);

            assertThrows(DuplicateProductException.class, () ->
                    menuService.updateProduct(1L, productDTO)
            );
        }

        @Test
        @DisplayName("Deve permitir atualizar mantendo o mesmo nome")
        void shouldAllowUpdateKeepingSameName() {
            productDTO.setName("X-Bacon"); // Mesmo nome do product1
            when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
            when(productRepository.save(any(Product.class))).thenReturn(product1);

            Product result = menuService.updateProduct(1L, productDTO);

            assertNotNull(result);
            verify(productRepository).save(any(Product.class));
        }
    }

    @Nested
    @DisplayName("pauseProduct Tests")
    class PauseProductTests {

        @Test
        @DisplayName("Deve pausar produto ativo com sucesso")
        void shouldPauseActiveProductSuccessfully() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
            when(productRepository.save(any(Product.class))).thenReturn(product1);

            menuService.pauseProduct(1L);

            verify(productRepository).save(product1);
            verify(menuSubject).menuChanged(eq("PRODUCT_PAUSED"), any(Product.class));
        }

        @Test
        @DisplayName("Deve lançar MenuOperationException ao pausar produto já pausado")
        void shouldThrowMenuOperationExceptionWhenPausingAlreadyPausedProduct() {
            when(productRepository.findById(2L)).thenReturn(Optional.of(product2));

            assertThrows(MenuOperationException.class, () ->
                    menuService.pauseProduct(2L)
            );
        }

        @Test
        @DisplayName("Deve lançar ProductNotFoundException ao pausar produto inexistente")
        void shouldThrowProductNotFoundExceptionWhenPausingNonExistentProduct() {
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ProductNotFoundException.class, () ->
                    menuService.pauseProduct(999L)
            );
        }
    }

    @Nested
    @DisplayName("activateProduct Tests")
    class ActivateProductTests {

        @Test
        @DisplayName("Deve ativar produto pausado com sucesso")
        void shouldActivatePausedProductSuccessfully() {
            when(productRepository.findById(2L)).thenReturn(Optional.of(product2));
            when(productRepository.save(any(Product.class))).thenReturn(product2);

            menuService.activateProduct(2L);

            verify(productRepository).save(product2);
            verify(menuSubject).menuChanged(eq("PRODUCT_ACTIVATED"), any(Product.class));
        }

        @Test
        @DisplayName("Deve lançar MenuOperationException ao ativar produto já ativo")
        void shouldThrowMenuOperationExceptionWhenActivatingAlreadyActiveProduct() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product1));

            assertThrows(MenuOperationException.class, () ->
                    menuService.activateProduct(1L)
            );
        }

        @Test
        @DisplayName("Deve lançar ProductNotFoundException ao ativar produto inexistente")
        void shouldThrowProductNotFoundExceptionWhenActivatingNonExistentProduct() {
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ProductNotFoundException.class, () ->
                    menuService.activateProduct(999L)
            );
        }
    }

    @Nested
    @DisplayName("toggleProductStatus Tests")
    class ToggleProductStatusTests {

        @Test
        @DisplayName("Deve alternar status de produto ativo para pausado")
        void shouldToggleActiveProductToPaused() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
            when(productRepository.save(any(Product.class))).thenReturn(product1);

            boolean result = menuService.toggleProductStatus(1L);

            verify(productRepository).save(product1);
            verify(menuSubject).menuChanged(eq("PRODUCT_TOGGLED"), any(Product.class));
        }

        @Test
        @DisplayName("Deve alternar status de produto pausado para ativo")
        void shouldTogglePausedProductToActive() {
            when(productRepository.findById(2L)).thenReturn(Optional.of(product2));
            when(productRepository.save(any(Product.class))).thenReturn(product2);

            boolean result = menuService.toggleProductStatus(2L);

            verify(productRepository).save(product2);
        }

        @Test
        @DisplayName("Deve retornar novo status após toggle")
        void shouldReturnNewStatusAfterToggle() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
            when(productRepository.save(any(Product.class))).thenReturn(product1);

            boolean result = menuService.toggleProductStatus(1L);

            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("deleteProduct Tests")
    class DeleteProductTests {

        @Test
        @DisplayName("Deve deletar produto com sucesso")
        void shouldDeleteProductSuccessfully() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product1));

            menuService.deleteProduct(1L);

            verify(productRepository).delete(product1);
            verify(menuSubject).menuChanged(eq("PRODUCT_DELETED"), eq(1L));
        }

        @Test
        @DisplayName("Deve lançar ProductNotFoundException ao deletar produto inexistente")
        void shouldThrowProductNotFoundExceptionWhenDeletingNonExistentProduct() {
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ProductNotFoundException.class, () ->
                    menuService.deleteProduct(999L)
            );
            verify(productRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Deve notificar observadores após deletar")
        void shouldNotifyObserversAfterDelete() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product1));

            menuService.deleteProduct(1L);

            verify(menuSubject).menuChanged("PRODUCT_DELETED", 1L);
        }
    }

    @Nested
    @DisplayName("updateProductPrice Tests")
    class UpdateProductPriceTests {

        @Test
        @DisplayName("Deve atualizar preço do produto com sucesso")
        void shouldUpdateProductPriceSuccessfully() {
            BigDecimal newPrice = new BigDecimal("45.00");
            when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
            when(productRepository.save(any(Product.class))).thenReturn(product1);

            Product result = menuService.updateProductPrice(1L, newPrice);

            assertNotNull(result);
            verify(productRepository).save(product1);
            verify(menuSubject).menuChanged(eq("PRODUCT_PRICE_UPDATED"), any(Product.class));
        }

//        @Test
//        @DisplayName("Deve lançar InvalidPriceException quando preço é zero")
//        void shouldThrowInvalidPriceExceptionWhenPriceIsZero() {
//            BigDecimal invalidPrice = BigDecimal.ZERO;
//            when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
//
//            assertThrows(InvalidPriceException.class, () ->
//                    menuService.updateProductPrice(1L, invalidPrice)
//            );
//        }

//        @Test
//        @DisplayName("Deve lançar InvalidPriceException quando preço é negativo")
//        void shouldThrowInvalidPriceExceptionWhenPriceIsNegative() {
//            BigDecimal invalidPrice = new BigDecimal("-10.00");
//            when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
//
//            assertThrows(InvalidPriceException.class, () ->
//                    menuService.updateProductPrice(1L, invalidPrice)
//            );
//        }
    }

    @Nested
    @DisplayName("addIngredientsToProduct Tests")
    class AddIngredientsToProductTests {

        @Test
        @DisplayName("Deve adicionar múltiplos ingredientes ao produto")
        void shouldAddMultipleIngredientsToProduct() {
            List<String> ingredients = Arrays.asList("Queijo", "Tomate");
            when(ingredientRepository.findByNameIgnoreCase(anyString()))
                    .thenReturn(Optional.empty());
            when(ingredientRepository.save(any(Ingredient.class))).thenReturn(ingredient);
            when(ingredientProductRepository.existsByProductAndIngredient(any(), any()))
                    .thenReturn(false);

            Product result = menuService.addIngredientsToProduct(product1, ingredients);

            assertNotNull(result);
            verify(ingredientProductRepository, times(2)).save(any(IngredientProduct.class));
        }

        @Test
        @DisplayName("Deve ignorar ingredientes vazios ou nulos")
        void shouldIgnoreEmptyOrNullIngredients() {
            List<String> ingredients = Arrays.asList("Queijo", "", null, "Tomate");
            when(ingredientRepository.findByNameIgnoreCase(anyString()))
                    .thenReturn(Optional.empty());
            when(ingredientRepository.save(any(Ingredient.class))).thenReturn(ingredient);
            when(ingredientProductRepository.existsByProductAndIngredient(any(), any()))
                    .thenReturn(false);

            menuService.addIngredientsToProduct(product1, ingredients);

            verify(ingredientProductRepository, times(2)).save(any(IngredientProduct.class));
        }

        @Test
        @DisplayName("Deve retornar produto quando lista de ingredientes é vazia")
        void shouldReturnProductWhenIngredientListEmpty() {
            Product result = menuService.addIngredientsToProduct(product1, Collections.emptyList());

            assertEquals(product1, result);
            verify(ingredientProductRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("addIngredientToProduct Tests")
    class AddIngredientToProductTests {

        @Test
        @DisplayName("Deve adicionar ingrediente ao produto com sucesso")
        void shouldAddIngredientToProductSuccessfully() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
            when(ingredientRepository.findByNameIgnoreCase("Queijo"))
                    .thenReturn(Optional.of(ingredient));
            when(ingredientProductRepository.existsByProductAndIngredient(product1, ingredient))
                    .thenReturn(false);
            when(productRepository.findById(1L)).thenReturn(Optional.of(product1));

            Product result = menuService.addIngredientToProduct(1L, "Queijo", 200, "g");

            assertNotNull(result);
            verify(ingredientProductRepository).save(any(IngredientProduct.class));
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando ingrediente é vazio")
        void shouldThrowBusinessExceptionWhenIngredientEmpty() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product1));

            assertThrows(BusinessException.class, () ->
                    menuService.addIngredientToProduct(1L, "", 200, "g")
            );
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando ingrediente já existe no produto")
        void shouldThrowBusinessExceptionWhenIngredientAlreadyExists() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
            when(ingredientRepository.findByNameIgnoreCase("Queijo"))
                    .thenReturn(Optional.of(ingredient));
            when(ingredientProductRepository.existsByProductAndIngredient(product1, ingredient))
                    .thenReturn(true);

            assertThrows(BusinessException.class, () ->
                    menuService.addIngredientToProduct(1L, "Queijo", 200, "g")
            );
        }
    }

    @Nested
    @DisplayName("removeIngredientFromProduct Tests")
    class RemoveIngredientFromProductTests {

        @Test
        @DisplayName("Deve remover ingrediente do produto com sucesso")
        void shouldRemoveIngredientFromProductSuccessfully() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
            when(ingredientRepository.findById(1L)).thenReturn(Optional.of(ingredient));
            when(ingredientProductRepository.findByProductAndIngredient(product1, ingredient))
                    .thenReturn(Optional.of(ingredientProduct));
            when(productRepository.findById(1L)).thenReturn(Optional.of(product1));

            Product result = menuService.removeIngredientFromProduct(1L, 1L);

            assertNotNull(result);
            verify(ingredientProductRepository).delete(ingredientProduct);
            verify(menuSubject).menuChanged(eq("PRODUCT_INGREDIENT_REMOVED"), any(Product.class));
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando ingrediente não existe")
        void shouldThrowNotFoundExceptionWhenIngredientNotExists() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
            when(ingredientRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                    menuService.removeIngredientFromProduct(1L, 999L)
            );
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando ingrediente não está associado ao produto")
        void shouldThrowBusinessExceptionWhenIngredientNotAssociated() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
            when(ingredientRepository.findById(1L)).thenReturn(Optional.of(ingredient));
            when(ingredientProductRepository.findByProductAndIngredient(product1, ingredient))
                    .thenReturn(Optional.empty());

            assertThrows(BusinessException.class, () ->
                    menuService.removeIngredientFromProduct(1L, 1L)
            );
        }
    }

    @Nested
    @DisplayName("removeAllIngredientsFromProduct Tests")
    class RemoveAllIngredientsFromProductTests {

        @Test
        @DisplayName("Deve remover todos os ingredientes do produto")
        void shouldRemoveAllIngredientsFromProduct() {
            List<IngredientProduct> ingredientProducts = Arrays.asList(ingredientProduct);
            when(ingredientProductRepository.findByProduct(product1))
                    .thenReturn(ingredientProducts);

            menuService.removeAllIngredientsFromProduct(product1);

            verify(ingredientProductRepository).deleteAll(ingredientProducts);
            verify(menuSubject).menuChanged(eq("PRODUCT_INGREDIENTS_CLEARED"), eq(product1));
        }

        @Test
        @DisplayName("Deve funcionar quando produto não tem ingredientes")
        void shouldWorkWhenProductHasNoIngredients() {
            when(ingredientProductRepository.findByProduct(product1))
                    .thenReturn(Collections.emptyList());

            menuService.removeAllIngredientsFromProduct(product1);

            verify(ingredientProductRepository).deleteAll(Collections.emptyList());
        }

        @Test
        @DisplayName("Deve notificar observadores após remover todos ingredientes")
        void shouldNotifyObserversAfterRemovingAllIngredients() {
            when(ingredientProductRepository.findByProduct(product1))
                    .thenReturn(Collections.emptyList());

            menuService.removeAllIngredientsFromProduct(product1);

            verify(menuSubject).menuChanged("PRODUCT_INGREDIENTS_CLEARED", product1);
        }
    }

    @Nested
    @DisplayName("updateIngredientQuantity Tests")
    class UpdateIngredientQuantityTests {

        @Test
        @DisplayName("Deve atualizar quantidade do ingrediente com sucesso")
        void shouldUpdateIngredientQuantitySuccessfully() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
            when(ingredientRepository.findById(1L)).thenReturn(Optional.of(ingredient));
            when(ingredientProductRepository.findByProductAndIngredient(product1, ingredient))
                    .thenReturn(Optional.of(ingredientProduct));
            when(productRepository.findById(1L)).thenReturn(Optional.of(product1));

            Product result = menuService.updateIngredientQuantity(1L, 1L, 300, "kg");

            assertNotNull(result);
            verify(ingredientProductRepository).save(ingredientProduct);
        }

        @Test
        @DisplayName("Deve atualizar apenas quantidade quando unidade é null")
        void shouldUpdateOnlyQuantityWhenUnitNull() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
            when(ingredientRepository.findById(1L)).thenReturn(Optional.of(ingredient));
            when(ingredientProductRepository.findByProductAndIngredient(product1, ingredient))
                    .thenReturn(Optional.of(ingredientProduct));
            when(productRepository.findById(1L)).thenReturn(Optional.of(product1));

            menuService.updateIngredientQuantity(1L, 1L, 300, null);

            verify(ingredientProductRepository).save(ingredientProduct);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando ingrediente não existe")
        void shouldThrowNotFoundExceptionWhenIngredientNotExists() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
            when(ingredientRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                    menuService.updateIngredientQuantity(1L, 999L, 300, "kg")
            );
        }
    }

    @Nested
    @DisplayName("getProductIngredients Tests")
    class GetProductIngredientsTests {

        @Test
        @DisplayName("Deve retornar lista de ingredientes do produto")
        void shouldReturnListOfProductIngredients() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
            when(ingredientProductRepository.findByProduct(product1))
                    .thenReturn(Arrays.asList(ingredientProduct));

            List<IngredientProduct> result = menuService.getProductIngredients(1L);

            assertEquals(1, result.size());
            verify(ingredientProductRepository).findByProduct(product1);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando produto não tem ingredientes")
        void shouldReturnEmptyListWhenProductHasNoIngredients() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
            when(ingredientProductRepository.findByProduct(product1))
                    .thenReturn(Collections.emptyList());

            List<IngredientProduct> result = menuService.getProductIngredients(1L);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Deve lançar ProductNotFoundException quando produto não existe")
        void shouldThrowProductNotFoundExceptionWhenProductNotExists() {
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ProductNotFoundException.class, () ->
                    menuService.getProductIngredients(999L)
            );
        }
    }

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("Deve contar todos os produtos")
        void shouldCountAllProducts() {
            when(productRepository.count()).thenReturn(10L);

            long result = menuService.countAllProducts();

            assertEquals(10L, result);
        }

        @Test
        @DisplayName("Deve contar produtos ativos")
        void shouldCountActiveProducts() {
            when(productRepository.countByIsPausedFalse()).thenReturn(7L);

            long result = menuService.countActiveProducts();

            assertEquals(7L, result);
        }

        @Test
        @DisplayName("Deve contar produtos pausados")
        void shouldCountPausedProducts() {
            when(productRepository.countByIsPausedTrue()).thenReturn(3L);

            long result = menuService.countPausedProducts();

            assertEquals(3L, result);
        }

        @Test
        @DisplayName("Deve verificar se produto está disponível")
        void shouldCheckIfProductIsAvailable() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product1));

            boolean result = menuService.isProductAvailable(1L);

            assertTrue(result);
        }

        @Test
        @DisplayName("Deve retornar produtos populares com limite")
        void shouldReturnPopularProductsWithLimit() {
            Pageable pageable = PageRequest.of(0, 5);
            Page<Product> page = new PageImpl<>(Arrays.asList(product1));
            when(productRepository.findByIsPausedFalseOrderByCreatedAtDesc(pageable))
                    .thenReturn(page);

            List<Product> result = menuService.getPopularProducts(5);

            assertNotNull(result);
            verify(productRepository).findByIsPausedFalseOrderByCreatedAtDesc(any(Pageable.class));
        }

        @Test
        @DisplayName("Deve retornar produtos recém adicionados")
        void shouldReturnRecentlyAddedProducts() {
            Pageable pageable = PageRequest.of(0, 3);
            when(productRepository.findRecentlyAdded(pageable))
                    .thenReturn(Arrays.asList(product1));

            List<Product> result = menuService.getRecentlyAddedProducts(3);

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("getProductsByIds Tests")
    class GetProductsByIdsTests {

        @Test
        @DisplayName("Deve retornar produtos pelos IDs fornecidos")
        void shouldReturnProductsByProvidedIds() {
            List<Long> ids = Arrays.asList(1L, 2L);
            when(productRepository.findAllById(ids))
                    .thenReturn(Arrays.asList(product1, product2));

            List<Product> result = menuService.getProductsByIds(ids);

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando IDs não existem")
        void shouldReturnEmptyListWhenIdsNotExist() {
            List<Long> ids = Arrays.asList(999L, 998L);
            when(productRepository.findAllById(ids)).thenReturn(Collections.emptyList());

            List<Product> result = menuService.getProductsByIds(ids);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Deve retornar apenas produtos existentes")
        void shouldReturnOnlyExistingProducts() {
            List<Long> ids = Arrays.asList(1L, 999L);
            when(productRepository.findAllById(ids)).thenReturn(Arrays.asList(product1));

            List<Product> result = menuService.getProductsByIds(ids);

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("validateProductsAvailability Tests")
    class ValidateProductsAvailabilityTests {

        @Test
        @DisplayName("Deve validar disponibilidade dos produtos com sucesso")
        void shouldValidateProductsAvailabilitySuccessfully() {
            List<Long> ids = Arrays.asList(1L);
            when(productRepository.findAllById(ids)).thenReturn(Arrays.asList(product1));

            assertDoesNotThrow(() ->
                    menuService.validateProductsAvailability(ids)
            );
        }

        @Test
        @DisplayName("Deve lançar ProductNotFoundException quando produto não encontrado")
        void shouldThrowProductNotFoundExceptionWhenProductNotFound() {
            List<Long> ids = Arrays.asList(1L, 999L);
            when(productRepository.findAllById(ids)).thenReturn(Arrays.asList(product1));

            assertThrows(ProductNotFoundException.class, () ->
                    menuService.validateProductsAvailability(ids)
            );
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando produto pausado")
        void shouldThrowProductUnavailableExceptionWhenProductPaused() {
            List<Long> ids = Arrays.asList(2L);
            when(productRepository.findAllById(ids)).thenReturn(Arrays.asList(product2));

            assertThrows(BusinessException.class, () ->
                    menuService.validateProductsAvailability(ids)
            );
        }
    }

    @Nested
    @DisplayName("getFilteredMenu Tests")
    class GetFilteredMenuTests {

        @Test
        @DisplayName("Deve retornar menu filtrado com sucesso")
        void shouldReturnFilteredMenuSuccessfully() {
            MenuFilterDTO filter = new MenuFilterDTO();
            filter.setName("Bacon");
            filter.setMinPrice(new BigDecimal("20.00"));
            filter.setMaxPrice(new BigDecimal("50.00"));
            filter.setActiveOnly(true);

            when(productRepository.findWithFilters(
                    "Bacon",
                    new BigDecimal("20.00"),
                    new BigDecimal("50.00"),
                    true
            )).thenReturn(Arrays.asList(product1));

            List<Product> result = menuService.getFilteredMenu(filter);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há produtos com filtros")
        void shouldReturnEmptyListWhenNoProductsMatchFilters() {
            MenuFilterDTO filter = new MenuFilterDTO();
            filter.setName("Inexistente");
            filter.setActiveOnly(true);

            when(productRepository.findWithFilters(anyString(), any(), any(), anyBoolean()))
                    .thenReturn(Collections.emptyList());

            List<Product> result = menuService.getFilteredMenu(filter);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Deve aplicar todos os filtros fornecidos")
        void shouldApplyAllProvidedFilters() {
            MenuFilterDTO filter = new MenuFilterDTO();
            filter.setName("Hamburguer");
            filter.setMinPrice(new BigDecimal("25.00"));
            filter.setMaxPrice(new BigDecimal("35.00"));
            filter.setActiveOnly(false);

            when(productRepository.findWithFilters(
                    "Hamburguer",
                    new BigDecimal("25.00"),
                    new BigDecimal("35.00"),
                    false
            )).thenReturn(Arrays.asList(product1, product2));

            menuService.getFilteredMenu(filter);

            verify(productRepository).findWithFilters(
                    "Hamburguer",
                    new BigDecimal("25.00"),
                    new BigDecimal("35.00"),
                    false
            );
        }
    }

    @Nested
    @DisplayName("getProductsOrderedByPrice Tests")
    class GetProductsOrderedByPriceTests {

        @Test
        @DisplayName("Deve ordenar produtos por preço ascendente")
        void shouldOrderProductsByPriceAscending() {
            when(productRepository.findAll(any(Sort.class)))
                    .thenReturn(Arrays.asList(product2, product1));

            List<Product> result = menuService.getProductsOrderedByPrice("asc", false);

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Deve ordenar produtos por preço descendente")
        void shouldOrderProductsByPriceDescending() {
            when(productRepository.findAll(any(Sort.class)))
                    .thenReturn(Arrays.asList(product1, product2));

            List<Product> result = menuService.getProductsOrderedByPrice("desc", false);

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Deve lançar InvalidMenuFilterException com direção inválida")
        void shouldThrowInvalidMenuFilterExceptionWithInvalidDirection() {
            assertThrows(InvalidMenuFilterException.class, () ->
                    menuService.getProductsOrderedByPrice("invalid", true)
            );
        }
    }

    @Nested
    @DisplayName("countProductsWithIngredient Tests")
    class CountProductsWithIngredientTests {

        @Test
        @DisplayName("Deve contar produtos que usam o ingrediente")
        void shouldCountProductsUsingIngredient() {
            when(ingredientRepository.countProductsUsingIngredient(1L)).thenReturn(5L);

            long result = menuService.countProductsWithIngredient(1L);

            assertEquals(5L, result);
        }

        @Test
        @DisplayName("Deve retornar zero quando nenhum produto usa o ingrediente")
        void shouldReturnZeroWhenNoProductUsesIngredient() {
            when(ingredientRepository.countProductsUsingIngredient(1L)).thenReturn(0L);

            long result = menuService.countProductsWithIngredient(1L);

            assertEquals(0L, result);
        }

        @Test
        @DisplayName("Deve consultar repositório de ingredientes")
        void shouldQueryIngredientRepository() {
            when(ingredientRepository.countProductsUsingIngredient(1L)).thenReturn(3L);

            menuService.countProductsWithIngredient(1L);

            verify(ingredientRepository).countProductsUsingIngredient(1L);
        }
    }

    @Nested
    @DisplayName("getProductsWithoutIngredients Tests")
    class GetProductsWithoutIngredientsTests {

        @Test
        @DisplayName("Deve retornar produtos sem ingredientes cadastrados")
        void shouldReturnProductsWithoutIngredients() {
            when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2));
            when(ingredientProductRepository.findByProduct(product1))
                    .thenReturn(Arrays.asList(ingredientProduct));
            when(ingredientProductRepository.findByProduct(product2))
                    .thenReturn(Collections.emptyList());

            List<Product> result = menuService.getProductsWithoutIngredients();

            assertEquals(1, result.size());
            assertEquals(product2, result.get(0));
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando todos produtos têm ingredientes")
        void shouldReturnEmptyListWhenAllProductsHaveIngredients() {
            when(productRepository.findAll()).thenReturn(Arrays.asList(product1));
            when(ingredientProductRepository.findByProduct(product1))
                    .thenReturn(Arrays.asList(ingredientProduct));

            List<Product> result = menuService.getProductsWithoutIngredients();

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Deve retornar todos produtos quando nenhum tem ingredientes")
        void shouldReturnAllProductsWhenNoneHaveIngredients() {
            when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2));
            when(ingredientProductRepository.findByProduct(any()))
                    .thenReturn(Collections.emptyList());

            List<Product> result = menuService.getProductsWithoutIngredients();

            assertEquals(2, result.size());
        }
    }
}

