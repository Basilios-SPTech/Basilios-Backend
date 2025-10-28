package com.basilios.basilios.infra.observer;

/**
 * Contrato para observadores que recebem atualizações do Subject.
 */
public interface Observer {
    void update(String event, Object payload);
}

