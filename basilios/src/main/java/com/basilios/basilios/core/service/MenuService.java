package com.basilios.basilios.core.service;

import com.basilios.basilios.core.model.Product;
import com.basilios.basilios.infra.repository.ProductRepository;
import com.basilios.basilios.app.dto.menu.ProductDTO;
import com.basilios.basilios.app.dto.menu.MenuFilterDTO;
import com.basilios.basilios.core.exception.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MenuService {

    @Autowired
    private ProductRepository productRepository;

    // ========== Active Menu ==========
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

    // ========== Find by ID ==========
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    // ========== Search ==========
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

    @Transactional(readOnly = true)
    public List<Product> getProductsByIngredient(String ingredient, boolean activeOnly) {
        return activeOnly
                ? productRepository.findByIngredientsContainingIgnoreCaseAndIsPausedFalse(ingredient)
                : productRepository.findByIngredientsContainingIgnoreCase(ingredient);
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
                filter.getIngredients(),
                filter.isActiveOnly()
        );
    }

    // ========== CRUD ==========
    public Product createProduct(ProductDTO dto) {
        if (productRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new DuplicateProductException(dto.getName());
        }

        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setIsPaused(false);

        return productRepository.save(product);
    }

    public Product updateProduct(Long id, ProductDTO dto) {
        Product product = getProductById(id);

        if (!product.getName().equalsIgnoreCase(dto.getName()) &&
                productRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new DuplicateProductException(dto.getName());
        }

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());

        return productRepository.save(product);
    }

    public void pauseProduct(Long id) {
        Product product = getProductById(id);
        if (product.getIsPaused()) throw MenuOperationException.cannotPause(id, "Product already paused");
        product.pause();
        productRepository.save(product);
    }

    public void activateProduct(Long id) {
        Product product = getProductById(id);
        if (!product.getIsPaused()) throw MenuOperationException.cannotActivate(id, "Product already active");
        product.activate();
        productRepository.save(product);
    }

    public boolean toggleProductStatus(Long id) {
        Product product = getProductById(id);
        product.toggleStatus();
        productRepository.save(product);
        return product.getIsPaused();
    }

    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }

    public Product updateProductPrice(Long id, BigDecimal newPrice) {
        if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPriceException(newPrice);
        }
        Product product = getProductById(id);
        product.setPrice(newPrice);
        return productRepository.save(product);
    }

    // ========== Popular & Stats ==========
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
        Pageable pageable = PageRequest.of(0, limit);
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
}
