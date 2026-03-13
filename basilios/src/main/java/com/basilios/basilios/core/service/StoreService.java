package com.basilios.basilios.core.service;

import com.basilios.basilios.core.exception.NotFoundException;
import com.basilios.basilios.core.model.Store;
import com.basilios.basilios.infra.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;

    public Store getMainStore() {
        List<Store> stores = storeRepository.findAll();
        if (stores.isEmpty()) {
            throw new NotFoundException("Nenhuma loja cadastrada");
        }
        return stores.get(0); // Retorna a primeira loja (loja principal)
    }

    public Store findById(Long id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Loja não encontrada"));
    }
}