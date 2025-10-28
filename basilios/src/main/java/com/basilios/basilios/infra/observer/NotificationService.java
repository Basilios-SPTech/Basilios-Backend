package com.basilios.basilios.infra.observer;

public interface NotificationService {
    void notifyClient(Long clientId, String message);
}

