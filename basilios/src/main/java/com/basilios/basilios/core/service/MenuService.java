package com.basilios.basilios.core.service;

import com.basilios.basilios.core.enums.ProductCategory;
import com.basilios.basilios.core.enums.ProductSubcategory;
import com.basilios.basilios.core.model.Product;
import com.basilios.basilios.core.model.Ingredient;
import com.basilios.basilios.core.model.IngredientProduct;
import com.basilios.basilios.infra.repository.ProductRepository;
import com.basilios.basilios.infra.repository.IngredientRepository;
import com.basilios.basilios.infra.repository.IngredientProductRepository;
import com.basilios.basilios.app.dto.product.ProductDTO;
import com.basilios.basilios.app.dto.menu.MenuFilterDTO;
import com.basilios.basilios.core.exception.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.basilios.basilios.infra.observer.MenuSubject;

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
    private MenuSubject menuSubject;

    // ========== MENU ATIVO ==========

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

    // ========== BUSCA POR ID ==========

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    // ========== BUSCAS ==========

    @Transactional(readOnly = true)
    public List<Product> searchByName(String name, boolean activeOnly) {
        if (name != null && name.trim().length() < 2) {
            throw InvalidMenuFilterException.invalidSearchTerm(name);
        }
        return activeOnly
                ? productRepository.findByNameContainingIgnoreCaseAndIsPausedFalse(name)
                : productRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByPriceRange(BigDecimal min, BigDecimal max, boolean activeOnly) {
        if (min.compareTo(max) > 0) {
            throw InvalidMenuFilterException.invalidPriceRange(min, max);
        }
        return activeOnly
                ? productRepository.findByPriceBetweenAndIsPausedFalse(min, max)
                : productRepository.findByPriceBetween(min, max);
    }

    /**
     * Busca produtos por ingrediente (usando relacionamento correto)
     */
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
        if (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc")) {
            throw InvalidMenuFilterException.invalidSortDirection(direction);
        }

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by("price").descending()
                : Sort.by("price").ascending();

        List<Product> products = productRepository.findAll(sort);
        if (activeOnly) {
            return products.stream().filter(Product::isActive).collect(Collectors.toList());
        }
        return products;
    }

    @Transactional(readOnly = true)
    public List<Product> getFilteredMenu(MenuFilterDTO filter) {
        return productRepository.findWithFilters(
                filter.getName(),
                filter.getMinPrice(),
                filter.getMaxPrice(),
                filter.isActiveOnly()
        );
    }

    // ========== CRUD DE PRODUTOS ==========

    /**
     * Cria produto com ingredientes
     */
    public Product createProduct(ProductDTO dto) {
        if (productRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new DuplicateProductException(dto.getName());
        }

        // Valida enums obrigatórios
        ProductCategory category = dto.getCategory();
        if (category == null) {
            throw new BusinessException("Categoria é obrigatória");
        }

        ProductSubcategory subcategory = dto.getSubcategory();
        // (Opcional) valida coerência categoria x subcategoria
        if (subcategory != null && subcategory.getCategory() != category) {
            throw new BusinessException("Subcategoria não pertence à categoria informada");
        }

        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .category(category)                       // <<=== ESSENCIAL
                .subcategory(subcategory)                 // <<=== ESSENCIAL
                .tags(dto.getTags() != null ? dto.getTags() : new ArrayList<>())
                .isPaused(Boolean.TRUE.equals(dto.getIsPaused()))
                .build();

        product = productRepository.save(product);

        // Adicionar ingredientes se fornecidos
        if (dto.getIngredientes() != null && !dto.getIngredientes().isEmpty()) {
            addIngredientsToProduct(product, dto.getIngredientes());
        }

        // Notificar observadores que um produto foi criado
        try {
            menuSubject.menuChanged("PRODUCT_CREATED", product);
        } catch (Exception ex) {
            // manter comportamento original mesmo se notificação falhar
        }

        return product;
    }

    /**
     * Atualiza produto e seus ingredientes
     */
    public Product updateProduct(Long id, ProductDTO dto) {
        Product product = getProductById(id);

        if (!product.getName().equalsIgnoreCase(dto.getName()) &&
                productRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new DuplicateProductException(dto.getName());
        }

        // Valida enums (categoria obrigatória no update também)
        ProductCategory category = dto.getCategory();
        if (category == null) {
            throw new BusinessException("Categoria é obrigatória");
        }

        ProductSubcategory subcategory = dto.getSubcategory();
        if (subcategory != null && subcategory.getCategory() != category) {
            throw new BusinessException("Subcategoria não pertence à categoria informada");
        }

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setCategory(category);             // <<=== GARANTE NO UPDATE
        product.setSubcategory(subcategory);       // <<=== GARANTE NO UPDATE

        product = productRepository.save(product);

        // Notificar observadores sobre atualização
        try {
            menuSubject.menuChanged("PRODUCT_UPDATED", product);
        } catch (Exception ex) {
            // ignora falha na notificação
        }

        // Atualizar ingredientes se fornecidos
        if (dto.getIngredientes() != null) {
            // Remover ingredientes antigos
            removeAllIngredientsFromProduct(product);

            // Adicionar novos ingredientes
            if (!dto.getIngredientes().isEmpty()) {
                addIngredientsToProduct(product, dto.getIngredientes());
            }
        }

        return product;
    }

    public void pauseProduct(Long id) {
        Product product = getProductById(id);
        if (product.getIsPaused()) {
            throw MenuOperationException.cannotPause(id, "Product already paused");
        }
        product.pause();
        productRepository.save(product);

        try {
            menuSubject.menuChanged("PRODUCT_PAUSED", product);
        } catch (Exception ex) {
            // ignore
        }
    }

    public void activateProduct(Long id) {
        Product product = getProductById(id);
        if (!product.getIsPaused()) {
            throw MenuOperationException.cannotActivate(id, "Product already active");
        }
        product.activate();
        productRepository.save(product);

        try {
            menuSubject.menuChanged("PRODUCT_ACTIVATED", product);
        } catch (Exception ex) {
            // ignore
        }
    }

    public boolean toggleProductStatus(Long id) {
        Product product = getProductById(id);
        product.toggleStatus();
        productRepository.save(product);

        try {
            menuSubject.menuChanged("PRODUCT_TOGGLED", product);
        } catch (Exception ex) {
            // ignore
        }
        return product.getIsPaused();
    }

    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);

        try {
            menuSubject.menuChanged("PRODUCT_DELETED", id);
        } catch (Exception ex) {
            // ignore
        }
    }

    public Product updateProductPrice(Long id, BigDecimal newPrice) {
        if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPriceException(newPrice);
        }
        Product product = getProductById(id);
        product.setPrice(newPrice);
        Product saved = productRepository.save(product);

        try {
            menuSubject.menuChanged("PRODUCT_PRICE_UPDATED", saved);
        } catch (Exception ex) {
            // ignore
        }
        return saved;
    }

    // ========== GERENCIAMENTO DE INGREDIENTES ==========

    /**
     * Adiciona ingredientes a um produto (aceita lista de nomes)
     */
    @Transactional
    public Product addIngredientsToProduct(Product product, List<String> ingredientNames) {
        if (ingredientNames == null || ingredientNames.isEmpty()) {
            return product;
        }

        for (String ingredientName : ingredientNames) {
            if (ingredientName == null || ingredientName.trim().isEmpty()) {
                continue;
            }

            // Buscar ou criar ingrediente
            Ingredient ingredient = ingredientRepository.findByNameIgnoreCase(ingredientName.trim())
                    .orElseGet(() -> {
                        Ingredient newIngredient = new Ingredient(ingredientName.trim());
                        return ingredientRepository.save(newIngredient);
                    });

            // Verificar se já existe este relacionamento
            if (!ingredientProductRepository.existsByProductAndIngredient(product, ingredient)) {
                // Criar relacionamento
                IngredientProduct ip = new IngredientProduct();
                ip.setProduct(product);
                ip.setIngredient(ingredient);
                ip.setQuantity(1); // Default
                ip.setMeasurementUnit("unidade"); // Default

                ingredientProductRepository.save(ip);
            }
        }

        try {
            // notifica que ingredientes do produto foram modificados
            menuSubject.menuChanged("PRODUCT_INGREDIENTS_UPDATED", product);
        } catch (Exception ex) {
            // ignore
        }
        return product;
    }

    /**
     * Adiciona um ingrediente específico com quantidade e unidade
     */
    @Transactional
    public Product addIngredientToProduct(Long productId, String ingredientName,
                                          Integer quantity, String measurementUnit) {
        Product product = getProductById(productId);

        if (ingredientName == null || ingredientName.trim().isEmpty()) {
            throw new BusinessException("Nome do ingrediente é obrigatório");
        }

        // Buscar ou criar ingrediente
        Ingredient ingredient = ingredientRepository.findByNameIgnoreCase(ingredientName.trim())
                .orElseGet(() -> {
                    Ingredient newIngredient = new Ingredient(ingredientName.trim());
                    return ingredientRepository.save(newIngredient);
                });

        // Verificar se já existe
        if (ingredientProductRepository.existsByProductAndIngredient(product, ingredient)) {
            throw new BusinessException("Ingrediente já adicionado ao produto");
        }

        // Criar relacionamento
        IngredientProduct ip = new IngredientProduct();
        ip.setProduct(product);
        ip.setIngredient(ingredient);
        ip.setQuantity(quantity != null ? quantity : 1);
        ip.setMeasurementUnit(measurementUnit != null ? measurementUnit : "unidade");

        ingredientProductRepository.save(ip);

        try {
            menuSubject.menuChanged("PRODUCT_INGREDIENT_ADDED", product);
        } catch (Exception ex) {
            // ignore
        }
        return productRepository.findById(productId).get();
    }

    /**
     * Remove um ingrediente de um produto
     */
    @Transactional
    public Product removeIngredientFromProduct(Long productId, Long ingredientId) {
        Product product = getProductById(productId);

        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new NotFoundException("Ingrediente não encontrado: " + ingredientId));

        IngredientProduct ip = ingredientProductRepository.findByProductAndIngredient(product, ingredient)
                .orElseThrow(() -> new BusinessException("Ingrediente não está associado ao produto"));

        ingredientProductRepository.delete(ip);

        try {
            menuSubject.menuChanged("PRODUCT_INGREDIENT_REMOVED", product);
        } catch (Exception ex) {
            // ignore
        }
        return productRepository.findById(productId).get();
    }

    /**
     * Remove todos os ingredientes de um produto
     */
    @Transactional
    public void removeAllIngredientsFromProduct(Product product) {
        List<IngredientProduct> ingredientProducts =
                ingredientProductRepository.findByProduct(product);

        ingredientProductRepository.deleteAll(ingredientProducts);

        try {
            menuSubject.menuChanged("PRODUCT_INGREDIENTS_CLEARED", product);
        } catch (Exception ex) {
            // ignore
        }
    }

    /**
     * Atualiza quantidade de um ingrediente em um produto
     */
    @Transactional
    public Product updateIngredientQuantity(Long productId, Long ingredientId,
                                            Integer quantity, String measurementUnit) {
        Product product = getProductById(productId);

        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new NotFoundException("Ingrediente não encontrado: " + ingredientId));

        IngredientProduct ip = ingredientProductRepository.findByProductAndIngredient(product, ingredient)
                .orElseThrow(() -> new BusinessException("Ingrediente não está associado ao produto"));

        if (quantity != null) {
            ip.setQuantity(quantity);
        }
        if (measurementUnit != null) {
            ip.setMeasurementUnit(measurementUnit);
        }

        ingredientProductRepository.save(ip);

        try {
            menuSubject.menuChanged("PRODUCT_INGREDIENT_QUANTITY_UPDATED", product);
        } catch (Exception ex) {
            // ignore
        }
        return productRepository.findById(productId).get();
    }

    /**
     * Lista todos os ingredientes de um produto
     */
    @Transactional(readOnly = true)
    public List<IngredientProduct> getProductIngredients(Long productId) {
        Product product = getProductById(productId);
        return ingredientProductRepository.findByProduct(product);
    }

    // ========== ESTATÍSTICAS E RELATÓRIOS ==========

    @Transactional(readOnly = true)
    public List<Product> getPopularProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findByIsPausedFalseOrderByCreatedAtDesc(pageable).getContent();
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

    @Transactional(readOnly = true)
    public boolean isProductAvailable(Long id) {
        Product product = getProductById(id);
        return product.isActive();
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByIds(List<Long> ids) {
        return productRepository.findAllById(ids);
    }

    @Transactional(readOnly = true)
    public void validateProductsAvailability(List<Long> productIds) {
        List<Product> products = getProductsByIds(productIds);

        if (products.size() != productIds.size()) {
            throw new ProductNotFoundException("One or more products not found", null);
        }

        List<String> pausedProducts = products.stream()
                .filter(p -> !p.isActive())
                .map(Product::getName)
                .collect(Collectors.toList());

        if (!pausedProducts.isEmpty()) {
            throw new ProductUnavailableException(pausedProducts);
        }
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByPriceCategory(String category, boolean activeOnly) {
        return productRepository.findByPriceCategory(category.toUpperCase(), activeOnly);
    }

    @Transactional(readOnly = true)
    public List<Product> getSimilarProducts(Long productId, String keyword, int limit) {
        return productRepository.findSimilarProducts(productId, keyword);
    }

    @Transactional(readOnly = true)
    public Object[] getMenuStatistics() {
        return productRepository.getMenuStatistics();
    }

    @Transactional(readOnly = true)
    public List<Product> getRecentlyAddedProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findRecentlyAdded(pageable);
    }

    @Transactional(readOnly = true)
    public List<Product> getAvailableProductsByIds(List<Long> ids) {
        return productRepository.findAvailableProductsByIds(ids);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductSuggestions(Long currentId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findSuggestions(currentId, pageable);
    }

    /**
     * Conta quantos produtos usam um ingrediente específico
     */
    @Transactional(readOnly = true)
    public long countProductsWithIngredient(Long ingredientId) {
        return ingredientRepository.countProductsUsingIngredient(ingredientId);
    }

    /**
     * Lista produtos que NÃO têm ingredientes cadastrados
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsWithoutIngredients() {
        List<Product> allProducts = productRepository.findAll();
        return allProducts.stream()
                .filter(p -> ingredientProductRepository.findByProduct(p).isEmpty())
                .collect(Collectors.toList());
    }
}
