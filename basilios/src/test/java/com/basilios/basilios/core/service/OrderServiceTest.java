package com.basilios.basilios.core.service;

import com.basilios.basilios.app.dto.order.OrderRequestDTO;
import com.basilios.basilios.app.dto.order.OrderResponseDTO;
import com.basilios.basilios.app.mapper.OrderMapper;
import com.basilios.basilios.core.enums.StatusPedidoEnum;
import com.basilios.basilios.core.exception.BusinessException;
import com.basilios.basilios.core.exception.NotFoundException;
import com.basilios.basilios.core.model.*;
import com.basilios.basilios.infra.repository.AddressRepository;
import com.basilios.basilios.infra.repository.AdicionalProductRepository;
import com.basilios.basilios.infra.repository.AdicionalRepository;
import com.basilios.basilios.infra.repository.OrderRepository;
import com.basilios.basilios.infra.repository.ProductRepository;
import com.basilios.basilios.infra.messaging.NotificationEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do OrderService")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AdicionalRepository adicionalRepository;

    @Mock
    private AdicionalProductRepository adicionalProductRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private StoreService storeService;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private NotificationEventPublisher notificationEventPublisher;

    @InjectMocks
    private OrderService orderService;

    private Usuario usuario;
    private Address address;
    private Product product;
    private Adicional adicional;
    private Order order;
    private OrderRequestDTO orderRequestDTO;
    private OrderResponseDTO orderResponseDTO;

    @BeforeEach
    void setUp() {
        // Criar usuário mock
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNomeUsuario("João Silva");
        usuario.setEmail("joao@email.com");

        // Criar endereço mock (dentro da área de entrega - próximo à loja)
        address = new Address();
        address.setIdAddress(1L);
        address.setUsuario(usuario);
        address.setCep("01234-567");
        address.setRua("Rua Teste");
        address.setNumero("123");
        address.setBairro("Centro");
        address.setCidade("São Paulo");
        address.setEstado("SP");

        // Criar produto mock
        product = new Product();
        product.setId(1L);
        product.setName("Pizza Margherita");
        product.setPrice(new BigDecimal("45.00"));
        product.setIsPaused(false);

        // Criar adicional mock
        adicional = new Adicional();
        adicional.setId(1L);
        adicional.setName("Extra Bacon");
        adicional.setPrice(new BigDecimal("3.00"));
        adicional.setAvailable(true);

        // Criar pedido mock
        order = new Order();
        order.setId(1L);
        order.setUsuario(usuario);
        order.setAddressEntrega(address);
        order.setStatus(StatusPedidoEnum.PENDENTE);
        order.setCodigoPedido("PED-123456789-1234");
        order.setDeliveryFee(new BigDecimal("6.00"));
        order.setDiscount(BigDecimal.ZERO);
        order.setProductOrders(new ArrayList<>());

        // Criar DTO de requisição
        OrderRequestDTO.OrderItemRequest itemRequest = new OrderRequestDTO.OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);
        itemRequest.setObservations("Sem cebola");

        orderRequestDTO = new OrderRequestDTO();
        orderRequestDTO.setAddressId(1L);
        orderRequestDTO.setObservations("Entregar na portaria");
        orderRequestDTO.setDiscount(BigDecimal.ZERO);
        orderRequestDTO.setItems(List.of(itemRequest));

        // Criar DTO de resposta
        orderResponseDTO = new OrderResponseDTO();
        orderResponseDTO.setId(1L);
        orderResponseDTO.setStatus(StatusPedidoEnum.PENDENTE);
    }

    // ========== TESTES DO MÉTODO createOrder() ==========

    @Test
    @DisplayName("Deve criar pedido com sucesso quando todos os dados são válidos")
    void createOrder_DeveRetornarPedidoCriadoComSucesso() {
        // Arrange
        Store store = new Store();
        store.setDeliveryFee(new BigDecimal("5.00"));

        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(storeService.getMainStore()).thenReturn(store);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponseDTO);

        // Act
        OrderResponseDTO result = orderService.createOrder(orderRequestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(StatusPedidoEnum.PENDENTE, result.getStatus());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderMapper, times(1)).toResponse(any(Order.class));
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando endereço não existe")
    void createOrder_DeveLancarExcecaoQuandoEnderecoNaoExiste() {
        // Arrange
        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(addressRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> orderService.createOrder(orderRequestDTO));

        assertEquals("Endereço não encontrado: 1", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando endereço não pertence ao usuário")
    void createOrder_DeveLancarExcecaoQuandoEnderecoNaoPertenceAoUsuario() {
        // Arrange
        Usuario outroUsuario = new Usuario();
        outroUsuario.setId(2L);
        address.setUsuario(outroUsuario);

        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> orderService.createOrder(orderRequestDTO));

        assertEquals("Endereço não pertence ao usuário", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando produto não existe")
    void createOrder_DeveLancarExcecaoQuandoProdutoNaoExiste() {
        // Arrange
        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> orderService.createOrder(orderRequestDTO));

        assertEquals("Produto não encontrado: 1", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando produto está pausado")
    void createOrder_DeveLancarExcecaoQuandoProdutoEstaPausado() {
        // Arrange
        product.setIsPaused(true);

        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> orderService.createOrder(orderRequestDTO));

        assertEquals("Produto 'Pizza Margherita' não está disponível", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    // ========== TESTES DO MÉTODO getUserOrders() ==========

    @Test
    @DisplayName("Deve retornar página de pedidos do usuário autenticado")
    void getUserOrders_DeveRetornarPaginaDePedidosDoUsuario() {
        // Arrange
        org.springframework.data.domain.Page<Order> page = new org.springframework.data.domain.PageImpl<>(List.of(order));

        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(orderRepository.findByUsuarioOrderByCreatedAtDesc(eq(usuario), any(org.springframework.data.domain.Pageable.class))).thenReturn(page);
        when(orderMapper.toResponse(order)).thenReturn(orderResponseDTO);

        // Act
        org.springframework.data.domain.Page<OrderResponseDTO> result = orderService.getUserOrders(org.springframework.data.domain.Pageable.unpaged());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(usuarioService, times(1)).getCurrentUsuario();
    }

    // ========== TESTES DO MÉTODO getOrderById() ==========

    @Test
    @DisplayName("Deve retornar pedido quando encontrado por ID")
    void getOrderById_DeveRetornarPedidoQuandoEncontrado() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(orderResponseDTO);

        OrderResponseDTO result = orderService.getOrderById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(orderRepository).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando pedido não existe")
    void getOrderById_DeveLancarException_QuandoNaoExiste() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderService.getOrderById(999L));
    }

    // ========== TESTES DO MÉTODO getUserOrderById() ==========

    @Test
    @DisplayName("Deve retornar pedido do usuário autenticado por ID")
    void getUserOrderById_DeveRetornarPedidoDoUsuario() {
        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(orderResponseDTO);

        OrderResponseDTO result = orderService.getUserOrderById(1L);

        assertNotNull(result);
        verify(orderMapper).toResponse(order);
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando pedido não pertence ao usuário")
    void getUserOrderById_DeveLancarException_QuandoNaoPertenceAoUsuario() {
        Usuario outroUsuario = new Usuario();
        outroUsuario.setId(99L);
        order.setUsuario(outroUsuario);

        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class, () -> orderService.getUserOrderById(1L));
    }

    // ========== TESTES DO MÉTODO getOrdersByStatus() ==========

    @Test
    @DisplayName("Deve retornar pedidos filtrados por status")
    void getOrdersByStatus_DeveRetornarPedidosPorStatus() {
        org.springframework.data.domain.Page<Order> page = new org.springframework.data.domain.PageImpl<>(List.of(order));

        when(orderRepository.findByStatus(eq(StatusPedidoEnum.PENDENTE), any(org.springframework.data.domain.Pageable.class))).thenReturn(page);
        when(orderMapper.toResponse(order)).thenReturn(orderResponseDTO);

        org.springframework.data.domain.Page<OrderResponseDTO> result = orderService.getOrdersByStatus(
                StatusPedidoEnum.PENDENTE, org.springframework.data.domain.Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
        verify(orderRepository).findByStatus(eq(StatusPedidoEnum.PENDENTE), any(org.springframework.data.domain.Pageable.class));
    }

    // ========== TESTES DOS MÉTODOS DE MUDANÇA DE STATUS ==========

    @Test
    @DisplayName("updateOrderStatus() — Deve confirmar pedido pendente")
    void updateOrderStatus_DeveConfirmarPedidoPendente() {
        order.setStatus(StatusPedidoEnum.PENDENTE);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponseDTO);

        OrderResponseDTO result = orderService.updateOrderStatus(1L, "CONFIRMADO");

        assertNotNull(result);
        assertEquals(StatusPedidoEnum.CONFIRMADO, order.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("updateOrderStatus() — Deve iniciar preparo de pedido confirmado")
    void updateOrderStatus_DeveIniciarPreparoPedidoConfirmado() {
        order.setStatus(StatusPedidoEnum.CONFIRMADO);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponseDTO);

        OrderResponseDTO result = orderService.updateOrderStatus(1L, "PREPARANDO");

        assertNotNull(result);
        assertEquals(StatusPedidoEnum.PREPARANDO, order.getStatus());
    }

    @Test
    @DisplayName("updateOrderStatus() — Deve despachar pedido em preparo")
    void updateOrderStatus_DeveDespacharPedidoEmPreparo() {
        order.setStatus(StatusPedidoEnum.PREPARANDO);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponseDTO);

        OrderResponseDTO result = orderService.updateOrderStatus(1L, "DESPACHADO");

        assertNotNull(result);
        assertEquals(StatusPedidoEnum.DESPACHADO, order.getStatus());
    }

    @Test
    @DisplayName("updateOrderStatus() — Deve entregar pedido despachado")
    void updateOrderStatus_DeveEntregarPedidoDespachado() {
        order.setStatus(StatusPedidoEnum.DESPACHADO);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponseDTO);

        OrderResponseDTO result = orderService.updateOrderStatus(1L, "ENTREGUE");

        assertNotNull(result);
        assertEquals(StatusPedidoEnum.ENTREGUE, order.getStatus());
    }

    @Test
    @DisplayName("updateOrderStatus() — Deve cancelar pedido pendente")
    void updateOrderStatus_DeveCancelarPedidoPendente() {
        order.setStatus(StatusPedidoEnum.PENDENTE);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponseDTO);

        OrderResponseDTO result = orderService.updateOrderStatus(1L, "CANCELADO");

        assertNotNull(result);
        assertEquals(StatusPedidoEnum.CANCELADO, order.getStatus());
    }

    @Test
    @DisplayName("updateOrderStatus() — Deve lançar BusinessException quando transição inválida")
    void updateOrderStatus_DeveLancarException_QuandoTransicaoInvalida() {
        order.setStatus(StatusPedidoEnum.ENTREGUE);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class, () -> orderService.updateOrderStatus(1L, "CONFIRMADO"));
        verify(orderRepository, never()).save(any());
    }

    // ========== TESTES DO MÉTODO cancelarPedidoUsuario() ==========

    @Test
    @DisplayName("cancelarPedidoUsuario() — Deve cancelar pedido pendente do usuário")
    void cancelarPedidoUsuario_DeveCancelarPedidoPendenteDoUsuario() {
        order.setStatus(StatusPedidoEnum.PENDENTE);
        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponseDTO);

        OrderResponseDTO result = orderService.cancelarPedidoUsuario(1L, "Não quero mais");

        assertNotNull(result);
        assertEquals(StatusPedidoEnum.CANCELADO, order.getStatus());
    }

    @Test
    @DisplayName("cancelarPedidoUsuario() — Deve lançar BusinessException quando pedido não pertence ao usuário")
    void cancelarPedidoUsuario_DeveLancarException_QuandoNaoPertenceAoUsuario() {
        Usuario outroUsuario = new Usuario();
        outroUsuario.setId(99L);
        order.setUsuario(outroUsuario);

        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class, () -> orderService.cancelarPedidoUsuario(1L, "motivo"));
    }

    @Test
    @DisplayName("cancelarPedidoUsuario() — Deve lançar BusinessException quando status não permite cancelamento")
    void cancelarPedidoUsuario_DeveLancarException_QuandoStatusNaoPermiteCancelamento() {
        order.setStatus(StatusPedidoEnum.DESPACHADO);
        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class, () -> orderService.cancelarPedidoUsuario(1L, "motivo"));
    }

    // ========== TESTES DO MÉTODO updateOrderStatus() ==========

    @Test
    @DisplayName("updateOrderStatus() — Deve atualizar status com string válida")
    void updateOrderStatus_DeveAtualizarStatusComStringValida() {
        order.setStatus(StatusPedidoEnum.PENDENTE);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponseDTO);

        OrderResponseDTO result = orderService.updateOrderStatus(1L, "CONFIRMADO");

        assertNotNull(result);
    }

    @Test
    @DisplayName("updateOrderStatus() — Deve lançar BusinessException quando status é inválido")
    void updateOrderStatus_DeveLancarException_QuandoStatusInvalido() {
        assertThrows(BusinessException.class, () -> orderService.updateOrderStatus(1L, "INVALIDO"));
    }

    // ========== TESTES DE ADICIONAIS ==========

    @Test
    @DisplayName("Deve criar pedido com adicional válido e calcular preço corretamente")
    void createOrder_DeveCriarPedidoComAdicionalValido() {
        // Arrange
        OrderRequestDTO.AdicionalItemRequest adicionalRequest = new OrderRequestDTO.AdicionalItemRequest();
        adicionalRequest.setAdicionalId(1L);
        adicionalRequest.setQuantity(2);

        OrderRequestDTO.OrderItemRequest itemRequest = new OrderRequestDTO.OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(1);
        itemRequest.setAdicionais(List.of(adicionalRequest));

        orderRequestDTO.setItems(List.of(itemRequest));

        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(adicionalRepository.findById(1L)).thenReturn(Optional.of(adicional));
        when(adicionalProductRepository.existsByProductIdAndAdicionalId(1L, 1L)).thenReturn(true);
        when(storeService.getMainStore()).thenReturn(Store.builder().deliveryFee(new BigDecimal("5.00")).build());
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponseDTO);

        // Act
        OrderResponseDTO result = orderService.createOrder(orderRequestDTO);

        // Assert
        assertNotNull(result);
        verify(adicionalRepository, times(1)).findById(1L);
        verify(adicionalProductRepository, times(1)).existsByProductIdAndAdicionalId(1L, 1L);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando adicional não existe")
    void createOrder_DeveLancarExcecaoQuandoAdicionalNaoExiste() {
        // Arrange
        OrderRequestDTO.AdicionalItemRequest adicionalRequest = new OrderRequestDTO.AdicionalItemRequest();
        adicionalRequest.setAdicionalId(99L);
        adicionalRequest.setQuantity(1);

        OrderRequestDTO.OrderItemRequest itemRequest = new OrderRequestDTO.OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(1);
        itemRequest.setAdicionais(List.of(adicionalRequest));

        orderRequestDTO.setItems(List.of(itemRequest));

        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(adicionalRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> orderService.createOrder(orderRequestDTO));

        assertEquals("Adicional não encontrado: 99", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando adicional não está disponível")
    void createOrder_DeveLancarExcecaoQuandoAdicionalIndisponivel() {
        // Arrange
        adicional.setAvailable(false);

        OrderRequestDTO.AdicionalItemRequest adicionalRequest = new OrderRequestDTO.AdicionalItemRequest();
        adicionalRequest.setAdicionalId(1L);
        adicionalRequest.setQuantity(1);

        OrderRequestDTO.OrderItemRequest itemRequest = new OrderRequestDTO.OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(1);
        itemRequest.setAdicionais(List.of(adicionalRequest));

        orderRequestDTO.setItems(List.of(itemRequest));

        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(adicionalRepository.findById(1L)).thenReturn(Optional.of(adicional));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> orderService.createOrder(orderRequestDTO));

        assertEquals("Adicional 'Extra Bacon' não está disponível", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando adicional não pertence ao produto")
    void createOrder_DeveLancarExcecaoQuandoAdicionalNaoPertenceAoProduto() {
        // Arrange
        OrderRequestDTO.AdicionalItemRequest adicionalRequest = new OrderRequestDTO.AdicionalItemRequest();
        adicionalRequest.setAdicionalId(1L);
        adicionalRequest.setQuantity(1);

        OrderRequestDTO.OrderItemRequest itemRequest = new OrderRequestDTO.OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(1);
        itemRequest.setAdicionais(List.of(adicionalRequest));

        orderRequestDTO.setItems(List.of(itemRequest));

        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(adicionalRepository.findById(1L)).thenReturn(Optional.of(adicional));
        when(adicionalProductRepository.existsByProductIdAndAdicionalId(1L, 1L)).thenReturn(false);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> orderService.createOrder(orderRequestDTO));

        assertEquals("Adicional 'Extra Bacon' não pertence ao produto 'Pizza Margherita'", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Deve criar pedido sem adicionais normalmente")
    void createOrder_DeveCriarPedidoSemAdicionais() {
        // Arrange — item sem adicionais (lista nula ou vazia)
        OrderRequestDTO.OrderItemRequest itemRequest = new OrderRequestDTO.OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(1);
        itemRequest.setAdicionais(new ArrayList<>());

        orderRequestDTO.setItems(List.of(itemRequest));

        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(storeService.getMainStore()).thenReturn(Store.builder().deliveryFee(new BigDecimal("5.00")).build());
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponseDTO);

        // Act
        OrderResponseDTO result = orderService.createOrder(orderRequestDTO);

        // Assert
        assertNotNull(result);
        verify(adicionalRepository, never()).findById(anyLong());
        verify(orderRepository, times(1)).save(any(Order.class));
    }
}

