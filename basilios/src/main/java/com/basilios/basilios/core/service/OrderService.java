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
import com.basilios.basilios.util.DistanceCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private static final double MAX_DELIVERY_DISTANCE_KM = 7.0;
    private static final BigDecimal BASE_DELIVERY_FEE = new BigDecimal("5.00");
    private static final BigDecimal DELIVERY_FEE_PER_KM = new BigDecimal("2.00");

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private OrderMapper orderMapper;

    @Value("${store.latitude:#{-23.550520}}")
    private Double storeLatitude;

    @Value("${store.longitude:#{-46.633308}}")
    private Double storeLongitude;

    /**
     * Cria novo pedido com relacionamento puro (ProductOrder)
     */



    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO request) {
        Usuario usuario = usuarioService.getCurrentUsuario();

        // Buscar endereço de entrega
        Address addressEntrega = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new NotFoundException("Endereço não encontrado: " + request.getAddressId()));

        // Verificar se endereço pertence ao usuário
        if (!addressEntrega.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessException("Endereço não pertence ao usuário");
        }

        // Verificar se endereço está ativo
        if (!addressEntrega.isAtivo()) {
            throw new BusinessException("Endereço não está ativo");
        }

        // Verificar distância
        double distance = DistanceCalculator.calculateDistance(
                storeLatitude, storeLongitude,
                addressEntrega.getLatitude(), addressEntrega.getLongitude()
        );

        // Se fora da área de entrega, retornar redirecionamento
        if (distance > MAX_DELIVERY_DISTANCE_KM) {
            Map<String, String> partnerLinks = new HashMap<>();
            partnerLinks.put("ifood", "https://www.ifood.com.br");
            partnerLinks.put("99food", "https://www.99food.com.br");
            partnerLinks.put("rappi", "https://www.rappi.com.br");

            return OrderResponseDTO.builder()
                    .redirectToPartners(true)
                    .partnerLinks(partnerLinks)
                    .build();
        }

        // Criar pedido
        Order order = Order.builder()
                .usuario(usuario)
                .addressEntrega(addressEntrega)
                .status(StatusPedidoEnum.PENDENTE)
                .observations(request.getObservations())
                .build();

        // Processar items do pedido
        for (OrderRequestDTO.OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new NotFoundException("Produto não encontrado: " + itemRequest.getProductId()));

            // Verificar se produto está disponível
            if (product.getIsPaused()) {
                throw new BusinessException("Produto '" + product.getName() + "' não está disponível");
            }

            // Determinar preço (verifica se há promoção ativa)
            BigDecimal unitPrice = product.getFinalPrice(); // Já considera promoções
            BigDecimal originalPrice = product.getPrice();
            boolean hadPromotion = product.isOnPromotion();
            String promotionName = null;

            if (hadPromotion) {
                Promotion promotion = product.getBestCurrentPromotion();
                if (promotion != null) {
                    promotionName = promotion.getTitle();
                }
            }

            // Criar ProductOrder
            ProductOrder productOrder = ProductOrder.builder()
                    .product(product)
                    .order(order)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(unitPrice)
                    .productName(product.getName())
                    .observations(itemRequest.getObservations())
                    .hadPromotion(hadPromotion)
                    .promotionName(promotionName)
                    .originalPrice(hadPromotion ? originalPrice : null)
                    .build();

            // calculateSubtotal() será chamado automaticamente no @PrePersist
            order.getProductOrders().add(productOrder);
        }

        // Calcular taxa de entrega baseada na distância
        BigDecimal deliveryFee = calculateDeliveryFee(distance);
        order.setDeliveryFee(deliveryFee);

        // Aplicar desconto se fornecido
        if (request.getDiscount() != null && request.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
            order.setDiscount(request.getDiscount());
        }

        // calculateTotal() será chamado automaticamente no @PrePersist
        order = orderRepository.save(order);

        // Retornar resposta
        return orderMapper.toResponse(order);
    }

    /**
     * Calcula taxa de entrega baseada na distância
     * Fórmula: BASE_FEE + (distância * FEE_PER_KM)
     */
    private BigDecimal calculateDeliveryFee(double distanceKm) {
        if (distanceKm <= 0) {
            return BASE_DELIVERY_FEE;
        }

        BigDecimal distanceFee = DELIVERY_FEE_PER_KM.multiply(BigDecimal.valueOf(distanceKm));
        BigDecimal totalFee = BASE_DELIVERY_FEE.add(distanceFee);

        // Arredondar para 2 casas decimais
        return totalFee.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Lista pedidos do usuário autenticado (ordenados por data decrescente)
     */
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getUserOrders() {
        Usuario usuario = usuarioService.getCurrentUsuario();
        List<Order> orders = orderRepository.findByUsuarioOrderByCreatedAtDesc(usuario);
        return orderMapper.toResponseList(orders);
    }

    /**
     * Lista pedidos do usuário autenticado de forma simplificada (sem items)
     */
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getUserOrdersSimple() {
        Usuario usuario = usuarioService.getCurrentUsuario();
        List<Order> orders = orderRepository.findByUsuarioOrderByCreatedAtDesc(usuario);
        return orders.stream()
                .map(orderMapper::toSimpleResponse)
                .toList();
    }

    /**
     * Busca pedido por ID (completo com items)
     */
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(Long id) {
        Order order = findById(id);
        return orderMapper.toResponse(order);
    }

    /**
     * Busca pedido por ID do usuário autenticado
     */
    @Transactional(readOnly = true)
    public OrderResponseDTO getUserOrderById(Long id) {
        Usuario usuario = usuarioService.getCurrentUsuario();
        Order order = findById(id);

        // Verificar se pedido pertence ao usuário
        if (!order.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessException("Pedido não pertence ao usuário");
        }

        return orderMapper.toResponse(order);
    }

    /**
     * Busca entidade Order por ID (uso interno)
     */
    @Transactional(readOnly = true)
    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pedido não encontrado: " + id));
    }

    /**
     * Busca pedidos de um usuário específico (admin)
     */
    @Transactional(readOnly = true)
    public List<Order> findByUsuario(Usuario usuario) {
        return orderRepository.findByUsuario(usuario);
    }

    /**
     * Busca pedidos por status
     */
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByStatus(StatusPedidoEnum status) {
        List<Order> orders = orderRepository.findByStatus(status);
        return orderMapper.toResponseList(orders);
    }

    /**
     * Lista todos os pedidos (admin) — inclui todos os pedidos do sistema
     */
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orderMapper.toResponseList(orders);
    }

    /**
     * Busca pedidos pendentes (para cozinha)
     */
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getPendingOrders() {
        List<Order> orders = orderRepository.findPendingOrders();
        return orderMapper.toResponseList(orders);
    }

    /**
     * Busca pedidos em andamento (confirmado, preparando, despachado)
     */
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getActiveOrders() {
        List<Order> orders = orderRepository.findActiveOrders();
        return orderMapper.toResponseList(orders);
    }

    /**
     * Busca pedidos recentes de um usuário (últimos 30 dias)
     */
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getUserRecentOrders() {
        Usuario usuario = usuarioService.getCurrentUsuario();
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Order> orders = orderRepository.findByUsuarioAndCreatedAtAfter(usuario, thirtyDaysAgo);
        return orderMapper.toResponseList(orders);
    }

    // ========== MUDANÇA DE STATUS ==========

    /**
     * Confirma pedido (PENDENTE → CONFIRMADO)
     */
    @Transactional
    public OrderResponseDTO confirmarPedido(Long id) {
        Order order = findById(id);
        validateStatusTransition(order, StatusPedidoEnum.CONFIRMADO);
        order.confirmar();
        order = orderRepository.save(order);
        return orderMapper.toResponse(order);
    }

    /**
     * Inicia preparo do pedido (CONFIRMADO → PREPARANDO)
     */
    @Transactional
    public OrderResponseDTO iniciarPreparo(Long id) {
        Order order = findById(id);
        validateStatusTransition(order, StatusPedidoEnum.PREPARANDO);
        order.iniciarPreparo();
        order = orderRepository.save(order);
        return orderMapper.toResponse(order);
    }

    /**
     * Despacha pedido para entrega (PREPARANDO → DESPACHADO)
     */
    @Transactional
    public OrderResponseDTO despacharPedido(Long id) {
        Order order = findById(id);
        validateStatusTransition(order, StatusPedidoEnum.DESPACHADO);
        order.despachar();
        order = orderRepository.save(order);
        return orderMapper.toResponse(order);
    }

    /**
     * Marca pedido como entregue (DESPACHADO → ENTREGUE)
     */
    @Transactional
    public OrderResponseDTO entregarPedido(Long id) {
        Order order = findById(id);
        validateStatusTransition(order, StatusPedidoEnum.ENTREGUE);
        order.entregar();
        order = orderRepository.save(order);
        return orderMapper.toResponse(order);
    }

    /**
     * Cancela pedido (qualquer status → CANCELADO, exceto ENTREGUE)
     */
    @Transactional
    public OrderResponseDTO cancelarPedido(Long id, String motivo) {
        Order order = findById(id);
        validateStatusTransition(order, StatusPedidoEnum.CANCELADO);
        order.cancelar(motivo);
        order = orderRepository.save(order);
        return orderMapper.toResponse(order);
    }

    /**
     * Cancela pedido do usuário autenticado
     * Só pode cancelar se estiver PENDENTE ou CONFIRMADO
     */
    @Transactional
    public OrderResponseDTO cancelarPedidoUsuario(Long id, String motivo) {
        Usuario usuario = usuarioService.getCurrentUsuario();
        Order order = findById(id);

        // Verificar se pedido pertence ao usuário
        if (!order.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessException("Pedido não pertence ao usuário");
        }

        // Cliente só pode cancelar pedidos PENDENTE ou CONFIRMADO
        if (!order.isPendente() && !order.isConfirmado()) {
            throw new BusinessException("Não é possível cancelar pedido neste status: " + order.getStatus());
        }

        order.cancelar(motivo);
        order = orderRepository.save(order);
        return orderMapper.toResponse(order);
    }

    /**
     * Valida se é possível transicionar para o novo status
     */
    private void validateStatusTransition(Order order, StatusPedidoEnum newStatus) {
        if (!order.getStatus().podeTransicionarPara(newStatus)) {
            throw new BusinessException(
                    String.format("Não é possível mudar status de %s para %s",
                            order.getStatus(), newStatus)
            );
        }
    }

    // ========== ESTATÍSTICAS ==========

    /**
     * Conta total de pedidos do usuário
     */
    @Transactional(readOnly = true)
    public long countUserOrders() {
        Usuario usuario = usuarioService.getCurrentUsuario();
        return orderRepository.countByUsuario(usuario);
    }

    /**
     * Conta pedidos do usuário por status
     */
    @Transactional(readOnly = true)
    public long countUserOrdersByStatus(StatusPedidoEnum status) {
        Usuario usuario = usuarioService.getCurrentUsuario();
        return orderRepository.countByUsuarioAndStatus(usuario, status);
    }

    /**
     * Calcula valor total gasto pelo usuário
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateUserTotalSpent() {
        Usuario usuario = usuarioService.getCurrentUsuario();
        List<Order> orders = orderRepository.findByUsuarioAndStatus(usuario, StatusPedidoEnum.ENTREGUE);

        return orders.stream()
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Verifica se pedido pode ser cancelado pelo usuário
     */
    @Transactional(readOnly = true)
    public boolean canUserCancelOrder(Long orderId) {
        try {
            Usuario usuario = usuarioService.getCurrentUsuario();
            Order order = findById(orderId);

            if (!order.getUsuario().getId().equals(usuario.getId())) {
                return false;
            }

            return order.isPendente() || order.isConfirmado();
        } catch (Exception e) {
            return false;
        }
    }
}
