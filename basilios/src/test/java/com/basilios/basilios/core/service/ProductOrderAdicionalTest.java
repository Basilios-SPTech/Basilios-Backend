package com.basilios.basilios.core.service;

import com.basilios.basilios.core.model.Adicional;
import com.basilios.basilios.core.model.ProductOrder;
import com.basilios.basilios.core.model.ProductOrderAdicional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes do ProductOrderAdicional")
class ProductOrderAdicionalTest {

    private ProductOrderAdicional poa;

    @BeforeEach
    void setUp() {
        poa = ProductOrderAdicional.builder()
                .adicionalId(1L)
                .adicionalName("Extra Bacon")
                .unitPrice(new BigDecimal("3.00"))
                .quantity(2)
                .build();
    }

    @Test
    @DisplayName("Deve calcular subtotal corretamente (unitPrice × quantity)")
    void calculateSubtotal_DeveCalcularCorretamente() {
        poa.calculateSubtotal();

        assertEquals(new BigDecimal("6.00"), poa.getSubtotal());
    }

    @Test
    @DisplayName("Deve calcular subtotal para quantidade 1")
    void calculateSubtotal_DeveCalcularParaQuantidade1() {
        poa.setQuantity(1);
        poa.calculateSubtotal();

        assertEquals(new BigDecimal("3.00"), poa.getSubtotal());
    }

    @Test
    @DisplayName("Deve calcular subtotal para preço zero")
    void calculateSubtotal_DeveCalcularParaPrecoZero() {
        poa.setUnitPrice(BigDecimal.ZERO);
        poa.calculateSubtotal();

        assertEquals(BigDecimal.ZERO, poa.getSubtotal());
    }

    @Test
    @DisplayName("Deve retornar null no subtotal quando quantity é null")
    void calculateSubtotal_DeveNaoCalcularQuandoQuantityNula() {
        poa.setQuantity(null);
        poa.calculateSubtotal();

        assertNull(poa.getSubtotal());
    }

    @Test
    @DisplayName("Deve retornar null no subtotal quando unitPrice é null")
    void calculateSubtotal_DeveNaoCalcularQuandoUnitPriceNulo() {
        poa.setUnitPrice(null);
        poa.calculateSubtotal();

        assertNull(poa.getSubtotal());
    }

    @Test
    @DisplayName("Deve guardar snapshot do nome mesmo se adicional for removido")
    void adicionalName_DeveGuardarSnapshotDoNome() {
        assertEquals("Extra Bacon", poa.getAdicionalName());
        assertEquals(1L, poa.getAdicionalId());
    }
}
