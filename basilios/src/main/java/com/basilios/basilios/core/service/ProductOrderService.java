package com.basilios.basilios.core.service;

import com.basilios.basilios.infra.repository.ProductOrderRepository;
import com.basilios.basilios.infra.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ProductOrderService {

    private final ProductOrderRepository productOrderRepository;
    private final ProductRepository productRepository;

    @Autowired
    public ProductOrderService(ProductOrderRepository productOrderRepository, ProductRepository productRepository) {
        this.productOrderRepository = productOrderRepository;
        this.productRepository = productRepository;
    }

    /**
     * Retorna a soma das quantidades vendidas no período; aceita nulls e delega defaults para a query nativa
     */
    public long getItemsSold(LocalDateTime start, LocalDateTime end) {
        Long qty = productOrderRepository.sumItemsQuantityBetweenWithDefaults(start, end);
        return qty == null ? 0L : qty;
    }

    /**
     * Conta quantos produtos não tiveram vendas no período (baseado em ProductOrder.createdAt)
     */
    public long getProductsNotSold(LocalDateTime start, LocalDateTime end) {
        // Note: service normaliza dates if necessary outside; repository expects concrete datetimes
        Long count = productRepository.countProductsNotSoldBetween(start, end);
        return count == null ? 0L : count;
    }
}

