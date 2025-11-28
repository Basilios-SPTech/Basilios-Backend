-- ===========================================
-- SEED DATA PARA BASILIOS HAMBURGERIA
-- Sintaxe compatível com MySQL
-- ===========================================

-- ===========================================
-- LOJA
-- ===========================================
INSERT IGNORE INTO stores (name, address, latitude, longitude, phone, opening_hours)
VALUES (
    'Basilios Hamburgeria',
    'Rua das Hamburguerias, 123 - São Paulo, SP',
    -23.550520,
    -46.633308,
    '(11) 99999-9999',
    '18:00 - 02:00'
);

-- ===========================================
-- USUÁRIOS
-- ===========================================

-- Usuário FUNCIONÁRIO (tem as roles: CLIENTE e FUNCIONARIO)
-- Email: admin@basilios.com | Senha: admin123
INSERT IGNORE INTO usuario (nome_usuario, email, password, cpf, telefone, enabled, created_at, updated_at)
VALUES (
    'Administrador Basilios',
    'admin@basilios.com',
    '$2a$10$jO.Tuln5gZTzaoS/d47youLMtq47QbI6N7rNxr4CybO10p5ZYDFXC',
    '58745699854',
    '11999999999',
    true,
    NOW(),
    NOW()
);

-- Roles do Funcionário
INSERT IGNORE INTO usuario_roles (usuario_id, role)
SELECT u.id, 'ROLE_FUNCIONARIO'
FROM usuario u
WHERE u.email = 'admin@basilios.com'
AND NOT EXISTS (
    SELECT 1 FROM usuario_roles ur
    WHERE ur.usuario_id = u.id AND ur.role = 'ROLE_FUNCIONARIO'
);

INSERT IGNORE INTO usuario_roles (usuario_id, role)
SELECT u.id, 'ROLE_CLIENTE'
FROM usuario u
WHERE u.email = 'admin@basilios.com'
AND NOT EXISTS (
    SELECT 1 FROM usuario_roles ur
    WHERE ur.usuario_id = u.id AND ur.role = 'ROLE_CLIENTE'
);

-- Usuário CLIENTE (tem apenas a role: CLIENTE)
-- Email: cliente@teste.com | Senha: cliente123
--INSERT IGNORE INTO usuario (nome_usuario, email, password, cpf, telefone, enabled, created_at, updated_at)
--VALUES (
--    'João Silva',
--    'cliente@teste.com',
--    '$2a$10$zF.vGSGQZCGDGM.8E3ZV6uik3ohXsvbPlpD.0QFV6pDNYL31gbCK6',
--    '12345678900',
--    '11988888888',
--    true,
--    NOW(),
--    NOW()
--);
--
---- Role do Cliente
--INSERT IGNORE INTO usuario_roles (usuario_id, role)
--SELECT u.id, 'ROLE_CLIENTE'
--FROM usuario u
--WHERE u.email = 'cliente@teste.com'
--AND NOT EXISTS (
--    SELECT 1 FROM usuario_roles ur
--    WHERE ur.usuario_id = u.id AND ur.role = 'ROLE_CLIENTE'
--);

-- ===========================================
-- PRODUTOS
-- ===========================================

INSERT IGNORE INTO product (name, description, category, subcategory, price, is_paused, created_at, updated_at) VALUES
-- Burgers
('Burger Clássico', 'Hambúrguer tradicional com carne bovina, alface, tomate e molho especial', 'BURGER', 'BEEF', 25.90, false, NOW(), NOW()),
('Cheeseburger Premium', 'Hambúrguer com carne bovina, queijo cheddar, cebola caramelizada e bacon', 'BURGER', 'BEEF', 32.90, false, NOW(), NOW()),
('Chicken Burger', 'Hambúrguer de frango grelhado com molho barbecue', 'BURGER', 'CHICKEN', 28.90, false, NOW(), NOW()),
('Veggie Burger', 'Hambúrguer vegetariano com blend de legumes', 'BURGER', 'VEGETARIAN', 26.90, false, NOW(), NOW()),
('Smash Burger Duplo', 'Dois smash burgers com queijo e molho especial', 'BURGER', 'BEEF', 38.90, false, NOW(), NOW()),

-- Sides (Acompanhamentos)
('Batata Frita Grande', 'Porção grande de batatas fritas crocantes', 'SIDE', 'FRIES', 18.90, false, NOW(), NOW()),
('Onion Rings', 'Anéis de cebola empanados e fritos', 'SIDE', 'ONION_RINGS', 16.90, false, NOW(), NOW()),

-- Drinks (Bebidas)
('Milkshake Chocolate', 'Milkshake cremoso de chocolate', 'DRINK', 'MILKSHAKE', 15.90, false, NOW(), NOW()),
('Refrigerante 350ml', 'Refrigerante gelado', 'DRINK', 'SODA', 8.90, false, NOW(), NOW()),
('Água 500ml', 'Água mineral', 'DRINK', 'WATER', 5.90, false, NOW(), NOW());

-- ===========================================
-- ENDEREÇO DO ADMIN (necessário para endereco_entrega_id nos pedidos)
-- ===========================================
INSERT IGNORE INTO endereco (usuario_id, rua, numero, bairro, cep, cidade, estado, complemento, latitude, longitude, created_at, updated_at)
VALUES (
    (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1),
    'Rua do Administrador',
    '123',
    'Centro',
    '01001000',
    'São Paulo',
    'SP',
    'Apto 101',
    -23.550520,
    -46.633308,
    '2025-11-01 10:00:00',
    '2025-11-01 10:00:00'
);

-- ===========================================
-- PEDIDOS (orders) - usando colunas reais do schema
-- Observações:
-- - coluna de total se chama `total`
-- - coluna de endereço é `endereco_entrega_id` (referencia `endereco.id_endereco`)
-- - usamos valores fixos de created_at em Novembro/2025 para testes previsíveis
-- - status usa nomes do enum (PENDENTE, CONFIRMADO, PREPARANDO, DESPACHADO, ENTREGUE, CANCELADO)
-- ===========================================

INSERT IGNORE INTO orders (usuario_id, endereco_entrega_id, status, total, created_at)
VALUES
((SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1), (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), 'ENTREGUE', 59.80, '2025-11-11 19:30:00'),
((SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1), (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), 'ENTREGUE', 89.70, '2025-11-12 20:10:00'),
((SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1), (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), 'CANCELADO', 0.00, '2025-11-13 18:05:00'),
((SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1), (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), 'ENTREGUE', 45.80, '2025-11-14 21:15:00'),
((SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1), (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), 'ENTREGUE', 120.60, '2025-11-15 19:45:00'),
((SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1), (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), 'ENTREGUE', 32.90, '2025-11-16 18:30:00'),
((SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1), (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), 'ENTREGUE', 65.80, '2025-11-17 20:00:00'),
((SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1), (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), 'ENTREGUE', 25.90, '2025-11-18 19:00:00'),
((SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1), (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), 'ENTREGUE', 78.70, '2025-11-19 20:45:00'),
((SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1), (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), 'ENTREGUE', 42.80, '2025-11-20 18:50:00'),
((SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1), (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), 'ENTREGUE', 59.80, '2025-11-21 19:30:00'),
((SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1), (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), 'ENTREGUE', 89.70, '2025-11-22 20:10:00'),
((SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1), (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), 'ENTREGUE', 45.80, '2025-11-23 21:15:00'),
((SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1), (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), 'CANCELADO', 0.00, '2025-11-24 18:05:00'),
((SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1), (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), 'ENTREGUE', 120.60, '2025-11-25 19:45:00'),
((SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1), (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), 'ENTREGUE', 32.90, '2025-11-26 18:30:00'),
((SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1), (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), 'ENTREGUE', 65.80, '2025-11-27 20:00:00'),
((SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1), (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), 'ENTREGUE', 25.90, '2025-11-28 19:00:00'),
((SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1), (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), 'ENTREGUE', 78.70, '2025-11-29 20:45:00'),
((SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1), (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), 'ENTREGUE', 42.80, '2025-11-30 18:50:00');

-- ===========================================
-- ITENS DOS PEDIDOS (product_order)
-- Inserimos vários items por pedido para gerar volumes e possibilitar top-5
-- ===========================================

-- Exemplo: para o pedido criado em '2025-11-11 19:30:00' inserimos alguns produtos
INSERT IGNORE INTO product_order (product_id, order_id, quantity, unit_price, subtotal, product_name)
VALUES
((SELECT id FROM product WHERE name = 'Cheeseburger Premium' LIMIT 1), (SELECT id FROM orders WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) AND created_at = '2025-11-11 19:30:00' LIMIT 1), 3, (SELECT price FROM product WHERE name = 'Cheeseburger Premium' LIMIT 1), (SELECT price FROM product WHERE name = 'Cheeseburger Premium' LIMIT 1) * 3, 'Cheeseburger Premium'),
((SELECT id FROM product WHERE name = 'Burger Clássico' LIMIT 1), (SELECT id FROM orders WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) AND created_at = '2025-11-11 19:30:00' LIMIT 1), 2, (SELECT price FROM product WHERE name = 'Burger Clássico' LIMIT 1), (SELECT price FROM product WHERE name = 'Burger Clássico' LIMIT 1) * 2, 'Burger Clássico'),
((SELECT id FROM product WHERE name = 'Smash Burger Duplo' LIMIT 1), (SELECT id FROM orders WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) AND created_at = '2025-11-11 19:30:00' LIMIT 1), 1, (SELECT price FROM product WHERE name = 'Smash Burger Duplo' LIMIT 1), (SELECT price FROM product WHERE name = 'Smash Burger Duplo' LIMIT 1) * 1, 'Smash Burger Duplo');

-- Repete para outros dias (ex.: 12, 13, 14...) com quantidades variadas para formar o ranking
INSERT IGNORE INTO product_order (product_id, order_id, quantity, unit_price, subtotal, product_name)
VALUES
((SELECT id FROM product WHERE name = 'Cheeseburger Premium' LIMIT 1), (SELECT id FROM orders WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) AND created_at = '2025-11-12 20:10:00' LIMIT 1), 2, (SELECT price FROM product WHERE name = 'Cheeseburger Premium' LIMIT 1), (SELECT price FROM product WHERE name = 'Cheeseburger Premium' LIMIT 1) * 2, 'Cheeseburger Premium'),
((SELECT id FROM product WHERE name = 'Batata Frita Grande' LIMIT 1), (SELECT id FROM orders WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) AND created_at = '2025-11-12 20:10:00' LIMIT 1), 1, (SELECT price FROM product WHERE name = 'Batata Frita Grande' LIMIT 1), (SELECT price FROM product WHERE name = 'Batata Frita Grande' LIMIT 1) * 1, 'Batata Frita Grande'),
((SELECT id FROM product WHERE name = 'Refrigerante 350ml' LIMIT 1), (SELECT id FROM orders WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) AND created_at = '2025-11-12 20:10:00' LIMIT 1), 2, (SELECT price FROM product WHERE name = 'Refrigerante 350ml' LIMIT 1), (SELECT price FROM product WHERE name = 'Refrigerante 350ml' LIMIT 1) * 2, 'Refrigerante 350ml');

-- Insert para 2025-11-13 (cancelado) ainda terá items para simular cancelamento
INSERT IGNORE INTO product_order (product_id, order_id, quantity, unit_price, subtotal, product_name)
VALUES
((SELECT id FROM product WHERE name = 'Veggie Burger' LIMIT 1), (SELECT id FROM orders WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) AND created_at = '2025-11-13 18:05:00' LIMIT 1), 1, (SELECT price FROM product WHERE name = 'Veggie Burger' LIMIT 1), (SELECT price FROM product WHERE name = 'Veggie Burger' LIMIT 1) * 1, 'Veggie Burger');

-- Continuação: mais inserts para os demais dias para criar volume suficiente
INSERT IGNORE INTO product_order (product_id, order_id, quantity, unit_price, subtotal, product_name)
VALUES
((SELECT id FROM product WHERE name = 'Smash Burger Duplo' LIMIT 1), (SELECT id FROM orders WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) AND created_at = '2025-11-14 21:15:00' LIMIT 1), 2, (SELECT price FROM product WHERE name = 'Smash Burger Duplo' LIMIT 1), (SELECT price FROM product WHERE name = 'Smash Burger Duplo' LIMIT 1) * 2, 'Smash Burger Duplo'),
((SELECT id FROM product WHERE name = 'Batata Frita Grande' LIMIT 1), (SELECT id FROM orders WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) AND created_at = '2025-11-14 21:15:00' LIMIT 1), 1, (SELECT price FROM product WHERE name = 'Batata Frita Grande' LIMIT 1), (SELECT price FROM product WHERE name = 'Batata Frita Grande' LIMIT 1) * 1, 'Batata Frita Grande');

-- Mais inserts distribuídos (exemplos):
INSERT IGNORE INTO product_order (product_id, order_id, quantity, unit_price, subtotal, product_name)
VALUES
((SELECT id FROM product WHERE name = 'Cheeseburger Premium' LIMIT 1), (SELECT id FROM orders WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) AND created_at = '2025-11-15 19:45:00' LIMIT 1), 4, (SELECT price FROM product WHERE name = 'Cheeseburger Premium' LIMIT 1), (SELECT price FROM product WHERE name = 'Cheeseburger Premium' LIMIT 1) * 4, 'Cheeseburger Premium'),
((SELECT id FROM product WHERE name = 'Milkshake Chocolate' LIMIT 1), (SELECT id FROM orders WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) AND created_at = '2025-11-15 19:45:00' LIMIT 1), 1, (SELECT price FROM product WHERE name = 'Milkshake Chocolate' LIMIT 1), (SELECT price FROM product WHERE name = 'Milkshake Chocolate' LIMIT 1) * 1, 'Milkshake Chocolate');

-- Dois inserts extras para aumentar contagem do Smash e Fries (ajuda no top-5)
INSERT IGNORE INTO product_order (product_id, order_id, quantity, unit_price, subtotal, product_name)
VALUES
((SELECT id FROM product WHERE name = 'Smash Burger Duplo' LIMIT 1), (SELECT id FROM orders WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) AND created_at = '2025-11-21 19:30:00' LIMIT 1), 3, (SELECT price FROM product WHERE name = 'Smash Burger Duplo' LIMIT 1), (SELECT price FROM product WHERE name = 'Smash Burger Duplo' LIMIT 1) * 3, 'Smash Burger Duplo'),
((SELECT id FROM product WHERE name = 'Batata Frita Grande' LIMIT 1), (SELECT id FROM orders WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) AND created_at = '2025-11-22 20:10:00' LIMIT 1), 2, (SELECT price FROM product WHERE name = 'Batata Frita Grande' LIMIT 1), (SELECT price FROM product WHERE name = 'Batata Frita Grande' LIMIT 1) * 2, 'Batata Frita Grande');

-- Fim da seção de pedidos e itens
