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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Tests")
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
        // Configurar coordenadas da loja
        ReflectionTestUtils.setField(orderService, "storeLatitude", -23.550520);
        ReflectionTestUtils.setField(orderService, "storeLongitude", -46.633308);

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNomeUsuario("João Silva");
        usuario.setEmail("joao@email.com");

        address = new Address();
        address.setIdAddress(1L);
        address.setUsuario(usuario);
        address.setRua("Av. Paulista");
        address.setNumero("1000");
        address.setCidade("São Paulo");
        address.setEstado("SP");
        address.setLatitude(-23.561414);
        address.setLongitude(-46.656147);
        address.setDeletedAt(null);

        product = new Product();
        product.setId(1L);
        product.setName("X-Bacon");
        product.setPrice(new BigDecimal("35.00"));
        product.setIsPaused(false);

        order = new Order();
        order.setId(1L);
        order.setUsuario(usuario);
        order.setAddressEntrega(address);
        order.setStatus(StatusPedidoEnum.PENDENTE);
        order.setDeliveryFee(new BigDecimal("10.00"));
        order.setSubtotal(new BigDecimal("35.00"));
        order.setTotal(new BigDecimal("45.00"));
        order.setCreatedAt(LocalDateTime.now());

        OrderRequestDTO.OrderItemRequest itemRequest = new OrderRequestDTO.OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(1);
        itemRequest.setObservations("Sem cebola");

        orderRequestDTO = new OrderRequestDTO();
        orderRequestDTO.setAddressId(1L);
        orderRequestDTO.setItems(Arrays.asList(itemRequest));
        orderRequestDTO.setObservations("Entrega rápida");

        orderResponseDTO = new OrderResponseDTO();
        orderResponseDTO.setId(1L);
        orderResponseDTO.setStatus(StatusPedidoEnum.PENDENTE);
        orderResponseDTO.setTotal(new BigDecimal("45.00"));
    }

    @Nested
    @DisplayName("createOrder Tests")
    class CreateOrderTests {

        @Test
        @DisplayName("Deve criar pedido com sucesso")
        void shouldCreateOrderSuccessfully() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(orderRepository.save(any(Order.class))).thenReturn(order);
            when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponseDTO);

            OrderResponseDTO result = orderService.createOrder(orderRequestDTO);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando endereço não encontrado")
        void shouldThrowNotFoundExceptionWhenAddressNotFound() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(addressRepository.findById(1L)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                    orderService.createOrder(orderRequestDTO)
            );

            assertTrue(exception.getMessage().contains("Endereço não encontrado"));
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando endereço não pertence ao usuário")
        void shouldThrowBusinessExceptionWhenAddressDoesNotBelongToUser() {
            Usuario outroUsuario = new Usuario();
            outroUsuario.setId(2L);
            address.setUsuario(outroUsuario);

            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    orderService.createOrder(orderRequestDTO)
            );

            assertEquals("Endereço não pertence ao usuário", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando endereço não está ativo")
        void shouldThrowBusinessExceptionWhenAddressIsInactive() {
            address.setDeletedAt(LocalDateTime.now());

            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    orderService.createOrder(orderRequestDTO)
            );

            assertEquals("Endereço não está ativo", exception.getMessage());
        }

        @Test
        @DisplayName("Deve retornar redirecionamento quando fora da área de entrega")
        void shouldReturnRedirectWhenOutsideDeliveryArea() {
            // Endereço muito distante (mais de 7km)
            address.setLatitude(-23.462200);
            address.setLongitude(-46.533439);

            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

            OrderResponseDTO result = orderService.createOrder(orderRequestDTO);

            assertTrue(result.getRedirectToPartners());
            assertNotNull(result.getPartnerLinks());
            assertEquals(3, result.getPartnerLinks().size());
            assertTrue(result.getPartnerLinks().containsKey("ifood"));
            assertTrue(result.getPartnerLinks().containsKey("99food"));
            assertTrue(result.getPartnerLinks().containsKey("rappi"));
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando produto não encontrado")
        void shouldThrowNotFoundExceptionWhenProductNotFound() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
            when(productRepository.findById(1L)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                    orderService.createOrder(orderRequestDTO)
            );

            assertTrue(exception.getMessage().contains("Produto não encontrado"));
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando produto está pausado")
        void shouldThrowBusinessExceptionWhenProductIsPaused() {
            product.setIsPaused(true);

            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    orderService.createOrder(orderRequestDTO)
            );

            assertTrue(exception.getMessage().contains("não está disponível"));
        }

        @Test
        @DisplayName("Deve calcular taxa de entrega baseada na distância")
        void shouldCalculateDeliveryFeeBasedOnDistance() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order savedOrder = invocation.getArgument(0);
                assertNotNull(savedOrder.getDeliveryFee());
                assertTrue(savedOrder.getDeliveryFee().compareTo(BigDecimal.ZERO) > 0);
                return order;
            });
            when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponseDTO);

            orderService.createOrder(orderRequestDTO);

            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("Deve aplicar desconto quando fornecido")
        void shouldApplyDiscountWhenProvided() {
            orderRequestDTO.setDiscount(new BigDecimal("5.00"));

            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order savedOrder = invocation.getArgument(0);
                assertEquals(new BigDecimal("5.00"), savedOrder.getDiscount());
                return order;
            });
            when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponseDTO);

            orderService.createOrder(orderRequestDTO);

            verify(orderRepository).save(any(Order.class));
        }
    }

    @Nested
    @DisplayName("getUserOrders Tests")
    class GetUserOrdersTests {

        @Test
        @DisplayName("Deve retornar lista de pedidos do usuário ordenados por data")
        void shouldReturnUserOrdersSortedByDate() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(orderRepository.findByUsuarioOrderByCreatedAtDesc(usuario))
                    .thenReturn(Arrays.asList(order));
            when(orderMapper.toResponseList(anyList()))
                    .thenReturn(Arrays.asList(orderResponseDTO));

            List<OrderResponseDTO> result = orderService.getUserOrders();

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(orderRepository).findByUsuarioOrderByCreatedAtDesc(usuario);
            verify(orderMapper).toResponseList(anyList());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando usuário não tem pedidos")
        void shouldReturnEmptyListWhenUserHasNoOrders() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(orderRepository.findByUsuarioOrderByCreatedAtDesc(usuario))
                    .thenReturn(Collections.emptyList());
            when(orderMapper.toResponseList(anyList()))
                    .thenReturn(Collections.emptyList());

            List<OrderResponseDTO> result = orderService.getUserOrders();

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Deve retornar múltiplos pedidos do usuário")
        void shouldReturnMultipleUserOrders() {
            Order order2 = new Order();
            order2.setId(2L);
            order2.setUsuario(usuario);

            OrderResponseDTO response2 = new OrderResponseDTO();
            response2.setId(2L);

            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(orderRepository.findByUsuarioOrderByCreatedAtDesc(usuario))
                    .thenReturn(Arrays.asList(order, order2));
            when(orderMapper.toResponseList(anyList()))
                    .thenReturn(Arrays.asList(orderResponseDTO, response2));

            List<OrderResponseDTO> result = orderService.getUserOrders();

            assertEquals(2, result.size());
        }
    }

    @Nested
    @DisplayName("getUserOrdersSimple Tests")
    class GetUserOrdersSimpleTests {

        @Test
        @DisplayName("Deve retornar pedidos simplificados sem itens")
        void shouldReturnSimplifiedOrdersWithoutItems() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(orderRepository.findByUsuarioOrderByCreatedAtDesc(usuario))
                    .thenReturn(Arrays.asList(order));
            when(orderMapper.toSimpleResponse(any(Order.class)))
                    .thenReturn(orderResponseDTO);

            List<OrderResponseDTO> result = orderService.getUserOrdersSimple();

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(orderMapper).toSimpleResponse(order);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há pedidos")
        void shouldReturnEmptyListWhenNoOrders() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(orderRepository.findByUsuarioOrderByCreatedAtDesc(usuario))
                    .thenReturn(Collections.emptyList());

            List<OrderResponseDTO> result = orderService.getUserOrdersSimple();

            assertTrue(result.isEmpty());
            verify(orderMapper, never()).toSimpleResponse(any());
        }

        @Test
        @DisplayName("Deve usar toSimpleResponse para cada pedido")
        void shouldUseSimpleResponseForEachOrder() {
            Order order2 = new Order();
            order2.setId(2L);

            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(orderRepository.findByUsuarioOrderByCreatedAtDesc(usuario))
                    .thenReturn(Arrays.asList(order, order2));
            when(orderMapper.toSimpleResponse(any(Order.class)))
                    .thenReturn(orderResponseDTO);

            List<OrderResponseDTO> result = orderService.getUserOrdersSimple();

            assertEquals(2, result.size());
            verify(orderMapper, times(2)).toSimpleResponse(any(Order.class));
        }
    }

    @Nested
    @DisplayName("getOrderById Tests")
    class GetOrderByIdTests {

        @Test
        @DisplayName("Deve retornar pedido completo por ID")
        void shouldReturnCompleteOrderById() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderMapper.toResponse(order)).thenReturn(orderResponseDTO);

            OrderResponseDTO result = orderService.getOrderById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            verify(orderMapper).toResponse(order);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando pedido não existe")
        void shouldThrowNotFoundExceptionWhenOrderNotExists() {
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                    orderService.getOrderById(999L)
            );

            assertTrue(exception.getMessage().contains("Pedido não encontrado"));
        }

        @Test
        @DisplayName("Deve buscar pedido no repositório")
        void shouldSearchOrderInRepository() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderMapper.toResponse(order)).thenReturn(orderResponseDTO);

            orderService.getOrderById(1L);

            verify(orderRepository).findById(1L);
        }
    }

    @Nested
    @DisplayName("getUserOrderById Tests")
    class GetUserOrderByIdTests {

        @Test
        @DisplayName("Deve retornar pedido do usuário autenticado")
        void shouldReturnAuthenticatedUserOrder() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderMapper.toResponse(order)).thenReturn(orderResponseDTO);

            OrderResponseDTO result = orderService.getUserOrderById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando pedido não pertence ao usuário")
        void shouldThrowBusinessExceptionWhenOrderDoesNotBelongToUser() {
            Usuario outroUsuario = new Usuario();
            outroUsuario.setId(2L);
            order.setUsuario(outroUsuario);

            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    orderService.getUserOrderById(1L)
            );

            assertEquals("Pedido não pertence ao usuário", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando pedido não existe")
        void shouldThrowNotFoundExceptionWhenOrderNotExists() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                    orderService.getUserOrderById(999L)
            );
        }
    }

    @Nested
    @DisplayName("findById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Deve retornar entidade Order quando ID existe")
        void shouldReturnOrderEntityWhenIdExists() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            Order result = orderService.findById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando ID não existe")
        void shouldThrowNotFoundExceptionWhenIdNotExists() {
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                    orderService.findById(999L)
            );

            assertTrue(exception.getMessage().contains("Pedido não encontrado: 999"));
        }

        @Test
        @DisplayName("Deve retornar entidade completa com relacionamentos")
        void shouldReturnCompleteEntityWithRelationships() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            Order result = orderService.findById(1L);

            assertNotNull(result.getUsuario());
            assertNotNull(result.getAddressEntrega());
        }
    }

    @Nested
    @DisplayName("findByUsuario Tests")
    class FindByUsuarioTests {

        @Test
        @DisplayName("Deve retornar lista de pedidos do usuário específico")
        void shouldReturnOrdersForSpecificUser() {
            when(orderRepository.findByUsuario(usuario)).thenReturn(Arrays.asList(order));

            List<Order> result = orderService.findByUsuario(usuario);

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(orderRepository).findByUsuario(usuario);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando usuário não tem pedidos")
        void shouldReturnEmptyListWhenUserHasNoOrders() {
            when(orderRepository.findByUsuario(usuario)).thenReturn(Collections.emptyList());

            List<Order> result = orderService.findByUsuario(usuario);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Deve retornar múltiplos pedidos do mesmo usuário")
        void shouldReturnMultipleOrdersForSameUser() {
            Order order2 = new Order();
            order2.setId(2L);
            order2.setUsuario(usuario);

            when(orderRepository.findByUsuario(usuario))
                    .thenReturn(Arrays.asList(order, order2));

            List<Order> result = orderService.findByUsuario(usuario);

            assertEquals(2, result.size());
        }
    }

    @Nested
    @DisplayName("getOrdersByStatus Tests")
    class GetOrdersByStatusTests {

        @Test
        @DisplayName("Deve retornar pedidos por status específico")
        void shouldReturnOrdersBySpecificStatus() {
            when(orderRepository.findByStatus(StatusPedidoEnum.PENDENTE))
                    .thenReturn(Arrays.asList(order));
            when(orderMapper.toResponseList(anyList()))
                    .thenReturn(Arrays.asList(orderResponseDTO));

            List<OrderResponseDTO> result = orderService.getOrdersByStatus(StatusPedidoEnum.PENDENTE);

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(orderRepository).findByStatus(StatusPedidoEnum.PENDENTE);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há pedidos com o status")
        void shouldReturnEmptyListWhenNoOrdersWithStatus() {
            when(orderRepository.findByStatus(StatusPedidoEnum.ENTREGUE))
                    .thenReturn(Collections.emptyList());
            when(orderMapper.toResponseList(anyList()))
                    .thenReturn(Collections.emptyList());

            List<OrderResponseDTO> result = orderService.getOrdersByStatus(StatusPedidoEnum.ENTREGUE);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Deve buscar pedidos para diferentes status")
        void shouldSearchOrdersForDifferentStatuses() {
            when(orderRepository.findByStatus(any(StatusPedidoEnum.class)))
                    .thenReturn(Collections.emptyList());
            when(orderMapper.toResponseList(anyList()))
                    .thenReturn(Collections.emptyList());

            orderService.getOrdersByStatus(StatusPedidoEnum.CONFIRMADO);
            orderService.getOrdersByStatus(StatusPedidoEnum.PREPARANDO);

            verify(orderRepository, times(2)).findByStatus(any(StatusPedidoEnum.class));
        }
    }

    @Nested
    @DisplayName("getPendingOrders Tests")
    class GetPendingOrdersTests {

        @Test
        @DisplayName("Deve retornar pedidos pendentes")
        void shouldReturnPendingOrders() {
            when(orderRepository.findPendingOrders()).thenReturn(Arrays.asList(order));
            when(orderMapper.toResponseList(anyList()))
                    .thenReturn(Arrays.asList(orderResponseDTO));

            List<OrderResponseDTO> result = orderService.getPendingOrders();

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(orderRepository).findPendingOrders();
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há pedidos pendentes")
        void shouldReturnEmptyListWhenNoPendingOrders() {
            when(orderRepository.findPendingOrders()).thenReturn(Collections.emptyList());
            when(orderMapper.toResponseList(anyList()))
                    .thenReturn(Collections.emptyList());

            List<OrderResponseDTO> result = orderService.getPendingOrders();

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Deve usar método específico do repositório")
        void shouldUseSpecificRepositoryMethod() {
            when(orderRepository.findPendingOrders()).thenReturn(Collections.emptyList());
            when(orderMapper.toResponseList(anyList()))
                    .thenReturn(Collections.emptyList());

            orderService.getPendingOrders();

            verify(orderRepository).findPendingOrders();
            verify(orderRepository, never()).findByStatus(any());
        }
    }

    @Nested
    @DisplayName("getActiveOrders Tests")
    class GetActiveOrdersTests {

        @Test
        @DisplayName("Deve retornar pedidos em andamento")
        void shouldReturnActiveOrders() {
            when(orderRepository.findActiveOrders()).thenReturn(Arrays.asList(order));
            when(orderMapper.toResponseList(anyList()))
                    .thenReturn(Arrays.asList(orderResponseDTO));

            List<OrderResponseDTO> result = orderService.getActiveOrders();

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(orderRepository).findActiveOrders();
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há pedidos ativos")
        void shouldReturnEmptyListWhenNoActiveOrders() {
            when(orderRepository.findActiveOrders()).thenReturn(Collections.emptyList());
            when(orderMapper.toResponseList(anyList()))
                    .thenReturn(Collections.emptyList());

            List<OrderResponseDTO> result = orderService.getActiveOrders();

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Deve incluir pedidos confirmados, preparando e despachados")
        void shouldIncludeConfirmedPreparingAndDispatchedOrders() {
            when(orderRepository.findActiveOrders()).thenReturn(Arrays.asList(order));
            when(orderMapper.toResponseList(anyList()))
                    .thenReturn(Arrays.asList(orderResponseDTO));

            orderService.getActiveOrders();

            verify(orderRepository).findActiveOrders();
        }
    }

    @Nested
    @DisplayName("getUserRecentOrders Tests")
    class GetUserRecentOrdersTests {

        @Test
        @DisplayName("Deve retornar pedidos dos últimos 30 dias")
        void shouldReturnOrdersFromLast30Days() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(orderRepository.findByUsuarioAndCreatedAtAfter(eq(usuario), any(LocalDateTime.class)))
                    .thenReturn(Arrays.asList(order));
            when(orderMapper.toResponseList(anyList()))
                    .thenReturn(Arrays.asList(orderResponseDTO));

            List<OrderResponseDTO> result = orderService.getUserRecentOrders();

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(orderRepository).findByUsuarioAndCreatedAtAfter(eq(usuario), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há pedidos recentes")
        void shouldReturnEmptyListWhenNoRecentOrders() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(orderRepository.findByUsuarioAndCreatedAtAfter(eq(usuario), any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());
            when(orderMapper.toResponseList(anyList()))
                    .thenReturn(Collections.emptyList());

            List<OrderResponseDTO> result = orderService.getUserRecentOrders();

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Deve calcular data de 30 dias atrás corretamente")
        void shouldCalculate30DaysAgoCorrectly() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(orderRepository.findByUsuarioAndCreatedAtAfter(eq(usuario), any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());
            when(orderMapper.toResponseList(anyList()))
                    .thenReturn(Collections.emptyList());

            orderService.getUserRecentOrders();

            verify(orderRepository).findByUsuarioAndCreatedAtAfter(eq(usuario), any(LocalDateTime.class));
        }
    }

    @Nested
    @DisplayName("confirmarPedido Tests")
    class ConfirmarPedidoTests {

        @Test
        @DisplayName("Deve confirmar pedido pendente com sucesso")
        void shouldConfirmPendingOrderSuccessfully() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenReturn(order);
            when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponseDTO);

            OrderResponseDTO result = orderService.confirmarPedido(1L);

            assertNotNull(result);
            verify(orderRepository).save(order);
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando transição inválida")
        void shouldThrowBusinessExceptionWhenInvalidTransition() {
            order.setStatus(StatusPedidoEnum.ENTREGUE);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            assertThrows(BusinessException.class, () ->
                    orderService.confirmarPedido(1L)
            );
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando pedido não existe")
        void shouldThrowNotFoundExceptionWhenOrderNotExists() {
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                    orderService.confirmarPedido(999L)
            );
        }
    }

    @Nested
    @DisplayName("iniciarPreparo Tests")
    class IniciarPreparoTests {

        @Test
        @DisplayName("Deve iniciar preparo de pedido confirmado")
        void shouldStartPreparationOfConfirmedOrder() {
            order.setStatus(StatusPedidoEnum.CONFIRMADO);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenReturn(order);
            when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponseDTO);

            OrderResponseDTO result = orderService.iniciarPreparo(1L);

            assertNotNull(result);
            verify(orderRepository).save(order);
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando status não permite preparo")
        void shouldThrowBusinessExceptionWhenStatusDoesNotAllowPreparation() {
            order.setStatus(StatusPedidoEnum.PENDENTE);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            assertThrows(BusinessException.class, () ->
                    orderService.iniciarPreparo(1L)
            );
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando pedido não existe")
        void shouldThrowNotFoundExceptionWhenOrderNotExists() {
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                    orderService.iniciarPreparo(999L)
            );
        }
    }

    @Nested
    @DisplayName("despacharPedido Tests")
    class DespacharPedidoTests {

        @Test
        @DisplayName("Deve despachar pedido em preparo")
        void shouldDispatchOrderInPreparation() {
            order.setStatus(StatusPedidoEnum.PREPARANDO);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenReturn(order);
            when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponseDTO);

            OrderResponseDTO result = orderService.despacharPedido(1L);

            assertNotNull(result);
            verify(orderRepository).save(order);
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando status não permite despacho")
        void shouldThrowBusinessExceptionWhenStatusDoesNotAllowDispatch() {
            order.setStatus(StatusPedidoEnum.CONFIRMADO);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            assertThrows(BusinessException.class, () ->
                    orderService.despacharPedido(1L)
            );
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando pedido não existe")
        void shouldThrowNotFoundExceptionWhenOrderNotExists() {
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                    orderService.despacharPedido(999L)
            );
        }
    }

    @Nested
    @DisplayName("entregarPedido Tests")
    class EntregarPedidoTests {

        @Test
        @DisplayName("Deve marcar pedido despachado como entregue")
        void shouldMarkDispatchedOrderAsDelivered() {
            order.setStatus(StatusPedidoEnum.DESPACHADO);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenReturn(order);
            when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponseDTO);

            OrderResponseDTO result = orderService.entregarPedido(1L);

            assertNotNull(result);
            verify(orderRepository).save(order);
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando status não permite entrega")
        void shouldThrowBusinessExceptionWhenStatusDoesNotAllowDelivery() {
            order.setStatus(StatusPedidoEnum.PREPARANDO);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            assertThrows(BusinessException.class, () ->
                    orderService.entregarPedido(1L)
            );
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando pedido não existe")
        void shouldThrowNotFoundExceptionWhenOrderNotExists() {
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                    orderService.entregarPedido(999L)
            );
        }
    }

    @Nested
    @DisplayName("cancelarPedido Tests")
    class CancelarPedidoTests {

        @Test
        @DisplayName("Deve cancelar pedido com motivo")
        void shouldCancelOrderWithReason() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenReturn(order);
            when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponseDTO);

            OrderResponseDTO result = orderService.cancelarPedido(1L, "Cliente desistiu");

            assertNotNull(result);
            verify(orderRepository).save(order);
        }

        @Test
        @DisplayName("Deve validar transição para status cancelado")
        void shouldValidateTransitionToCanceledStatus() {
            order.setStatus(StatusPedidoEnum.PENDENTE);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenReturn(order);
            when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponseDTO);

            orderService.cancelarPedido(1L, "Motivo");

            verify(orderRepository).save(order);
        }

        @Test
        @DisplayName("Deve lançar BusinessException ao cancelar pedido entregue")
        void shouldThrowBusinessExceptionWhenCancellingDeliveredOrder() {
            order.setStatus(StatusPedidoEnum.ENTREGUE);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            assertThrows(BusinessException.class, () ->
                    orderService.cancelarPedido(1L, "Motivo")
            );
        }
    }

    @Nested
    @DisplayName("cancelarPedidoUsuario Tests")
    class CancelarPedidoUsuarioTests {

        @Test
        @DisplayName("Deve permitir usuário cancelar pedido pendente")
        void shouldAllowUserToCancelPendingOrder() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenReturn(order);
            when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponseDTO);

            OrderResponseDTO result = orderService.cancelarPedidoUsuario(1L, "Mudei de ideia");

            assertNotNull(result);
            verify(orderRepository).save(order);
        }

        @Test
        @DisplayName("Deve permitir usuário cancelar pedido confirmado")
        void shouldAllowUserToCancelConfirmedOrder() {
            order.setStatus(StatusPedidoEnum.CONFIRMADO);

            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenReturn(order);
            when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponseDTO);

            OrderResponseDTO result = orderService.cancelarPedidoUsuario(1L, "Mudei de ideia");

            assertNotNull(result);
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando pedido não pertence ao usuário")
        void shouldThrowBusinessExceptionWhenOrderDoesNotBelongToUser() {
            Usuario outroUsuario = new Usuario();
            outroUsuario.setId(2L);
            order.setUsuario(outroUsuario);

            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    orderService.cancelarPedidoUsuario(1L, "Motivo")
            );

            assertEquals("Pedido não pertence ao usuário", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar BusinessException ao cancelar pedido em preparo")
        void shouldThrowBusinessExceptionWhenCancellingOrderInPreparation() {
            order.setStatus(StatusPedidoEnum.PREPARANDO);

            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    orderService.cancelarPedidoUsuario(1L, "Motivo")
            );

            assertTrue(exception.getMessage().contains("Não é possível cancelar pedido neste status"));
        }

        @Test
        @DisplayName("Deve lançar BusinessException ao cancelar pedido despachado")
        void shouldThrowBusinessExceptionWhenCancellingDispatchedOrder() {
            order.setStatus(StatusPedidoEnum.DESPACHADO);

            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            assertThrows(BusinessException.class, () ->
                    orderService.cancelarPedidoUsuario(1L, "Motivo")
            );
        }

        @Test
        @DisplayName("Deve lançar BusinessException ao cancelar pedido entregue")
        void shouldThrowBusinessExceptionWhenCancellingDeliveredOrder() {
            order.setStatus(StatusPedidoEnum.ENTREGUE);

            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            assertThrows(BusinessException.class, () ->
                    orderService.cancelarPedidoUsuario(1L, "Motivo")
            );
        }
    }

    @Nested
    @DisplayName("countUserOrders Tests")
    class CountUserOrdersTests {

        @Test
        @DisplayName("Deve contar total de pedidos do usuário")
        void shouldCountTotalUserOrders() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(orderRepository.countByUsuario(usuario)).thenReturn(10L);

            long result = orderService.countUserOrders();

            assertEquals(10L, result);
            verify(orderRepository).countByUsuario(usuario);
        }

        @Test
        @DisplayName("Deve retornar zero quando usuário não tem pedidos")
        void shouldReturnZeroWhenUserHasNoOrders() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(orderRepository.countByUsuario(usuario)).thenReturn(0L);

            long result = orderService.countUserOrders();

            assertEquals(0L, result);
        }

        @Test
        @DisplayName("Deve buscar usuário autenticado para contagem")
        void shouldGetAuthenticatedUserForCounting() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(orderRepository.countByUsuario(usuario)).thenReturn(5L);

            orderService.countUserOrders();

            verify(usuarioService).getCurrentUsuario();
        }
    }

    @Nested
    @DisplayName("countUserOrdersByStatus Tests")
    class CountUserOrdersByStatusTests {

        @Test
        @DisplayName("Deve contar pedidos do usuário por status")
        void shouldCountUserOrdersByStatus() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(orderRepository.countByUsuarioAndStatus(usuario, StatusPedidoEnum.ENTREGUE))
                    .thenReturn(5L);

            long result = orderService.countUserOrdersByStatus(StatusPedidoEnum.ENTREGUE);

            assertEquals(5L, result);
        }

        @Test
        @DisplayName("Deve retornar zero quando não há pedidos com o status")
        void shouldReturnZeroWhenNoOrdersWithStatus() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(orderRepository.countByUsuarioAndStatus(usuario, StatusPedidoEnum.CANCELADO))
                    .thenReturn(0L);

            long result = orderService.countUserOrdersByStatus(StatusPedidoEnum.CANCELADO);

            assertEquals(0L, result);
        }

        @Test
        @DisplayName("Deve contar pedidos para diferentes status")
        void shouldCountOrdersForDifferentStatuses() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(orderRepository.countByUsuarioAndStatus(eq(usuario), any(StatusPedidoEnum.class)))
                    .thenReturn(2L);

            orderService.countUserOrdersByStatus(StatusPedidoEnum.PENDENTE);
            orderService.countUserOrdersByStatus(StatusPedidoEnum.CONFIRMADO);

            verify(orderRepository, times(2)).countByUsuarioAndStatus(eq(usuario), any(StatusPedidoEnum.class));
        }
    }

    @Nested
    @DisplayName("calculateUserTotalSpent Tests")
    class CalculateUserTotalSpentTests {

        @Test
        @DisplayName("Deve calcular valor total gasto pelo usuário")
        void shouldCalculateTotalSpentByUser() {
            Order order1 = new Order();
            order1.setTotal(new BigDecimal("50.00"));

            Order order2 = new Order();
            order2.setTotal(new BigDecimal("75.00"));

            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(orderRepository.findByUsuarioAndStatus(usuario, StatusPedidoEnum.ENTREGUE))
                    .thenReturn(Arrays.asList(order1, order2));

            BigDecimal result = orderService.calculateUserTotalSpent();

            assertEquals(new BigDecimal("125.00"), result);
        }

        @Test
        @DisplayName("Deve retornar zero quando usuário não tem pedidos entregues")
        void shouldReturnZeroWhenUserHasNoDeliveredOrders() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(orderRepository.findByUsuarioAndStatus(usuario, StatusPedidoEnum.ENTREGUE))
                    .thenReturn(Collections.emptyList());

            BigDecimal result = orderService.calculateUserTotalSpent();

            assertEquals(BigDecimal.ZERO, result);
        }

        @Test
        @DisplayName("Deve considerar apenas pedidos entregues")
        void shouldConsiderOnlyDeliveredOrders() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(orderRepository.findByUsuarioAndStatus(usuario, StatusPedidoEnum.ENTREGUE))
                    .thenReturn(Collections.emptyList());

            orderService.calculateUserTotalSpent();

            verify(orderRepository).findByUsuarioAndStatus(usuario, StatusPedidoEnum.ENTREGUE);
        }
    }

    @Nested
    @DisplayName("canUserCancelOrder Tests")
    class CanUserCancelOrderTests {

        @Test
        @DisplayName("Deve retornar true quando usuário pode cancelar pedido pendente")
        void shouldReturnTrueWhenUserCanCancelPendingOrder() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            boolean result = orderService.canUserCancelOrder(1L);

            assertTrue(result);
        }

        @Test
        @DisplayName("Deve retornar true quando usuário pode cancelar pedido confirmado")
        void shouldReturnTrueWhenUserCanCancelConfirmedOrder() {
            order.setStatus(StatusPedidoEnum.CONFIRMADO);

            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            boolean result = orderService.canUserCancelOrder(1L);

            assertTrue(result);
        }

        @Test
        @DisplayName("Deve retornar false quando pedido está em preparo")
        void shouldReturnFalseWhenOrderIsInPreparation() {
            order.setStatus(StatusPedidoEnum.PREPARANDO);

            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            boolean result = orderService.canUserCancelOrder(1L);

            assertFalse(result);
        }

        @Test
        @DisplayName("Deve retornar false quando pedido não pertence ao usuário")
        void shouldReturnFalseWhenOrderDoesNotBelongToUser() {
            Usuario outroUsuario = new Usuario();
            outroUsuario.setId(2L);
            order.setUsuario(outroUsuario);

            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            boolean result = orderService.canUserCancelOrder(1L);

            assertFalse(result);
        }

        @Test
        @DisplayName("Deve retornar false quando pedido não existe")
        void shouldReturnFalseWhenOrderNotExists() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            boolean result = orderService.canUserCancelOrder(999L);

            assertFalse(result);
        }

        @Test
        @DisplayName("Deve retornar false em caso de exceção")
        void shouldReturnFalseOnException() {
            when(usuarioService.getCurrentUsuario()).thenThrow(new RuntimeException("Erro"));

            boolean result = orderService.canUserCancelOrder(1L);

            assertFalse(result);
        }
    }
}