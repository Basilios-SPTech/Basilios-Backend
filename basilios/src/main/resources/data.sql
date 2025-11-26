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

