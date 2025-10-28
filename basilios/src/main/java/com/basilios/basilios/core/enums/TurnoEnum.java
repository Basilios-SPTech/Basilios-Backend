package com.basilios.basilios.core.enums;

public enum TurnoEnum {
    MANHA("Manhã", "06:00 - 14:00"),
    TARDE("Tarde", "14:00 - 22:00"),
    NOITE("Noite", "22:00 - 06:00");

    private final String descricao;
    private final String horario;

    TurnoEnum(String descricao, String horario) {
        this.descricao = descricao;
        this.horario = horario;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getHorario() {
        return horario;
    }
}