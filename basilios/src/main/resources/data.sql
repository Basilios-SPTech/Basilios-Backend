-- ===========================================
-- SEED DATA PARA BASILIOS HAMBURGERIA
-- Sintaxe compatível com MySQL
-- ===========================================

-- ===========================================
-- LOJA
-- -- ===========================================
-- INSERT IGNORE INTO stores (name, address, latitude, longitude, phone, opening_hours)
-- VALUES (
--   'Basilios Burger & Açaí',
--   'R. Basílio da Cunha, 454 — Vila Deodoro, São Paulo, SP, 01544-001',
--   -23.57694,
--   -46.62444,
--   '(11) 4801-4864',
--   '12:00–00:00 (Seg–Sáb); 18:00–00:00 (Dom)'
-- );
--
--
-- -- ===========================================
-- -- USUÁRIOS
-- -- ===========================================
--
-- -- Usuário FUNCIONÁRIO (tem as roles: CLIENTE e FUNCIONARIO)
-- -- Email: admin@basilios.com | Senha: admin123
-- INSERT IGNORE INTO usuario (nome_usuario, email, password, cpf, telefone, enabled, created_at, updated_at)
-- VALUES (
--     'Administrador Basilios',
--     'admin@basilios.com',
--     '$2a$10$jO.Tuln5gZTzaoS/d47youLMtq47QbI6N7rNxr4CybO10p5ZYDFXC',
--     '58745699854',
--     '11999999999',
--     true,
--     NOW(),
--     NOW()
-- );
--
-- -- Roles do Funcionário
-- INSERT IGNORE INTO usuario_roles (usuario_id, role)
-- SELECT u.id, 'ROLE_FUNCIONARIO'
-- FROM usuario u
-- WHERE u.email = 'admin@basilios.com'
-- AND NOT EXISTS (
--     SELECT 1 FROM usuario_roles ur
--     WHERE ur.usuario_id = u.id AND ur.role = 'ROLE_FUNCIONARIO'
-- );
--
-- INSERT IGNORE INTO usuario_roles (usuario_id, role)
-- SELECT u.id, 'ROLE_CLIENTE'
-- FROM usuario u
-- WHERE u.email = 'admin@basilios.com'
-- AND NOT EXISTS (
--     SELECT 1 FROM usuario_roles ur
--     WHERE ur.usuario_id = u.id AND ur.role = 'ROLE_CLIENTE'
-- );
--
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
-- INSERT IGNORE INTO product
--     (id, category, created_at, description, is_paused, name, price, subcategory, updated_at, image_url)
-- VALUES
--     (1, 'BURGER', '2025-11-26 19:18:10.374821',
--      'Um delicioso hambúrguer picanha, com bacon triturado, cheddar fatiado, Catupiry empanado, alface, tomate e a deliciosíssima maionese da casa',
--      0, 'Glicério', 40.00, 'BEEF', '2025-11-26 19:18:10.374821',
--      '/uploads/24a1b1fa-8bb9-468e-8d6b-a4d8f074af53.jpg'),
--
--     (2, 'BURGER', '2025-11-26 19:20:58.061046',
--      'Um irresistível X-Frango, com peito de frango suculento, milho doce e crocante, o inconfundível Catupiry cremoso e aquele sabor que conquista em cada mordida.',
--      0, 'Vila Monumento', 28.00, 'CHICKEN', '2025-11-26 19:20:58.061046',
--      '/uploads/6c2cc5de-c2e9-4b60-bd47-0c22f14a73d1.jpg'),
--
--     (3, 'BURGER', '2025-11-26 19:23:23.962334',
--      'Basilios: Um hambúrguer de 110g suculento, coberto com o irresistível cheddar Polenghi, acompanhado de uma salada fresca, crocantes onion rings e finalizado com a cremosidade única da nossa maionese da casa. Uma explosão de sabor em cada mordida!',
--      0, 'Basilios', 35.00, 'BEEF', '2025-11-26 19:23:23.962334',
--      '/uploads/c1d40aa8-18fd-4092-8b8e-8331735c34b8.jpg'),
--
--     (4, 'BURGER', '2025-11-26 19:24:43.880204',
--      'Um X-Burger com molho tártaro irresistível, tomate fresco e o toque especial do agrião, trazendo equilíbrio entre sabor marcante e leveza em cada mordida.',
--      0, 'Cambuci', 32.00, 'BEEF', '2025-11-26 19:24:43.880204',
--      '/uploads/a1ce9257-8c35-43c7-aa6b-40db9d5e2942.jpg'),
--
--     (5, 'BURGER', '2025-11-26 19:28:19.371949',
--      'Dois hambúrgueres suculentos, cobertos com cheddar fatiado, bacon crocante, picles refrescantes e o irresistível molho tártaro que dá o toque final a essa explosão de sabor.',
--      0, 'Paraiso', 45.00, 'BEEF', '2025-11-26 19:28:19.371949',
--      '/uploads/d05efafe-c8fb-4d31-b2e9-ff336a9e9f79.jpg'),
--
--     (6, 'BURGER', '2025-11-26 19:30:01.472108',
--      'Um hambúrguer de picanha suculento, coberto com cheddar derretido, bacon crocante e uma salada fresca que traz equilíbrio e sabor em cada mordida.',
--      0, 'Vila Mariana', 38.00, 'BEEF', '2025-11-26 19:30:01.472108',
--      '/uploads/1451bfe2-2db6-41fe-a98c-1445407e9c94.jpg'),
--
--     (7, 'BURGER', '2025-11-26 19:31:05.401540',
--      'Um hambúrguer suculento, coberto com a cremosidade da mussarela de búfala, o sabor marcante do tomate seco, a leveza da rúcula fresca e finalizado com nossa maionese especial. Uma combinação sofisticada e irresistível!',
--      0, 'Klabin', 36.00, 'BEEF', '2025-11-26 19:31:05.401540',
--      '/uploads/27da6b46-a191-485a-a8ae-7cc488973f1b.jpg'),
--
--     (8, 'BURGER', '2025-11-26 19:32:21.533079',
--      'Um hambúrguer de 110g suculento no pão macio, coberto com queijo derretido e o irresistível cheddar que garante sabor intenso em cada mordida.',
--      0, 'X-burger cheddar', 31.00, 'BEEF', '2025-11-26 19:32:21.533079',
--      '/uploads/54c91904-2295-4c79-b993-d2ea12087244.jpg'),
--
--     (9, 'BURGER', '2025-11-26 19:33:13.714851',
--      'Um hambúrguer de 110g suculento no pão macio, coberto com queijo derretido e a cremosidade irresistível do Catupiry. Simples, clássico e cheio de sabor!',
--      0, 'X-burger Catupiry', 31.00, 'BEEF', '2025-11-26 19:33:13.714851',
--      '/uploads/9bdcbf3d-ce61-425a-b0da-96938282c8ae.jpg'),
--
--     (10, 'BURGER', '2025-11-26 19:34:30.920253',
--      'Um pão de hambúrguer macio, recheado com uma carne de 120g suculenta e coberto com queijo derretido que traz aquele sabor irresistível em cada mordida.',
--      0, 'X-Burger Simples', 27.00, 'BEEF', '2025-11-26 19:34:30.920253',
--      '/uploads/8cd04d65-fa18-4b46-8035-1cdc090d2b28.jpg'),
--
--     (11, 'BURGER', '2025-11-26 19:35:25.000772',
--      'Um pão de hambúrguer macio, recheado com um hambúrguer de 110g suculento, acompanhado de ovo saboroso e queijo derretido que juntos criam uma combinação clássica e irresistível.',
--      0, 'X-burger egg', 29.00, 'BEEF', '2025-11-26 19:35:25.000772',
--      '/uploads/318ea3a2-8198-4338-8417-d857bb19bdd0.jpg'),
--
--     (12, 'BURGER', '2025-11-26 19:36:34.009798',
--      'Um pão de hambúrguer macio, recheado com um hambúrguer de 110g suculento, acompanhado de alface crocante, tomate fresco, queijo derretido e a cremosidade da nossa maionese especial. Uma combinação clássica e irresistível!',
--      0, 'X-burger egg salada', 33.00, 'BEEF', '2025-11-26 19:36:34.009798',
--      '/uploads/65e41353-1e3a-47ea-afe9-1f3f7c30bc2d.jpg'),
--
--     (14, 'BURGER', '2025-11-26 21:09:44.562086',
--      'Um pão de hambúrguer macio, recheado com uma carne de 120g suculenta, coberta por queijo derretido e finalizada com bacon crocante que garante sabor intenso em cada mordida.',
--      0, 'X-Burger Bacon', 30.00, 'BEEF', '2025-11-26 21:09:44.562086',
--      '/uploads/8f1998d4-c3be-4358-9f9e-015a09146996.jpg'),
--
--     (15, 'BURGER', '2025-11-26 21:10:49.155825',
--      'Um pão de hambúrguer macio, recheado com um hambúrguer de 110g suculento, acompanhado de ovo saboroso e presunto que juntos criam uma combinação clássica e irresistível.',
--      0, 'X-burger egg presunto', 33.00, 'BEEF', '2025-11-26 21:10:49.155825',
--      '/uploads/faa492da-80c8-4eda-b46a-336c178a9914.jpg'),
--
--     (16, 'BURGER', '2025-11-26 21:12:33.519729',
--      'Um pão de hambúrguer macio, recheado com uma carne de 120g suculenta, coberta por queijo derretido, acompanhada de tomate fresco, alface crocante e finalizada com a cremosidade da nossa maionese caseira. Uma combinação clássica e irresistível!',
--      0, 'X-Burger Salada', 30.00, 'BEEF', '2025-11-26 21:12:33.519729',
--      '/uploads/fbda5c68-93af-4b77-94ae-932d2154d0f7.jpg'),
--
--     (17, 'BURGER', '2025-11-26 21:13:23.849419',
--      'Um pão de hambúrguer macio, recheado com um hambúrguer de 110g suculento e finalizado com cebola frita crocante que dá aquele toque especial de sabor.',
--      0, 'X-burger acebolado', 30.00, 'BEEF', '2025-11-26 21:13:23.849419',
--      '/uploads/3dba0349-208f-4fe5-8f76-2e548319ee3f.jpg'),
--
--     (18, 'BURGER', '2025-11-26 21:14:16.939855',
--      'Um pão de hambúrguer macio, recheado com uma carne de 120g suculenta, coberta por queijo derretido, acompanhada de tomate fresco, alface crocante e finalizada com a cremosidade da nossa maionese caseira. Uma combinação clássica e irresistível!',
--      0, 'X-Burger Bacon Salada', 36.00, 'BEEF', '2025-11-26 21:14:16.939855',
--      '/uploads/fe2b1c1b-0ccd-486f-8b7a-e30e139b4b3c.jpg'),
--
--     (19, 'BURGER', '2025-11-26 21:14:57.711666',
--      'Um pão de hambúrguer macio, recheado com um hambúrguer de 110g suculento, coberto por queijo derretido e finalizado com o sabor marcante do molho tártaro. Simples, clássico e irresistível!',
--      0, 'X-burger tártaro', 30.00, 'BEEF', '2025-11-29 19:36:03.066068',
--      '/uploads/37a428cc-b44d-4fae-aa35-d71e162e926d.jpg');

-- ===========================================
-- PEDIDOS DE 1 A 11 DE DEZEMBRO DE 2025
-- ===========================================

-- Dia 1 de dezembro
INSERT IGNORE INTO orders (id, cancellation_reason, cancelled_at, codigo_pedido, confirmed_at, created_at, delivered_at, delivery_fee, discount, dispatched_at, observations, preparing_at, status, subtotal, total, endereco_entrega_id, usuario_id)
VALUES
  (101, NULL, NULL, 'PED-20251201-0001', '2025-12-01 12:05:00', '2025-12-01 12:00:00', '2025-12-01 12:40:00', 5.00, 0.00, '2025-12-01 12:20:00', 'Sem cebola', '2025-12-01 12:10:00', 'ENTREGUE', 54.00, 59.00, 1, 1),
  (102, NULL, NULL, 'PED-20251201-0002', NULL, '2025-12-01 19:30:00', NULL, 5.00, 0.00, NULL, 'Com maionese à parte', NULL, 'PENDENTE', 33.00, 38.00, 1, 1);

INSERT IGNORE INTO product_order (id, created_at, had_promotion, observations, original_price, product_name, promotion_name, quantity, subtotal, unit_price, order_id, product_id)
VALUES
  (201, '2025-12-01 12:00:00', 0x00, 'Sem cebola', NULL, 'X-Burger Simples', NULL, 2, 54.00, 27.00, 101, 10),
  (202, '2025-12-01 19:30:00', 0x00, 'Com maionese à parte', NULL, 'X-burger egg salada', NULL, 1, 33.00, 33.00, 102, 12);

-- Dia 2 de dezembro
INSERT IGNORE INTO orders (id, cancellation_reason, cancelled_at, codigo_pedido, confirmed_at, created_at, delivered_at, delivery_fee, discount, dispatched_at, observations, preparing_at, status, subtotal, total, endereco_entrega_id, usuario_id)
VALUES
  (103, NULL, NULL, 'PED-20251202-0001', '2025-12-02 13:10:00', '2025-12-02 13:00:00', '2025-12-02 13:50:00', 5.00, 0.00, '2025-12-02 13:30:00', 'Sem tomate', '2025-12-02 13:15:00', 'ENTREGUE', 36.00, 41.00, 1, 1),
  (104, 'Pedido duplicado', '2025-12-02 20:10:00', 'PED-20251202-0002', NULL, '2025-12-02 20:00:00', NULL, 5.00, 0.00, NULL, 'Cancelar, pedido duplicado', NULL, 'CANCELADO', 45.00, 50.00, 1, 1);

INSERT IGNORE INTO product_order (id, created_at, had_promotion, observations, original_price, product_name, promotion_name, quantity, subtotal, unit_price, order_id, product_id)
VALUES
  (203, '2025-12-02 13:00:00', 0x00, 'Sem tomate', NULL, 'Klabin', NULL, 1, 36.00, 36.00, 103, 7),
  (204, '2025-12-02 20:00:00', 0x00, NULL, NULL, 'Paraiso', NULL, 1, 45.00, 45.00, 104, 5);

-- Dia 3 de dezembro
INSERT IGNORE INTO orders (id, cancellation_reason, cancelled_at, codigo_pedido, confirmed_at, created_at, delivered_at, delivery_fee, discount, dispatched_at, observations, preparing_at, status, subtotal, total, endereco_entrega_id, usuario_id)
VALUES
  (105, NULL, NULL, 'PED-20251203-0001', '2025-12-03 11:10:00', '2025-12-03 11:00:00', '2025-12-03 11:40:00', 5.00, 0.00, '2025-12-03 11:20:00', 'Com bacon extra', '2025-12-03 11:12:00', 'ENTREGUE', 30.00, 35.00, 1, 1),
  (106, NULL, NULL, 'PED-20251203-0002', NULL, '2025-12-03 18:00:00', NULL, 5.00, 0.00, NULL, NULL, NULL, 'PENDENTE', 31.00, 36.00, 1, 1);

INSERT IGNORE INTO product_order (id, created_at, had_promotion, observations, original_price, product_name, promotion_name, quantity, subtotal, unit_price, order_id, product_id)
VALUES
  (205, '2025-12-03 11:00:00', 0x01, 'Com bacon extra', 35.00, 'X-Burger Bacon', 'Promoção Bacon Day', 1, 27.00, 27.00, 105, 14),
  (206, '2025-12-03 18:00:00', 0x00, NULL, NULL, 'X-burger cheddar', NULL, 1, 31.00, 31.00, 106, 8);

-- Dia 4 de dezembro
INSERT IGNORE INTO orders (id, cancellation_reason, cancelled_at, codigo_pedido, confirmed_at, created_at, delivered_at, delivery_fee, discount, dispatched_at, observations, preparing_at, status, subtotal, total, endereco_entrega_id, usuario_id)
VALUES
  (107, NULL, NULL, 'PED-20251204-0001', '2025-12-04 12:10:00', '2025-12-04 12:00:00', '2025-12-04 12:45:00', 5.00, 0.00, '2025-12-04 12:20:00', 'Com cheddar extra', '2025-12-04 12:12:00', 'ENTREGUE', 38.00, 43.00, 1, 1),
  (108, NULL, NULL, 'PED-20251204-0002', NULL, '2025-12-04 19:00:00', NULL, 5.00, 0.00, NULL, 'Sem maionese', NULL, 'PENDENTE', 27.00, 32.00, 1, 1);

INSERT IGNORE INTO product_order (id, created_at, had_promotion, observations, original_price, product_name, promotion_name, quantity, subtotal, unit_price, order_id, product_id)
VALUES
  (207, '2025-12-04 12:00:00', 0x00, 'Com cheddar extra', NULL, 'Vila Mariana', NULL, 1, 38.00, 38.00, 107, 6),
  (208, '2025-12-04 19:00:00', 0x00, 'Sem maionese', NULL, 'X-Burger Simples', NULL, 1, 27.00, 27.00, 108, 10);

-- Dia 5 de dezembro
INSERT IGNORE INTO orders (id, cancellation_reason, cancelled_at, codigo_pedido, confirmed_at, created_at, delivered_at, delivery_fee, discount, dispatched_at, observations, preparing_at, status, subtotal, total, endereco_entrega_id, usuario_id)
VALUES
  (109, NULL, NULL, 'PED-20251205-0001', '2025-12-05 13:10:00', '2025-12-05 13:00:00', '2025-12-05 13:50:00', 5.00, 0.00, '2025-12-05 13:30:00', NULL, '2025-12-05 13:15:00', 'ENTREGUE', 35.00, 40.00, 1, 1),
  (110, 'Cliente desistiu', '2025-12-05 20:10:00', 'PED-20251205-0002', NULL, '2025-12-05 20:00:00', NULL, 5.00, 0.00, NULL, 'Desistiu após pedido', NULL, 'CANCELADO', 28.00, 33.00, 1, 1);

INSERT IGNORE INTO product_order (id, created_at, had_promotion, observations, original_price, product_name, promotion_name, quantity, subtotal, unit_price, order_id, product_id)
VALUES
  (209, '2025-12-05 13:00:00', 0x00, NULL, NULL, 'Basilios', NULL, 1, 35.00, 35.00, 109, 3),
  (210, '2025-12-05 20:00:00', 0x00, NULL, NULL, 'Vila Monumento', NULL, 1, 28.00, 28.00, 110, 2);

-- Dia 6 de dezembro
INSERT IGNORE INTO orders (id, cancellation_reason, cancelled_at, codigo_pedido, confirmed_at, created_at, delivered_at, delivery_fee, discount, dispatched_at, observations, preparing_at, status, subtotal, total, endereco_entrega_id, usuario_id)
VALUES
  (111, NULL, NULL, 'PED-20251206-0001', '2025-12-06 12:10:00', '2025-12-06 12:00:00', '2025-12-06 12:45:00', 5.00, 0.00, '2025-12-06 12:20:00', NULL, '2025-12-06 12:12:00', 'ENTREGUE', 32.00, 37.00, 1, 1),
  (112, NULL, NULL, 'PED-20251206-0002', NULL, '2025-12-06 19:00:00', NULL, 5.00, 0.00, NULL, 'Com salada extra', NULL, 'PENDENTE', 33.00, 38.00, 1, 1);

INSERT IGNORE INTO product_order (id, created_at, had_promotion, observations, original_price, product_name, promotion_name, quantity, subtotal, unit_price, order_id, product_id)
VALUES
  (211, '2025-12-06 12:00:00', 0x00, NULL, NULL, 'Cambuci', NULL, 1, 32.00, 32.00, 111, 4),
  (212, '2025-12-06 19:00:00', 0x01, 'Com salada extra', 33.00, 'X-burger egg salada', 'Promoção Salada', 1, 28.00, 28.00, 112, 12);

-- Dia 7 de dezembro
INSERT IGNORE INTO orders (id, cancellation_reason, cancelled_at, codigo_pedido, confirmed_at, created_at, delivered_at, delivery_fee, discount, dispatched_at, observations, preparing_at, status, subtotal, total, endereco_entrega_id, usuario_id)
VALUES
  (113, 'Mudança de planos', '2025-12-08 20:19:27', 'PED-20251207-0001', NULL, '2025-12-07 05:55:48', NULL, 5.00, 0.00, NULL, 'Entregar na portaria', NULL, 'CANCELADO', 87.00, 92.00, 1, 1);

INSERT IGNORE INTO product_order (id, created_at, had_promotion, observations, original_price, product_name, promotion_name, quantity, subtotal, unit_price, order_id, product_id)
VALUES
  (213, '2025-12-07 05:55:48', 0x00, 'Sem cebola', NULL, 'X-Burger Simples', NULL, 2, 54.00, 27.00, 113, 10),
  (214, '2025-12-07 05:55:48', 0x00, NULL, NULL, 'X-burger egg salada', NULL, 1, 33.00, 33.00, 113, 12);

-- Dia 8 de dezembro
INSERT IGNORE INTO orders (id, cancellation_reason, cancelled_at, codigo_pedido, confirmed_at, created_at, delivered_at, delivery_fee, discount, dispatched_at, observations, preparing_at, status, subtotal, total, endereco_entrega_id, usuario_id)
VALUES
  (114, NULL, NULL, 'PED-20251208-0001', NULL, '2025-12-08 20:52:34', NULL, 5.00, 0.00, NULL, 'Entregar na portaria', NULL, 'PENDENTE', 87.00, 92.00, 1, 1);

INSERT IGNORE INTO product_order (id, created_at, had_promotion, observations, original_price, product_name, promotion_name, quantity, subtotal, unit_price, order_id, product_id)
VALUES
  (215, '2025-12-08 20:52:34', 0x00, 'Sem cebola', NULL, 'X-Burger Simples', NULL, 2, 54.00, 27.00, 114, 10),
  (216, '2025-12-08 20:52:34', 0x00, NULL, NULL, 'X-burger egg salada', NULL, 1, 33.00, 33.00, 114, 12);

-- Dia 9 de dezembro
INSERT IGNORE INTO orders (id, cancellation_reason, cancelled_at, codigo_pedido, confirmed_at, created_at, delivered_at, delivery_fee, discount, dispatched_at, observations, preparing_at, status, subtotal, total, endereco_entrega_id, usuario_id)
VALUES
  (115, NULL, NULL, 'PED-20251209-0001', '2025-12-09 13:10:00', '2025-12-09 13:00:00', '2025-12-09 13:50:00', 5.00, 0.00, '2025-12-09 13:30:00', 'Com ovo extra', '2025-12-09 13:15:00', 'ENTREGUE', 29.00, 34.00, 1, 1),
  (116, NULL, NULL, 'PED-20251209-0002', NULL, '2025-12-09 20:00:00', NULL, 5.00, 0.00, NULL, NULL, NULL, 'PENDENTE', 30.00, 35.00, 1, 1);

INSERT IGNORE INTO product_order (id, created_at, had_promotion, observations, original_price, product_name, promotion_name, quantity, subtotal, unit_price, order_id, product_id)
VALUES
  (217, '2025-12-09 13:00:00', 0x01, 'Com ovo extra', 29.00, 'X-burger egg', 'Promoção Ovo Extra', 1, 24.00, 24.00, 115, 11),
  (218, '2025-12-09 20:00:00', 0x00, NULL, NULL, 'X-burger acebolado', NULL, 1, 30.00, 30.00, 116, 17);

-- Dia 10 de dezembro
INSERT IGNORE INTO orders (id, cancellation_reason, cancelled_at, codigo_pedido, confirmed_at, created_at, delivered_at, delivery_fee, discount, dispatched_at, observations, preparing_at, status, subtotal, total, endereco_entrega_id, usuario_id)
VALUES
  (117, NULL, NULL, 'PED-20251210-0001', '2025-12-10 12:10:00', '2025-12-10 12:00:00', '2025-12-10 12:45:00', 5.00, 0.00, '2025-12-10 12:20:00', NULL, '2025-12-10 12:12:00', 'ENTREGUE', 36.00, 41.00, 1, 1),
  (118, NULL, NULL, 'PED-20251210-0002', NULL, '2025-12-10 19:00:00', NULL, 5.00, 0.00, NULL, 'Com cheddar extra', NULL, 'PENDENTE', 31.00, 36.00, 1, 1);

INSERT IGNORE INTO product_order (id, created_at, had_promotion, observations, original_price, product_name, promotion_name, quantity, subtotal, unit_price, order_id, product_id)
VALUES
  (219, '2025-12-10 12:00:00', 0x00, NULL, NULL, 'Klabin', NULL, 1, 36.00, 36.00, 117, 7),
  (220, '2025-12-10 19:00:00', 0x00, 'Com cheddar extra', NULL, 'X-burger Catupiry', NULL, 1, 31.00, 31.00, 118, 9);

-- Dia 11 de dezembro
INSERT IGNORE INTO orders (id, cancellation_reason, cancelled_at, codigo_pedido, confirmed_at, created_at, delivered_at, delivery_fee, discount, dispatched_at, observations, preparing_at, status, subtotal, total, endereco_entrega_id, usuario_id)
VALUES
  (119, NULL, NULL, 'PED-20251211-0001', '2025-12-11 13:10:00', '2025-12-11 13:00:00', '2025-12-11 13:50:00', 5.00, 0.00, '2025-12-11 13:30:00', 'Com picles extra', '2025-12-11 13:15:00', 'ENTREGUE', 45.00, 50.00, 1, 1),
  (120, NULL, NULL, 'PED-20251211-0002', NULL, '2025-12-11 20:00:00', NULL, 5.00, 0.00, NULL, NULL, NULL, 'PENDENTE', 30.00, 35.00, 1, 1);

INSERT IGNORE INTO product_order (id, created_at, had_promotion, observations, original_price, product_name, promotion_name, quantity, subtotal, unit_price, order_id, product_id)
VALUES
  (221, '2025-12-11 13:00:00', 0x01, 'Com picles extra', 45.00, 'Paraiso', 'Black Friday', 1, 35.00, 35.00, 119, 5),
  (222, '2025-12-11 20:00:00', 0x00, NULL, NULL, 'X-burger tártaro', NULL, 1, 30.00, 30.00, 120, 19);
