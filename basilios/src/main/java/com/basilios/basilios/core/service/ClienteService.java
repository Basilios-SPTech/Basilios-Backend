package com.basilios.basilios.core.service;

import com.basilios.basilios.core.exception.BusinessException;
import com.basilios.basilios.core.exception.ResourceNotFoundException;
import com.basilios.basilios.core.model.Cliente;
import com.basilios.basilios.core.model.Usuario;
import com.basilios.basilios.infra.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * Lista todos os clientes ativos
     */
    @Transactional(readOnly = true)
    public List<Cliente> listarClientes() {
        return clienteRepository.findAllAtivos();
    }

    /**
     * Busca cliente por ID
     */
    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com ID: " + id));
    }

    /**
     * Busca cliente por ID com seus pedidos carregados
     */
    @Transactional(readOnly = true)
    public Cliente buscarPorIdComPedidos(Long id) {
        return clienteRepository.findByIdWithPedidos(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com ID: " + id));
    }

    /**
     * Busca cliente por email
     */
    @Transactional(readOnly = true)
    public Cliente buscarPorEmail(String email) {
        return clienteRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com email: " + email));
    }

    /**
     * Busca cliente por nomeUsuario
     */
    @Transactional(readOnly = true)
    public Cliente buscarPorNomeUsuario(String nomeUsuario) {
        return clienteRepository.findByNomeUsuario(nomeUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com username: " + nomeUsuario));
    }

    /**
     * Atualiza cliente completamente
     */
    @Transactional
    public Cliente atualizarCliente(Long id, Cliente clienteAtualizado) {
        Cliente clienteExistente = buscarPorId(id);

        // Validar se email já existe em outro cliente
        if (!clienteExistente.getEmail().equals(clienteAtualizado.getEmail()) &&
                clienteRepository.findByEmail(clienteAtualizado.getEmail()).isPresent()) {
            throw new BusinessException("Email já cadastrado para outro cliente");
        }

        // Validar se nomeUsuario já existe em outro cliente
        if (!clienteExistente.getNomeUsuario().equals(clienteAtualizado.getNomeUsuario()) &&
                clienteRepository.findByNomeUsuario(clienteAtualizado.getNomeUsuario()).isPresent()) {
            throw new BusinessException("Nome de usuário já existe");
        }

        // Atualizar campos (não atualizar password diretamente)
        clienteExistente.setNomeUsuario(clienteAtualizado.getNomeUsuario());
        clienteExistente.setEmail(clienteAtualizado.getEmail());
        clienteExistente.setTelefone(clienteAtualizado.getTelefone());
        clienteExistente.setCpf(clienteAtualizado.getCpf());
        clienteExistente.setDataNascimento(clienteAtualizado.getDataNascimento());
        clienteExistente.setEnabled(clienteAtualizado.getEnabled());

        return clienteRepository.save(clienteExistente);
    }

    /**
     * Atualização parcial de cliente
     */
    @Transactional
    public Cliente atualizarParcial(Long id, Map<String, Object> campos) {
        Cliente cliente = buscarPorId(id);

        try {
            for (Map.Entry<String, Object> entry : campos.entrySet()) {
                String nomeCampo = entry.getKey();
                Object valor = entry.getValue();

                // Não permitir atualização de campos sensíveis via PATCH
                if (nomeCampo.equals("id") || nomeCampo.equals("password") ||
                        nomeCampo.equals("roles") || nomeCampo.equals("createdAt") ||
                        nomeCampo.equals("deletedAt") || nomeCampo.equals("pedidos")) {
                    continue;
                }

                // Validações específicas
                if (nomeCampo.equals("email") && valor != null) {
                    String novoEmail = valor.toString();
                    if (!cliente.getEmail().equals(novoEmail) &&
                            clienteRepository.findByEmail(novoEmail).isPresent()) {
                        throw new BusinessException("Email já cadastrado para outro cliente");
                    }
                }

                if (nomeCampo.equals("nomeUsuario") && valor != null) {
                    String novoUsername = valor.toString();
                    if (!cliente.getNomeUsuario().equals(novoUsername) &&
                            clienteRepository.findByNomeUsuario(novoUsername).isPresent()) {
                        throw new BusinessException("Nome de usuário já existe");
                    }
                }

                // Usar reflection para setar o campo na classe pai (Usuario) ou na classe Cliente
                Field field;
                try {
                    field = Cliente.class.getDeclaredField(nomeCampo);
                } catch (NoSuchFieldException e) {
                    // Tentar buscar no pai (Usuario)
                    field = Usuario.class.getDeclaredField(nomeCampo);
                }

                field.setAccessible(true);
                field.set(cliente, valor);
            }

            return clienteRepository.save(cliente);
        } catch (NoSuchFieldException e) {
            throw new BusinessException("Campo inválido: " + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new BusinessException("Erro ao atualizar campo: " + e.getMessage());
        }
    }

    /**
     * Deleta cliente (soft delete)
     */
    @Transactional
    public void deletarCliente(Long id) {
        Cliente cliente = buscarPorId(id);
        cliente.softDelete();
        clienteRepository.save(cliente);
    }

    /**
     * Restaura cliente deletado
     */
    @Transactional
    public Cliente restaurarCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com ID: " + id));

        if (cliente.isAtivo()) {
            throw new BusinessException("Cliente já está ativo");
        }

        cliente.restaurar();
        return clienteRepository.save(cliente);
    }

    /**
     * Conta total de clientes ativos
     */
    @Transactional(readOnly = true)
    public long contarClientesAtivos() {
        return clienteRepository.countAtivos();
    }
}