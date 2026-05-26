package com.basilios.basilios.core.enums;

public enum StatusPagamentoEnum {
    PENDENTE("pendente"),
    PAGO("pago"),
    FALHOU("falhou");

    private final String valor;

    StatusPagamentoEnum(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static StatusPagamentoEnum fromValor(String valor) {
        for (StatusPagamentoEnum status : StatusPagamentoEnum.values()) {
            if (status.valor.equalsIgnoreCase(valor) || status.name().equalsIgnoreCase(valor)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Status de pagamento não encontrado: " + valor);
    }

    public boolean podeTransicionarPara(StatusPagamentoEnum novoStatus) {
        return switch (this) {
            case PENDENTE -> novoStatus == PAGO || novoStatus == FALHOU;
            case FALHOU -> novoStatus == PENDENTE;
            case PAGO -> false;
        };
    }

    public boolean isFinal() {
        return this == PAGO;
    }

    @Override
    public String toString() {
        return valor;
    }
}
