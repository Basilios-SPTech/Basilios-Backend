package com.basilios.basilios.service;

import com.basilios.basilios.dto.endereco.EnderecoRequest;
import com.basilios.basilios.dto.order.CreateOrderRequest;
import com.basilios.basilios.dto.order.OrderItemRequest;
import com.basilios.basilios.dto.order.OrderResponse;
import com.basilios.basilios.exception.BusinessException;
import com.basilios.basilios.exception.InvalidDistanceException;
import com.basilios.basilios.exception.ResourceNotFoundException;
import com.basilios.basilios.model.*;
import com.basilios.basilios.repository.EnderecoRepository;
import com.basilios.basilios.repository.OrderRepository;
import com.basilios.basilios.repository.ProdutoRepository;
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
    private UserService userService;

    @Autowired
    private StoreService storeService;

    @Value("${store.latitude:#{-23.550520}}")
    private Double storeLatitude;

    @Value("${store.longitude:#{-46.633308}}")
    private Double storeLongitude;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        User user = userService.getCurrentUser();

        // Criar e salvar endereço de entrega
        Endereco enderecoEntrega = createEndereco(request.getEnderecoEntrega());
        enderecoEntrega = enderecoRepository.save(enderecoEntrega);

        // Verificar distância
        double distance = DistanceCalculator.calculateDistance(
                storeLatitude, storeLongitude,
                enderecoEntrega.getLatitude(), enderecoEntrega.getLongitude()
        );

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
            Produto produto = produtoRepository.findById((long) itemRequest.getProductId().intValue())
                    .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));

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
                .user(user)
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

    private Endereco createEndereco(EnderecoRequest request) {
        Endereco endereco = new Endereco();
        endereco.setRua(request.getRua());
        endereco.setNumero(request.getNumero());
        endereco.setBairro(request.getBairro());
        endereco.setCep(request.getCep());
        endereco.setCidade(request.getCidade());
        endereco.setEstado(request.getEstado());
        endereco.setComplemento(request.getComplemento());
        endereco.setLatitude(request.getLatitude());
        endereco.setLongitude(request.getLongitude());
        return endereco;
    }

    public List<Order> getUserOrders() {
        User user = userService.getCurrentUser();
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));
    }
}