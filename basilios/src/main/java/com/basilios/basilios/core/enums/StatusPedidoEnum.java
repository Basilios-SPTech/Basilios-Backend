package com.basilios.basilios.core.enums;

public enum StatusPedidoEnum{
    PENDENTE("pendente"),
    CONFIRMADO("confirmado"),
    PREPARANDO("preparando"),
    DESPACHADO("despachado"),
    ENTREGUE("entregue"),
    CANCELADO("cancelado");

    private final String valor;

    StatusPedidoEnum(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static StatusPedidoEnum fromValor(String valor) {
        for (StatusPedidoEnum status : StatusPedidoEnum.values()) {
            if (status.valor.equals(valor)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Status de pedido não encontrado: " + valor);
    }

    public boolean podeTransicionarPara(StatusPedidoEnum novoStatus) {
        return switch (this) {
            case PENDENTE -> novoStatus == CONFIRMADO || novoStatus == CANCELADO;
            case CONFIRMADO -> novoStatus == PREPARANDO || novoStatus == CANCELADO;
            case PREPARANDO -> novoStatus == DESPACHADO || novoStatus == CANCELADO;
            case DESPACHADO -> novoStatus == ENTREGUE || novoStatus == CANCELADO;
            case ENTREGUE -> false; // Estado final
            case CANCELADO -> false; // Estado final
        };
    }

    public boolean isFinal() {
        return this == ENTREGUE || this == CANCELADO;
    }

    public boolean isAtivo() {
        return !isFinal();
    }

    @Override
    public String toString() {
        return valor;
    }
}