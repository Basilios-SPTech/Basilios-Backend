# Projeto PI - Padrão Observer (Spring Events)

## Nome do padrão: Observer

## Implementação Atual

Utilizamos o **Spring Events** (`ApplicationEventPublisher` + `@EventListener`) como implementação do padrão Observer. Esta abordagem é mais idiomática para Spring Boot e oferece melhor integração com transações, suporte nativo a execução assíncrona e retry automático.

### Motivo da escolha (Spring Events vs Observer manual)

| Aspecto | Spring Events | Observer Manual |
|---------|---------------|-----------------|
| **Integração** | ✅ Nativo Spring, funciona com `@Transactional` | ⚠️ Requer wiring manual |
| **Async** | ✅ `@Async` nativo | ❌ Implementação manual |
| **Transação** | ✅ `@TransactionalEventListener(AFTER_COMMIT)` | ⚠️ Pode causar problemas |
| **Retry** | ✅ `@Retryable` do Spring Retry | ❌ Implementação manual |
| **Type Safety** | ✅ Eventos fortemente tipados | ⚠️ Payload genérico `Object` |
| **Manutenção** | ✅ Padrão da indústria | ⚠️ Código customizado |

## Arquitetura Implementada

```
┌─────────────────┐
│  OrderService   │
│  (Publisher)    │
└────────┬────────┘
         │ publishEvent()
         ▼
┌─────────────────────────────────────┐
│     OrderStatusChangedEvent         │
│  (order, oldStatus, newStatus)      │
└────────┬───────────────┬────────────┘
         │               │
         ▼               ▼
┌─────────────────┐  ┌─────────────────────┐
│ Notification    │  │  Dashboard          │
│ Listener        │  │  Listener           │
│ (@Async)        │  │  (@Async)           │
│ (@Retryable)    │  │                     │
└────────┬────────┘  └──────────┬──────────┘
         │                      │
         ▼                      ▼
    EmailService           WebSocket
    (com retry)         (tempo real)
         │
         ▼ (após 3 falhas)
    FailedNotification
    (tabela para reprocessamento)
```

## Classes Envolvidas

### Evento de Domínio
- **`OrderStatusChangedEvent`** (`core/model/events/`)
  - Contém: `Order`, `oldStatus`, `newStatus`, `timestamp`, `motivo`
  - Métodos auxiliares: `isCreation()`, `isCancellation()`, `isDelivery()`

### Publisher (Publicador)
- **`OrderService`** (`core/service/`)
  - Injeta `ApplicationEventPublisher`
  - Publica evento após cada mudança de status:
    - `confirmarPedido()` → PENDENTE → CONFIRMADO
    - `iniciarPreparo()` → CONFIRMADO → PREPARANDO
    - `despacharPedido()` → PREPARANDO → DESPACHADO
    - `entregarPedido()` → DESPACHADO → ENTREGUE
    - `cancelarPedido()` → * → CANCELADO

### Listeners (Observadores)

1. **`OrderNotificationListener`** (`infra/listener/`)
   - `@TransactionalEventListener(AFTER_COMMIT)`: só executa após commit
   - `@Async("taskExecutor")`: executa em thread separada
   - `@Retryable(maxAttempts=3)`: retry com backoff exponencial
   - `@Recover`: salva em `FailedNotification` após 3 falhas

2. **`OrderDashboardListener`** (`infra/listener/`)
   - Envia atualizações via WebSocket para:
     - `/topic/orders`: painel administrativo (todos os pedidos)
     - `/topic/orders/{id}`: pedido específico
     - `/user/{userId}/queue/orders`: cliente do pedido

### Serviços de Suporte

- **`EmailService`** (`core/service/`)
  - `sendOrderConfirmedEmail()`: pedido confirmado
  - `sendOrderPreparingEmail()`: em preparo
  - `sendOrderDispatchedEmail()`: saiu para entrega
  - `sendOrderDeliveredEmail()`: entregue
  - `sendOrderCancelledEmail()`: cancelado

- **`FailedNotification`** (`core/model/`)
  - Entidade para armazenar notificações que falharam
  - Permite reprocessamento manual via painel

### Configurações

- **`AsyncConfig`** (`infra/config/`)
  - Pool de threads: 2-5 threads, fila de 100
  - Handler para exceções não capturadas

- **`WebSocketConfig`** (`infra/config/`)
  - Endpoint: `/ws` (com SockJS) e `/ws-native`
  - Brokers: `/topic`, `/queue`, `/user`

## Fluxo de Execução

```
1. OrderController recebe requisição de mudança de status
2. OrderService.confirmarPedido(id) é chamado
3. Valida transição de status
4. Salva no banco (commit pendente)
5. publishEvent(OrderStatusChangedEvent)
6. Transação faz COMMIT
7. @TransactionalEventListener(AFTER_COMMIT) é acionado
8. OrderNotificationListener (assíncrono):
   - Tenta enviar email
   - Se falhar: retry até 3x com backoff (1s, 2s, 4s)
   - Se falhar 3x: salva em FailedNotification
9. OrderDashboardListener (assíncrono):
   - Envia para WebSocket
10. Response retorna imediatamente (não espera listeners)
```

## Benefícios da Implementação

1. **Desacoplamento**: OrderService não conhece os listeners
2. **Resiliência**: retry automático + fallback para DLQ local
3. **Performance**: execução assíncrona não bloqueia resposta
4. **Consistência**: AFTER_COMMIT garante que evento só dispara se commit OK
5. **Observabilidade**: logs estruturados com @Slf4j
6. **Escalabilidade**: fácil adicionar novos listeners sem modificar OrderService

## Exemplo de Uso no Frontend (WebSocket)

```javascript
// Conectar ao WebSocket
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    // Assinar atualizações gerais (painel admin)
    stompClient.subscribe('/topic/orders', function(message) {
        const update = JSON.parse(message.body);
        console.log('Pedido atualizado:', update);
        // Atualizar painel
    });
    
    // Assinar atualizações do usuário logado
    stompClient.subscribe('/user/queue/orders', function(message) {
        const update = JSON.parse(message.body);
        console.log('Meu pedido atualizado:', update);
        // Mostrar notificação
    });
});
```

## Extensibilidade

Para adicionar um novo listener (ex: integração com cozinha):

```java
@Component
@Slf4j
public class OrderKitchenListener {

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        if (event.getNewStatus() == StatusPedidoEnum.CONFIRMADO) {
            log.info("Enviando pedido {} para sistema da cozinha", 
                    event.getOrder().getCodigoPedido());
            // Integração com sistema da cozinha
        }
    }
}
```

Não é necessário modificar `OrderService` — apenas criar o novo listener.

