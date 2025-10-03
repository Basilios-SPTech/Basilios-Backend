package com.basilios.basilios.core.service;

import com.basilios.basilios.app.dto.endereco.EnderecoRequest;
import com.basilios.basilios.app.dto.order.CreateOrderRequest;
import com.basilios.basilios.app.dto.order.OrderItemRequest;
import com.basilios.basilios.app.dto.order.OrderResponse;
import com.basilios.basilios.core.exception.BusinessException;
import com.basilios.basilios.core.exception.ResourceNotFoundException;
import com.basilios.basilios.core.model.Endereco;
import com.basilios.basilios.core.model.Order;
import com.basilios.basilios.core.model.Produto;
import com.basilios.basilios.core.model.Usuario;
import com.basilios.basilios.infra.repository.EnderecoRepository;
import com.basilios.basilios.infra.repository.OrderRepository;
import com.basilios.basilios.infra.repository.ProdutoRepository;
import com.basilios.basilios.util.DistanceCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private static final double MAX_DELIVERY_DISTANCE_KM = 7.0;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EnderecoRepository enderecoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Value("${store.latitude:#{-23.550520}}")
    private Double storeLatitude;

    @Value("${store.longitude:#{-46.633308}}")
    private Double storeLongitude;

    /**
     * Cria novo pedido
     * Retorna OrderResponse com redirecionamento se necessário
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Usuario usuario = usuarioService.getCurrentUsuario();

        // Criar e salvar endereço de entrega
        Endereco enderecoEntrega = createEndereco(request.getEnderecoEntrega(), usuario);
        enderecoEntrega = enderecoRepository.save(enderecoEntrega);

        // Verificar distância
        double distance = DistanceCalculator.calculateDistance(
                storeLatitude, storeLongitude,
                enderecoEntrega.getLatitude(), enderecoEntrega.getLongitude()
        );

        // Se fora da área de entrega, retornar redirecionamento
        if (distance > MAX_DELIVERY_DISTANCE_KM) {
            Map<String, String> partnerLinks = new HashMap<>();
            partnerLinks.put("ifood", "https://www.ifood.com.br");
            partnerLinks.put("99food", "https://www.99food.com.br");

            return OrderResponse.builder()
                    .redirectToPartners(true)
                    .partnerLinks(partnerLinks)
                    .build();
        }

        // Processar itens do pedido
        List<Order.OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            Produto produto = produtoRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + itemRequest.getProductId()));

            if (produto.getIsPaused()) {
                throw new BusinessException("Produto " + produto.getNomeProduto() + " não está disponível");
            }

            BigDecimal subtotal = produto.getPreco().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            Order.OrderItem orderItem = new Order.OrderItem(
                    produto.getIdProduto(),
                    produto.getNomeProduto(),
                    itemRequest.getQuantity(),
                    produto.getPreco(),
                    subtotal
            );

            orderItems.add(orderItem);
            total = total.add(subtotal);
        }

        // Criar e salvar pedido
        Order order = Order.builder()
                .usuario(usuario)
                .items(orderItems)
                .total(total)
                .enderecoEntrega(enderecoEntrega)
                .build();

        order = orderRepository.save(order);

        return OrderResponse.builder()
                .id(order.getId())
                .items(order.getItems())
                .total(order.getTotal())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .redirectToPartners(false)
                .build();
    }

    /**
     * Lista pedidos do usuário autenticado
     */
    @Transactional(readOnly = true)
    public List<Order> getUserOrders() {
        Usuario usuario = usuarioService.getCurrentUsuario();
        return orderRepository.findByUsuarioOrderByCreatedAtDesc(usuario);
    }

    /**
     * Busca pedido por ID
     */
    @Transactional(readOnly = true)
    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado: " + id));
    }

    /**
     * Busca pedidos de um usuário específico
     */
    @Transactional(readOnly = true)
    public List<Order> findByUsuario(Usuario usuario) {
        return orderRepository.findByUsuario(usuario);
    }

    /**
     * Confirma pedido (muda status para CONFIRMADO)
     */
    @Transactional
    public Order confirmarPedido(Long id) {
        Order order = findById(id);
        order.confirmar();
        return orderRepository.save(order);
    }

    /**
     * Inicia preparo do pedido
     */
    @Transactional
    public Order iniciarPreparo(Long id) {
        Order order = findById(id);
        order.iniciarPreparo();
        return orderRepository.save(order);
    }

    /**
     * Despacha pedido para entrega
     */
    @Transactional
    public Order despacharPedido(Long id) {
        Order order = findById(id);
        order.despachar();
        return orderRepository.save(order);
    }

    /**
     * Marca pedido como entregue
     */
    @Transactional
    public Order entregarPedido(Long id) {
        Order order = findById(id);
        order.entregar();
        return orderRepository.save(order);
    }

    /**
     * Cancela pedido
     */
    @Transactional
    public Order cancelarPedido(Long id) {
        Order order = findById(id);
        order.cancelar();
        return orderRepository.save(order);
    }

    /**
     * Cria objeto Endereco a partir do DTO
     */
    private Endereco createEndereco(EnderecoRequest request, Usuario usuario) {
        Endereco endereco = Endereco.builder()
                .usuario(usuario)
                .rua(request.getRua())
                .numero(request.getNumero())
                .bairro(request.getBairro())
                .cep(request.getCep())
                .cidade(request.getCidade())
                .estado(request.getEstado())
                .complemento(request.getComplemento())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();

        return endereco;
    }
}