// ProductCategory.java
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.text.Normalizer;

@Getter
public enum ProductCategory {
    BURGER("Hambúrguer", "Burgers artesanais e tradicionais"),
    SIDE("Acompanhamento", "Batatas, onion rings, saladas"),
    DRINK("Bebida", "Refrigerantes, sucos, milkshakes, cervejas"),
    DESSERT("Sobremesa", "Açai"),
    COMBO("Combo", "Combinações especiais com desconto");

    private final String displayName;
    private final String description;

    ProductCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    private static String normalize(String s) {
        if (s == null) return null;
        String n = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return n.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    @JsonCreator
    public static ProductCategory fromJson(String raw) {
        if (raw == null) return null;
        String s = normalize(raw);

        // casa com nome do enum
        for (var c : values()) if (normalize(c.name()).equals(s)) return c;
        // casa com displayName
        for (var c : values()) if (normalize(c.displayName).equals(s)) return c;

        // rótulos compostos vindos do front:
        switch (s) {
            case "LANCHESHAMBURGUER": return BURGER;        // "Lanches / Hambúrguer"
            case "ACOMPANHAMENTOSIDE": return SIDE;          // "Acompanhamento / Side"
            case "BEBIDAS": return DRINK;                    // "Bebidas"
            case "SOBREMESA": return DESSERT;                // "Sobremesa"
            case "COMBOPROMOCAO": return COMBO;              // "Combo / Promoção"
        }
        throw new IllegalArgumentException("Categoria inválida: " + raw);
    }

    @JsonValue
    public String toJson() { return name(); }
}
