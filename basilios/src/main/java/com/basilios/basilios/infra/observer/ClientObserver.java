package com.basilios.basilios.infra.observer;

public class ClientObserver implements Observer {
    private final Long clientId;
    private final NotificationService notificationService;

    public ClientObserver(Long clientId, NotificationService notificationService) {
        this.clientId = clientId;
        this.notificationService = notificationService;
    }

    @Override
    public void update(String event, Object payload) {
        String message = "Evento: " + event + " - " + (payload != null ? payload.toString() : "");
        notificationService.notifyClient(clientId, message);
    }
}

