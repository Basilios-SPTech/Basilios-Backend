package com.basilios.basilios.core.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "client")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Client extends Usuario {

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    // Utility methods for managing orders
    public void addOrder(Order order) {
        orders.add(order);
        order.setUsuario(this);
    }

    public void removeOrder(Order order) {
        orders.remove(order);
        order.setUsuario(null);
    }

    public int getTotalOrders() {
        return orders.size();
    }
}
