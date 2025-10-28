package com.basilios.basilios.core.service;

import com.basilios.basilios.core.exception.BusinessException;
import com.basilios.basilios.core.exception.ResourceNotFoundException;
import com.basilios.basilios.core.model.Client;
import com.basilios.basilios.core.model.Usuario;
import com.basilios.basilios.infra.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    /**
     * Lista todos os clients ativos
     */
    @Transactional(readOnly = true)
    public List<Client> listClients() {
        return clientRepository.findAllActive();
    }

    /**
     * Busca client por ID
     */
    @Transactional(readOnly = true)
    public Client findById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client não encontrado com ID: " + id));
    }

    /**
     * Busca client por ID com seus pedidos carregados
     */
    @Transactional(readOnly = true)
    public Client findByIdWithOrders(Long id) {
        return clientRepository.findByIdWithOrders(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client não encontrado com ID: " + id));
    }

    /**
     * Busca client por email
     */
    @Transactional(readOnly = true)
    public Client findByEmail(String email) {
        return clientRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Client não encontrado com email: " + email));
    }

    /**
     * Busca client por username
     */
    @Transactional(readOnly = true)
    public Client findByUsername(String username) {
        return clientRepository.findByNomeUsuario(username)
                .orElseThrow(() -> new ResourceNotFoundException("Client não encontrado com username: " + username));
    }

    /**
     * Atualiza client completamente
     */
    @Transactional
    public Client updateClient(Long id, Client updatedClient) {
        Client existingClient = findById(id);

        // Validar se email já existe em outro client
        if (!existingClient.getEmail().equals(updatedClient.getEmail()) &&
                clientRepository.findByEmail(updatedClient.getEmail()).isPresent()) {
            throw new BusinessException("Email já cadastrado para outro client");
        }

        // Validar se username já existe em outro client
        if (!existingClient.getNomeUsuario().equals(updatedClient.getNomeUsuario()) &&
                clientRepository.findByNomeUsuario(updatedClient.getNomeUsuario()).isPresent()) {
            throw new BusinessException("Nome de usuário já existe");
        }

        // Atualizar campos (não atualizar password diretamente)
        existingClient.setNomeUsuario(updatedClient.getNomeUsuario());
        existingClient.setEmail(updatedClient.getEmail());
        existingClient.setTelefone(updatedClient.getTelefone());
        existingClient.setCpf(updatedClient.getCpf());
        existingClient.setEnabled(updatedClient.getEnabled());

        return clientRepository.save(existingClient);
    }

    /**
     * Atualização parcial de client
     */
    @Transactional
    public Client partialUpdate(Long id, Map<String, Object> fields) {
        Client client = findById(id);

        try {
            for (Map.Entry<String, Object> entry : fields.entrySet()) {
                String fieldName = entry.getKey();
                Object value = entry.getValue();

                // Não permitir atualização de campos sensíveis via PATCH
                if (fieldName.equals("id") || fieldName.equals("password") ||
                        fieldName.equals("roles") || fieldName.equals("createdAt") ||
                        fieldName.equals("deletedAt") || fieldName.equals("orders")) {
                    continue;
                }

                // Validações específicas
                if (fieldName.equals("email") && value != null) {
                    String newEmail = value.toString();
                    if (!client.getEmail().equals(newEmail) &&
                            clientRepository.findByEmail(newEmail).isPresent()) {
                        throw new BusinessException("Email já cadastrado para outro client");
                    }
                }

                if (fieldName.equals("nomeUsuario") && value != null) {
                    String newUsername = value.toString();
                    if (!client.getNomeUsuario().equals(newUsername) &&
                            clientRepository.findByNomeUsuario(newUsername).isPresent()) {
                        throw new BusinessException("Nome de usuário já existe");
                    }
                }

                // Usar reflection para setar o campo na classe pai (Usuario) ou na classe Client
                Field field;
                try {
                    field = Client.class.getDeclaredField(fieldName);
                } catch (NoSuchFieldException e) {
                    // Tentar buscar no pai (Usuario)
                    field = Usuario.class.getDeclaredField(fieldName);
                }

                field.setAccessible(true);
                field.set(client, value);
            }

            return clientRepository.save(client);
        } catch (NoSuchFieldException e) {
            throw new BusinessException("Campo inválido: " + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new BusinessException("Erro ao atualizar campo: " + e.getMessage());
        }
    }

    /**
     * Deleta client (soft delete)
     */
    @Transactional
    public void deleteClient(Long id) {
        Client client = findById(id);
        client.softDelete();
        clientRepository.save(client);
    }

    /**
     * Restaura client deletado
     */
    @Transactional
    public Client restoreClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client não encontrado com ID: " + id));

        if (client.isAtivo()) {
            throw new BusinessException("Client já está ativo");
        }

        client.restaurar();
        return clientRepository.save(client);
    }

    /**
     * Conta total de clients ativos
     */
    @Transactional(readOnly = true)
    public long countActiveClients() {
        return clientRepository.countActive();
    }
}
