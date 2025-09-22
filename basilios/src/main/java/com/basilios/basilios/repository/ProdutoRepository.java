package com.basilios.basilios.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.basilios.basilios.model.Produto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    // ========== Buscas por Status ==========

    /**
     * Busca produtos ativos (não pausados)
     */
    List<Produto> findByIsPausedFalse();

    /**
     * Busca produtos pausados
     */
    List<Produto> findByIsPausedTrue();

    /**
     * Busca produtos ativos com paginação
     */
    Page<Produto> findByIsPausedFalse(Pageable pageable);

    // ========== Buscas por Nome ==========

    /**
     * Busca por nome exato (case insensitive)
     */
    Optional<Produto> findByNomeProdutoIgnoreCase(String nomeProduto);

    /**
     * Busca por nome contendo texto (case insensitive)
     */
    List<Produto> findByNomeProdutoContainingIgnoreCase(String nome);

    /**
     * Busca por nome contendo texto apenas entre produtos ativos
     */
    List<Produto> findByNomeProdutoContainingIgnoreCaseAndIsPausedFalse(String nome);

    /**
     * Verifica se existe produto com nome específico
     */
    boolean existsByNomeProdutoIgnoreCase(String nomeProduto);

    // ========== Buscas por Preço ==========

    /**
     * Busca produtos por faixa de preço
     */
    List<Produto> findByPrecoBetween(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Busca produtos ativos por faixa de preço
     */
    List<Produto> findByPrecoBetweenAndIsPausedFalse(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Busca produtos com preço menor ou igual
     */
    List<Produto> findByPrecoLessThanEqual(BigDecimal maxPrice);

    /**
     * Busca produtos ativos com preço menor ou igual
     */
    List<Produto> findByPrecoLessThanEqualAndIsPausedFalse(BigDecimal maxPrice);

    /**
     * Busca produtos com preço maior ou igual
     */
    List<Produto> findByPrecoGreaterThanEqual(BigDecimal minPrice);

    /**
     * Busca produtos ativos com preço maior ou igual
     */
    List<Produto> findByPrecoGreaterThanEqualAndIsPausedFalse(BigDecimal minPrice);

    // ========== Buscas por Ingredientes ==========

    /**
     * Busca produtos que contêm ingrediente específico (usando JSON_CONTAINS para MySQL)
     */
    @Query(value = "SELECT * FROM produto p WHERE JSON_CONTAINS(p.ingredientes, JSON_QUOTE(:ingrediente))",
            nativeQuery = true)
    List<Produto> findByIngredientesContaining(@Param("ingrediente") String ingrediente);

    /**
     * Busca produtos ativos que contêm ingrediente específico
     */
    @Query(value = "SELECT * FROM produto p WHERE JSON_CONTAINS(p.ingredientes, JSON_QUOTE(:ingrediente)) AND p.is_paused = false",
            nativeQuery = true)
    List<Produto> findByIngredientesContainingAndIsPausedFalse(@Param("ingrediente") String ingrediente);

    /**
     * Busca por ingrediente usando LIKE para compatibilidade
     */
    @Query("SELECT p FROM Produto p WHERE LOWER(CAST(p.ingredientes AS string)) LIKE LOWER(CONCAT('%', :ingrediente, '%'))")
    List<Produto> findByIngredientesContainingIgnoreCase(@Param("ingrediente") String ingrediente);

    /**
     * Busca produtos ativos por ingrediente usando LIKE
     */
    @Query("SELECT p FROM Produto p WHERE LOWER(CAST(p.ingredientes AS string)) LIKE LOWER(CONCAT('%', :ingrediente, '%')) AND p.isPaused = false")
    List<Produto> findByIngredientesContainingIgnoreCaseAndIsPausedFalse(@Param("ingrediente") String ingrediente);

    // ========== Ordenações ==========

    /**
     * Busca produtos ativos ordenados por data de criação (mais recentes primeiro)
     */
    Page<Produto> findByIsPausedFalseOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Busca produtos ordenados por preço crescente
     */
    List<Produto> findByOrderByPrecoAsc();

    /**
     * Busca produtos ativos ordenados por preço crescente
     */
    List<Produto> findByIsPausedFalseOrderByPrecoAsc();

    /**
     * Busca produtos ordenados por preço decrescente
     */
    List<Produto> findByOrderByPrecoDesc();

    /**
     * Busca produtos ativos ordenados por preço decrescente
     */
    List<Produto> findByIsPausedFalseOrderByPrecoDesc();

    /**
     * Busca produtos ordenados por nome
     */
    List<Produto> findByOrderByNomeProdutoAsc();

    // ========== Contadores ==========

    /**
     * Conta produtos ativos
     */
    long countByIsPausedFalse();

    /**
     * Conta produtos pausados
     */
    long countByIsPausedTrue();

    /**
     * Conta produtos por faixa de preço
     */
    long countByPrecoBetween(BigDecimal minPrice, BigDecimal maxPrice);

    // ========== Consultas Personalizadas ==========

    /**
     * Busca produtos com múltiplos filtros
     */
    @Query("SELECT p FROM Produto p WHERE " +
            "(:nome IS NULL OR LOWER(p.nomeProduto) LIKE LOWER(CONCAT('%', :nome, '%'))) AND " +
            "(:minPrice IS NULL OR p.preco >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.preco <= :maxPrice) AND " +
            "(:activeOnly = false OR p.isPaused = false)")
    List<Produto> findWithFilters(@Param("nome") String nome,
                                  @Param("minPrice") BigDecimal minPrice,
                                  @Param("maxPrice") BigDecimal maxPrice,
                                  @Param("ingredientes") List<String> ingredientes,
                                  @Param("activeOnly") boolean activeOnly);

    /**
     * Busca produtos similares por nome
     */
    @Query("SELECT p FROM Produto p WHERE p.idProduto != :id AND " +
            "LOWER(p.nomeProduto) LIKE LOWER(CONCAT('%', :keyword, '%')) AND " +
            "p.isPaused = false")
    List<Produto> findSimilarProdutos(@Param("id") Long id, @Param("keyword") String keyword);

    /**
     * Busca produtos por categoria de preço
     */
    @Query("SELECT p FROM Produto p WHERE " +
            "CASE " +
            "WHEN :categoria = 'ECONOMICO' THEN p.preco <= 15.0 " +
            "WHEN :categoria = 'MEDIO' THEN p.preco > 15.0 AND p.preco <= 30.0 " +
            "WHEN :categoria = 'PREMIUM' THEN p.preco > 30.0 " +
            "ELSE true END " +
            "AND (:activeOnly = false OR p.isPaused = false)")
    List<Produto> findByPriceCategory(@Param("categoria") String categoria,
                                      @Param("activeOnly") boolean activeOnly);

    /**
     * Estatísticas de produtos
     */
    @Query("SELECT " +
            "COUNT(p) as total, " +
            "SUM(CASE WHEN p.isPaused = false THEN 1 ELSE 0 END) as ativos, " +
            "SUM(CASE WHEN p.isPaused = true THEN 1 ELSE 0 END) as pausados, " +
            "AVG(p.preco) as precoMedio, " +
            "MIN(p.preco) as menorPreco, " +
            "MAX(p.preco) as maiorPreco " +
            "FROM Produto p")
    Object[] getMenuStatistics();

    /**
     * Produtos recentemente adicionados
     */
    @Query("SELECT p FROM Produto p WHERE p.isPaused = false ORDER BY p.createdAt DESC")
    List<Produto> findRecentlyAdded(Pageable pageable);

    /**
     * Validar disponibilidade de múltiplos produtos
     */
    @Query("SELECT p FROM Produto p WHERE p.idProduto IN :ids AND p.isPaused = false")
    List<Produto> findAvailableProductsByIds(@Param("ids") List<Long> ids);

    /**
     * Buscar produtos para sugestões (excluindo o produto atual)
     */
    @Query("SELECT p FROM Produto p WHERE p.idProduto != :currentId AND p.isPaused = false " +
            "ORDER BY p.createdAt DESC")
    List<Produto> findSuggestions(@Param("currentId") Long currentId, Pageable pageable);
}