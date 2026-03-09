-- ===========================================
-- SEED DATA PARA BASILIOS HAMBURGERIA
-- Sintaxe compatível com MySQL
-- ===========================================

-- ===========================================
-- LOJA
-- -- ===========================================
INSERT IGNORE INTO stores (name, address, latitude, longitude, phone, opening_hours)
VALUES (
  'Basilios Burger & Açaí',
  'R. Basílio da Cunha, 454 — Vila Deodoro, São Paulo, SP, 01544-001',
  -23.57694,
  -46.62444,
  '(11) 4801-4864',
  '12:00–00:00 (Seg–Sáb); 18:00–00:00 (Dom)'
);


-- -- ===========================================
-- -- USUÁRIOS
-- -- ===========================================
--
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

-- Endereço para o usuário de id 1
INSERT IGNORE INTO endereco (
    bairro, cep, cidade, complemento, created_at, deleted_at, estado, latitude, longitude, numero, rua, updated_at, usuario_id
) VALUES (
    'Vila Deodoro', '01544001', 'São Paulo', NULL, NOW(), NULL, 'SP', -23.57694, -46.62444, '454', 'R. Basílio da Cunha', NOW(), 1
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

-- -- Usuário CLIENTE (tem apenas a role: CLIENTE)
-- -- Email: cliente@teste.com | Senha: cliente123
-- --INSERT IGNORE INTO usuario (nome_usuario, email, password, cpf, telefone, enabled, created_at, updated_at)
-- --VALUES (
-- --    'João Silva',
-- --    'cliente@teste.com',
-- --    '$2a$10$zF.vGSGQZCGDGM.8E3ZV6uik3ohXsvbPlpD.0QFV6pDNYL31gbCK6',
-- --    '12345678900',
-- --    '11988888888',
-- --    true,
-- --    NOW(),
-- --    NOW()
-- --);
-- --
-- ---- Role do Cliente
-- --INSERT IGNORE INTO usuario_roles (usuario_id, role)
-- --SELECT u.id, 'ROLE_CLIENTE'
-- --FROM usuario u
-- --WHERE u.email = 'cliente@teste.com'
-- --AND NOT EXISTS (
-- --    SELECT 1 FROM usuario_roles ur
-- --    WHERE ur.usuario_id = u.id AND ur.role = 'ROLE_CLIENTE'
-- --);
--
-- -- ===========================================
-- -- PRODUTOS
-- -- ===========================================
-- -- ===========================================
-- -- PRODUTOS
-- -- ===========================================
INSERT IGNORE INTO product
    (id, category, created_at, description, is_paused, name, price, subcategory, updated_at, image_url)
VALUES
    (1, 'BURGER', '2025-11-26 19:18:10.374821',
     'Um delicioso hambúrguer picanha, com bacon triturado, cheddar fatiado, Catupiry empanado, alface, tomate e a deliciosíssima maionese da casa',
     0, 'Glicério', 40.00, 'BEEF', '2025-11-26 19:18:10.374821',
     '/uploads/24a1b1fa-8bb9-468e-8d6b-a4d8f074af53.jpg'),

    (2, 'BURGER', '2025-11-26 19:20:58.061046',
     'Um irresistível X-Frango, com peito de frango suculento, milho doce e crocante, o inconfundível Catupiry cremoso e aquele sabor que conquista em cada mordida.',
     0, 'Vila Monumento', 28.00, 'CHICKEN', '2025-11-26 19:20:58.061046',
     '/uploads/6c2cc5de-c2e9-4b60-bd47-0c22f14a73d1.jpg'),

    (3, 'BURGER', '2025-11-26 19:23:23.962334',
     'Basilios: Um hambúrguer de 110g suculento, coberto com o irresistível cheddar Polenghi, acompanhado de uma salada fresca, crocantes onion rings e finalizado com a cremosidade única da nossa maionese da casa. Uma explosão de sabor em cada mordida!',
     0, 'Basilios', 35.00, 'BEEF', '2025-11-26 19:23:23.962334',
     '/uploads/c1d40aa8-18fd-4092-8b8e-8331735c34b8.jpg'),

    (4, 'BURGER', '2025-11-26 19:24:43.880204',
     'Um X-Burger com molho tártaro irresistível, tomate fresco e o toque especial do agrião, trazendo equilíbrio entre sabor marcante e leveza em cada mordida.',
     0, 'Cambuci', 32.00, 'BEEF', '2025-11-26 19:24:43.880204',
     '/uploads/a1ce9257-8c35-43c7-aa6b-40db9d5e2942.jpg'),

    (5, 'BURGER', '2025-11-26 19:28:19.371949',
     'Dois hambúrgueres suculentos, cobertos com cheddar fatiado, bacon crocante, picles refrescantes e o irresistível molho tártaro que dá o toque final a essa explosão de sabor.',
     0, 'Paraiso', 45.00, 'BEEF', '2025-11-26 19:28:19.371949',
     '/uploads/d05efafe-c8fb-4d31-b2e9-ff336a9e9f79.jpg'),

    (6, 'BURGER', '2025-11-26 19:30:01.472108',
     'Um hambúrguer de picanha suculento, coberto com cheddar derretido, bacon crocante e uma salada fresca que traz equilíbrio e sabor em cada mordida.',
     0, 'Vila Mariana', 38.00, 'BEEF', '2025-11-26 19:30:01.472108',
     '/uploads/1451bfe2-2db6-41fe-a98c-1445407e9c94.jpg'),

    (7, 'BURGER', '2025-11-26 19:31:05.401540',
     'Um hambúrguer suculento, coberto com a cremosidade da mussarela de búfala, o sabor marcante do tomate seco, a leveza da rúcula fresca e finalizado com nossa maionese especial. Uma combinação sofisticada e irresistível!',
     0, 'Klabin', 36.00, 'BEEF', '2025-11-26 19:31:05.401540',
     '/uploads/27da6b46-a191-485a-a8ae-7cc488973f1b.jpg'),

    (8, 'BURGER', '2025-11-26 19:32:21.533079',
     'Um hambúrguer de 110g suculento no pão macio, coberto com queijo derretido e o irresistível cheddar que garante sabor intenso em cada mordida.',
     0, 'X-burger cheddar', 31.00, 'BEEF', '2025-11-26 19:32:21.533079',
     '/uploads/54c91904-2295-4c79-b993-d2ea12087244.jpg'),

    (9, 'BURGER', '2025-11-26 19:33:13.714851',
     'Um hambúrguer de 110g suculento no pão macio, coberto com queijo derretido e a cremosidade irresistível do Catupiry. Simples, clássico e cheio de sabor!',
     0, 'X-burger Catupiry', 31.00, 'BEEF', '2025-11-26 19:33:13.714851',
     '/uploads/9bdcbf3d-ce61-425a-b0da-96938282c8ae.jpg'),

    (10, 'BURGER', '2025-11-26 19:34:30.920253',
     'Um pão de hambúrguer macio, recheado com uma carne de 120g suculenta e coberto com queijo derretido que traz aquele sabor irresistível em cada mordida.',
     0, 'X-Burger Simples', 27.00, 'BEEF', '2025-11-26 19:34:30.920253',
     '/uploads/8cd04d65-fa18-4b46-8035-1cdc090d2b28.jpg'),

    (11, 'BURGER', '2025-11-26 19:35:25.000772',
     'Um pão de hambúrguer macio, recheado com um hambúrguer de 110g suculento, acompanhado de ovo saboroso e queijo derretido que juntos criam uma combinação clássica e irresistível.',
     0, 'X-burger egg', 29.00, 'BEEF', '2025-11-26 19:35:25.000772',
     '/uploads/318ea3a2-8198-4338-8417-d857bb19bdd0.jpg'),

    (12, 'BURGER', '2025-11-26 19:36:34.009798',
     'Um pão de hambúrguer macio, recheado com um hambúrguer de 110g suculento, acompanhado de alface crocante, tomate fresco, queijo derretido e a cremosidade da nossa maionese especial. Uma combinação clássica e irresistível!',
     0, 'X-burger egg salada', 33.00, 'BEEF', '2025-11-26 19:36:34.009798',
     '/uploads/65e41353-1e3a-47ea-afe9-1f3f7c30bc2d.jpg'),

    (13, 'BURGER', '2025-11-26 21:09:44.562086',
     'Um pão de hambúrguer macio, recheado com uma carne de 120g suculenta, coberta por queijo derretido e finalizada com bacon crocante que garante sabor intenso em cada mordida.',
     0, 'X-Burger Bacon', 30.00, 'BEEF', '2025-11-26 21:09:44.562086',
     '/uploads/8f1998d4-c3be-4358-9f9e-015a09146996.jpg'),

    (14, 'BURGER', '2025-11-26 21:10:49.155825',
     'Um pão de hambúrguer macio, recheado com um hambúrguer de 110g suculento, acompanhado de ovo saboroso e presunto que juntos criam uma combinação clássica e irresistível.',
     0, 'X-burger egg presunto', 33.00, 'BEEF', '2025-11-26 21:10:49.155825',
     '/uploads/faa492da-80c8-4eda-b46a-336c178a9914.jpg'),

    (15, 'BURGER', '2025-11-26 21:12:33.519729',
     'Um pão de hambúrguer macio, recheado com uma carne de 120g suculenta, coberta por queijo derretido, acompanhada de tomate fresco, alface crocante e finalizada com a cremosidade da nossa maionese caseira. Uma combinação clássica e irresistível!',
     0, 'X-Burger Salada', 30.00, 'BEEF', '2025-11-26 21:12:33.519729',
     '/uploads/fbda5c68-93af-4b77-94ae-932d2154d0f7.jpg'),

    (16, 'BURGER', '2025-11-26 21:13:23.849419',
     'Um pão de hambúrguer macio, recheado com um hambúrguer de 110g suculento e finalizado com cebola frita crocante que dá aquele toque especial de sabor.',
     0, 'X-burger acebolado', 30.00, 'BEEF', '2025-11-26 21:13:23.849419',
     '/uploads/3dba0349-208f-4fe5-8f76-2e548319ee3f.jpg'),

    (17, 'BURGER', '2025-11-26 21:14:16.939855',
     'Um pão de hambúrguer macio, recheado com uma carne de 120g suculenta, coberta por queijo derretido, acompanhada de tomate fresco, alface crocante e finalizada com a cremosidade da nossa maionese caseira. Uma combinação clássica e irresistível!',
     0, 'X-Burger Bacon Salada', 36.00, 'BEEF', '2025-11-26 21:14:16.939855',
     '/uploads/fe2b1c1b-0ccd-486f-8b7a-e30e139b4b3c.jpg'),

    (18, 'BURGER', '2025-11-26 21:14:57.711666',
     'Um pão de hambúrguer macio, recheado com um hambúrguer de 110g suculento, coberto por queijo derretido e finalizado com o sabor marcante do molho tártaro. Simples, clássico e irresistível!',
     0, 'X-burger tártaro', 30.00, 'BEEF', '2025-12-02 21:11:32.647936',
     '/uploads/37a428cc-b44d-4fae-aa35-d71e162e926d.jpg'),

    (19, 'BURGER', '2025-12-02 21:18:22.907896',
     'O clássico X-Bacon Egg com um hambúrguer suculento, bacon crocante e ovo saboroso, acompanhado de salada fresca, presunto e cebola acebolada que trazem equilíbrio e intensidade de sabor em cada mordida.',
     0, 'Aclimação', 39.00, 'BEEF', '2025-12-02 21:18:22.909892',
     '/uploads/8d8e343e-e134-44bd-9903-958cd4e4e538.jpg'),

    (20, 'BURGER', '2025-12-07 03:02:23.961220',
     'Beirute médio de frango com mussarela e presunto, acompanhado de alface fresca, tomate e maionese cremosa.',
     0, 'Beirute médio (Frango)', 35.00, 'CHICKEN', '2025-12-07 03:02:23.969262',
     NULL),

    (21, 'BURGER', '2025-12-07 03:05:44.842412',
     'Beirute grande de frango com mussarela e presunto, alface fresca, tomate e maionese cremosa, em porção mais generosa.',
     0, 'Beirute Grande (Frango)', 43.00, 'CHICKEN', '2025-12-07 03:05:44.845900',
     NULL),

    (22, 'BURGER', '2025-12-07 03:06:45.040924',
     'Beirute médio de filé mignon com mussarela e presunto, alface fresca, tomate e maionese cremosa.',
     0, 'Beirute médio (Filé Mignon)', 41.00, 'BEEF', '2025-12-07 03:06:45.040924',
     NULL),

    (23, 'BURGER', '2025-12-07 03:07:37.775835',
     'Beirute grande de filé mignon com mussarela e presunto, alface fresca, tomate e maionese cremosa, em porção mais generosa.',
     0, 'Beirute Grande (Filé Mignon)', 52.00, 'BEEF', '2025-12-07 03:07:37.775835',
     NULL),

    (24, 'BURGER', '2025-12-07 03:18:31.326171',
     'Hot dog premium com duas salsichas, bacon crocante, cheddar, Catupiry, maionese e batata palha.',
     0, 'Dog Premium', 23.00, 'BEEF', '2025-12-07 03:18:31.334135',
     '/uploads/54d0d658-477a-4b82-97a4-224c092c1dcd.jpg'),

    (25, 'BURGER', '2025-12-07 03:19:58.206518',
     'Hot dog com pão macio, duas salsichas, maionese cremosa e batata palha crocante.',
     0, 'Dog Maionese Batata Palha', 18.00, 'BEEF', '2025-12-07 03:19:58.206518',
     NULL),

    (26, 'SIDE', '2025-12-07 03:22:46.316517',
     'Deliciosa Fritas',
     0, 'Batata Frita', 14.00, 'FRIES', '2025-12-07 03:22:46.316517',
     '/uploads/ea655094-3f82-4cd3-8b35-20c750bd4347.jpg'),

    (27, 'SIDE', '2025-12-07 03:25:31.705628',
     'Batata Crinkle bem crocante!',
     0, 'Batata Crinkle', 14.00, 'FRIES', '2025-12-07 03:25:31.705628',
     '/uploads/d5b28c84-2f30-44cd-8a95-4fb5ff887de5.jpg'),

    (28, 'SIDE', '2025-12-07 03:27:02.855919',
     'Deliciosas fritas c/ Cheddar e Bacon',
     0, 'Batata Frita c/ Cheddar e Bacon', 22.00, 'FRIES', '2025-12-07 03:27:02.869810',
     '/uploads/1dcb040e-71d5-4fe9-b024-7c263fc2fd46.jpg'),

    (29, 'SIDE', '2025-12-07 03:28:05.194697',
     'Anéis de cebola empanado',
     0, 'Onion Rings', 24.00, 'ONION_RINGS', '2025-12-07 03:28:05.194697',
     '/uploads/0a99630c-4b77-4991-8bf6-7dc66ef016ea.jpg'),

    (30, 'SIDE', '2025-12-07 03:29:11.554788',
     'Batata em forma de canoa',
     0, 'Batata Canoa', 21.00, 'FRIES', '2025-12-07 03:29:11.554788',
     '/uploads/b35a327e-c8a3-409f-84cb-61aa5aaed243.jpg'),

    (31, 'COMBO', '2025-12-07 03:32:01.064697',
     'X-salada (pão de hamburguer, hamburguer 110g, queijo, alface, tomate e maionese) + fritas 100g + refrigerante lata 350ml',
     0, 'Combo X-Salada', 42.00, NULL, '2025-12-08 14:48:25.307498',
     '/uploads/b12edbe8-111b-4f1e-97a5-6057f1e86ed0.jpg'),

    (32, 'COMBO', '2025-12-07 03:34:03.130399',
     'Combo X-Bacon salada com hambúrguer suculento, bacon crocante, queijo derretido, alface, tomate e maionese, acompanhado de batata frita e refrigerante.',
     0, 'Combo X-Bacon Salada', 45.00, NULL, '2025-12-07 03:34:03.133372',
     '/uploads/9ff9351e-bc05-47c4-93e5-18d489be2584.jpg');

-- ORDERS DE 11 A 30 DE NOVEMBRO DE 2025 (IDs fixos iniciando em 1)
INSERT IGNORE INTO orders (
    id, cancellation_reason, cancelled_at, codigo_pedido, confirmed_at, created_at, delivered_at, delivery_fee, discount, dispatched_at, observations, preparing_at, status, subtotal, total, endereco_entrega_id, usuario_id
) VALUES
-- 1) 2025-11-11 ENTREGUE - total 59.80
(1, NULL, NULL, 'PED-0001', '2025-12-01 19:31:00', '2025-12-01 19:30:00', '2025-12-01 20:30:00', 7.00, 0.00, '2025-12-11 20:00:00', 'Pedido automático dia 11/11', '2025-11-11 19:32:00', 'ENTREGUE', 52.80, 59.80, (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1)),
-- 2) 2025-11-12 ENTREGUE - total 89.70
(2, NULL, NULL, 'PED-0002', '2025-12-02 20:10:00', '2025-12-02 20:09:00', '2025-12-02 20:50:00', 7.00, 0.00, '2025-12-02 20:10:00', 'Pedido automático dia 12/11', '2025-12-02 20:10:00', 'ENTREGUE', 82.70, 89.70, (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1)),
-- 3) 2025-11-13 CANCELADO - total 0.00
(3, 'Pedido cancelado', '2025-12-03 18:05:00', 'PED-0003', NULL, '2025-12-03 18:03:00', NULL, 0.00, 0.00, NULL, 'Pedido cancelado dia 03/12', NULL, 'CANCELADO', 0.00, 0.00, (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1)),
-- 4) 2025-11-14 ENTREGUE - total 45.80
(4, NULL, NULL, 'PED-0004', '2025-12-04 21:15:00', '2025-12-04 21:13:00', '2025-12-04 21:50:00', 7.00, 0.00, '2025-12-04 21:30:00', 'Pedido automático dia 14/11', '2025-12-04 21:16:00', 'ENTREGUE', 38.80, 45.80, (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1)),
-- 5) 2025-11-15 ENTREGUE - total 120.60
(5, NULL, NULL, 'PED-0005', '2025-12-05 19:45:00', '2025-12-05 19:43:00', '2025-12-05 20:45:00', 7.00, 0.00, '2025-12-05 20:05:00', 'Pedido automático dia 15/11', '2025-11-15 19:46:00', 'ENTREGUE', 113.60, 120.60, (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1)),
-- 6) 2025-11-16 ENTREGUE - total 32.90
(6, NULL, NULL, 'PED-0006', '2025-12-06 18:30:50', '2025-12-06 18:30:00', '2025-12-06 20:30:00', 7.00, 0.00, '2025-12-06 20:31:00', 'Pedido automático dia 16/11', '2025-12-06 18:34:00', 'ENTREGUE', 25.90, 32.90, (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1)),
-- 7) 2025-11-17 ENTREGUE - total 65.80
(7, NULL, NULL, 'PED-0007', '2025-12-07 20:30:00', '2025-12-07 20:00:00', '2025-12-07 21:00:00', 7.00, 0.00, '2025-12-07 20:50:00', 'Pedido automático dia 17/11', '2025-12-07 20:32:00', 'ENTREGUE', 58.80, 65.80, (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1)),
-- 8) 2025-11-18 ENTREGUE - total 25.90
(8, NULL, NULL, 'PED-0008', '2025-12-08 19:00:00', '2025-12-08 18:50:00', '2025-12-08 20:00:00', 7.00, 0.00, '2025-12-08 19:30:00', 'Pedido automático dia 18/11', '2025-12-08 19:01:00', 'ENTREGUE', 18.90, 25.90, (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1)),
-- 9) 2025-11-19 ENTREGUE - total 78.70
(9, NULL, NULL, 'PED-0009', '2025-12-09 20:45:00', '2025-12-09 20:35:00', '2025-12-09 22:45:00', 7.00, 0.00, '2025-12-09 21:45:00', 'Pedido automático dia 19/11', '2025-12-09 20:46:00', 'ENTREGUE', 71.70, 78.70, (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1)),
-- 10) 2025-11-20 ENTREGUE - total 42.80
(10, NULL, NULL, 'PED-0010', '2025-12-10 18:51:00', '2025-12-10 18:50:00', '2025-12-10 19:50:00', 7.00, 0.00, '2025-12-10 19:30:00', 'Pedido automático dia 20/11', '2025-12-10 18:52:00', 'ENTREGUE', 35.80, 42.80, (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1)),
-- 11) 2025-11-21 ENTREGUE - total 59.80
(11, NULL, NULL, 'PED-0011', '2025-12-11 19:30:20', '2025-12-11 19:30:00', '2025-12-11 20:30:00', 7.00, 0.00, '2025-12-11 19:32:00', 'Pedido automático dia 21/11', '2025-12-11 20:10:00', 'ENTREGUE', 52.80, 59.80, (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1)),
-- 12) 2025-11-22 ENTREGUE - total 89.70
(12, NULL, NULL, 'PED-0012', '2025-12-12 20:11:00', '2025-12-12 20:10:00', '2025-12-12 20:40:00', 7.00, 0.00, '2025-12-12 20:30:00', 'Pedido automático dia 22/11', '2025-12-12 20:12:00', 'ENTREGUE', 82.70, 89.70, (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1)),
-- 13) 2025-11-23 ENTREGUE - total 45.80
(13, NULL, NULL, 'PED-0013', '2025-12-13 21:16:00', '2025-12-13 21:15:00', '2025-12-13 22:15:00', 7.00, 0.00, '2025-12-13 22:00:00', 'Pedido automático dia 23/11', '2025-11-23 21:17:00', 'ENTREGUE', 38.80, 45.80, (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1)),
-- 14) 2025-11-24 CANCELADO - total 0.00
(14, 'Pedido cancelado', '2025-12-14 18:05:00', 'PED-0014', NULL, '2025-12-24 18:02:00', NULL, 0.00, 0.00, NULL, 'Pedido cancelado dia 24/11', NULL, 'CANCELADO', 0.00, 0.00, (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1)),
-- 15) 2025-11-25 ENTREGUE - total 120.60
(15, NULL, NULL, 'PED-0015', '2025-12-15 19:46:00', '2025-12-15 19:45:00', '2025-12-15 20:45:00', 7.00, 0.00, '2025-12-15 20:15:00', 'Pedido automático dia 25/11', '2025-12-15 19:49:00', 'ENTREGUE', 113.60, 120.60, (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1)),
-- 16) 2025-11-26 ENTREGUE - total 32.90
(16, NULL, NULL, 'PED-0016', '2025-12-16 18:32:00', '2025-12-16 18:30:00', '2025-12-16 19:30:00', 7.00, 0.00, '2025-12-16 19:00:00', 'Pedido automático dia 26/11', '2025-12-16 18:33:00', 'ENTREGUE', 25.90, 32.90, (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1)),
-- 17) 2025-11-27 ENTREGUE - total 65.80
(17, NULL, NULL, 'PED-0017', '2025-12-17 20:01:00', '2025-12-17 20:00:00', '2025-12-17 20:59:00', 7.00, 0.00, '2025-12-17 20:30:00', 'Pedido automático dia 27/11', '2025-12-17 20:04:00', 'ENTREGUE', 58.80, 65.80, (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1)),
-- 18) 2025-11-28 ENTREGUE - total 25.90
(18, NULL, NULL, 'PED-0018', '2025-12-18 19:01:00', '2025-12-18 19:00:00', '2025-12-18 20:00:00', 7.00, 0.00, '2025-12-18 19:50:00', 'Pedido automático dia 28/11', '2025-12-18 19:05:00', 'ENTREGUE', 18.90, 25.90, (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1)),
-- 19) 2025-11-29 ENTREGUE - total 78.70
(19, NULL, NULL, 'PED-0019', '2025-12-19 20:46:00', '2025-12-19 20:45:00', '2025-12-19 22:45:00', 7.00, 0.00, '2025-12-19 21:45:00', 'Pedido automático dia 29/11', '2025-12-19 20:47:00', 'ENTREGUE', 71.70, 78.70, (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1)),
-- 20) 2025-11-30 ENTREGUE - total 42.80
(20, NULL, NULL, 'PED-0020', '2025-12-20 18:51:00', '2025-12-20 18:50:00', '2025-12-20 19:50:00', 7.00, 0.00, '2025-12-20 19:30:00', 'Pedido automático dia 30/11', '2025-12-20 18:54:00', 'ENTREGUE', 35.80, 42.80, (SELECT id_endereco FROM endereco WHERE usuario_id = (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1) LIMIT 1), (SELECT id FROM usuario WHERE email = 'admin@basilios.com' LIMIT 1));

-- ===========================================
-- ITENS DOS PEDIDOS (product_order)
-- ===========================================
-- ORDER 1 - 2025-12-01
INSERT INTO product_order (created_at, had_promotion, observations, original_price, product_name, promotion_name,  quantity, subtotal, unit_price, order_id, product_id) VALUES
('2025-12-01 19:31:00', 1, 'Item gerado via seed', 35.00, 'Basilios', 'Promo Basilios Day',   2, 60.00, 30.00, 1, 3),
('2025-12-01 19:31:00', 0, 'Item gerado via seed', 31.00, 'X-burger cheddar', NULL,   1, 31.00, 31.00, 1, 8);
-- ORDER 2 - 2025-12-02
INSERT INTO product_order (created_at, had_promotion, observations, original_price, product_name, promotion_name,  quantity, subtotal, unit_price, order_id, product_id) VALUES
('2025-12-02 20:10:00', 1, 'Item gerado via seed', 35.00, 'Basilios', 'Promo Basilios Day',   1, 30.00, 30.00, 2, 3),
('2025-12-02 20:10:00', 0, 'Item gerado via seed', 14.00, 'Batata Frita', NULL,   2, 28.00, 14.00, 2, 26);
-- ORDER 3 - 2025-12-03 (CANCELADO, mas teve item criado)
INSERT INTO product_order (created_at, had_promotion, observations, original_price, product_name, promotion_name,  quantity, subtotal, unit_price, order_id, product_id) VALUES
('2025-12-03 18:03:00', 0, 'Item gerado via seed', 29.00, 'X-burger egg', NULL,   1, 29.00, 29.00, 3, 11);
-- ORDER 4 - 2025-12-04
INSERT INTO product_order (created_at, had_promotion, observations, original_price, product_name, promotion_name,  quantity, subtotal, unit_price, order_id, product_id) VALUES
('2025-12-04 21:15:00', 1, 'Item gerado via seed', 30.00, 'X-Burger Bacon', 'Promo Bacon Day',   2, 54.00, 27.00, 4, 13),
('2025-12-04 21:15:00', 0, 'Item gerado via seed', 23.00, 'Dog Premium', NULL,   1, 23.00, 23.00, 4, 24);
-- ORDER 5 - 2025-12-05
INSERT INTO product_order (created_at, had_promotion, observations, original_price, product_name, promotion_name,  quantity, subtotal, unit_price, order_id, product_id) VALUES
('2025-12-05 19:45:00', 1, 'Item gerado via seed', 35.00, 'Basilios', 'Promo Basilios Day',   3, 90.00, 30.00, 5, 3),
('2025-12-05 19:45:00', 0, 'Item gerado via seed', 22.00, 'Batata Frita c/ Cheddar e Bacon', NULL,   1, 22.00, 22.00, 5, 28);
-- ORDER 11 - 2025-12-11
INSERT INTO product_order (created_at, had_promotion, observations, original_price, product_name, promotion_name,  quantity, subtotal, unit_price, order_id, product_id) VALUES
('2025-12-11 19:30:20', 1, 'Item gerado via seed', 35.00, 'Basilios', 'Promo Basilios Day',   1, 30.00, 30.00, 11, 3),
('2025-12-11 19:30:20', 0, 'Item gerado via seed', 45.00, 'Combo X-Bacon Salada', NULL,   1, 45.00, 45.00, 11, 32);
-- ORDER 12 - 2025-12-12
INSERT INTO product_order (created_at, had_promotion, observations, original_price, product_name, promotion_name,  quantity, subtotal, unit_price, order_id, product_id) VALUES
('2025-12-12 20:11:00', 0, 'Item gerado via seed', 42.00, 'Combo X-Salada', NULL,   2, 84.00, 42.00, 12, 31);
