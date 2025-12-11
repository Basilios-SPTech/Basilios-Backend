package com.basilios.basilios.core.service;

import com.basilios.basilios.app.dto.order.OrderRequestDTO;
import com.basilios.basilios.app.dto.order.OrderResponseDTO;
import com.basilios.basilios.app.mapper.OrderMapper;
import com.basilios.basilios.core.enums.StatusPedidoEnum;
import com.basilios.basilios.core.exception.BusinessException;
import com.basilios.basilios.core.exception.NotFoundException;
import com.basilios.basilios.core.model.*;
import com.basilios.basilios.infra.repository.AddressRepository;
import com.basilios.basilios.infra.repository.OrderRepository;
import com.basilios.basilios.infra.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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
    private UsuarioService usuarioService;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private Usuario usuario;
    private Address address;
    private Product product;
    private Order order;
    private OrderRequestDTO orderRequestDTO;
    private OrderResponseDTO orderResponseDTO;

    @BeforeEach
    void setUp() {
        // Configurar coordenadas da loja usando ReflectionTestUtils
        ReflectionTestUtils.setField(orderService, "storeLatitude", -23.550520);
        ReflectionTestUtils.setField(orderService, "storeLongitude", -46.633308);

        // Criar usuário mock
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNomeUsuario("João Silva");
        usuario.setEmail("joao@email.com");

        // Criar endereço mock (dentro da área de entrega - próximo à loja)
        address = new Address();
        address.setIdAddress(1L);
        address.setUsuario(usuario);
        address.setLatitude(-23.555520); // ~0.5km da loja
        address.setLongitude(-46.638308);
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
        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
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
    @DisplayName("Deve redirecionar para parceiros quando endereço está fora da área de entrega")
    void createOrder_DeveRedirecionarParaParceirosQuandoForaDaAreaDeEntrega() {
        // Arrange
        address.setLatitude(-23.650520); // ~11km da loja (fora da área de 7km)
        address.setLongitude(-46.733308);

        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        // Act
        OrderResponseDTO result = orderService.createOrder(orderRequestDTO);

        // Assert
        assertNotNull(result);
        assertTrue(result.getRedirectToPartners());
        assertNotNull(result.getPartnerLinks());
        assertTrue(result.getPartnerLinks().containsKey("ifood"));
        assertTrue(result.getPartnerLinks().containsKey("99food"));
        assertTrue(result.getPartnerLinks().containsKey("rappi"));
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
    @DisplayName("Deve retornar lista de pedidos do usuário autenticado")
    void getUserOrders_DeveRetornarListaDePedidosDoUsuario() {
        // Arrange
        List<Order> orders = List.of(order);
        List<OrderResponseDTO> responseDTOs = List.of(orderResponseDTO);

        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(orderRepository.findByUsuarioOrderByCreatedAtDesc(usuario)).thenReturn(orders);
        when(orderMapper.toResponseList(orders)).thenReturn(responseDTOs);

        // Act
        List<OrderResponseDTO> result = orderService.getUserOrders();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(orderResponseDTO, result.get(0));
        verify(usuarioService, times(1)).getCurrentUsuario();
        verify(orderRepository, times(1)).findByUsuarioOrderByCreatedAtDesc(usuario);
        verify(orderMapper, times(1)).toResponseList(orders);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando usuário não possui pedidos")
    void getUserOrders_DeveRetornarListaVaziaQuandoUsuarioNaoPossuiPedidos() {
        // Arrange
        List<Order> orders = new ArrayList<>();
        List<OrderResponseDTO> responseDTOs = new ArrayList<>();

        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(orderRepository.findByUsuarioOrderByCreatedAtDesc(usuario)).thenReturn(orders);
        when(orderMapper.toResponseList(orders)).thenReturn(responseDTOs);

        // Act
        List<OrderResponseDTO> result = orderService.getUserOrders();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(usuarioService, times(1)).getCurrentUsuario();
        verify(orderRepository, times(1)).findByUsuarioOrderByCreatedAtDesc(usuario);
    }

    @Test
    @DisplayName("Deve retornar múltiplos pedidos ordenados por data")
    void getUserOrders_DeveRetornarMultiplosPedidosOrdenadosPorData() {
        // Arrange
        Order order2 = new Order();
        order2.setId(2L);
        order2.setUsuario(usuario);
        order2.setCodigoPedido("PED-123456790-5678");

        OrderResponseDTO response2 = new OrderResponseDTO();
        response2.setId(2L);

        List<Order> orders = List.of(order, order2);
        List<OrderResponseDTO> responseDTOs = List.of(orderResponseDTO, response2);

        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(orderRepository.findByUsuarioOrderByCreatedAtDesc(usuario)).thenReturn(orders);
        when(orderMapper.toResponseList(orders)).thenReturn(responseDTOs);

        // Act
        List<OrderResponseDTO> result = orderService.getUserOrders();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    // ========== TESTES DO MÉTODO findByUsuario() ==========

    @Test
    @DisplayName("Deve retornar lista de pedidos para usuário específico")
    void findByUsuario_DeveRetornarListaDePedidosParaUsuarioEspecifico() {
        // Arrange
        List<Order> orders = List.of(order);
        when(orderRepository.findByUsuario(usuario)).thenReturn(orders);

        // Act
        List<Order> result = orderService.findByUsuario(usuario);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(order, result.get(0));
        verify(orderRepository, times(1)).findByUsuario(usuario);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando usuário não possui pedidos")
    void findByUsuario_DeveRetornarListaVaziaQuandoUsuarioNaoPossuiPedidos() {
        // Arrange
        List<Order> orders = new ArrayList<>();
        when(orderRepository.findByUsuario(usuario)).thenReturn(orders);

        // Act
        List<Order> result = orderService.findByUsuario(usuario);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository, times(1)).findByUsuario(usuario);
    }

    @Test
    @DisplayName("Deve retornar múltiplos pedidos para o mesmo usuário")
    void findByUsuario_DeveRetornarMultiplosPedidosParaMesmoUsuario() {
        // Arrange
        Order order2 = new Order();
        order2.setId(2L);
        order2.setUsuario(usuario);
        order2.setStatus(StatusPedidoEnum.CONFIRMADO);

        Order order3 = new Order();
        order3.setId(3L);
        order3.setUsuario(usuario);
        order3.setStatus(StatusPedidoEnum.ENTREGUE);

        List<Order> orders = List.of(order, order2, order3);
        when(orderRepository.findByUsuario(usuario)).thenReturn(orders);

        // Act
        List<Order> result = orderService.findByUsuario(usuario);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(StatusPedidoEnum.PENDENTE, result.get(0).getStatus());
        assertEquals(StatusPedidoEnum.CONFIRMADO, result.get(1).getStatus());
        assertEquals(StatusPedidoEnum.ENTREGUE, result.get(2).getStatus());
        verify(orderRepository, times(1)).findByUsuario(usuario);
    }

    @Test
    @DisplayName("Deve buscar pedidos apenas do usuário informado")
    void findByUsuario_DeveBuscarPedidosApenasDoUsuarioInformado() {
        // Arrange
        Usuario outroUsuario = new Usuario();
        outroUsuario.setId(2L);
        outroUsuario.setNomeUsuario("Maria Santos");

        List<Order> ordersUsuario1 = List.of(order);
        when(orderRepository.findByUsuario(usuario)).thenReturn(ordersUsuario1);
        when(orderRepository.findByUsuario(outroUsuario)).thenReturn(new ArrayList<>());

        // Act
        List<Order> resultUsuario1 = orderService.findByUsuario(usuario);
        List<Order> resultUsuario2 = orderService.findByUsuario(outroUsuario);

        // Assert
        assertNotNull(resultUsuario1);
        assertEquals(1, resultUsuario1.size());
        assertEquals(usuario.getId(), resultUsuario1.get(0).getUsuario().getId());

        assertNotNull(resultUsuario2);
        assertTrue(resultUsuario2.isEmpty());

        verify(orderRepository, times(1)).findByUsuario(usuario);
        verify(orderRepository, times(1)).findByUsuario(outroUsuario);
    }
}