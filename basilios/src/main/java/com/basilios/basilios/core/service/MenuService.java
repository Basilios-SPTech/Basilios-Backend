package com.basilios.basilios.core.service;

import com.basilios.basilios.core.model.Produto;
import com.basilios.basilios.infra.repository.ProdutoRepository;
import com.basilios.basilios.app.dto.menu.ProdutoDTO;
import com.basilios.basilios.app.dto.menu.MenuFilterDTO;
import com.basilios.basilios.core.exception.ProdutoNotFoundException;
import com.basilios.basilios.core.exception.ProdutoUnavailableException;
import com.basilios.basilios.core.exception.InvalidPriceException;
import com.basilios.basilios.core.exception.DuplicateProdutoException;
import com.basilios.basilios.core.exception.InvalidMenuFilterException;
import com.basilios.basilios.core.exception.MenuOperationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MenuService {

    @Autowired
    private ProdutoRepository produtoRepository;

    /**
     * Retorna apenas produtos ativos (não pausados)
     */
    @Transactional(readOnly = true)
    public List<Produto> getActiveMenu() {
        return produtoRepository.findAll().stream()
                .filter(produto -> !produto.getIsPaused())
                .collect(Collectors.toList());
    }

    /**
     * Retorna todos os produtos
     */
    @Transactional(readOnly = true)
    public List<Produto> getAllMenu() {
        return produtoRepository.findAll();
    }

    /**
     * Retorna produtos com paginação
     */
    @Transactional(readOnly = true)
    public Page<Produto> getMenuPaginated(boolean activeOnly, Pageable pageable) {
        if (activeOnly) {
            return produtoRepository.findByIsPausedFalse(pageable);
        }
        return produtoRepository.findAll(pageable);
    }

    /**
     * Busca produto por ID
     */
    @Transactional(readOnly = true)
    public Produto getProdutoById(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ProdutoNotFoundException(id));
    }

    /**
     * Busca produtos por nome
     */
    @Transactional(readOnly = true)
    public List<Produto> searchByNome(String nome, boolean activeOnly) {
        if (nome != null && nome.trim().length() < 2) {
            throw InvalidMenuFilterException.invalidSearchTerm(nome);
        }

        if (activeOnly) {
            return produtoRepository.findByNomeProdutoContainingIgnoreCaseAndIsPausedFalse(nome);
        }
        return produtoRepository.findByNomeProdutoContainingIgnoreCase(nome);
    }

    /**
     * Busca produtos por faixa de preço
     */
    @Transactional(readOnly = true)
    public List<Produto> getProdutosByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, boolean activeOnly) {
        if (minPrice.compareTo(maxPrice) > 0) {
            throw InvalidMenuFilterException.invalidPriceRange(minPrice, maxPrice);
        }

        if (activeOnly) {
            return produtoRepository.findByPrecoBetweenAndIsPausedFalse(minPrice, maxPrice);
        }
        return produtoRepository.findByPrecoBetween(minPrice, maxPrice);
    }

    /**
     * Busca produtos por ingrediente
     */
    @Transactional(readOnly = true)
    public List<Produto> getProdutosByIngredient(String ingrediente, boolean activeOnly) {
        if (activeOnly) {
            return produtoRepository.findByIngredientesContainingIgnoreCaseAndIsPausedFalse(ingrediente);
        }
        return produtoRepository.findByIngredientesContainingIgnoreCase(ingrediente);
    }

    /**
     * Busca produtos ordenados por preço
     */
    @Transactional(readOnly = true)
    public List<Produto> getProdutosOrderedByPrice(String direction, boolean activeOnly) {
        if (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc")) {
            throw InvalidMenuFilterException.invalidSortDirection(direction);
        }

        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by("preco").descending() :
                Sort.by("preco").ascending();

        List<Produto> produtos = produtoRepository.findAll(sort);

        if (activeOnly) {
            return produtos.stream()
                    .filter(produto -> !produto.getIsPaused())
                    .collect(Collectors.toList());
        }

        return produtos;
    }

    /**
     * Busca com múltiplos filtros
     */
    @Transactional(readOnly = true)
    public List<Produto> getFilteredMenu(MenuFilterDTO filter) {
        return produtoRepository.findWithFilters(
                filter.getNome(),
                filter.getMinPrice(),
                filter.getMaxPrice(),
                filter.getIngredientes(),
                filter.isActiveOnly()
        );
    }

    /**
     * Criar novo produto
     */
    public Produto createProduto(ProdutoDTO produtoDTO) {
        // Verificar se já existe produto com mesmo nome
        if (produtoRepository.existsByNomeProdutoIgnoreCase(produtoDTO.getNomeProduto())) {
            throw new DuplicateProdutoException(produtoDTO.getNomeProduto());
        }

        Produto produto = new Produto();
        produto.setNomeProduto(produtoDTO.getNomeProduto());
        produto.setDescricao(produtoDTO.getDescricao());
        produto.setIngredientes(produtoDTO.getIngredientes());
        produto.setPreco(produtoDTO.getPreco());
        produto.setIsPaused(false);

        return produtoRepository.save(produto);
    }

    /**
     * Atualizar produto existente
     */
    public Produto updateProduto(Long id, ProdutoDTO produtoDTO) {
        Produto produto = getProdutoById(id);

        // Verificar se nome já existe em outro produto
        if (!produto.getNomeProduto().equalsIgnoreCase(produtoDTO.getNomeProduto()) &&
                produtoRepository.existsByNomeProdutoIgnoreCase(produtoDTO.getNomeProduto())) {
            throw new DuplicateProdutoException(produtoDTO.getNomeProduto());
        }

        produto.setNomeProduto(produtoDTO.getNomeProduto());
        produto.setDescricao(produtoDTO.getDescricao());
        produto.setIngredientes(produtoDTO.getIngredientes());
        produto.setPreco(produtoDTO.getPreco());

        return produtoRepository.save(produto);
    }

    /**
     * Pausar produto
     */
    public void pauseProduto(Long id) {
        Produto produto = getProdutoById(id);
        if (produto.getIsPaused()) {
            throw MenuOperationException.cannotPause(id, "Produto já está pausado");
        }
        produto.pausar();
        produtoRepository.save(produto);
    }

    /**
     * Ativar produto
     */
    public void activateProduto(Long id) {
        Produto produto = getProdutoById(id);
        if (!produto.getIsPaused()) {
            throw MenuOperationException.cannotActivate(id, "Produto já está ativo");
        }
        produto.ativar();
        produtoRepository.save(produto);
    }

    /**
     * Alternar status do produto
     */
    public boolean toggleProdutoStatus(Long id) {
        Produto produto = getProdutoById(id);
        produto.alternarStatus();
        produtoRepository.save(produto);
        return produto.getIsPaused();
    }

    /**
     * Deletar produto
     */
    public void deleteProduto(Long id) {
        Produto produto = getProdutoById(id);
        // Verificar se produto pode ser deletado (ex: não possui pedidos pendentes)
        // Esta verificação pode ser implementada conforme regras de negócio
        produtoRepository.delete(produto);
    }

    /**
     * Atualizar preço do produto
     */
    public Produto updateProdutoPrice(Long id, BigDecimal novoPreco) {
        if (novoPreco.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPriceException(novoPreco);
        }

        Produto produto = getProdutoById(id);
        produto.setPreco(novoPreco);
        return produtoRepository.save(produto);
    }

    /**
     * Buscar produtos mais vendidos (implementação básica)
     */
    @Transactional(readOnly = true)
    public List<Produto> getPopularProdutos(int limit) {
        // Esta implementação pode ser melhorada quando houver dados de vendas
        Pageable pageable = PageRequest.of(0, limit);
        return produtoRepository.findByIsPausedFalseOrderByCreatedAtDesc(pageable).getContent();
    }

    /**
     * Contar todos os produtos
     */
    @Transactional(readOnly = true)
    public long countAllProdutos() {
        return produtoRepository.count();
    }

    /**
     * Contar produtos ativos
     */
    @Transactional(readOnly = true)
    public long countActiveProdutos() {
        return produtoRepository.countByIsPausedFalse();
    }

    /**
     * Contar produtos pausados
     */
    @Transactional(readOnly = true)
    public long countPausedProdutos() {
        return produtoRepository.countByIsPausedTrue();
    }

    /**
     * Verificar se produto está disponível
     */
    @Transactional(readOnly = true)
    public boolean isProdutoAvailable(Long id) {
        Produto produto = getProdutoById(id);
        return produto.isAtivo();
    }

    /**
     * Buscar produtos por lista de IDs
     */
    @Transactional(readOnly = true)
    public List<Produto> getProdutosByIds(List<Long> ids) {
        return produtoRepository.findAllById(ids);
    }

    /**
     * Validar se todos os produtos estão disponíveis
     */
    @Transactional(readOnly = true)
    public void validateProdutosAvailability(List<Long> produtoIds) {
        List<Produto> produtos = getProdutosByIds(produtoIds);

        if (produtos.size() != produtoIds.size()) {
            throw new ProdutoNotFoundException("Um ou mais produtos não foram encontrados", null);
        }

        List<String> produtosPausados = produtos.stream()
                .filter(produto -> produto.getIsPaused())
                .map(Produto::getNomeProduto)
                .collect(Collectors.toList());

        if (!produtosPausados.isEmpty()) {
            throw new ProdutoUnavailableException(produtosPausados);
        }
    }

    /**
     * Buscar produtos por categoria de preço
     */
    @Transactional(readOnly = true)
    public List<Produto> getProdutosByPriceCategory(String categoria, boolean activeOnly) {
        return produtoRepository.findByPriceCategory(categoria.toUpperCase(), activeOnly);
    }

    /**
     * Buscar produtos similares
     */
    @Transactional(readOnly = true)
    public List<Produto> getSimilarProdutos(Long produtoId, String keyword, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return produtoRepository.findSimilarProdutos(produtoId, keyword);
    }

    /**
     * Obter estatísticas do menu
     */
    @Transactional(readOnly = true)
    public Object[] getMenuStatistics() {
        return produtoRepository.getMenuStatistics();
    }

    /**
     * Buscar produtos recém adicionados
     */
    @Transactional(readOnly = true)
    public List<Produto> getRecentlyAddedProdutos(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return produtoRepository.findRecentlyAdded(pageable);
    }

    /**
     * Validar disponibilidade de produtos por IDs
     */
    @Transactional(readOnly = true)
    public List<Produto> getAvailableProductsByIds(List<Long> ids) {
        return produtoRepository.findAvailableProductsByIds(ids);
    }

    /**
     * Buscar sugestões de produtos
     */
    @Transactional(readOnly = true)
    public List<Produto> getProdutoSuggestions(Long currentId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return produtoRepository.findSuggestions(currentId, pageable);
    }
}