package com.basilios.basilios.service;

import com.basilios.basilios.exception.ResourceNotFoundException;
import com.basilios.basilios.model.Store;
import com.basilios.basilios.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;

    public Store getMainStore() {
        List<Store> stores = storeRepository.findAll();
        if (stores.isEmpty()) {
            throw new ResourceNotFoundException("Nenhuma loja cadastrada");
        }
        return stores.get(0); // Retorna a primeira loja (loja principal)
    }

    public Store findById(Long id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loja não encontrada"));
    }
}