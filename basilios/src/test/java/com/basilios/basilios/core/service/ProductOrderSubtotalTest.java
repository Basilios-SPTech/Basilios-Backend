package com.basilios.basilios.core.service;

import com.basilios.basilios.core.model.ProductOrder;
import com.basilios.basilios.core.model.ProductOrderAdicional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes do cálculo de subtotal do ProductOrder com adicionais")
class ProductOrderSubtotalTest {

    private ProductOrder productOrder;

    @BeforeEach
    void setUp() {
        productOrder = ProductOrder.builder()
                .productName("Hamburguer Glicério")
                .unitPrice(new BigDecimal("40.00"))
                .quantity(1)
                .adicionais(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Deve calcular subtotal sem adicionais")
    void calculateSubtotal_SemAdicionais() {
        productOrder.calculateSubtotal();

        assertEquals(new BigDecimal("40.00"), productOrder.getSubtotal());
    }

    @Test
    @DisplayName("Deve calcular subtotal somando um adicional")
    void calculateSubtotal_ComUmAdicional() {
        ProductOrderAdicional bacon = buildAdicional("Extra Bacon", new BigDecimal("3.00"), 1);
        productOrder.getAdicionais().add(bacon);

        productOrder.calculateSubtotal();

        // 40 + 3 = 43
        assertEquals(new BigDecimal("43.00"), productOrder.getSubtotal());
    }

    @Test
    @DisplayName("Deve calcular subtotal somando múltiplos adicionais")
    void calculateSubtotal_ComMultiplosAdicionais() {
        productOrder.getAdicionais().add(buildAdicional("Extra Bacon", new BigDecimal("3.00"), 2));   // 6.00
        productOrder.getAdicionais().add(buildAdicional("Extra Queijo", new BigDecimal("3.00"), 1));  // 3.00

        productOrder.calculateSubtotal();

        // (40 × 1) + 6 + 3 = 49
        assertEquals(new BigDecimal("49.00"), productOrder.getSubtotal());
    }

    @Test
    @DisplayName("Deve calcular subtotal com quantity > 1 multiplicando também os adicionais")
    void calculateSubtotal_ComQuantidadeProdutoMaiorQueUm() {
        productOrder.setQuantity(2);
        productOrder.getAdicionais().add(buildAdicional("Extra Bacon", new BigDecimal("3.00"), 1)); // 3.00

        productOrder.calculateSubtotal();

        // (40 × 2) + 3 = 83
        assertEquals(new BigDecimal("83.00"), productOrder.getSubtotal());
    }

    @Test
    @DisplayName("Deve ignorar adicional com subtotal null no cálculo")
    void calculateSubtotal_DeveIgnorarAdicionalComSubtotalNull() {
        ProductOrderAdicional semSubtotal = ProductOrderAdicional.builder()
                .adicionalName("Sem Subtotal")
                .unitPrice(new BigDecimal("2.00"))
                .quantity(1)
                .build(); // subtotal propositalmente não calculado

        productOrder.getAdicionais().add(semSubtotal);
        productOrder.calculateSubtotal();

        // subtotal null é tratado como ZERO
        assertEquals(new BigDecimal("40.00"), productOrder.getSubtotal());
    }

    // ---- helper ----

    private ProductOrderAdicional buildAdicional(String name, BigDecimal price, int qty) {
        ProductOrderAdicional poa = ProductOrderAdicional.builder()
                .adicionalName(name)
                .unitPrice(price)
                .quantity(qty)
                .build();
        poa.calculateSubtotal();
        return poa;
    }
}
