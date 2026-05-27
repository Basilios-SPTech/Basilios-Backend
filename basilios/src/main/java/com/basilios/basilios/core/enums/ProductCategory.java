package com.basilios.basilios.core.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.text.Normalizer;

@Getter
public enum ProductCategory {
    BURGER("Hambúrguer", "Burgers artesanais e tradicionais"),
    SIDE("Acompanhamento", "Batatas, onion rings, saladas"),
    DRINK("Bebida", "Refrigerantes, sucos, milkshakes, cervejas"),
    DESSERT("Sobremesa", "Açai"),
    COMBO("Combo", "Combinações especiais com desconto"),
    COMBOS_INDIVIDUAIS("Combos Individuais", "Combos individuais"),
    LANCHES_PREMIUM("Lanches Premium", "Lanches premium"),
    BEIRUTES("Beirutes", "Linha de beirutes"),
    HOT_DOG("Hot-dog", "Linha de hot-dog"),
    VEGANOS("Veganos", "Linha vegana"),
    PORCOES("Porções", "Porções e acompanhamentos"),
    SOBREMESAS("Sobremesas", "Sobremesas"),
    BEBIDAS("Bebidas", "Bebidas");

    private final String displayName;
    private final String description;

    ProductCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    // ----- ENTRADA: converte payload -> enum (aceita rótulos do front)
    @JsonCreator
    public static ProductCategory fromJson(String raw) {
        if (raw == null) return null;
        String s = normalize(raw);

        // bate em NOME do enum
        for (var c : values()) {
            if (normalize(c.name()).equals(s)) return c;
        }
        // bate no displayName
        for (var c : values()) {
            if (normalize(c.displayName).equals(s)) return c;
        }
        // mapeia rótulos “compostos” usados no front
        switch (s) {
            case "LANCHESHAMBURGUER": return BURGER;           // "Lanches / Hambúrguer"
            case "ACOMPANHAMENTOSIDE": return SIDE;             // "Acompanhamento / Side"
            case "BEBIDAS": return BEBIDAS;                     // plural novo
            case "SOBREMESA": return DESSERT;                   // singular legado
            case "SOBREMESAS": return SOBREMESAS;               // plural novo
            case "COMBOPROMOCAO": return COMBO;                 // "Combo / Promoção"
            case "COMBOSINDIVIDUAIS": return COMBOS_INDIVIDUAIS;
            case "LANCHESPREMIUM": return LANCHES_PREMIUM;
            case "BEIRUTES": return BEIRUTES;
            case "HOTDOG": return HOT_DOG;
            case "VEGANOS": return VEGANOS;
            case "PORCOES": return PORCOES;
        }
        throw new IllegalArgumentException("Categoria inválida: " + raw);
    }

    // ----- SAÍDA: controla o que volta no JSON (aqui mando o NOME do enum)
    @JsonValue
    public String toJson() {
        return name();
    }

    private static String normalize(String s) {
        String n = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        n = n.replaceAll("[^A-Za-z0-9]", "")
                .toUpperCase();
        return n;
    }
}
