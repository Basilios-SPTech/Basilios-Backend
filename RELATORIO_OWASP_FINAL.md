# Relatório Final de Conformidade OWASP Top 10
## Basilios — Avaliação Completa (Backend + Microservice + Frontend + Infra)

**Data:** 30 de abril de 2026 (atualizado)  
**Projeto:** Basilios Hamburgueria — Ecossistema Completo  
**Stack:** Spring Boot 3.5.4, Java 21, Spring Security, JWT (jjwt 0.11.5), MySQL, RabbitMQ, React 18/Vite, Docker Compose  
---

## Resumo Executivo

| Categoria OWASP | Backend | Email-API | Frontend | Infra | Geral |
|---|---|---|---|---|---|
| A01 — Broken Access Control | 🟢 Alto | 🔴 Crítico | 🟡 Médio | 🟢 OK | 🟡 Médio |
| A02 — Cryptographic Failures | 🟢 Alto | 🟢 Alto | 🟡 Médio | 🟡 Médio | 🟡 Médio-Alto |
| A03 — Injection (XSS incluso) | 🟢 Alto | 🟢 Alto | 🟡 Médio | 🟢 OK | 🟢 Alto |
| A04 — Insecure Design | 🟢 Alto | 🟢 Alto | 🟢 Alto | 🟢 Alto | 🟢 Alto |
| A05 — Security Misconfiguration | 🟡 Médio | 🟡 Médio | 🟡 Médio | 🔴 Crítico | 🟡 Médio |
| A06 — Vulnerable Components | 🟡 Médio | 🟡 Médio | 🟡 Médio | 🟡 Médio | 🟡 Médio |
| A07 — Auth Failures | 🟢 Alto | N/A | 🟢 Alto | 🟢 OK | 🟢 Alto |
| A08 — Data Integrity Failures | 🟢 Alto | 🟢 Alto | 🟢 Alto | 🟢 OK | 🟢 Alto |
| A09 — Security Logging | 🟡 Médio | 🟡 Médio | 🔴 Ausente | 🔴 Ausente | 🟡 Médio-Baixo |
| A10 — SSRF | 🟢 Alto | 🟢 Alto | 🟢 Alto | 🟢 OK | 🟢 Alto |

**Conformidade Geral: ~75%** — O Backend mantém boa postura, mas a análise expandida revela gaps críticos no Microservice (acesso sem autenticação), Infra (segredos hardcoded) e Frontend (token em localStorage).

---

## Componentes Avaliados

| Componente | Repositório | Stack |
|---|---|---|
| Backend API | `Basilios-Backend/basilios` | Spring Boot 3.5.4, Java 21, JWT, MySQL |
| Email Microservice | `-Basilios-Microservice/email-api` | Spring Boot 3.5.4, Java 21, RabbitMQ |
| Frontend SPA | `Basilios---Projeto-de-PI-/basilios-auth-ui` | React 18, Vite, Axios, Tailwind |
| Infraestrutura | `Basilios-Containers` | Docker Compose, MySQL 8.0, RabbitMQ 3 |

---

## A01 — Broken Access Control (Controle de Acesso Quebrado)
**Status: 🟢 ALTO**

### Controles Implementados

| Controle | Arquivo | Detalhe |
|---|---|---|
| Autenticação JWT obrigatória | `SecurityConfig.java` | `anyRequest().authenticated()` — todo endpoint não listado exige token |
| Roles RBAC | `SecurityConfig.java` | Regras distintas para `ROLE_FUNCIONARIO` e `ROLE_CLIENTE` |
| Method-level security | `UsuarioController.java` | `@PreAuthorize("hasRole('FUNCIONARIO')")` em listagem e exclusão de usuários |
| Ownership check (usuários) | `UsuarioController.java` | `@PreAuthorize("hasRole('FUNCIONARIO') or @usuarioService.getCurrentUsuario().id == #id")` |
| Ownership check (endereços) | `AddressController.java` | `@PreAuthorize("hasRole('FUNCIONARIO') or @addressService.isOwner(#id)")` |
| Upload restrito a FUNCIONARIO | `SecurityConfig.java` | `.requestMatchers("/api/upload/**").hasRole("FUNCIONARIO")` |
| Sessão Stateless | `SecurityConfig.java` | `SessionCreationPolicy.STATELESS` — sem session hijacking |
| Frame Options deny | `SecurityConfig.java` | `.frameOptions(frame -> frame.deny())` impede clickjacking |
| Soft Delete com filtro | Model `Usuario`/`Address` | `@SQLRestriction` garante que dados desativados não apareçam em queries |

### Evidência de Código
```java
// SecurityConfig.java — Controle de acesso por role
.requestMatchers("/api/funcionario/**").hasRole("FUNCIONARIO")
.requestMatchers("/api/cliente/**").hasRole("CLIENTE")
.requestMatchers("/api/upload/**").hasRole("FUNCIONARIO")
.anyRequest().authenticated()

// UsuarioController.java — Controle a nível de método
@PreAuthorize("hasRole('FUNCIONARIO')")
public ResponseEntity<List<UsuarioListarDTO>> getAllUsers() { ... }

@PreAuthorize("hasRole('FUNCIONARIO') or @usuarioService.getCurrentUsuario().id == #id")
public ResponseEntity<UsuarioProfileResponse> getUserById(@PathVariable Long id) { ... }
```

---

## A02 — Cryptographic Failures (Falhas Criptográficas)
**Status: 🟢 ALTO**

### Controles Implementados

| Controle | Arquivo | Detalhe |
|---|---|---|
| Senhas com BCrypt | `SecurityConfig.java` | `BCryptPasswordEncoder` — hash com salt automático |
| JWT com HMAC-SHA256 | `JwtUtil.java` | `SignatureAlgorithm.HS256` com chave de 256 bits mínimo |
| Validação de chave JWT | `JwtUtil.java` | `@PostConstruct` valida `secret.length() < 32` — rejeita chaves fracas |
| Segredo JWT externalizado | `application.properties` | `jwt.secret=${JWT_SECRET:...}` — variável de ambiente em produção |
| Credenciais DB externalizadas | `application.properties` | `${DB_USERNAME:dev}`, `${DB_PASSWORD:dev123}` |
| Token de reset com SHA-256 | `AuthService.java` | Token bruto enviado ao usuário, hash SHA-256 armazenado no DB |
| SecureRandom para tokens | `AuthService.java` | `new SecureRandom().nextBytes(bytes)` — 32 bytes (256 bits) |
| HSTS habilitado | `SecurityConfig.java` | `httpStrictTransportSecurity` com 1 ano e includeSubDomains |

### Evidência de Código
```java
// JwtUtil.java — Validação de força da chave
@PostConstruct
public void init() {
    if (secret == null || secret.length() < 32) {
        throw new IllegalStateException("JWT secret must be at least 32 characters long");
    }
}

// AuthService.java — Token de reset com hash
String rawToken = generateToken();       // SecureRandom 32 bytes
String tokenHash = sha256(rawToken);     // Armazena hash, envia raw
```

---

## A03 — Injection (Injeção)
**Status: 🟢 ALTO**

### Controles Implementados

| Controle | Arquivo | Detalhe |
|---|---|---|
| JPA/Hibernate (queries parametrizadas) | Todo o projeto | Spring Data JPA usa `PreparedStatement` — imune a SQL injection |
| Bean Validation | DTOs | `@Valid`, `@NotBlank`, `@Email`, `@Pattern`, `@Size` em todos os endpoints |
| Validação de CPF por regex | `UsuarioRegisterDTO.java` | `@Pattern(regexp = "^(\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2})$")` |
| Validação de telefone por regex | `UsuarioRegisterDTO.java` | Regex para formatos brasileiros |
| Normalização de entrada | `AuthService.java` | `normalizarCpf()` e `normalizarTelefone()` removem caracteres não numéricos |
| Path traversal prevention | `FileStorageService.java` | `targetLocation.startsWith(this.uploadDir)` bloqueia `../` |
| UUID para nomes de arquivo | `FileStorageService.java` | `UUID.randomUUID() + extension` — impede injeção via nome de arquivo |
| GlobalExceptionHandler | `GlobalExceptionHandler.java` | Catch-all handler não expõe stack traces nem mensagens internas |

### Evidência de Código
```java
// FileStorageService.java — Prevenção de Path Traversal
Path targetLocation = this.uploadDir.resolve(fileName).normalize();
if (!targetLocation.startsWith(this.uploadDir)) {
    throw new BusinessException("Caminho de arquivo inválido");
}

// GlobalExceptionHandler.java — Catch-all seguro
@ExceptionHandler(Exception.class)
public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex) {
    body.put("message", "An unexpected error occurred. Please try again later.");
    // Não expõe ex.getMessage() nem stack trace
}
```

---

## A04 — Insecure Design (Design Inseguro)
**Status: 🟢 ALTO**

### Controles Implementados

| Controle | Arquivo | Detalhe |
|---|---|---|
| Arquitetura em camadas | Projeto inteiro | Controllers → Services → Repositories — separação de responsabilidades |
| DTOs para entrada/saída | Package `dto/` | Nunca expõe entidades JPA diretamente |
| Anti-enumeração no reset | `AuthController.java` | Resposta genérica: "Se o email estiver cadastrado..." |
| Token de uso único | `AuthService.java` | Token de reset deletado após uso (`deleteByUsuarioId`) |
| Expiração de token de reset | `AuthService.java` | TTL configurável (`app.password-reset.ttl-minutes=60`) |
| Limpeza de tokens expirados | `AuthService.java` | `deleteByExpiracaoBefore(LocalDateTime.now())` a cada request |
| Event-driven para notificações | Listeners | `OrderStatusChangedEvent` + listeners desacoplados |
| Error handling centralizado | `GlobalExceptionHandler.java` | 11 handlers tipados + catch-all seguro |
| `open-in-view=false` | `application.properties` | Previne lazy loading fora da transação |

### Evidência de Código
```java
// AuthController.java — Anti-enumeração
@PostMapping("/esqueci-senha")
public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody EmailDTO request) {
    authService.requestPasswordReset(request.getEmail());
    return ResponseEntity.ok(Map.of(
        "message", "Se o email estiver cadastrado, voce recebera as instrucoes de redefinicao."
    ));
}

// AuthService.java — Não vaza erro de email
usuarioRepository.findByEmail(email).ifPresent(usuario -> {
    // Só executa se existir, mas retorno é sempre o mesmo
});
```

---

## A05 — Security Misconfiguration (Configuração Incorreta de Segurança)
**Status: 🟡 MÉDIO-ALTO**

### Controles Implementados

| Controle | Arquivo | Detalhe |
|---|---|---|
| CSRF desabilitado (correto para API REST stateless) | `SecurityConfig.java` | `.csrf(csrf -> csrf.disable())` |
| CORS restritivo | `CorsConfig.java` | Origins explícitas: `localhost:5173`, `localhost:3000`, etc. — **sem wildcard `*`** |
| Headers de segurança | `SecurityConfig.java` | `X-Frame-Options: DENY`, `X-Content-Type-Options`, `HSTS` |
| Perfil de produção | `application-production.properties` | Stack traces ocultos, SQL logging desligado, H2 desabilitado |
| Limite de upload | `application.properties` | `max-file-size=5MB`, `max-request-size=10MB` |
| Credenciais via env vars | `application.properties` | `${JWT_SECRET}`, `${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}` |
| Swagger público apenas em dev | `SecurityConfig.java` | Swagger listado em `permitAll()` — pode ser restrito em produção |

### Pontos de Atenção
| Item | Risco | Recomendação |
|---|---|---|
| Credenciais SMTP hardcoded em `application.properties` | 🔴 Alto | Mover para variáveis de ambiente: `${MAIL_USERNAME}`, `${MAIL_PASSWORD}` |
| Swagger público em produção | 🟡 Médio | Condicionar a disponibilidade ao perfil (ex: profile `dev` apenas) |
| `spring.jpa.hibernate.ddl-auto=update` | 🟡 Médio | Usar `validate` em produção com migrations (Flyway/Liquibase) |
| Logging DEBUG em dev | 🟢 OK | Perfil production já define `WARN` |

---

## A06 — Vulnerable and Outdated Components (Componentes Vulneráveis)
**Status: 🟡 MÉDIO**

### Controles Implementados

| Controle | Detalhe |
|---|---|
| Spring Boot 3.5.4 | Versão mais recente da linha 3.x |
| Java 21 (LTS) | Versão LTS com suporte de longo prazo |
| jjwt 0.11.5 | Biblioteca JWT mantida e sem CVEs críticas conhecidas |
| Spring Security (managed) | Versão gerenciada pelo Spring Boot BOM |
| MySQL Connector 8.3.0 | Versão atualizada do driver JDBC |

### Recomendações
| Item | Recomendação |
|---|---|
| Auditoria de CVEs | Executar `mvn dependency:tree` + OWASP Dependency Check plugin periodicamente |
| Dependabot/Renovate | Configurar no GitHub para PRs automáticos de atualização |

---

## A07 — Identification and Authentication Failures (Falhas de Autenticação)
**Status: 🟢 ALTO**

### Controles Implementados

| Controle | Arquivo | Detalhe |
|---|---|---|
| BCrypt com salt automático | `SecurityConfig.java` | `BCryptPasswordEncoder` — resistente a rainbow tables |
| JWT com expiração | `JwtUtil.java` | Token expira em 24h (`jwt.expiration=86400000`) |
| Validação de token | `JwtUtil.java` | Verifica username + expiração em cada request |
| Rate limit (estrutura) | `RateLimitFilter.java` | Filter registrado no chain — ativação disponível |
| Duplicidade de email/CPF | `AuthService.java` | Verifica `existsByEmail` e `existsByCpf` antes do registro |
| Conta desativada | `AuthService.java` | `if (!usuario.getEnabled())` bloqueia login |
| Reset seguro com hash | `AuthService.java` | Token bruto → SHA-256 no DB, expiração, uso único |
| Anti-enumeração | `AuthController.java` | Resposta genérica no endpoint de reset |
| Roles no JWT | `JwtUtil.java` | `generateToken(username, roles)` — roles incluídas no claim |

### Evidência de Código
```java
// AuthService.java — Fluxo de reset seguro
String rawToken = generateToken();              // SecureRandom 32 bytes
String tokenHash = sha256(rawToken);            // Hash armazenado no DB
PasswordReset reset = new PasswordReset(tokenHash, expiracao, usuario);
passwordResetRepository.save(reset);
emailService.sendPasswordResetEmail(email, buildResetLink(rawToken)); // Raw enviado ao user
```

---

## A08 — Software and Data Integrity Failures (Falhas de Integridade)
**Status: 🟢 ALTO**

### Controles Implementados

| Controle | Arquivo | Detalhe |
|---|---|---|
| Validação de tipo MIME | `FileStorageService.java` | Whitelist: `image/jpeg`, `image/png`, `image/webp`, `image/gif` |
| Validação de extensão | `FileStorageService.java` | Whitelist: `.jpg`, `.jpeg`, `.png`, `.webp`, `.gif` |
| Limite de tamanho de arquivo | `FileStorageService.java` | Máximo 5MB — rejeita arquivos maiores |
| UUID para nomes de arquivo | `FileStorageService.java` | Impede colisão e injeção via nome original |
| JWT assinado com HMAC-SHA256 | `JwtUtil.java` | Token não pode ser modificado sem a chave secreta |
| Bean Validation com `@Valid` | Controllers | Todos os DTOs de entrada são validados antes do processamento |
| `@Transactional` | Services | Operações críticas são atômicas (registro, reset de senha) |
| Dependências gerenciadas por BOM | `pom.xml` | Spring Boot BOM garante versões compatíveis e verificadas |

### Evidência de Código
```java
// FileStorageService.java — Tripla validação de upload
if (file.getSize() > MAX_FILE_SIZE) { throw new BusinessException("..."); }
if (!ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) { throw new BusinessException("..."); }
if (!ALLOWED_EXTENSIONS.contains(extension)) { throw new BusinessException("..."); }
```

---

## A09 — Security Logging and Monitoring Failures (Falhas de Logging)
**Status: 🟡 MÉDIO**

### Controles Implementados

| Controle | Arquivo | Detalhe |
|---|---|---|
| Auditoria de login | `SecurityAuditLogger.java` | `[SECURITY_AUDIT] action=LOGIN_SUCCESS user=...` |
| Auditoria de falhas | `SecurityAuditLogger.java` | `[SECURITY_AUDIT] action=LOGIN_FAILED user=... reason=...` |
| Log de falha no envio de email | `AuthService.java` | `log.error("Falha ao enviar email de reset...")` |
| Logging estruturado com SLF4J | Todo o projeto | Uso de `@Slf4j` do Lombok |
| Perfil production com WARN | `application-production.properties` | Reduz ruído em produção |

### Recomendações
| Item | Recomendação |
|---|---|
| Log de acesso a dados sensíveis | Registrar quando um FUNCIONARIO listar todos os usuários |
| Log de alterações de senha | Registrar quando uma senha for alterada (reset ou update) |
| Centralização de logs | Configurar envio para ELK Stack, Datadog, ou CloudWatch em produção |
| Alertas automáticos | Configurar alertas para padrões suspeitos (muitas falhas de login) |

---

## A10 — Server-Side Request Forgery (SSRF)
**Status: 🟢 ALTO**

### Controles Implementados

| Controle | Detalhe |
|---|---|
| Sem HTTP client externo | A API não faz requisições HTTP para URLs fornecidas pelo usuário |
| URLs de reset controladas | `passwordResetBaseUrl` é configurável apenas via properties/env var, não via input |
| Upload local apenas | `FileStorageService` salva arquivos localmente — sem fetch de URLs externas |
| Sem redirecionamentos dinâmicos | Nenhum endpoint aceita URL como parâmetro para redirect |

---

## Cobertura de Testes de Segurança

### Backend (Basilios-Backend)
| Área | Testes | Status |
|---|---|---|
| Autenticação (registro/login) | `AuthServiceTest` | ✅ 4 testes |
| Reset de senha | `PasswordResetServiceTest` | ✅ 2 testes |
| Controle de acesso | `AuthControllerTest` | ✅ 3 testes |
| Serviço de email | `EmailServiceTest` | ✅ 2 testes |
| Serviço de usuário | `UsuarioServiceTest` | ✅ 9 testes |
| Serviço de pedidos | `OrderServiceTest` | ✅ 13 testes |
| Serviço de produtos | `ProductServiceTest` | ✅ 7 testes |
| Dashboard | `DashboardServiceTest` | ✅ 14 testes |
| Endereços | `AddressServicePartialTest` | ✅ 6 testes |
| **Total Backend** | **78 testes** | **77 passando (98.7%)** |

### Email Microservice (-Basilios-Microservice)
| Área | Testes | Status |
|---|---|---|
| Idempotência | `IdempotencyServiceTest` | ✅ Passando |
| Consumer de pedidos | `OrderStatusConsumerTest` | ✅ Passando |
| Consumer de reset | `PasswordResetConsumerTest` | ✅ Passando |
| Context load | `EmailApiApplicationTests` | ✅ Passando |

### Frontend (basilios-auth-ui)
| Área | Testes | Status |
|---|---|---|
| E2E (Cypress) | `cypress/e2e/` | ⚠️ Estrutura presente, cobertura não verificada |

---

## NOVAS DESCOBERTAS — Avaliação Expandida (Abril 2026)

### 🔴 VULNERABILIDADES CRÍTICAS ENCONTRADAS

#### 1. Email-API — Endpoint sem Autenticação
**OWASP A01 — Broken Access Control**
```
GET /api/notifications/failed   → PÚBLICO, SEM AUTH
```
O `FailedNotificationController` expõe notificações com dados de clientes (emails, nomes) sem nenhuma camada de autenticação. O microservice não possui Spring Security configurado.

**Impacto:** Qualquer pessoa com acesso à rede pode listar emails de clientes e detalhes de pedidos.  
**Correção:** Adicionar Spring Security ao email-api ou restringir o endpoint via rede (internal-only no Docker).

#### 2. Credenciais SMTP Hardcoded em Produção
**OWASP A05 — Security Misconfiguration**
```properties
# application-production.properties (Backend)
spring.mail.username=resetpsswd-basilios@hotmail.com
spring.mail.password=hqmdccgwxzxbdoqf
```
Senha de email em texto plano no repositório Git. Qualquer pessoa com acesso ao código tem as credenciais.

**Impacto:** Comprometimento da conta de email, possível envio de phishing usando a identidade do projeto.  
**Correção:** Remover credenciais do arquivo, usar `${MAIL_USERNAME}` e `${MAIL_PASSWORD}` (como já existe no `application.properties` base).

#### 3. Docker Compose — Segredos em Texto Plano
**OWASP A05 — Security Misconfiguration**
```yaml
# docker-compose.yml
MYSQL_ROOT_PASSWORD: root
MYSQL_PASSWORD: basilios123
RABBITMQ_DEFAULT_PASS: basilios123
JWT_SECRET: ${JWT_SECRET:-troque-este-segredo-em-producao}
```
Senhas previsíveis e default de JWT fraco commitados no repositório.

**Impacto:** Em ambiente de produção, banco de dados e broker de mensagens ficam com credenciais conhecidas.  
**Correção:** Usar Docker Secrets ou `.env` (no .gitignore) para valores sensíveis.

#### 4. Frontend — JWT em localStorage
**OWASP A02 — Cryptographic Failures / A07 — Auth Failures**
```javascript
// storageAuth.js
const KEY = 'auth_token'
localStorage.setItem(KEY, token)
```
Token JWT armazenado em `localStorage` é acessível via XSS. Se qualquer script malicioso executar no contexto da página, pode roubar o token.

**Impacto:** Roubo de sessão via XSS.  
**Correção ideal:** HttpOnly cookie com flag Secure + SameSite=Strict. Alternativa mínima: manter em memória e usar refresh token.

---

### 🟡 VULNERABILIDADES MÉDIAS

#### 5. Email-API — `ddl-auto=update` sem perfil de produção
**OWASP A05**
```yaml
# application.yml (email-api)
jpa:
  hibernate:
    ddl-auto: update
  show-sql: true
```
Não existe `application-production.yml` no microservice. Em produção, o Hibernate pode alterar o schema do banco automaticamente e SQL é logado.

#### 6. Email-API — Actuator exposto sem restrição
**OWASP A05**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```
Endpoints de métricas expostos sem autenticação. Embora `health` e `info` sejam de baixo risco, `metrics` pode revelar informações internas.

#### 7. Frontend — Sem Content-Security-Policy (CSP)
**OWASP A05**
Nenhum header CSP configurado no Vite ou em meta tags. Isso permite que scripts inline e externos sejam executados sem restrição, facilitando ataques XSS.

#### 8. Frontend — `innerHTML` em highContrast.js
**OWASP A03 — Injection (XSS)**
```javascript
// utils/highContrast.js
host.innerHTML = `<button class="hc-btn" ...>...</button>...`
```
Uso de `innerHTML` com template literal. Embora neste caso o conteúdo seja estático e não inclua input do usuário, é um padrão perigoso que pode se degradar em futuras modificações.

#### 9. Backend — RateLimitFilter desativado
**OWASP A07 — Auth Failures**
```java
// RateLimitFilter.java — apenas faz passthrough
@Override
protected void doFilterInternal(...) {
    filterChain.doFilter(request, response); // sem limitação
}
```
Nenhuma proteção contra brute force nos endpoints de login/reset.

#### 10. Docker — Portas expostas desnecessariamente
**OWASP A05**
```yaml
rabbitmq:
  ports:
    - "5672:5672"    # AMQP aberto no host
    - "15672:15672"  # Management UI aberta
```
RabbitMQ Management UI e porta AMQP acessíveis externamente. Somente serviços internos precisam dessas portas.

#### 11. Backend — Swagger público em todos os perfis
**OWASP A05**
O Swagger/OpenAPI está acessível sem restrição mesmo com `SPRING_PROFILES_ACTIVE=production`.

---

## Matriz de Conformidade — Visão Geral Atualizada

```
OWASP Top 10 2021          Backend   Email-API  Frontend  Infra    GERAL
──────────────────────────────────────────────────────────────────────────
A01  Broken Access Control    90%       30%        70%      80%      68%
A02  Cryptographic Failures   95%       85%        60%      50%      73%
A03  Injection/XSS            95%       90%        75%      N/A      87%
A04  Insecure Design          85%       85%        80%      75%      81%
A05  Security Misconfig       70%       50%        55%      40%      54%
A06  Vulnerable Components    70%       70%        60%      65%      66%
A07  Auth Failures            85%       N/A        80%      N/A      83%
A08  Data Integrity           85%       90%        80%      75%      83%
A09  Security Logging         65%       60%        20%      20%      41%
A10  SSRF                     95%       95%        95%      N/A      95%
──────────────────────────────────────────────────────────────────────────
             MÉDIA POR COMPONENTE:  84%   73%      68%      58%
                              MÉDIA GERAL PONDERADA:  ~75%
```

---

## Plano de Correção Priorizado

### 🔴 Prioridade Crítica (corrigir imediatamente)
| # | Vulnerabilidade | Componente | Correção |
|---|---|---|---|
| 1 | Credenciais SMTP hardcoded | Backend | Remover de `application-production.properties`, usar `${MAIL_USERNAME}` |
| 2 | Endpoint `/api/notifications/failed` público | Email-API | Adicionar Spring Security ou bloquear via network policy |
| 3 | Segredos no docker-compose | Infra | Migrar para `.env` (gitignored) ou Docker Secrets |

### 🟠 Prioridade Alta (corrigir em sprint)
| # | Vulnerabilidade | Componente | Correção |
|---|---|---|---|
| 4 | JWT em localStorage | Frontend | Migrar para HttpOnly cookie ou memória + refresh token |
| 5 | RateLimitFilter desativado | Backend | Implementar limitação real (Bucket4j ou similar) |
| 6 | `ddl-auto=update` sem perfil prod | Email-API | Criar `application-production.yml` com `ddl-auto=validate` |
| 7 | RabbitMQ/MySQL portas expostas | Infra | Remover `ports` ou bind em `127.0.0.1` |

### 🟡 Prioridade Média (próximo ciclo)
| # | Vulnerabilidade | Componente | Correção |
|---|---|---|---|
| 8 | Sem CSP header | Frontend | Adicionar meta tag ou configurar no proxy/nginx |
| 9 | Swagger público em produção | Backend | Restringir ao perfil `dev` com `@Profile("dev")` |
| 10 | Actuator metrics público | Email-API | Proteger com basic auth ou remover `metrics` do exposure |
| 11 | Sem centralização de logs | Todos | Integrar ELK/Datadog/CloudWatch |
| 12 | Sem scan de CVEs automatizado | Todos | OWASP Dependency-Check + `npm audit` no CI |

### 🟢 Prioridade Baixa (melhorias futuras)
| # | Melhoria | Componente |
|---|---|---|
| 13 | Refresh Token com rotação | Backend + Frontend |
| 14 | 2FA para FUNCIONARIOS | Backend |
| 15 | Migrations Flyway/Liquibase | Backend + Email-API |
| 16 | Docker image scanning (Trivy) | Infra |
| 17 | Dependabot/Renovate | Todos os repos |

---

## Conclusão

A avaliação expandida (abril/2026) revela que o **Backend mantém boa postura (~84%)**, porém a visão sistêmica do projeto apresenta **gaps significativos**:

- **Email-API** sem qualquer autenticação nos endpoints HTTP
- **Credenciais de produção** commitadas em texto plano no repositório
- **Frontend** vulnerável a roubo de sessão via XSS (token em localStorage)
- **Infraestrutura** com senhas hardcoded e portas desnecessariamente expostas

A conformidade geral caiu de 85% (somente backend) para **~75%** quando consideramos todo o ecossistema. As 3 correções críticas devem ser priorizadas antes do próximo deploy em produção.
