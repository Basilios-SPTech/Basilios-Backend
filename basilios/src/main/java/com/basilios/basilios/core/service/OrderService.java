package com.basilios.basilios.core.service;

import com.basilios.basilios.app.dto.order.OrderRequestDTO;
import com.basilios.basilios.app.dto.order.OrderResponseDTO;
import com.basilios.basilios.app.mapper.OrderMapper;
import com.basilios.basilios.core.enums.StatusPagamentoEnum;
import com.basilios.basilios.core.enums.StatusPedidoEnum;
import com.basilios.basilios.core.exception.BusinessException;
import com.basilios.basilios.core.exception.NotFoundException;
import com.basilios.basilios.core.model.*;
import com.basilios.basilios.core.model.events.OrderStatusChangedEvent;
import com.basilios.basilios.infra.messaging.NotificationEventPublisher;
import com.basilios.basilios.infra.repository.AddressRepository;
import com.basilios.basilios.infra.repository.AdicionalProductRepository;
import com.basilios.basilios.infra.repository.AdicionalRepository;
import com.basilios.basilios.infra.repository.OrderRepository;
import com.basilios.basilios.infra.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final AdicionalRepository adicionalRepository;
    private final AdicionalProductRepository adicionalProductRepository;
    private final UsuarioService usuarioService;
    private final StoreService storeService;
    private final OrderMapper orderMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationEventPublisher notificationEventPublisher;

    /**
     * Cria novo pedido com relacionamento puro (ProductOrder)
     */



    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO request) {
        log.info("=== CRIANDO PEDIDO ===");
        log.info("Request recebido: addressId={}, items={}, discount={}",
                request.getAddressId(),
                request.getItems() != null ? request.getItems().size() : "null",
                request.getDiscount());

        log.info("Buscando usuário autenticado...");
        Usuario usuario = usuarioService.getCurrentUsuario();
        log.info("Usuário encontrado: id={}, email={}", usuario.getId(), usuario.getEmail());

        // Buscar endereço de entrega
        log.info("Buscando endereço id={}", request.getAddressId());
        Address addressEntrega = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new NotFoundException("Endereço não encontrado: " + request.getAddressId()));
        log.info("Endereço encontrado: id={}, ativo={}, usuarioId={}",
                addressEntrega.getIdAddress(), addressEntrega.isAtivo(),
                addressEntrega.getUsuario() != null ? addressEntrega.getUsuario().getId() : "null");

        // Verificar se endereço pertence ao usuário
        if (!addressEntrega.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessException("Endereço não pertence ao usuário");
        }

        // Verificar se endereço está ativo
        if (!addressEntrega.isAtivo()) {
            throw new BusinessException("Endereço não está ativo");
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

            // Processar adicionais do item
            if (itemRequest.getAdicionais() != null) {
                for (OrderRequestDTO.AdicionalItemRequest adicionalRequest : itemRequest.getAdicionais()) {
                    Adicional adicional = adicionalRepository.findById(adicionalRequest.getAdicionalId())
                            .orElseThrow(() -> new NotFoundException("Adicional não encontrado: " + adicionalRequest.getAdicionalId()));

                    if (!adicional.getAvailable()) {
                        throw new BusinessException("Adicional '" + adicional.getName() + "' não está disponível");
                    }

                    if (!adicionalProductRepository.existsByProductIdAndAdicionalId(product.getId(), adicional.getId())) {
                        throw new BusinessException("Adicional '" + adicional.getName() + "' não pertence ao produto '" + product.getName() + "'");
                    }

                    ProductOrderAdicional poa = ProductOrderAdicional.builder()
                            .productOrder(productOrder)
                            .adicionalId(adicional.getId())
                            .adicionalName(adicional.getName())
                            .unitPrice(adicional.getPrice())
                            .quantity(adicionalRequest.getQuantity())
                            .build();
                    poa.calculateSubtotal();
                    productOrder.getAdicionais().add(poa);
                }
            }

            // Calcula o subtotal do item (produto + adicionais)
            productOrder.calculateSubtotal();
            order.getProductOrders().add(productOrder);
        }

        // Taxa de entrega configurada na loja
        BigDecimal deliveryFee = storeService.getMainStore().getDeliveryFee();
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
     * Lista pedidos do usuário autenticado (ordenados por data decrescente)
     */

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getUserOrders(Pageable pageable) {
        Usuario usuario = usuarioService.getCurrentUsuario();
        Page<Order> orders = orderRepository.findByUsuarioOrderByCreatedAtDesc(usuario, pageable);
        return orders.map(orderMapper::toResponse);
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
     * Busca pedidos por status
     */

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getOrdersByStatus(StatusPedidoEnum status, Pageable pageable) {
        Page<Order> orders = orderRepository.findByStatus(status, pageable);
        return orders.map(orderMapper::toResponse);
    }

    /**
     * Lista todos os pedidos (admin) — inclui todos os pedidos do sistema
     */
    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getAllOrders(Pageable pageable) {
        Page<Order> page = orderRepository.findAll(pageable);
        return page.map(orderMapper::toResponse);
    }

    /**
     * Soft delete (marca deletedAt via JPA @SQLDelete ou via serviço se for necessário)
     */
    @Transactional
    public void softDelete(Long id) {
        Order order = findById(id);
        orderRepository.delete(order);
    }

    // ========== MUDANÇA DE STATUS ==========

    /**
     * Cancela pedido do usuário autenticado
     * Só pode cancelar se estiver PENDENTE ou CONFIRMADO
     */
    @Transactional
    public OrderResponseDTO cancelarPedidoUsuario(Long id, String motivo) {
        Usuario usuario = usuarioService.getCurrentUsuario();
        Order order = findById(id);

        if (!order.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessException("Pedido não pertence ao usuário");
        }

        if (!order.isPendente() && !order.isConfirmado()) {
            throw new BusinessException("Não é possível cancelar pedido neste status: " + order.getStatus());
        }

        StatusPedidoEnum oldStatus = order.getStatus();
        order.cancelar(motivo);
        order = orderRepository.save(order);

        publishStatusChangedEvent(order, oldStatus, StatusPedidoEnum.CANCELADO, motivo);
        log.info("Pedido {} cancelado pelo usuário. Motivo: {}", order.getCodigoPedido(), motivo);

        return orderMapper.toResponse(order);
    }

    // ========== MUDANÇA DE STATUS ==========

    /**
     * Atualiza o status de um pedido de forma genérica, validando a transição.
     * Toda lógica de transição é delegada ao enum (validação) e à entity (execução).
     */
    @Transactional
    public OrderResponseDTO updateOrderStatus(Long id, String statusStr) {
        StatusPedidoEnum novoStatus;
        try {
            novoStatus = StatusPedidoEnum.valueOf(statusStr.toUpperCase());
        } catch (Exception e) {
            throw new BusinessException("Status inválido: " + statusStr);
        }

        Order order = findById(id);
        StatusPedidoEnum oldStatus = order.getStatus();

        if (!oldStatus.podeTransicionarPara(novoStatus)) {
            throw new BusinessException(
                    String.format("Não é possível mudar status de %s para %s", oldStatus, novoStatus));
        }

        switch (novoStatus) {
            case CONFIRMADO -> order.confirmar();
            case PREPARANDO -> order.iniciarPreparo();
            case DESPACHADO -> order.despachar();
            case ENTREGUE -> order.entregar();
            case CANCELADO -> order.cancelar("Cancelado via API");
            default -> throw new BusinessException("Transição de status não suportada: " + novoStatus);
        }

        order = orderRepository.save(order);
        publishStatusChangedEvent(order, oldStatus, novoStatus);
        log.info("Pedido {} atualizado: {} → {}", order.getCodigoPedido(), oldStatus, novoStatus);

        return orderMapper.toResponse(order);
    }

    // ========== PAGAMENTO ==========

    /**
     * Atualiza o status de pagamento de um pedido.
     * Toda lógica de transição está no enum + entity.
     */
    @Transactional
    public OrderResponseDTO updatePaymentStatus(Long id, String statusStr) {
        StatusPagamentoEnum novoStatus;
        try {
            novoStatus = StatusPagamentoEnum.fromValor(statusStr);
        } catch (Exception e) {
            throw new BusinessException("Status de pagamento inválido: " + statusStr);
        }

        Order order = findById(id);
        order.atualizarPagamento(novoStatus);
        order = orderRepository.save(order);

        log.info("Pagamento do pedido {} atualizado para: {}", order.getCodigoPedido(), novoStatus);
        return orderMapper.toResponse(order);
    }

    // ========== EVENTOS ==========

    /**
     * Publica evento de mudança de status do pedido
     */
    private void publishStatusChangedEvent(Order order, StatusPedidoEnum oldStatus, StatusPedidoEnum newStatus) {
        publishStatusChangedEvent(order, oldStatus, newStatus, null);
    }

    /**
     * Publica evento de mudança de status do pedido (com motivo opcional)
     * Envia tanto via Spring Events (para WebSocket/Dashboard) quanto via RabbitMQ (para email-api)
     */
    private void publishStatusChangedEvent(Order order, StatusPedidoEnum oldStatus, StatusPedidoEnum newStatus, String motivo) {
        try {
            // Evento local Spring (para WebSocket/Dashboard listener)
            OrderStatusChangedEvent event = new OrderStatusChangedEvent(order, oldStatus, newStatus, motivo);
            eventPublisher.publishEvent(event);
            log.debug("Evento local publicado: {}", event);
        } catch (Exception e) {
            log.error("Erro ao publicar evento local do pedido {}: {}", order.getId(), e.getMessage());
        }

        try {
            // Evento RabbitMQ (para microserviço email-api)
            notificationEventPublisher.publishOrderStatusChanged(order, oldStatus, newStatus, motivo);
        } catch (Exception e) {
            log.error("Erro ao publicar evento RabbitMQ do pedido {}: {}", order.getId(), e.getMessage());
        }
    }
}
