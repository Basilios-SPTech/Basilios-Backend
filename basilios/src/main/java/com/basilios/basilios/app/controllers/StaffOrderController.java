package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.order.OrderResponseDTO;
import com.basilios.basilios.core.enums.StatusPedidoEnum;
import com.basilios.basilios.core.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/funcionario/orders")
@PreAuthorize("hasRole('FUNCIONARIO')")
@Tag(name = "Funcionário - Pedidos", description = "Gerenciamento de pedidos para funcionários")
@SecurityRequirement(name = "bearer-jwt")
@RequiredArgsConstructor
public class StaffOrderController {

    private final OrderService orderService;

    // ========== LISTAGEM DE PEDIDOS ==========

    @GetMapping("/pending")
    @Operation(summary = "Listar pedidos pendentes",
            description = "Lista pedidos aguardando confirmação")
    @ApiResponse(responseCode = "200", description = "Lista de pedidos pendentes")
    public ResponseEntity<List<OrderResponseDTO>> getPendingOrders() {
        List<OrderResponseDTO> orders = orderService.getPendingOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/active")
    @Operation(summary = "Listar pedidos em andamento",
            description = "Lista pedidos confirmados, preparando ou despachados")
    @ApiResponse(responseCode = "200", description = "Lista de pedidos ativos")
    public ResponseEntity<List<OrderResponseDTO>> getActiveOrders() {
        List<OrderResponseDTO> orders = orderService.getActiveOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/by-status")
    @Operation(summary = "Listar pedidos por status",
            description = "Lista pedidos filtrados por status específico")
    @ApiResponse(responseCode = "200", description = "Lista de pedidos")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByStatus(
            @Parameter(description = "Status do pedido")
            @RequestParam StatusPedidoEnum status) {
        List<OrderResponseDTO> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pedido por ID",
            description = "Retorna detalhes completos de qualquer pedido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @Parameter(description = "ID do pedido") @PathVariable Long id) {
        OrderResponseDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    // ========== MUDANÇAS DE STATUS (COZINHA) ==========

    @PatchMapping("/{id}/confirm")
    @Operation(summary = "Confirmar pedido",
            description = "Muda status de PENDENTE para CONFIRMADO")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido confirmado"),
            @ApiResponse(responseCode = "400", description = "Transição de status inválida"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    public ResponseEntity<OrderResponseDTO> confirmOrder(
            @Parameter(description = "ID do pedido") @PathVariable Long id) {
        OrderResponseDTO order = orderService.confirmarPedido(id);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{id}/start-preparation")
    @Operation(summary = "Iniciar preparo",
            description = "Muda status de CONFIRMADO para PREPARANDO")
    @ApiResponse(responseCode = "200", description = "Preparo iniciado")
    public ResponseEntity<OrderResponseDTO> startPreparation(
            @Parameter(description = "ID do pedido") @PathVariable Long id) {
        OrderResponseDTO order = orderService.iniciarPreparo(id);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{id}/dispatch")
    @Operation(summary = "Despachar pedido",
            description = "Muda status de PREPARANDO para DESPACHADO")
    @ApiResponse(responseCode = "200", description = "Pedido despachado")
    public ResponseEntity<OrderResponseDTO> dispatchOrder(
            @Parameter(description = "ID do pedido") @PathVariable Long id) {
        OrderResponseDTO order = orderService.despacharPedido(id);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{id}/deliver")
    @Operation(summary = "Marcar como entregue",
            description = "Muda status de DESPACHADO para ENTREGUE")
    @ApiResponse(responseCode = "200", description = "Pedido entregue")
    public ResponseEntity<OrderResponseDTO> deliverOrder(
            @Parameter(description = "ID do pedido") @PathVariable Long id) {
        OrderResponseDTO order = orderService.entregarPedido(id);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancelar pedido",
            description = "Funcionário cancela qualquer pedido (exceto ENTREGUE)")
    @ApiResponse(responseCode = "200", description = "Pedido cancelado")
    public ResponseEntity<OrderResponseDTO> cancelOrder(
            @Parameter(description = "ID do pedido") @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String motivo = body != null ? body.get("motivo") : "Cancelado pela equipe";
        OrderResponseDTO order = orderService.cancelarPedido(id, motivo);
        return ResponseEntity.ok(order);
    }
}