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

}

