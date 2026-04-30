-- Normalize missing/invalid subcategory values to avoid enum parse errors.
-- Adjust names/ids as needed for your dataset.

-- 1) Default any NULL subcategory to OUTRO
UPDATE adicionais
SET subcategory = 'OUTRO'
WHERE subcategory IS NULL;

-- 2) Specific fix for "Hamburguer Extra" (choose PROTEINA or OUTRO)
UPDATE adicionais
SET subcategory = 'PROTEINA'
WHERE LOWER(name) = 'hamburguer extra';

-- 3) (Optional) Insert bebidas/pao as adicionais and link to products
-- Replace placeholders with real product IDs and desired names/prices.
--
-- INSERT INTO adicionais (name, description, subcategory, price, available)
-- VALUES
--   ('Pao Brioche', 'Tipo de pão', 'OUTRO', 0.00, true),
--   ('Pao Australiano', 'Tipo de pão', 'OUTRO', 0.00, true),
--   ('Coca-Cola Lata', 'Bebida', 'OUTRO', 0.00, true);
--
-- -- Link adicionais to a specific product (replace :productId and :adicionalId)
-- INSERT INTO adicional_product (product_id, adicional_id)
-- VALUES
--   (:productId, :adicionalId);

