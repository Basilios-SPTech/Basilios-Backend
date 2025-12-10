-- ===========================================
-- SEED DATA PARA BASILIOS HAMBURGERIA
-- Sintaxe compatível com MySQL
-- ===========================================

-- ===========================================
-- LOJA
-- ===========================================
INSERT IGNORE INTO stores (name, address, latitude, longitude, phone, opening_hours)
VALUES (
  'Basilios Burger & Açaí',
  'R. Basílio da Cunha, 454 — Vila Deodoro, São Paulo, SP, 01544-001',
  -23.57694,
  -46.62444,
  '(11) 4801-4864',
  '12:00–00:00 (Seg–Sáb); 18:00–00:00 (Dom)'
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
-- ===========================================
-- PRODUTOS
-- ===========================================
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

    (14, 'BURGER', '2025-11-26 21:09:44.562086',
     'Um pão de hambúrguer macio, recheado com uma carne de 120g suculenta, coberta por queijo derretido e finalizada com bacon crocante que garante sabor intenso em cada mordida.',
     0, 'X-Burger Bacon', 30.00, 'BEEF', '2025-11-26 21:09:44.562086',
     '/uploads/8f1998d4-c3be-4358-9f9e-015a09146996.jpg'),

    (15, 'BURGER', '2025-11-26 21:10:49.155825',
     'Um pão de hambúrguer macio, recheado com um hambúrguer de 110g suculento, acompanhado de ovo saboroso e presunto que juntos criam uma combinação clássica e irresistível.',
     0, 'X-burger egg presunto', 33.00, 'BEEF', '2025-11-26 21:10:49.155825',
     '/uploads/faa492da-80c8-4eda-b46a-336c178a9914.jpg'),

    (16, 'BURGER', '2025-11-26 21:12:33.519729',
     'Um pão de hambúrguer macio, recheado com uma carne de 120g suculenta, coberta por queijo derretido, acompanhada de tomate fresco, alface crocante e finalizada com a cremosidade da nossa maionese caseira. Uma combinação clássica e irresistível!',
     0, 'X-Burger Salada', 30.00, 'BEEF', '2025-11-26 21:12:33.519729',
     '/uploads/fbda5c68-93af-4b77-94ae-932d2154d0f7.jpg'),

    (18, 'BURGER', '2025-11-26 21:14:16.939855',
     'Um pão de hambúrguer macio, recheado com uma carne de 120g suculenta, coberta por queijo derretido, acompanhada de tomate fresco, alface crocante e finalizada com a cremosidade da nossa maionese caseira. Uma combinação clássica e irresistível!',
     0, 'X-Burger Bacon Salada', 36.00, 'BEEF', '2025-11-26 21:14:16.939855',
     '/uploads/fe2b1c1b-0ccd-486f-8b7a-e30e139b4b3c.jpg'),

    (19, 'BURGER', '2025-11-26 21:14:57.711666',
     'Um pão de hambúrguer macio, recheado com um hambúrguer de 110g suculento, coberto por queijo derretido e finalizado com o sabor marcante do molho tártaro. Simples, clássico e irresistível!',
     0, 'X-burger tártaro', 30.00, 'BEEF', '2025-12-02 21:11:32.647936',
     '/uploads/37a428cc-b44d-4fae-aa35-d71e162e926d.jpg'),

    (21, 'BURGER', '2025-12-02 21:18:22.907896',
     'O clássico X-Bacon Egg com um hambúrguer suculento, bacon crocante e ovo saboroso, acompanhado de salada fresca, presunto e cebola acebolada que trazem equilíbrio e intensidade de sabor em cada mordida.',
     0, 'Aclimação', 39.00, 'BEEF', '2025-12-02 21:18:22.909892',
     '/uploads/8d8e343e-e134-44bd-9903-958cd4e4e538.jpg'),

    (24, 'BURGER', '2025-12-07 03:02:23.961220',
     'Beirute médio de frango com mussarela e presunto, acompanhado de alface fresca, tomate e maionese cremosa.',
     0, 'Beirute médio (Frango)', 35.00, 'CHICKEN', '2025-12-07 03:02:23.969262',
     NULL),

    (25, 'BURGER', '2025-12-07 03:05:44.842412',
     'Beirute grande de frango com mussarela e presunto, alface fresca, tomate e maionese cremosa, em porção mais generosa.',
     0, 'Beirute Grande (Frango)', 43.00, 'CHICKEN', '2025-12-07 03:05:44.845900',
     NULL),

    (26, 'BURGER', '2025-12-07 03:06:45.040924',
     'Beirute médio de filé mignon com mussarela e presunto, alface fresca, tomate e maionese cremosa.',
     0, 'Beirute médio (Filé Mignon)', 41.00, 'BEEF', '2025-12-07 03:06:45.040924',
     NULL),

    (27, 'BURGER', '2025-12-07 03:07:37.775835',
     'Beirute grande de filé mignon com mussarela e presunto, alface fresca, tomate e maionese cremosa, em porção mais generosa.',
     0, 'Beirute Grande (Filé Mignon)', 52.00, 'BEEF', '2025-12-07 03:07:37.775835',
     NULL),

    (28, 'BURGER', '2025-12-07 03:18:31.326171',
     'Hot dog premium com duas salsichas, bacon crocante, cheddar, Catupiry, maionese e batata palha.',
     0, 'Dog Premium', 23.00, 'BEEF', '2025-12-07 03:18:31.334135',
     '/uploads/54d0d658-477a-4b82-97a4-224c092c1dcd.jpg'),

    (29, 'BURGER', '2025-12-07 03:19:58.206518',
     'Hot dog com pão macio, duas salsichas, maionese cremosa e batata palha crocante.',
     0, 'Dog Maionese Batata Palha', 18.00, 'BEEF', '2025-12-07 03:19:58.206518',
     NULL),

    (30, 'SIDE', '2025-12-07 03:22:46.316517',
     'Deliciosa Fritas',
     0, 'Batata Frita', 14.00, 'FRIES', '2025-12-07 03:22:46.316517',
     '/uploads/ea655094-3f82-4cd3-8b35-20c750bd4347.jpg'),

    (31, 'SIDE', '2025-12-07 03:25:31.705628',
     'Batata Crinkle bem crocante!',
     0, 'Batata Crinkle', 14.00, 'FRIES', '2025-12-07 03:25:31.705628',
     '/uploads/d5b28c84-2f30-44cd-8a95-4fb5ff887de5.jpg'),

    (32, 'SIDE', '2025-12-07 03:27:02.855919',
     'Deliciosas fritas c/ Cheddar e Bacon',
     0, 'Batata Frita c/ Cheddar e Bacon', 22.00, 'FRIES', '2025-12-07 03:27:02.869810',
     '/uploads/1dcb040e-71d5-4fe9-b024-7c263fc2fd46.jpg'),

    (33, 'SIDE', '2025-12-07 03:28:05.194697',
     'Anéis de cebola empanado',
     0, 'Onion Rings', 24.00, 'ONION_RINGS', '2025-12-07 03:28:05.194697',
     '/uploads/0a99630c-4b77-4991-8bf6-7dc66ef016ea.jpg'),

    (34, 'SIDE', '2025-12-07 03:29:11.554788',
     'Batata em forma de canoa',
     0, 'Batata Canoa', 21.00, 'FRIES', '2025-12-07 03:29:11.554788',
     '/uploads/b35a327e-c8a3-409f-84cb-61aa5aaed243.jpg'),

    (35, 'COMBO', '2025-12-07 03:32:01.064697',
     'X-salada (pão de hamburguer, hamburguer 110g, queijo, alface, tomate e maionese) + fritas 100g + refrigerante lata 350ml',
     0, 'Combo X-Salada', 42.00, NULL, '2025-12-08 14:48:25.307498',
     '/uploads/b12edbe8-111b-4f1e-97a5-6057f1e86ed0.jpg'),

    (36, 'COMBO', '2025-12-07 03:34:03.130399',
     'Combo X-Bacon salada com hambúrguer suculento, bacon crocante, queijo derretido, alface, tomate e maionese, acompanhado de batata frita e refrigerante.',
     0, 'Combo X-Bacon Salada', 45.00, NULL, '2025-12-07 03:34:03.133372',
     '/uploads/9ff9351e-bc05-47c4-93e5-18d489be2584.jpg');

