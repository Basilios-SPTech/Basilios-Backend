package com.basilios.basilios.infra.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuração do WebSocket com STOMP para notificações em tempo real.
 * 
 * Endpoints:
 * - /ws: endpoint principal para conexão WebSocket (com SockJS fallback)
 * 
 * Destinos:
 * - /topic/*: mensagens públicas (broadcast para todos os assinantes)
 *   - /topic/orders: atualizações gerais de pedidos
 *   - /topic/orders/{id}: atualizações de pedido específico
 * 
 * - /user/queue/*: mensagens privadas para usuário específico
 *   - /user/{userId}/queue/orders: notificações do pedido do cliente
 * 
 * - /app/*: destino para mensagens enviadas pelos clientes (se necessário)
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilita broker simples em memória para /topic (público) e /queue (privado)
        config.enableSimpleBroker("/topic", "/queue", "/user");
        
        // Prefixo para mensagens enviadas pelos clientes (ex: @MessageMapping)
        config.setApplicationDestinationPrefixes("/app");
        
        // Prefixo para mensagens privadas de usuário
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(
                        "http://localhost:5173",
                        "http://127.0.0.1:5173",
                        "http://localhost:3000"
                )
                .withSockJS();

        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns(
                        "http://localhost:5173",
                        "http://127.0.0.1:5173",
                        "http://localhost:3000"
                );
    }
}
