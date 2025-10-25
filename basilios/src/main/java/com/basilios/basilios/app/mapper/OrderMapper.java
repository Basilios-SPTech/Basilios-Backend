package com.basilios.basilios.app.mapper;

import com.basilios.basilios.app.dto.order.OrderResponseDTO;
import com.basilios.basilios.core.model.Address;
import com.basilios.basilios.core.model.Order;
import com.basilios.basilios.core.model.ProductOrder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    /**
     * Converte Order para OrderResponse
     */
    public OrderResponseDTO toResponse(Order order) {
        if (order == null) {
            return null;
        }

        return OrderResponseDTO.builder()
                .id(order.getId())
                .items(toItemResponseList(order.getProductOrders()))
                .subtotal(order.getSubtotal())
                .deliveryFee(order.getDeliveryFee())
                .discount(order.getDiscount())
                .total(order.getTotal())
                .status(order.getStatus())
                .address(toAddressResponse(order.getAddressEntrega()))
                .createdAt(order.getCreatedAt())
                .confirmedAt(order.getConfirmedAt())
                .preparingAt(order.getPreparingAt())
                .dispatchedAt(order.getDispatchedAt())
                .deliveredAt(order.getDeliveredAt())
                .cancelledAt(order.getCancelledAt())
                .cancellationReason(order.getCancellationReason())
                .observations(order.getObservations())
                .totalItems(order.getTotalItems())
                .totalPromotionDiscount(order.getTotalPromotionDiscount())
                .build();
    }

    /**
     * Converte lista de Orders para lista de OrderResponse
     */
    public List<OrderResponseDTO> toResponseList(List<Order> orders) {
        if (orders == null) {
            return List.of();
        }
        return orders.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Converte OrderResponse simplificado (sem items detalhados)
     * Útil para listagens
     */
    public OrderResponseDTO toSimpleResponse(Order order) {
        if (order == null) {
            return null;
        }

        return OrderResponseDTO.builder()
                .id(order.getId())
                .subtotal(order.getSubtotal())
                .deliveryFee(order.getDeliveryFee())
                .discount(order.getDiscount())
                .total(order.getTotal())
                .status(order.getStatus())
                .address(toAddressResponse(order.getAddressEntrega()))
                .createdAt(order.getCreatedAt())
                .totalItems(order.getTotalItems())
                .build();
    }

    /**
     * Converte ProductOrder para OrderItemResponse
     */
    private OrderResponseDTO.OrderItemResponse toItemResponse(ProductOrder productOrder) {
        if (productOrder == null) {
            return null;
        }

        return OrderResponseDTO.OrderItemResponse.builder()
                .id(productOrder.getId())
                .productId(productOrder.getProduct() != null ? productOrder.getProduct().getId() : null)
                .productName(productOrder.getProductName())
                .quantity(productOrder.getQuantity())
                .unitPrice(productOrder.getUnitPrice())
                .subtotal(productOrder.getSubtotal())
                .observations(productOrder.getObservations())
                .hadPromotion(productOrder.getHadPromotion())
                .promotionName(productOrder.getPromotionName())
                .originalPrice(productOrder.getOriginalPrice())
                .discount(productOrder.getTotalDiscount())
                .discountPercentage(productOrder.getDiscountPercentage())
                .build();
    }

    /**
     * Converte lista de ProductOrder para lista de OrderItemResponse
     */
    private List<OrderResponseDTO.OrderItemResponse> toItemResponseList(Iterable<ProductOrder> productOrders) {
        if (productOrders == null) {
            return List.of();
        }

        return ((List<ProductOrder>) productOrders).stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());
    }

    /**
     * Converte Address para AddressResponse
     */
    private OrderResponseDTO.AddressResponse toAddressResponse(Address address) {
        if (address == null) {
            return null;
        }

        return OrderResponseDTO.AddressResponse.builder()
                .id(address.getIdEndereco())
                .rua(address.getRua())
                .numero(address.getNumero())
                .bairro(address.getBairro())
                .cep(address.getCep())
                .cidade(address.getCidade())
                .estado(address.getEstado())
                .complemento(address.getComplemento())
                .enderecoCompleto(address.getEnderecoCompleto())
                .build();
    }
}