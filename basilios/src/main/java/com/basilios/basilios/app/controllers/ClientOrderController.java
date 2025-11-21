package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.order.OrderRequestDTO;
import com.basilios.basilios.app.dto.order.OrderResponseDTO;
import com.basilios.basilios.core.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cliente/orders")
@PreAuthorize("hasRole('CLIENTE')")
@Tag(name = "Cliente - Pedidos", description = "Gerenciamento de pedidos do cliente")
@SecurityRequirement(name = "bearer-jwt")
public class ClientOrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    @Operation(summary = "Criar novo pedido", description = "Cliente cria um novo pedido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou endereço fora da área de entrega"),
            @ApiResponse(responseCode = "404", description = "Endereço ou produto não encontrado")
    })
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody OrderRequestDTO request) {
        OrderResponseDTO response = orderService.createOrder(request);

        // Se redirecionou para parceiros (fora da área)
        if (response.getRedirectToPartners() != null && response.getRedirectToPartners()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar meus pedidos", description = "Lista todos os pedidos do cliente autenticado")
    @ApiResponse(responseCode = "200", description = "Lista de pedidos retornada com sucesso")
    public ResponseEntity<List<OrderResponseDTO>> getMyOrders() {
        List<OrderResponseDTO> orders = orderService.getUserOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/simple")
    @Operation(summary = "Listar meus pedidos (simplificado)",
            description = "Lista pedidos do cliente sem detalhes dos items")
    @ApiResponse(responseCode = "200", description = "Lista simplificada retornada com sucesso")
    public ResponseEntity<List<OrderResponseDTO>> getMyOrdersSimple() {
        List<OrderResponseDTO> orders = orderService.getUserOrdersSimple();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar meu pedido por ID", description = "Retorna detalhes de um pedido específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
            @ApiResponse(responseCode = "403", description = "Pedido não pertence ao cliente"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    public ResponseEntity<OrderResponseDTO> getMyOrderById(
            @Parameter(description = "ID do pedido") @PathVariable Long id) {
        OrderResponseDTO order = orderService.getUserOrderById(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/recent")
    @Operation(summary = "Listar meus pedidos recentes",
            description = "Lista pedidos dos últimos 30 dias")
    @ApiResponse(responseCode = "200", description = "Lista de pedidos recentes")
    public ResponseEntity<List<OrderResponseDTO>> getMyRecentOrders() {
        List<OrderResponseDTO> orders = orderService.getUserRecentOrders();
        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancelar meu pedido",
            description = "Cliente cancela seu próprio pedido (apenas PENDENTE ou CONFIRMADO)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido cancelado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Não é possível cancelar pedido neste status"),
            @ApiResponse(responseCode = "403", description = "Pedido não pertence ao cliente"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    public ResponseEntity<OrderResponseDTO> cancelMyOrder(
            @Parameter(description = "ID do pedido") @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String motivo = body != null ? body.get("motivo") : "Cancelado pelo cliente";
        OrderResponseDTO order = orderService.cancelarPedidoUsuario(id, motivo);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{id}/can-cancel")
    @Operation(summary = "Verificar se pode cancelar",
            description = "Verifica se o cliente pode cancelar o pedido")
    @ApiResponse(responseCode = "200", description = "Retorna true/false")
    public ResponseEntity<Map<String, Boolean>> canCancelOrder(
            @Parameter(description = "ID do pedido") @PathVariable Long id) {
        boolean canCancel = orderService.canUserCancelOrder(id);
        return ResponseEntity.ok(Map.of("canCancel", canCancel));
    }

    @GetMapping("/stats/count")
    @Operation(summary = "Contar meus pedidos", description = "Retorna total de pedidos do cliente")
    @ApiResponse(responseCode = "200", description = "Contagem retornada")
    public ResponseEntity<Map<String, Long>> getMyOrdersCount() {
        long count = orderService.countUserOrders();
        return ResponseEntity.ok(Map.of("totalOrders", count));
    }

    @GetMapping("/stats/total-spent")
    @Operation(summary = "Total gasto", description = "Retorna valor total gasto em pedidos entregues")
    @ApiResponse(responseCode = "200", description = "Valor total retornado")
    public ResponseEntity<Map<String, BigDecimal>> getMyTotalSpent() {
        BigDecimal total = orderService.calculateUserTotalSpent();
        return ResponseEntity.ok(Map.of("totalSpent", total));
    }
}