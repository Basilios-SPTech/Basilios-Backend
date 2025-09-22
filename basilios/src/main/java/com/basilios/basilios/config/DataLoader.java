package com.basilios.basilios.config;

import com.basilios.basilios.enums.RoleEnum;
import com.basilios.basilios.model.Produto;
import com.basilios.basilios.model.Store;
import com.basilios.basilios.model.User;
import com.basilios.basilios.repository.ProdutoRepository;
import com.basilios.basilios.repository.StoreRepository;
import com.basilios.basilios.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Set;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Criar usuário padrão se não existir
        if (!userRepository.existsByEmail("admin@basilios.com")) {
            User admin = User.builder()
                    .name("Administrador Basilios")
                    .email("admin@basilios.com")
                    .password(passwordEncoder.encode("admin123"))
                    .roles(Set.of(RoleEnum.ROLE_ADMIN, RoleEnum.ROLE_USER))
                    .enabled(true)
                    .build();
            userRepository.save(admin);
            System.out.println("✅ Usuário admin criado: admin@basilios.com / admin123");
        }

        // Criar usuário comum para testes
        if (!userRepository.existsByEmail("teste@basilios.com")) {
            User user = User.builder()
                    .name("Usuário Teste")
                    .email("teste@basilios.com")
                    .password(passwordEncoder.encode("123456"))
                    .roles(Set.of(RoleEnum.ROLE_USER))
                    .enabled(true)
                    .build();
            userRepository.save(user);
            System.out.println("✅ Usuário teste criado: teste@basilios.com / 123456");
        }

        // Criar loja padrão se não existir
        if (storeRepository.count() == 0) {
            Store store = Store.builder()
                    .name("Basilios Hamburgeria")
                    .address("Rua das Hamburguerias, 123 - São Paulo, SP")
                    .latitude(-23.550520)
                    .longitude(-46.633308)
                    .phone("(11) 99999-9999")
                    .openingHours("18:00 - 02:00")
                    .build();
            storeRepository.save(store);
            System.out.println("✅ Loja padrão criada: " + store.getName());
        }

        // Criar produtos de exemplo se não existirem
        if (produtoRepository.count() == 0) {
            Produto[] produtos = {
                    new Produto("Burger Clássico", "Hambúrguer tradicional com carne bovina, alface, tomate e molho especial",
                            Arrays.asList("Pão brioche", "Carne bovina 180g", "Alface", "Tomate", "Molho especial"),
                            new BigDecimal("25.90")),

                    new Produto("Cheeseburger Premium", "Hambúrguer com carne bovina, queijo cheddar, cebola caramelizada e bacon",
                            Arrays.asList("Pão brioche", "Carne bovina 200g", "Queijo cheddar", "Bacon", "Cebola caramelizada"),
                            new BigDecimal("32.90")),

                    new Produto("Chicken Burger", "Hambúrguer de frango grelhado com molho barbecue",
                            Arrays.asList("Pão integral", "Frango grelhado 150g", "Alface", "Tomate", "Molho barbecue"),
                            new BigDecimal("28.90")),

                    new Produto("Veggie Burger", "Hambúrguer vegetariano com blend de legumes",
                            Arrays.asList("Pão integral", "Hambúrguer vegetal", "Alface", "Tomate", "Abacate", "Molho tahine"),
                            new BigDecimal("26.90")),

                    new Produto("Smash Burger Duplo", "Dois smash burgers com queijo e molho especial",
                            Arrays.asList("Pão brioche", "2x Carne smash 100g", "2x Queijo", "Cebola", "Molho especial"),
                            new BigDecimal("38.90")),

                    new Produto("Batata Frita Grande", "Porção grande de batatas fritas crocantes",
                            Arrays.asList("Batata", "Óleo", "Sal"),
                            new BigDecimal("18.90")),

                    new Produto("Onion Rings", "Anéis de cebola empanados e fritos",
                            Arrays.asList("Cebola", "Farinha", "Temperos"),
                            new BigDecimal("16.90")),

                    new Produto("Milkshake Chocolate", "Milkshake cremoso de chocolate",
                            Arrays.asList("Sorvete de baunilha", "Leite", "Calda de chocolate", "Chantilly"),
                            new BigDecimal("15.90")),

                    new Produto("Refrigerante 350ml", "Refrigerante gelado",
                            Arrays.asList("Refrigerante"),
                            new BigDecimal("8.90")),

                    new Produto("Água 500ml", "Água mineral",
                            Arrays.asList("Água mineral"),
                            new BigDecimal("5.90"))
            };

            for (Produto produto : produtos) {
                produtoRepository.save(produto);
            }
            System.out.println("✅ " + produtos.length + " produtos de exemplo criados");
        }

        System.out.println("🍔 Basilios Hamburgeria - Dados iniciais carregados com sucesso!");
        System.out.println("📧 Login: teste@basilios.com | Senha: 123456");
        System.out.println("📧 Admin: admin@basilios.com | Senha: admin123");
    }
}