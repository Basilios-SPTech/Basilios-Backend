package com.basilios.basilios.core.service;

import com.basilios.basilios.app.dto.product.ProductRequestDTO;
import com.basilios.basilios.app.dto.product.ProductResponseDTO;
import com.basilios.basilios.core.exception.*;
import com.basilios.basilios.core.model.*;
import com.basilios.basilios.infra.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    @Autowired private ProductRepository productRepository;
    @Autowired private IngredientRepository ingredientRepository;
    @Autowired private IngredientProductRepository ingredientProductRepository;
    @Autowired private ProductOrderRepository productOrderRepository;
    @Autowired private ProductComboRepository productComboRepository;
    @Autowired private PromotionRepository promotionRepository;

    // ========== CRUD BÁSICO ==========

    /**
     * Cria novo produto com ingredientes
     */
    public ProductResponseDTO createProduct(ProductRequestDTO dto) {
        // Validar duplicação
        if (productRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new DuplicateProductException(dto.getName());
        }

        // Validar categoria obrigatória
        if (dto.getCategory() == null) {
            throw new BusinessException("Categoria é obrigatória");
        }

        // Validar coerência categoria x subcategoria
        if (dto.getSubcategory() != null &&
                dto.getSubcategory().getCategory() != dto.getCategory()) {
            throw new BusinessException("Subcategoria não pertence à categoria informada");
        }

        // Criar produto
        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .category(dto.getCategory())
                .subcategory(dto.getSubcategory())
                .tags(dto.getTags() != null ? dto.getTags() : new ArrayList<>())
                .price(dto.getPrice())
                .isPaused(false)
                .build();

        product = productRepository.save(product);

        // Adicionar ingredientes se fornecidos
        if (dto.getIngredientes() != null && !dto.getIngredientes().isEmpty()) {
            addIngredientsToProduct(product, dto.getIngredientes());
        }

        return convertToResponseDTO(product);
    }

    /**
     * Lista todos os produtos
     */
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getAllProducts(boolean activeOnly) {
        List<Product> products = activeOnly
                ? productRepository.findByIsPausedFalse()
                : productRepository.findAll();

        return products.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca produto por ID
     */
    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(Long id) {
        Product product = findProductOrThrow(id);
        return convertToResponseDTO(product);
    }

    /**
     * Atualiza produto
     */
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO dto) {
        Product product = findProductOrThrow(id);

        // Validar nome duplicado (se mudou)
        if (!product.getName().equalsIgnoreCase(dto.getName()) &&
                productRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new DuplicateProductException(dto.getName());
        }

        // Validar categoria
        if (dto.getCategory() == null) {
            throw new BusinessException("Categoria é obrigatória");
        }

        if (dto.getSubcategory() != null &&
                dto.getSubcategory().getCategory() != dto.getCategory()) {
            throw new BusinessException("Subcategoria não pertence à categoria informada");
        }

        // Atualizar campos
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setImageUrl(dto.getImageUrl());
        product.setCategory(dto.getCategory());
        product.setSubcategory(dto.getSubcategory());
        product.setTags(dto.getTags() != null ? dto.getTags() : new ArrayList<>());
        product.setPrice(dto.getPrice());

        // Atualizar ingredientes
        if (dto.getIngredientes() != null) {
            removeAllIngredientsFromProduct(product);
            if (!dto.getIngredientes().isEmpty()) {
                addIngredientsToProduct(product, dto.getIngredientes());
            }
        }

        product = productRepository.save(product);
        return convertToResponseDTO(product);
    }

    /**
     * Deleta produto com validações
     */
    public void deleteProduct(Long id) {
        Product product = findProductOrThrow(id);

        // Verificar se está em pedidos
        long orderCount = productOrderRepository.countByProductId(id);
        if (orderCount > 0) {
            throw new BusinessException(
                    "Produto não pode ser deletado. Está em " + orderCount + " pedidos");
        }

        // Verificar se está em combos
        long comboCount = productComboRepository.countByProductId(id);
        if (comboCount > 0) {
            throw new BusinessException(
                    "Produto não pode ser deletado. Está em " + comboCount + " combos");
        }

        productRepository.delete(product);
    }

    // ========== OPERAÇÕES DE STATUS ==========

    /**
     * Pausa produto (desativa do menu)
     */
    public ProductResponseDTO pauseProduct(Long id) {
        Product product = findProductOrThrow(id);

        if (product.getIsPaused()) {
            throw new BusinessException("Produto já está pausado");
        }

        product.pause();
        product = productRepository.save(product);
        return convertToResponseDTO(product);
    }

    /**
     * Ativa produto (volta ao menu)
     */
    public ProductResponseDTO activateProduct(Long id) {
        Product product = findProductOrThrow(id);

        if (!product.getIsPaused()) {
            throw new BusinessException("Produto já está ativo");
        }

        product.activate();
        product = productRepository.save(product);
        return convertToResponseDTO(product);
    }

    /**
     * Atualiza preço do produto
     */
    public ProductResponseDTO updatePrice(Long id, BigDecimal newPrice) {
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPriceException(newPrice);
        }

        Product product = findProductOrThrow(id);
        product.setPrice(newPrice);
        product = productRepository.save(product);
        return convertToResponseDTO(product);
    }

    /**
     * Atualiza status do produto
     */
    public ProductResponseDTO updateStatus(Long id, Boolean isPaused) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Produto não encontrado"));
        product.setIsPaused(isPaused);
        productRepository.save(product);
        return convertToResponseDTO(product);
    }

    // ========== GERENCIAMENTO DE INGREDIENTES ==========

    /**
     * Adiciona ingrediente ao produto
     */
    public ProductResponseDTO addIngredient(Long productId, String name, Integer qty, String unit) {
        Product product = findProductOrThrow(productId);

        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException("Nome do ingrediente é obrigatório");
        }

        // Buscar ou criar ingrediente
        Ingredient ingredient = ingredientRepository.findByNameIgnoreCase(name.trim())
                .orElseGet(() -> {
                    Ingredient newIng = new Ingredient();
                    newIng.setName(name.trim());
                    return ingredientRepository.save(newIng);
                });

        // Verificar se já existe
        if (ingredientProductRepository.existsByProductAndIngredient(product, ingredient)) {
            throw new BusinessException("Ingrediente já adicionado ao produto");
        }

        // Criar relacionamento
        IngredientProduct ip = new IngredientProduct();
        ip.setProduct(product);
        ip.setIngredient(ingredient);
        ip.setQuantity(qty != null ? qty : 1);
        ip.setMeasurementUnit(unit != null ? unit : "unidade");
        ingredientProductRepository.save(ip);

        return convertToResponseDTO(productRepository.findById(productId).get());
    }

    /**
     * Remove ingrediente do produto
     */
    public ProductResponseDTO removeIngredient(Long productId, Long ingredientId) {
        Product product = findProductOrThrow(productId);

        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new NotFoundException("Ingrediente não encontrado: " + ingredientId));

        IngredientProduct ip = ingredientProductRepository
                .findByProductAndIngredient(product, ingredient)
                .orElseThrow(() -> new BusinessException("Ingrediente não está associado ao produto"));

        ingredientProductRepository.delete(ip);
        return convertToResponseDTO(productRepository.findById(productId).get());
    }

    /**
     * Lista ingredientes do produto
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getProductIngredients(Long productId) {
        Product product = findProductOrThrow(productId);

        return ingredientProductRepository.findByProduct(product).stream()
                .map(ip -> Map.of(
                        "id", (Object) ip.getIngredient().getId(),
                        "name", ip.getIngredient().getName(),
                        "quantity", ip.getQuantity(),
                        "unit", ip.getMeasurementUnit()
                ))
                .collect(Collectors.toList());
    }

    // ========== ESTATÍSTICAS GERAIS ==========

    /**
     * Estatísticas gerais de produtos
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistics() {
        Object[] stats = productRepository.getMenuStatistics();

        return Map.of(
                "total", stats[0],
                "active", stats[1],
                "paused", stats[2],
                "avgPrice", stats[3] != null ? stats[3] : BigDecimal.ZERO,
                "minPrice", stats[4] != null ? stats[4] : BigDecimal.ZERO,
                "maxPrice", stats[5] != null ? stats[5] : BigDecimal.ZERO
        );
    }

    /**
     * Análise de preços
     */
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getPriceAnalysis() {
        Object[] stats = productRepository.getMenuStatistics();

        return Map.of(
                "average", (BigDecimal) stats[3] != null ? (BigDecimal) stats[3] : BigDecimal.ZERO,
                "min", (BigDecimal) stats[4] != null ? (BigDecimal) stats[4] : BigDecimal.ZERO,
                "max", (BigDecimal) stats[5] != null ? (BigDecimal) stats[5] : BigDecimal.ZERO
        );
    }

    // ========== ANÁLISES DE VENDAS ==========

    /**
     * Estatísticas de vendas de um produto
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getSalesStatistics(Long productId) {
        Product product = findProductOrThrow(productId);
        long totalSold = productOrderRepository.countByProductId(productId);
        BigDecimal revenue = productOrderRepository.calculateProductRevenue(productId);

        return Map.of(
                "productId", productId,
                "productName", product.getName(),
                "totalSold", totalSold,
                "revenue", revenue != null ? revenue : BigDecimal.ZERO
        );
    }

    /**
     * Produtos mais vendidos
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getBestSellers(int limit) {
        return productOrderRepository.findBestSellingProducts().stream()
                .limit(limit)
                .map(row -> {
                    Product p = (Product) row[0];
                    return Map.of(
                            "id", (Object) p.getId(),
                            "name", p.getName(),
                            "totalSold", row[1]
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Produtos nunca vendidos
     */
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getNeverSoldProducts() {
        return productOrderRepository.findNeverSoldProducts().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Produtos com baixo uso
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getLowUsageProducts(int minSales) {
        return productRepository.findAll().stream()
                .map(p -> {
                    long sales = productOrderRepository.countByProductId(p.getId());
                    return new AbstractMap.SimpleEntry<>(p, sales);
                })
                .filter(entry -> entry.getValue() < minSales)
                .map(entry -> {
                    Product p = entry.getKey();
                    return Map.of(
                            "id", (Object) p.getId(),
                            "name", p.getName(),
                            "price", p.getPrice(),
                            "sales", entry.getValue()
                    );
                })
                .collect(Collectors.toList());
    }

    // ========== ANÁLISES DE PROMOÇÃO ==========

    /**
     * Produtos em promoção
     */
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getProductsOnPromotion() {
        LocalDate today = LocalDate.now();
        List<Promotion> currentPromos = promotionRepository.findCurrentPromotions(today);

        return currentPromos.stream()
                .flatMap(promo -> promo.getProducts().stream())
                .distinct()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Promoções de um produto
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getProductPromotions(Long productId) {
        findProductOrThrow(productId);
        LocalDate today = LocalDate.now();

        return promotionRepository.findCurrentPromotionsByProductId(productId, today).stream()
                .map(p -> Map.of(
                        "id", (Object) p.getId(),
                        "title", p.getTitle(),
                        "discount", p.getDiscountPercentage(),
                        "startDate", p.getStartDate(),
                        "endDate", p.getEndDate()
                ))
                .collect(Collectors.toList());
    }

    // ========== ANÁLISES DE COMBOS ==========

    /**
     * Combos que contêm o produto
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getProductCombos(Long productId) {
        findProductOrThrow(productId);

        return productComboRepository.findByProductId(productId).stream()
                .map(pc -> Map.of(
                        "comboId", (Object) pc.getCombo().getId(),
                        "comboName", pc.getCombo().getName(),
                        "quantity", pc.getQuantity()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Produtos mais usados em combos
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMostUsedInCombos(int limit) {
        return productComboRepository.findMostUsedProductsInCombos().stream()
                .limit(limit)
                .map(row -> {
                    Product p = (Product) row[0];
                    return Map.of(
                            "name", (Object) p.getName(),
                            "usageCount", row[1]
                    );
                })
                .collect(Collectors.toList());
    }

    // ========== ANÁLISES POR CATEGORIA ==========

    /**
     * Produtos por categoria de preço
     */
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getByPriceCategory(String category) {
        return getByCategory(category, true);
    }

    /**
     * Produtos por categoria
     */
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getByCategory(String category, boolean activeOnly) {
        return productRepository.findByPriceCategory(category.toUpperCase(), activeOnly).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Estatísticas por categoria de preço
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getCategoryStatistics() {
        long economic = productRepository.findByPriceCategory("ECONOMIC", false).size();
        long medium = productRepository.findByPriceCategory("MEDIUM", false).size();
        long premium = productRepository.findByPriceCategory("PREMIUM", false).size();

        return Map.of(
                "ECONOMIC", (long) economic,
                "MEDIUM", (long) medium,
                "PREMIUM", (long) premium
        );
    }

    // ========== ANÁLISES DE INGREDIENTES ==========

    /**
     * Produtos sem ingredientes cadastrados
     */
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getProductsWithoutIngredients() {
        return productRepository.findAll().stream()
                .filter(p -> ingredientProductRepository.findByProduct(p).isEmpty())
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // ========== VALIDAÇÕES ==========

    /**
     * Valida disponibilidade de produtos
     */
    @Transactional(readOnly = true)
    public Map<String, Object> validateAvailability(List<Long> productIds) {
        List<Product> products = productRepository.findAllById(productIds);

        if (products.size() != productIds.size()) {
            throw new ProductNotFoundException("Um ou mais produtos não encontrados", null);
        }

        List<String> unavailable = products.stream()
                .filter(p -> p.getIsPaused())
                .map(Product::getName)
                .collect(Collectors.toList());

        return Map.of(
                "allAvailable", unavailable.isEmpty(),
                "unavailable", unavailable,
                "available", products.size() - unavailable.size()
        );
    }

    /**
     * Verifica se pode deletar produto
     */
    @Transactional(readOnly = true)
    public Map<String, Object> canDeleteProduct(Long id) {
        findProductOrThrow(id);

        long orderCount = productOrderRepository.countByProductId(id);
        long comboCount = productComboRepository.countByProductId(id);
        boolean canDelete = orderCount == 0 && comboCount == 0;

        return Map.of(
                "canDelete", canDelete,
                "orderCount", orderCount,
                "comboCount", comboCount,
                "reason", !canDelete ?
                        "Produto está em " + orderCount + " pedidos e " + comboCount + " combos" :
                        "Pode ser deletado"
        );
    }

    /**
     * Impacto de pausar produto (quantos pedidos em aberto serão afetados)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getPauseImpact(Long id) {
        findProductOrThrow(id);

        // Você precisaria de uma query customizada no ProductOrderRepository
        // Por enquanto, retornar informações básicas
        long totalSold = productOrderRepository.countByProductId(id);
        long comboUsage = productComboRepository.countByProductId(id);

        return Map.of(
                "productId", id,
                "totalTimesSold", totalSold,
                "usedInCombos", comboUsage,
                "impact", totalSold > 100 ? "ALTO" : totalSold > 10 ? "MÉDIO" : "BAIXO"
        );
    }

    // ========== HELPERS PRIVADOS ==========

    /**
     * Busca produto ou lança exceção
     */
    private Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    /**
     * Adiciona ingredientes à lista de um produto
     */
    private void addIngredientsToProduct(Product product, List<String> names) {
        for (String name : names) {
            if (name == null || name.trim().isEmpty()) continue;

            Ingredient ing = ingredientRepository.findByNameIgnoreCase(name.trim())
                    .orElseGet(() -> {
                        Ingredient newIng = new Ingredient();
                        newIng.setName(name.trim());
                        return ingredientRepository.save(newIng);
                    });

            if (!ingredientProductRepository.existsByProductAndIngredient(product, ing)) {
                IngredientProduct ip = new IngredientProduct();
                ip.setProduct(product);
                ip.setIngredient(ing);
                ip.setQuantity(1);
                ip.setMeasurementUnit("unidade");
                ingredientProductRepository.save(ip);
            }
        }
    }

    /**
     * Remove todos os ingredientes de um produto
     */
    private void removeAllIngredientsFromProduct(Product product) {
        List<IngredientProduct> ips = ingredientProductRepository.findByProduct(product);
        ingredientProductRepository.deleteAll(ips);
    }

    /**
     * Converte Product para ProductResponseDTO
     */
    private ProductResponseDTO convertToResponseDTO(Product product) {
        // Buscar ingredientes
        List<ProductResponseDTO.IngredientResponse> ingredients =
                ingredientProductRepository.findByProduct(product).stream()
                        .map(ip -> ProductResponseDTO.IngredientResponse.builder()
                                .id(ip.getIngredient().getId())
                                .name(ip.getIngredient().getName())
                                .quantity(ip.getQuantity())
                                .measurementUnit(ip.getMeasurementUnit())
                                .build())
                        .collect(Collectors.toList());

        // Buscar melhor promoção vigente
        Promotion promo = product.getBestCurrentPromotion();
        ProductResponseDTO.PromotionSummary promoSummary = null;
        if (promo != null) {
            BigDecimal discounted = promo.calculateDiscountedPrice(product.getPrice());
            BigDecimal savings = product.getPrice().subtract(discounted);
            promoSummary = ProductResponseDTO.PromotionSummary.builder()
                    .id(promo.getId())
                    .title(promo.getTitle())
                    .discountPercentage(promo.getDiscountPercentage())
                    .discountAmount(savings)
                    .savings(savings)
                    .build();
        }

        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                // 🔥 AQUI: manda a categoria pro front
                .category(product.getCategory() != null
                        ? product.getCategory().name()
                        : null)
                .subcategory(product.getSubcategory() != null
                        ? product.getSubcategory().getDisplayName()
                        : null)
                .subcategoryCode(product.getSubcategory() != null
                        ? product.getSubcategory().name()
                        : null)
                .ingredients(ingredients)
                .price(product.getPrice())
                .finalPrice(product.getFinalPrice())
                .isOnPromotion(product.isOnPromotion())
                .currentPromotion(promoSummary)
                .isPaused(product.getIsPaused())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }




    /**
     * Exposição pública da conversão Product -> ProductResponseDTO
     * Permite que outros serviços/controllers reutilizem a conversão centralizada
     */
    @Transactional(readOnly = true)
    public ProductResponseDTO convertEntityToDTO(Product product) {
        return convertToResponseDTO(product);
    }
}
