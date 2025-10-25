package com.basilios.basilios.infra.observer;

/**
 * Contrato para um sujeito (Observable) do padrão Observer.
 */
public interface Subject {
    void registerObserver(Observer o);
    void removeObserver(Observer o);
    void notifyObservers(String event, Object payload);
}

