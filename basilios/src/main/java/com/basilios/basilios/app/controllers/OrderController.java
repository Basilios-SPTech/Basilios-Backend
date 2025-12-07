package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.order.OrderRequestDTO;
import com.basilios.basilios.app.dto.order.OrderResponseDTO;
import com.basilios.basilios.app.dto.order.UpdateOrderStatusDTO;
import com.basilios.basilios.core.enums.StatusPedidoEnum;
import com.basilios.basilios.core.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Endpoints para clientes e staff/admin")
@SecurityRequirement(name = "bearer-jwt")
public class OrderController {

    private final OrderService orderService;

    // ========== ENDPOINTS DE CLIENTE ==========

    @PreAuthorize("hasRole('CLIENTE')")
    @PostMapping
    @Operation(summary = "Criar novo pedido", description = "Cliente cria um novo pedido")
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody OrderRequestDTO request) {
        OrderResponseDTO response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('CLIENTE')")
    @GetMapping("/me")
    @Operation(summary = "Listar meus pedidos", description = "Lista todos os pedidos do cliente autenticado")
    public ResponseEntity<List<OrderResponseDTO>> getMyOrders() {
        List<OrderResponseDTO> orders = orderService.getUserOrders();
        return ResponseEntity.ok(orders);
    }

    @PreAuthorize("hasRole('CLIENTE')")
    @GetMapping("/me/{id}")
    @Operation(summary = "Buscar meu pedido por ID", description = "Retorna detalhes de um pedido específico do cliente")
    public ResponseEntity<OrderResponseDTO> getMyOrderById(@PathVariable Long id) {
        OrderResponseDTO order = orderService.getUserOrderById(id);
        return ResponseEntity.ok(order);
    }

    @PreAuthorize("hasRole('CLIENTE')")
    @PatchMapping("/me/{id}/cancel")
    @Operation(summary = "Cancelar meu pedido", description = "Cliente cancela seu próprio pedido (apenas status permitido)")
    public ResponseEntity<OrderResponseDTO> cancelMyOrder(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String motivo = body != null ? body.get("motivo") : "Cancelado pelo cliente";
        OrderResponseDTO order = orderService.cancelarPedidoUsuario(id, motivo);
        return ResponseEntity.ok(order);
    }

    // ========== ENDPOINTS DE STAFF/ADMIN ==========

    @PreAuthorize("hasRole('FUNCIONARIO')")
    @GetMapping
    @Operation(summary = "Listar todos os pedidos", description = "Retorna todos os pedidos do sistema")
    public ResponseEntity<Page<OrderResponseDTO>> findAll(Pageable pageable) {
        Page<OrderResponseDTO> page = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(page);
    }

    @PreAuthorize("hasRole('FUNCIONARIO')")
    @GetMapping("/{id}")
    @Operation(summary = "Buscar pedido por ID", description = "Retorna detalhes completos de um pedido")
    public ResponseEntity<OrderResponseDTO> findById(@PathVariable Long id) {
        OrderResponseDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @PreAuthorize("hasRole('FUNCIONARIO')")
    @GetMapping("/by-status")
    @Operation(summary = "Listar pedidos por status", description = "Lista pedidos filtrados por status específico")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByStatus(@RequestParam StatusPedidoEnum status) {
        List<OrderResponseDTO> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    @PreAuthorize("hasRole('FUNCIONARIO')")
    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status do pedido", description = "Atualiza o status de um pedido existente")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestBody UpdateOrderStatusDTO dto) {
        try {
            orderService.updateOrderStatus(id, dto.getStatus());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao atualizar status do pedido");
        }
    }
}
