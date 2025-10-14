package com.basilios.basilios.infra.config;

import com.basilios.basilios.core.enums.CargoEnum;
import com.basilios.basilios.core.enums.RoleEnum;
import com.basilios.basilios.core.model.Employee;
import com.basilios.basilios.core.model.Product;
import com.basilios.basilios.core.model.Store;
import com.basilios.basilios.infra.repository.ProductRepository;
import com.basilios.basilios.infra.repository.StoreRepository;
import com.basilios.basilios.infra.repository.UsuarioRepository;
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
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Criar usuário padrão se não existir
        if (!usuarioRepository.existsByEmail("admin@basilios.com")) {
            Employee admin = Employee.builder()
                    .nomeUsuario("Administrador Basilios")
                    .email("admin@basilios.com")
                    .cpf("58745699854")
                    .telefone("4002892202")
                    .cargo(CargoEnum.DONO)
                    .password(passwordEncoder.encode("admin123"))
                    .roles(Set.of(RoleEnum.ROLE_ADMIN, RoleEnum.ROLE_CLIENTE, RoleEnum.ROLE_FUNCIONARIO))
                    .enabled(true)
                    .build();
            usuarioRepository.save(admin);
            System.out.println("✅ Usuário admin criado: admin@basilios.com / admin123");
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
        if (productRepository.count() == 0) {
            Product[] products = {
                    new Product("Burger Clássico", "Hambúrguer tradicional com carne bovina, alface, tomate e molho especial",
                            Arrays.asList("Pão brioche", "Carne bovina 180g", "Alface", "Tomate", "Molho especial"),
                            new BigDecimal("25.90")),

                    new Product("Cheeseburger Premium", "Hambúrguer com carne bovina, queijo cheddar, cebola caramelizada e bacon",
                            Arrays.asList("Pão brioche", "Carne bovina 200g", "Queijo cheddar", "Bacon", "Cebola caramelizada"),
                            new BigDecimal("32.90")),

                    new Product("Chicken Burger", "Hambúrguer de frango grelhado com molho barbecue",
                            Arrays.asList("Pão integral", "Frango grelhado 150g", "Alface", "Tomate", "Molho barbecue"),
                            new BigDecimal("28.90")),

                    new Product("Veggie Burger", "Hambúrguer vegetariano com blend de legumes",
                            Arrays.asList("Pão integral", "Hambúrguer vegetal", "Alface", "Tomate", "Abacate", "Molho tahine"),
                            new BigDecimal("26.90")),

                    new Product("Smash Burger Duplo", "Dois smash burgers com queijo e molho especial",
                            Arrays.asList("Pão brioche", "2x Carne smash 100g", "2x Queijo", "Cebola", "Molho especial"),
                            new BigDecimal("38.90")),

                    new Product("Batata Frita Grande", "Porção grande de batatas fritas crocantes",
                            Arrays.asList("Batata", "Óleo", "Sal"),
                            new BigDecimal("18.90")),

                    new Product("Onion Rings", "Anéis de cebola empanados e fritos",
                            Arrays.asList("Cebola", "Farinha", "Temperos"),
                            new BigDecimal("16.90")),

                    new Product("Milkshake Chocolate", "Milkshake cremoso de chocolate",
                            Arrays.asList("Sorvete de baunilha", "Leite", "Calda de chocolate", "Chantilly"),
                            new BigDecimal("15.90")),

                    new Product("Refrigerante 350ml", "Refrigerante gelado",
                            Arrays.asList("Refrigerante"),
                            new BigDecimal("8.90")),

                    new Product("Água 500ml", "Água mineral",
                            Arrays.asList("Água mineral"),
                            new BigDecimal("5.90"))
            };

            for (Product product : products) {
                productRepository.save(product);
            }
            System.out.println("✅ " + products.length + " produtos de exemplo criados");
        }

        System.out.println("🍔 Basilios Hamburgeria - Dados iniciais carregados com sucesso!");
        System.out.println("📧 Admin: admin@basilios.com | Senha: admin123");
    }
}