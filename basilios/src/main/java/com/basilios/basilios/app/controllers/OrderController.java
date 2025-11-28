package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.order.OrderRequestDTO;
import com.basilios.basilios.app.dto.order.OrderResponseDTO;
import com.basilios.basilios.app.dto.order.OrderUpdateDTO;
import com.basilios.basilios.core.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Pedidos", description = "Endpoints públicos/admin para pedidos")
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "Listar todos os pedidos", description = "Retorna todos os pedidos do sistema")
    @ApiResponse(responseCode = "200", description = "Lista de pedidos retornada")
    public ResponseEntity<Page<OrderResponseDTO>> findAll(Pageable pageable) {
        Page<OrderResponseDTO> page = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pedido por ID", description = "Retorna detalhes completos de um pedido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    public ResponseEntity<OrderResponseDTO> findById(@Parameter(description = "ID do pedido") @PathVariable Long id) {
        OrderResponseDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @PostMapping
    @Operation(summary = "Criar novo pedido", description = "Cria um novo pedido")
    @ApiResponse(responseCode = "201", description = "Pedido criado")
    public ResponseEntity<OrderResponseDTO> create(@Valid @RequestBody OrderRequestDTO request) {
        OrderResponseDTO response = orderService.createOrder(request);
        if (response.getRedirectToPartners() != null && response.getRedirectToPartners()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar parcialmente pedido", description = "Atualiza campos permitidos do pedido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    public ResponseEntity<OrderResponseDTO> update(@PathVariable Long id,
                                                   @Valid @RequestBody OrderUpdateDTO updates) {
        OrderResponseDTO updated = orderService.updateOrder(id, updates);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover pedido (soft delete)", description = "Realiza remoção/soft-delete de um pedido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pedido removido"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        // Soft delete via OrderService (aciona @SQLDelete na entidade)
        orderService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
