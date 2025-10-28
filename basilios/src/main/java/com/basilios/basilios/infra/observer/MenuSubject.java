package com.basilios.basilios.infra.observer;

import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

@Component
public class MenuSubject implements Subject {
    private final CopyOnWriteArrayList<Observer> observers = new CopyOnWriteArrayList<>();

    @Override
    public void registerObserver(Observer o) {
        if (o != null && !observers.contains(o)) {
            observers.add(o);
        }
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers(String event, Object payload) {
        for (Observer o : observers) {
            try {
                o.update(event, payload);
            } catch (Exception ex) {
                // não interrompe a notificação para os demais
                System.err.println("Erro ao notificar observador: " + ex.getMessage());
            }
        }
    }

    // método de conveniência para sinalizar mudança
    public void menuChanged(String event, Object payload) {
        notifyObservers(event, payload);
    }
}
