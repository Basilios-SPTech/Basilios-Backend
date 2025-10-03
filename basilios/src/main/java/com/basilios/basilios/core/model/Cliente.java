package com.basilios.basilios.core.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cliente")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Cliente extends Usuario {

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Order> pedidos = new ArrayList<>();

    // Métodos utilitários específicos de Cliente
    public void addPedido(Order pedido) {
        pedidos.add(pedido);
        pedido.setUsuario(this);
    }

    public void removePedido(Order pedido) {
        pedidos.remove(pedido);
        pedido.setUsuario(null);
    }


    public int getTotalPedidos() {
        return pedidos.size();
    }
}