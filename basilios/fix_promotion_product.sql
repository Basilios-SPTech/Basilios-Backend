-- Fix promotion_product association for promotion ID 1
-- First, find the product ID for "Combo X-Salada"
SELECT id, nome FROM product WHERE nome LIKE '%Combo X-Salada%' LIMIT 5;

-- Insert the association if it doesn't exist
INSERT INTO promotion_product (promotion_id, product_id)
SELECT 1, p.id 
FROM product p
WHERE p.nome = 'Combo X-Salada'
  AND NOT EXISTS (
    SELECT 1 FROM promotion_product pp
    WHERE pp.promotion_id = 1 AND pp.product_id = p.id
  )
LIMIT 1;

-- Verify the association was created
SELECT pp.promotion_id, pp.product_id, p.nome
FROM promotion_product pp
JOIN product p ON pp.product_id = p.id
WHERE pp.promotion_id = 1;
