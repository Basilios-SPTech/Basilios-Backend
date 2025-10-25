package com.basilios.basilios.infra.config;

import com.basilios.basilios.core.enums.CargoEnum;
import com.basilios.basilios.core.enums.RoleEnum;
import com.basilios.basilios.core.model.Employee;
import com.basilios.basilios.core.model.Product;
import com.basilios.basilios.core.model.Store;
import com.basilios.basilios.infra.repository.ProductRepository;
import com.basilios.basilios.infra.repository.StoreRepository;
import com.basilios.basilios.infra.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import com.basilios.basilios.infra.observer.MenuSubject;
import com.basilios.basilios.infra.observer.NotificationService;
import com.basilios.basilios.infra.observer.ClientObserver;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;

    private final MenuSubject menuSubject;
    private final NotificationService notificationService;

    @Override
    public void run(String... args) {
        createAdminUserIfNotExists();
        createDefaultStoreIfNotExists();
        createSampleProductsIfNotExists();

        // Registrar um observer de demonstração para o admin (se existir)
        usuarioRepository.findByEmail("admin@basilios.com").ifPresent(u -> {
            try {
                menuSubject.registerObserver(new ClientObserver(u.getId(), notificationService));
                log.info("🔔 ClientObserver registrado para admin (id={})", u.getId());
            } catch (Exception ex) {
                log.warn("Falha ao registrar ClientObserver: {}", ex.getMessage());
            }
        });

        log.info("🍔 Basilios Hamburgeria - Dados iniciais carregados com sucesso!");
        log.info("📧 Admin: admin@basilios.com | Senha: admin123");
    }

    private void createAdminUserIfNotExists() {
        final String adminEmail = "admin@basilios.com";

        if (usuarioRepository.existsByEmail(adminEmail)) {
            log.info("🔹 Usuário admin já existe: {}", adminEmail);
            return;
        }

        Employee admin = Employee.builder()
                .nomeUsuario("Administrador Basilios")
                .email(adminEmail)
                .cpf("58745699854")
                .telefone("4002892202")
                .cargo(CargoEnum.DONO)
                .password(passwordEncoder.encode("admin123"))
                .roles(Set.of(
                        RoleEnum.ROLE_ADMIN,
                        RoleEnum.ROLE_CLIENTE,
                        RoleEnum.ROLE_FUNCIONARIO
                ))
                .enabled(true)
                .build();

        usuarioRepository.save(admin);
        log.info("✅ Usuário admin criado: {} / admin123", adminEmail);
    }

    private void createDefaultStoreIfNotExists() {
        if (storeRepository.count() > 0) {
            log.info("🔹 Loja padrão já existe.");
            return;
        }

        Store store = Store.builder()
                .name("Basilios Hamburgeria")
                .address("Rua das Hamburguerias, 123 - São Paulo, SP")
                .latitude(-23.550520)
                .longitude(-46.633308)
                .phone("(11) 99999-9999")
                .openingHours("18:00 - 02:00")
                .build();

        storeRepository.save(store);
        log.info("✅ Loja padrão criada: {}", store.getName());
    }

    private void createSampleProductsIfNotExists() {
        if (productRepository.count() > 0) {
            log.info("🔹 Produtos já cadastrados, pulando seed.");
            return;
        }

        List<Product> products = List.of(
                Product.builder()
                        .name("Burger Clássico")
                        .description("Hambúrguer tradicional com carne bovina, alface, tomate e molho especial")
                        .price(new BigDecimal("25.90"))
                        .build(),

                Product.builder()
                        .name("Cheeseburger Premium")
                        .description("Hambúrguer com carne bovina, queijo cheddar, cebola caramelizada e bacon")
                        .price(new BigDecimal("32.90"))
                        .build(),

                Product.builder()
                        .name("Chicken Burger")
                        .description("Hambúrguer de frango grelhado com molho barbecue")
                        .price(new BigDecimal("28.90"))
                        .build(),

                Product.builder()
                        .name("Veggie Burger")
                        .description("Hambúrguer vegetariano com blend de legumes")
                        .price(new BigDecimal("26.90"))
                        .build(),

                Product.builder()
                        .name("Smash Burger Duplo")
                        .description("Dois smash burgers com queijo e molho especial")
                        .price(new BigDecimal("38.90"))
                        .build(),

                Product.builder()
                        .name("Batata Frita Grande")
                        .description("Porção grande de batatas fritas crocantes")
                        .price(new BigDecimal("18.90"))
                        .build(),

                Product.builder()
                        .name("Onion Rings")
                        .description("Anéis de cebola empanados e fritos")
                        .price(new BigDecimal("16.90"))
                        .build(),

                Product.builder()
                        .name("Milkshake Chocolate")
                        .description("Milkshake cremoso de chocolate")
                        .price(new BigDecimal("15.90"))
                        .build(),

                Product.builder()
                        .name("Refrigerante 350ml")
                        .description("Refrigerante gelado")
                        .price(new BigDecimal("8.90"))
                        .build(),

                Product.builder()
                        .name("Água 500ml")
                        .description("Água mineral")
                        .price(new BigDecimal("5.90"))
                        .build()
        );

        productRepository.saveAll(products);
        log.info("✅ {} produtos de exemplo criados.", products.size());
    }
}
