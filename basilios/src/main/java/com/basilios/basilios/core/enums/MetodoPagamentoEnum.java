package com.basilios.basilios.core.enums;

public enum MetodoPagamentoEnum{
    DINHEIRO("dinheiro"),
    CARTAO_CREDITO("cartao_credito"),
    CARTAO_DEBITO("cartao_debito"),
    PIX("pix"),
    TRANSFERENCIA("transferencia");

    private final String valor;

    MetodoPagamentoEnum(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static MetodoPagamentoEnum fromValor(String valor) {
        for (MetodoPagamentoEnum metodo : MetodoPagamentoEnum.values()) {
            if (metodo.valor.equals(valor)) {
                return metodo;
            }
        }
        throw new IllegalArgumentException("Método de pagamento não encontrado: " + valor);
    }

    @Override
    public String toString() {
        return valor;
    }
}