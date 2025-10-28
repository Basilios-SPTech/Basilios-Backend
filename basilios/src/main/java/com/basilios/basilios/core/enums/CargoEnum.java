package com.basilios.basilios.core.enums;

public enum CargoEnum {
    DONO("Dono"),
    GERENTE("Gerente"),
    COZINHEIRO("Cozinheiro"),
    ENTREGADOR("Entregador");

    private final String descricao;

    CargoEnum(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}