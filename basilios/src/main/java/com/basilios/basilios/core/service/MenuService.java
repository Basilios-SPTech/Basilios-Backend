package com.basilios.basilios.core.service;

import com.basilios.basilios.app.dto.menu.MenuFilterDTO;
import com.basilios.basilios.app.dto.product.ProductRequestDTO;
import com.basilios.basilios.core.exception.*;
import com.basilios.basilios.core.model.*;
import com.basilios.basilios.infra.observer.MenuSubject;
import com.basilios.basilios.infra.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MenuService - Responsável APENAS por operações de LEITURA do cardápio
 *
 * Operações de ESCRITA (CRUD) foram movidas para ProductService em design novo,
 * mas mantemos compatibilidade adicionando métodos auxiliares que os testes
 * e clientes antigos utilizam.
 */
@Service
@Transactional
public class MenuService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private IngredientProductRepository ingredientProductRepository;

    @Autowired
    private ProductOrderRepository productOrderRepository;

    @Autowired
    private ProductComboRepository productComboRepository;

    @Autowired
    private MenuSubject menuSubject;

    // ========== MENU BÁSICO (leitura) ==========

    @Transactional(readOnly = true)
    public List<Product> getActiveMenu() {
        return productRepository.findByIsPausedFalse();
    }

    @Transactional(readOnly = true)
    public List<Product> getAllMenu() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Product> getMenuPaginated(boolean activeOnly, Pageable pageable) {
        return activeOnly
                ? productRepository.findByIsPausedFalse(pageable)
                : productRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Product> searchByName(String name, boolean activeOnly) {
        if (name == null || name.trim().length() < 2) {
            throw InvalidMenuFilterException.invalidSearchTerm(name);
        }

        return activeOnly
                ? productRepository.findByNameContainingIgnoreCaseAndIsPausedFalse(name)
                : productRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByPriceRange(BigDecimal min, BigDecimal max, boolean activeOnly) {
        if (min == null || max == null || min.compareTo(max) > 0) {
            throw InvalidMenuFilterException.invalidPriceRange(min, max);
        }

        return activeOnly
                ? productRepository.findByPriceBetweenAndIsPausedFalse(min, max)
                : productRepository.findByPriceBetween(min, max);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByIngredient(String ingredientName, boolean activeOnly) {
        if (ingredientName == null || ingredientName.trim().isEmpty()) {
            throw new BusinessException("Nome do ingrediente é obrigatório");
        }

        return activeOnly
                ? productRepository.findByIngredientNameAndActive(ingredientName)
                : productRepository.findByIngredientName(ingredientName);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsOrderedByPrice(String direction, boolean activeOnly) {
        if (direction == null || (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc"))) {
            throw InvalidMenuFilterException.invalidSortDirection(direction);
        }

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by("price").descending()
                : Sort.by("price").ascending();

        List<Product> products = productRepository.findAll(sort);

        if (activeOnly) {
            return products.stream()
                    .filter(Product::isActive)
                    .collect(Collectors.toList());
        }

        return products;
    }

    @Transactional(readOnly = true)
    public List<Product> getFilteredMenu(MenuFilterDTO filter) {
        if (filter == null) {
            return getActiveMenu();
        }

        return productRepository.findWithFilters(
                filter.getName(),
                filter.getMinPrice(),
                filter.getMaxPrice(),
                filter.isActiveOnly()
        );
    }

    @Transactional(readOnly = true)
    public List<Product> getPopularProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        Page<Product> page = productRepository.findByIsPausedFalseOrderByCreatedAtDesc(pageable);
        return page.getContent();
    }

    @Transactional(readOnly = true)
    public List<Product> getSimilarProducts(Long productId, String keyword, int limit) {
        getProductById(productId);
        return productRepository.findSimilarProducts(productId, keyword).stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Product> getProductSuggestions(Long currentProductId, int limit) {
        getProductById(currentProductId);
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findSuggestions(currentProductId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByPriceCategory(String category, boolean activeOnly) {
        if (category == null || category.trim().isEmpty()) {
            throw new BusinessException("Categoria de preço é obrigatória");
        }

        return productRepository.findByPriceCategory(category.toUpperCase(), activeOnly);
    }

    @Transactional(readOnly = true)
    public List<Product> getRecentlyAddedProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findRecentlyAdded(pageable);
    }

    @Transactional(readOnly = true)
    public List<Product> getAvailableProductsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return productRepository.findAvailableProductsByIds(ids);
    }

    @Transactional(readOnly = true)
    public void validateProductsAvailability(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) return;
        List<Product> products = productRepository.findAllById(productIds);
        if (products.size() != productIds.size()) {
            throw new ProductNotFoundException("Um ou mais produtos não encontrados", null);
        }
        List<String> pausedProducts = products.stream()
                .filter(p -> !p.isActive())
                .map(Product::getName)
                .collect(Collectors.toList());
        if (!pausedProducts.isEmpty()) {
            throw new BusinessException("Produtos indisponíveis: " + String.join(", ", pausedProducts));
        }
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return productRepository.findAllById(ids);
    }

    @Transactional(readOnly = true)
    public boolean isProductAvailable(Long id) {
        Product product = getProductById(id);
        return product.isActive();
    }

    @Transactional(readOnly = true)
    public Object[] getMenuStatistics() {
        return productRepository.getMenuStatistics();
    }

    @Transactional(readOnly = true)
    public long countAllProducts() {
        return productRepository.count();
    }

    @Transactional(readOnly = true)
    public long countActiveProducts() {
        return productRepository.countByIsPausedFalse();
    }

    @Transactional(readOnly = true)
    public long countPausedProducts() {
        return productRepository.countByIsPausedTrue();
    }

    // ========== COMPATIBILITY / WRAPPER METHODS (para testes antigos) ==========

    /**
     * Cria um produto a partir de um DTO genérico (usado nos testes que fornecem um ProductDTO de teste)
     */
    @Transactional
    public Product createProduct(Object dto) {
        String name = safeGetString(dto, "getName", "name");
        if (name == null) throw new BusinessException("Nome é obrigatório");

        if (productRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateProductException(name);
        }

        Product p = Product.builder()
                .name(name)
                .description(safeGetString(dto, "getDescription", "description"))
                .price(safeGetBigDecimal(dto, "getPrice", "price", BigDecimal.ZERO))
                .category(safeGetEnum(dto, "getCategory", "category", null, null))
                .subcategory(safeGetEnum(dto, "getSubcategory", "subcategory", null, null))
                .tags(safeGetStringList(dto, "getTags", "tags"))
                .isPaused(safeGetBoolean(dto, "getIsPaused", "isPaused", false))
                .build();

        p = productRepository.save(p);

        // ingredientes
        List<String> ingredientes = safeGetStringList(dto, "getIngredientes", "ingredientes");
        if (ingredientes != null && !ingredientes.isEmpty()) {
            addIngredientsToProduct(p, ingredientes);
        }

        menuSubject.menuChanged("PRODUCT_CREATED", p);
        return p;
    }

    @Transactional
    public Product updateProduct(Long id, Object dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        String name = safeGetString(dto, "getName", "name");
        if (name != null && !name.equalsIgnoreCase(product.getName()) && productRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateProductException(name);
        }

        if (name != null) product.setName(name);
        String desc = safeGetString(dto, "getDescription", "description");
        if (desc != null) product.setDescription(desc);
        BigDecimal price = safeGetBigDecimal(dto, "getPrice", "price", null);
        if (price != null) product.setPrice(price);
        List<String> tags = safeGetStringList(dto, "getTags", "tags");
        if (tags != null) product.setTags(tags);

        // ingredientes
        List<String> ingredientes = safeGetStringList(dto, "getIngredientes", "ingredientes");
        if (ingredientes != null) {
            // remove all and add
            removeAllIngredientsFromProduct(product);
            if (!ingredientes.isEmpty()) addIngredientsToProduct(product, ingredientes);
        }

        product = productRepository.save(product);
        menuSubject.menuChanged("PRODUCT_UPDATED", product);
        return product;
    }

    @Transactional
    public Product pauseProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
        if (product.getIsPaused()) throw MenuOperationException.cannotPause(id, "Produto já está pausado");
        product.pause();
        product = productRepository.save(product);
        menuSubject.menuChanged("PRODUCT_PAUSED", product);
        return product;
    }

    @Transactional
    public Product activateProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
        if (!product.getIsPaused()) throw MenuOperationException.cannotActivate(id, "Produto já está ativo");
        product.activate();
        product = productRepository.save(product);
        menuSubject.menuChanged("PRODUCT_ACTIVATED", product);
        return product;
    }

    @Transactional
    public boolean toggleProductStatus(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
        if (product.getIsPaused()) product.activate(); else product.pause();
        productRepository.save(product);
        menuSubject.menuChanged("PRODUCT_TOGGLED", product);
        return !product.getIsPaused();
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));

        long orderCount = productOrderRepository.countByProductId(id);
        if (orderCount > 0) throw new BusinessException("Produto não pode ser deletado. Está em " + orderCount + " pedidos");

        long comboCount = productComboRepository.countByProductId(id);
        if (comboCount > 0) throw new BusinessException("Produto não pode ser deletado. Está em " + comboCount + " combos");

        productRepository.delete(product);
        menuSubject.menuChanged("PRODUCT_DELETED", id);
    }

    @Transactional
    public Product updateProductPrice(Long id, BigDecimal newPrice) {
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) <= 0) throw new InvalidPriceException(newPrice);
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
        product.setPrice(newPrice);
        product = productRepository.save(product);
        menuSubject.menuChanged("PRODUCT_PRICE_UPDATED", product);
        return product;
    }

    @Transactional
    public Product addIngredientsToProduct(Product product, List<String> names) {
        if (names == null || names.isEmpty()) return product;
        for (String name : names) {
            if (name == null || name.trim().isEmpty()) continue;
            Ingredient ing = ingredientRepository.findByNameIgnoreCase(name.trim()).orElseGet(() -> {
                Ingredient i = new Ingredient(); i.setName(name.trim()); return ingredientRepository.save(i);
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
        menuSubject.menuChanged("PRODUCT_INGREDIENTS_ADDED", product);
        return product;
    }

    @Transactional
    public Product addIngredientToProduct(Long productId, String name, int qty, String unit) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));
        if (name == null || name.trim().isEmpty()) throw new BusinessException("Nome do ingrediente é obrigatório");
        Ingredient ing = ingredientRepository.findByNameIgnoreCase(name.trim()).orElseGet(() -> { Ingredient i = new Ingredient(); i.setName(name.trim()); return ingredientRepository.save(i); });
        if (ingredientProductRepository.existsByProductAndIngredient(product, ing)) throw new BusinessException("Ingrediente já adicionado ao produto");
        IngredientProduct ip = new IngredientProduct(); ip.setProduct(product); ip.setIngredient(ing); ip.setQuantity(qty); ip.setMeasurementUnit(unit != null ? unit : "unidade");
        ingredientProductRepository.save(ip);
        menuSubject.menuChanged("PRODUCT_INGREDIENT_ADDED", product);
        return product;
    }

    @Transactional
    public Product removeIngredientFromProduct(Long productId, Long ingredientId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));
        Ingredient ingredient = ingredientRepository.findById(ingredientId).orElseThrow(() -> new NotFoundException("Ingrediente não encontrado: " + ingredientId));
        IngredientProduct ip = ingredientProductRepository.findByProductAndIngredient(product, ingredient).orElseThrow(() -> new BusinessException("Ingrediente não está associado ao produto"));
        ingredientProductRepository.delete(ip);
        menuSubject.menuChanged("PRODUCT_INGREDIENT_REMOVED", product);
        return product;
    }

    @Transactional
    public void removeAllIngredientsFromProduct(Product product) {
        List<IngredientProduct> ips = ingredientProductRepository.findByProduct(product);
        ingredientProductRepository.deleteAll(ips);
        menuSubject.menuChanged("PRODUCT_INGREDIENTS_CLEARED", product);
    }

    @Transactional
    public Product updateIngredientQuantity(Long productId, Long ingredientId, int qty, String unit) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));
        Ingredient ingredient = ingredientRepository.findById(ingredientId).orElseThrow(() -> new NotFoundException("Ingrediente não encontrado: " + ingredientId));
        IngredientProduct ip = ingredientProductRepository.findByProductAndIngredient(product, ingredient).orElseThrow(() -> new BusinessException("Ingrediente não está associado ao produto"));
        ip.setQuantity(qty);
        if (unit != null) ip.setMeasurementUnit(unit);
        ingredientProductRepository.save(ip);
        menuSubject.menuChanged("PRODUCT_INGREDIENT_QUANTITY_UPDATED", product);
        return product;
    }

    @Transactional(readOnly = true)
    public List<IngredientProduct> getProductIngredients(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));
        return ingredientProductRepository.findByProduct(product);
    }

    @Transactional(readOnly = true)
    public long countProductsWithIngredient(Long ingredientId) {
        return ingredientRepository.countProductsUsingIngredient(ingredientId);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsWithoutIngredients() {
        return productRepository.findAll().stream()
                .filter(p -> ingredientProductRepository.findByProduct(p).isEmpty())
                .collect(Collectors.toList());
    }

    // ========== HELPERS PRIVADOS PARA REFLECTION ==========

    private String safeGetString(Object dto, String getter, String fieldName) {
        try {
            Method m = dto.getClass().getMethod(getter);
            Object v = m.invoke(dto);
            return v != null ? v.toString() : null;
        } catch (Exception e) {
            // tentar campo público
            try {
                Object v = dto.getClass().getField(fieldName).get(dto);
                return v != null ? v.toString() : null;
            } catch (Exception ex) {
                return null;
            }
        }
    }

    private java.math.BigDecimal safeGetBigDecimal(Object dto, String getter, String fieldName, java.math.BigDecimal def) {
        try {
            Method m = dto.getClass().getMethod(getter);
            Object v = m.invoke(dto);
            if (v instanceof java.math.BigDecimal) return (java.math.BigDecimal) v;
            if (v instanceof Number) return java.math.BigDecimal.valueOf(((Number) v).doubleValue());
            if (v instanceof String) return new java.math.BigDecimal((String) v);
        } catch (Exception ignored) {}
        try { Object v = dto.getClass().getField(fieldName).get(dto); if (v instanceof java.math.BigDecimal) return (java.math.BigDecimal) v; } catch (Exception ignored) {}
        return def;
    }

    @SuppressWarnings("unchecked")
    private List<String> safeGetStringList(Object dto, String getter, String fieldName) {
        try {
            Method m = dto.getClass().getMethod(getter);
            Object v = m.invoke(dto);
            if (v instanceof List) return (List<String>) v;
        } catch (Exception ignored) {}
        try { Object v = dto.getClass().getField(fieldName).get(dto); if (v instanceof List) return (List<String>) v; } catch (Exception ignored) {}
        return List.of();
    }

    private <E extends Enum<E>> E safeGetEnum(Object dto, String getter, String fieldName, Class<E> enumClass, E def) {
        try {
            Method m = dto.getClass().getMethod(getter);
            Object v = m.invoke(dto);
            if (v != null && enumClass != null) return (E) v;
        } catch (Exception ignored) {}
        return def;
    }

    private boolean safeGetBoolean(Object dto, String getter, String fieldName, boolean def) {
        try {
            Method m = dto.getClass().getMethod(getter);
            Object v = m.invoke(dto);
            if (v instanceof Boolean) return (Boolean) v;
        } catch (Exception ignored) {}
        try { Object v = dto.getClass().getField(fieldName).get(dto); if (v instanceof Boolean) return (Boolean) v; } catch (Exception ignored) {}
        return def;
    }
}
