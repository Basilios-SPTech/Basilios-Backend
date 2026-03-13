package com.basilios.basilios.core.service;

import com.basilios.basilios.app.dto.product.ProductRequestDTO;
import com.basilios.basilios.app.dto.product.ProductResponseDTO;
import com.basilios.basilios.core.exception.BusinessException;
import com.basilios.basilios.core.exception.DuplicateProductException;
import com.basilios.basilios.core.exception.InvalidPriceException;
import com.basilios.basilios.core.exception.ProductNotFoundException;
import com.basilios.basilios.core.model.Product;
import com.basilios.basilios.core.enums.ProductCategory;
import com.basilios.basilios.infra.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do ProductService")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private IngredientProductRepository ingredientProductRepository;

    @Mock
    private ProductOrderRepository productOrderRepository;

    @Mock
    private ProductComboRepository productComboRepository;

    @Mock
    private PromotionRepository promotionRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        // Criar produto mock ativo
        product = Product.builder()
                .id(1L)
                .name("Pizza Margherita")
                .description("Pizza clássica com molho de tomate, mussarela e manjericão")
                .imageUrl("https://example.com/pizza.jpg")
                .price(new BigDecimal("45.00"))
                .isPaused(false)
                .tags(new ArrayList<>())
                .build();
    }

    // ========== TESTES DO MÉTODO pauseProduct() ==========

    @Test
    @DisplayName("Deve pausar produto com sucesso quando produto está ativo")
    void pauseProduct_DevePausarProdutoComSucesso() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(ingredientProductRepository.findByProduct(any(Product.class))).thenReturn(new ArrayList<>());

        // Simular que o produto foi pausado
        Product produtoPausado = Product.builder()
                .id(1L)
                .name("Pizza Margherita")
                .description("Pizza clássica com molho de tomate, mussarela e manjericão")
                .imageUrl("https://example.com/pizza.jpg")
                .price(new BigDecimal("45.00"))
                .isPaused(true)
                .tags(new ArrayList<>())
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(produtoPausado);

        // Act
        ProductResponseDTO result = productService.pauseProduct(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.getIsPaused());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando produto já está pausado")
    void pauseProduct_DeveLancarExcecaoQuandoProdutoJaEstaPausado() {
        // Arrange
        product.pause(); // Pausar o produto antes
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> productService.pauseProduct(1L));

        assertEquals("Produto já está pausado", exception.getMessage());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Deve lançar ProductNotFoundException quando produto não existe")
    void pauseProduct_DeveLancarExcecaoQuandoProdutoNaoExiste() {
        // Arrange
        Long produtoIdInexistente = 999L;
        when(productRepository.findById(produtoIdInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
                () -> productService.pauseProduct(produtoIdInexistente));

        assertNotNull(exception.getMessage());
        verify(productRepository, times(1)).findById(produtoIdInexistente);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Deve chamar o método pause() do produto ao pausar")
    void pauseProduct_DeveChamarMetodoPauseDoModelo() {
        // Arrange
        Product produtoSpy = spy(product);
        when(productRepository.findById(1L)).thenReturn(Optional.of(produtoSpy));
        when(ingredientProductRepository.findByProduct(any(Product.class))).thenReturn(new ArrayList<>());
        when(productRepository.save(any(Product.class))).thenReturn(produtoSpy);

        // Act
        productService.pauseProduct(1L);

        // Assert
        verify(produtoSpy, times(1)).pause();
        verify(productRepository, times(1)).save(produtoSpy);
    }

    @Test
    @DisplayName("Deve persistir o produto pausado no banco de dados")
    void pauseProduct_DevePersistirProdutoPausado() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(ingredientProductRepository.findByProduct(any(Product.class))).thenReturn(new ArrayList<>());

        Product produtoPausado = Product.builder()
                .id(1L)
                .name("Pizza Margherita")
                .isPaused(true)
                .price(new BigDecimal("45.00"))
                .tags(new ArrayList<>())
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(produtoPausado);

        // Act
        ProductResponseDTO result = productService.pauseProduct(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.getIsPaused());
        assertEquals("Pizza Margherita", result.getName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Deve retornar DTO com informações corretas após pausar")
    void pauseProduct_DeveRetornarDTOComInformacoesCorretas() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(ingredientProductRepository.findByProduct(any(Product.class))).thenReturn(new ArrayList<>());

        Product produtoPausado = Product.builder()
                .id(1L)
                .name("Pizza Margherita")
                .description("Pizza clássica com molho de tomate, mussarela e manjericão")
                .price(new BigDecimal("45.00"))
                .isPaused(true)
                .tags(new ArrayList<>())
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(produtoPausado);

        // Act
        ProductResponseDTO result = productService.pauseProduct(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Pizza Margherita", result.getName());
        assertEquals("Pizza clássica com molho de tomate, mussarela e manjericão", result.getDescription());
        assertEquals(0, new BigDecimal("45.00").compareTo(result.getPrice()));
        assertTrue(result.getIsPaused());
    }

    @Test
    @DisplayName("Deve validar que produto está ativo antes de pausar")
    void pauseProduct_DeveValidarQueProdutoEstaAtivoAntesDePausar() {
        // Arrange
        product.pause(); // Produto já pausado
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act & Assert
        assertThrows(BusinessException.class,
                () -> productService.pauseProduct(1L));

        // Verifica que não tentou salvar um produto já pausado
        verify(productRepository, never()).save(any(Product.class));
    }

    // ========== TESTES DO MÉTODO getProductById() ==========

    @Test
    @DisplayName("Deve retornar produto quando ID existe")
    void getProductById_DeveRetornarProdutoQuandoIdExiste() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(ingredientProductRepository.findByProduct(any(Product.class))).thenReturn(new ArrayList<>());

        ProductResponseDTO result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals("Pizza Margherita", result.getName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar ProductNotFoundException quando ID não existe")
    void getProductById_DeveLancarExcecaoQuandoIdNaoExiste() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productService.getProductById(999L));
    }

    // ========== TESTES DO MÉTODO getAllProducts() ==========

    @Test
    @DisplayName("Deve listar apenas produtos ativos quando activeOnly=true")
    void getAllProducts_DeveListarApenasAtivosQuandoActiveOnlyTrue() {
        when(productRepository.findByIsPausedFalse()).thenReturn(List.of(product));

        List<ProductResponseDTO> result = productService.getAllProducts(true);

        assertEquals(1, result.size());
        verify(productRepository, times(1)).findByIsPausedFalse();
        verify(productRepository, never()).findAll();
    }

    @Test
    @DisplayName("Deve listar todos os produtos quando activeOnly=false")
    void getAllProducts_DeveListarTodosQuandoActiveOnlyFalse() {
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<ProductResponseDTO> result = productService.getAllProducts(false);

        assertEquals(1, result.size());
        verify(productRepository, times(1)).findAll();
    }

    // ========== TESTES DO MÉTODO activateProduct() ==========

    @Test
    @DisplayName("Deve ativar produto pausado com sucesso")
    void activateProduct_DeveAtivarProdutoPausadoComSucesso() {
        product.pause();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(ingredientProductRepository.findByProduct(any(Product.class))).thenReturn(new ArrayList<>());

        Product produtoAtivo = Product.builder()
                .id(1L).name("Pizza Margherita").isPaused(false)
                .price(new BigDecimal("45.00")).tags(new ArrayList<>()).build();
        when(productRepository.save(any(Product.class))).thenReturn(produtoAtivo);

        ProductResponseDTO result = productService.activateProduct(1L);

        assertNotNull(result);
        assertFalse(result.getIsPaused());
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando produto já está ativo")
    void activateProduct_DeveLancarExcecaoQuandoJaAtivo() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(BusinessException.class,
                () -> productService.activateProduct(1L));
    }

    // ========== TESTES DO MÉTODO updatePrice() ==========

    @Test
    @DisplayName("Deve atualizar preço com sucesso")
    void updatePrice_DeveAtualizarPrecoComSucesso() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(ingredientProductRepository.findByProduct(any(Product.class))).thenReturn(new ArrayList<>());

        Product produtoAtualizado = Product.builder()
                .id(1L).name("Pizza Margherita").isPaused(false)
                .price(new BigDecimal("55.00")).tags(new ArrayList<>()).build();
        when(productRepository.save(any(Product.class))).thenReturn(produtoAtualizado);

        ProductResponseDTO result = productService.updatePrice(1L, new BigDecimal("55.00"));

        assertNotNull(result);
        assertEquals(0, new BigDecimal("55.00").compareTo(result.getPrice()));
    }

    @Test
    @DisplayName("Deve lançar InvalidPriceException quando preço é zero ou negativo")
    void updatePrice_DeveLancarExcecaoQuandoPrecoInvalido() {
        assertThrows(InvalidPriceException.class,
                () -> productService.updatePrice(1L, BigDecimal.ZERO));

        assertThrows(InvalidPriceException.class,
                () -> productService.updatePrice(1L, new BigDecimal("-5.00")));
    }

    // ========== TESTES DO MÉTODO deleteProduct() ==========

    @Test
    @DisplayName("Deve deletar produto quando não está em pedidos nem combos")
    void deleteProduct_DeveDeletarProdutoComSucesso() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productOrderRepository.countByProductId(1L)).thenReturn(0L);
        when(productComboRepository.countByProductId(1L)).thenReturn(0L);

        productService.deleteProduct(1L);

        verify(productRepository, times(1)).delete(product);
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando produto está em pedidos")
    void deleteProduct_DeveLancarExcecaoQuandoEmPedidos() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productOrderRepository.countByProductId(1L)).thenReturn(5L);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> productService.deleteProduct(1L));

        assertTrue(exception.getMessage().contains("5 pedidos"));
        verify(productRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando produto está em combos")
    void deleteProduct_DeveLancarExcecaoQuandoEmCombos() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productOrderRepository.countByProductId(1L)).thenReturn(0L);
        when(productComboRepository.countByProductId(1L)).thenReturn(3L);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> productService.deleteProduct(1L));

        assertTrue(exception.getMessage().contains("3 combos"));
        verify(productRepository, never()).delete(any());
    }

    // ========== TESTES DO MÉTODO createProduct() ==========

    @Test
    @DisplayName("Deve criar produto com sucesso")
    void createProduct_DeveCriarProdutoComSucesso() {
        ProductRequestDTO dto = ProductRequestDTO.builder()
                .name("Novo Burger")
                .description("Descrição do burger muito boa aqui")
                .price(new BigDecimal("35.00"))
                .category(ProductCategory.BURGER)
                .build();

        when(productRepository.existsByNameIgnoreCase("Novo Burger")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(ingredientProductRepository.findByProduct(any(Product.class))).thenReturn(new ArrayList<>());

        ProductResponseDTO result = productService.createProduct(dto);

        assertNotNull(result);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Deve lançar DuplicateProductException quando nome já existe")
    void createProduct_DeveLancarExcecaoQuandoNomeDuplicado() {
        ProductRequestDTO dto = ProductRequestDTO.builder()
                .name("Pizza Margherita")
                .description("Desc")
                .price(new BigDecimal("35.00"))
                .category(ProductCategory.BURGER)
                .build();

        when(productRepository.existsByNameIgnoreCase("Pizza Margherita")).thenReturn(true);

        assertThrows(DuplicateProductException.class,
                () -> productService.createProduct(dto));

        verify(productRepository, never()).save(any());
    }
}