package com.basilios.basilios.infra.observer;

import org.springframework.stereotype.Service;

@Service
public class SimpleNotificationService implements NotificationService {

    @Override
    public void notifyClient(Long clientId, String message) {
        // implementação simples para demonstração: apenas printa no console
        System.out.println("[Notification] clientId=" + clientId + " message=" + message);
    }
}
