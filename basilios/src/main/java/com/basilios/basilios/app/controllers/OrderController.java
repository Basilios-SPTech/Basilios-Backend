package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.order.BusinessHoursResponseDTO;
import com.basilios.basilios.app.dto.order.CancelOrderDTO;
import com.basilios.basilios.app.dto.order.OrderRequestDTO;
import com.basilios.basilios.app.dto.order.OrderResponseDTO;
import com.basilios.basilios.app.dto.order.UpdateOrderStatusDTO;
import com.basilios.basilios.app.dto.order.UpdatePaymentStatusDTO;
import com.basilios.basilios.core.enums.StatusPedidoEnum;
import com.basilios.basilios.core.service.BusinessHoursService;
import com.basilios.basilios.core.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Endpoints para clientes e staff/admin")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;
    private final BusinessHoursService businessHoursService;

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
    public ResponseEntity<Page<OrderResponseDTO>> getMyOrders(
            @ParameterObject Pageable pageable) {
        Page<OrderResponseDTO> orders = orderService.getUserOrders(pageable);
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
    public ResponseEntity<OrderResponseDTO> cancelMyOrder(@PathVariable Long id, @RequestBody(required = false) CancelOrderDTO cancelDTO) {
        String motivo = cancelDTO != null ? cancelDTO.getMotivo() : "Cancelado pelo cliente";
        OrderResponseDTO order = orderService.cancelarPedidoUsuario(id, motivo);
        return ResponseEntity.ok(order);
    }

    // ========== ENDPOINTS DE INFORMAÇÃO SOBRE HORÁRIOS ==========

    @GetMapping("/business-hours/status")
    @Operation(summary = "Verificar status de funcionamento", description = "Verifica se a loja está aberta no momento")
    public ResponseEntity<BusinessHoursResponseDTO> checkBusinessStatus() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.DayOfWeek dayOfWeek = now.getDayOfWeek();
        
        if (businessHoursService.isOpen()) {
            return ResponseEntity.ok(BusinessHoursResponseDTO.open(
                    businessHoursService.getOpeningTime(),
                    businessHoursService.getClosingTime(dayOfWeek)
            ));
        } else {
            return ResponseEntity.ok(BusinessHoursResponseDTO.closed(
                    businessHoursService.getOpeningTime(),
                    businessHoursService.getClosingTime(dayOfWeek)
            ));
        }
    }

    @GetMapping("/business-hours/info")
    @Operation(summary = "Obter horários de funcionamento", description = "Retorna os horários de funcionamento da loja")
    public ResponseEntity<BusinessHoursResponseDTO> getBusinessHoursInfo() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.DayOfWeek dayOfWeek = now.getDayOfWeek();
        
        if (businessHoursService.isOpen()) {
            return ResponseEntity.ok(BusinessHoursResponseDTO.open(
                    businessHoursService.getOpeningTime(),
                    businessHoursService.getClosingTime(dayOfWeek)
            ));
        } else {
            return ResponseEntity.ok(BusinessHoursResponseDTO.closed(
                    businessHoursService.getOpeningTime(),
                    businessHoursService.getClosingTime(dayOfWeek)
            ));
        }
    }

    // ========== ENDPOINTS DE STAFF/ADMIN ==========

    @PreAuthorize("hasRole('FUNCIONARIO')")
    @GetMapping
    @Operation(summary = "Listar todos os pedidos", description = "Retorna todos os pedidos do sistema")
    public ResponseEntity<Page<OrderResponseDTO>> findAll(@ParameterObject Pageable pageable) {
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
    public ResponseEntity<Page<OrderResponseDTO>> getOrdersByStatus(
            @RequestParam StatusPedidoEnum status,
            @ParameterObject Pageable pageable) {
        Page<OrderResponseDTO> orders = orderService.getOrdersByStatus(status, pageable);
        return ResponseEntity.ok(orders);
    }

    @PreAuthorize("hasRole('FUNCIONARIO')")
    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status do pedido", description = "Atualiza o status de um pedido existente")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(@PathVariable Long id, @Valid @RequestBody UpdateOrderStatusDTO dto) {
        OrderResponseDTO responseDTO = orderService.updateOrderStatus(id, dto.getStatus());
        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasAnyRole('CLIENTE', 'FUNCIONARIO')")
    @PatchMapping("/{id}/payment-status")
    @Operation(summary = "Atualizar status de pagamento", description = "Atualiza o status de pagamento do pedido (PENDENTE, PAGO, FALHOU)")
    public ResponseEntity<OrderResponseDTO> updatePaymentStatus(@PathVariable Long id, @Valid @RequestBody UpdatePaymentStatusDTO dto) {
        OrderResponseDTO responseDTO = orderService.updatePaymentStatus(id, dto.getStatus());
        return ResponseEntity.ok(responseDTO);
    }
}
