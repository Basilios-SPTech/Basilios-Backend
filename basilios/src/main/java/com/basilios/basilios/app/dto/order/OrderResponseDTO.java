package com.basilios.basilios.app.dto.order;

import com.basilios.basilios.core.enums.StatusPedidoEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {

    private Long id;

    @Builder.Default
    private List<OrderItemResponse> items = new ArrayList<>();

    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal discount;
    private BigDecimal total;

    private StatusPedidoEnum status;

    private AddressResponse address;

    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime preparingAt;
    private LocalDateTime dispatchedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;

    private String cancellationReason;
    private String observations;

    private Integer totalItems;
    private BigDecimal totalPromotionDiscount;

    // Para redirecionamento quando fora da área de entrega
    private Boolean redirectToPartners;
    private Map<String, String> partnerLinks;

    /**
     * DTO para representar um item do pedido
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private String observations;
        private Boolean hadPromotion;
        private String promotionName;
        private BigDecimal originalPrice;
        private BigDecimal discount;
        private BigDecimal discountPercentage;
    }

    /**
     * DTO simplificado para endereço
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressResponse {
        private Long id;
        private String rua;
        private String numero;
        private String bairro;
        private String cep;
        private String cidade;
        private String estado;
        private String complemento;
        private String enderecoCompleto;
    }
}