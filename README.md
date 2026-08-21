# Basilios API

API REST da plataforma **Basilios** — delivery de hamburgueria com pedidos online, painel operacional, autenticação JWT, promoções e integração com pagamentos/armazenamento.

## Stack

- Java 21 · Spring Boot 3.5
- Spring Security + JWT
- Spring Data JPA · MySQL
- RabbitMQ (eventos de pedido / reset de senha)
- AWS S3 (imagens)
- WebSocket · SpringDoc OpenAPI
- Rate limiting (Bucket4j)

## O que a API cobre

- Auth e usuários (roles cliente / funcionário)
- Produtos, adicionais e promoções
- Pedidos e status
- Horários da loja e dashboard
- Upload de arquivos (S3)
- Publicação de eventos para o microsserviço de e-mail

## Arquitetura (visão rápida)

```mermaid
flowchart LR
  web[basilios-web] -->|HTTP_JWT| api[basilios-api]
  api --> mysql[(MySQL)]
  api --> s3[AWS_S3]
  api -->|eventos| mq[RabbitMQ]
  mq --> email[basilios-email-api]
```

Repositórios relacionados: [basilios-web](https://github.com/Basilios-SPTech/basilios-web) · [basilios-email-api](https://github.com/Basilios-SPTech/basilios-email-api) · [basilios-infra](https://github.com/Basilios-SPTech/basilios-infra)

## Como rodar localmente

Pré-requisitos: JDK 21, MySQL e RabbitMQ (ou use o [basilios-infra](https://github.com/Basilios-SPTech/basilios-infra) com Docker Compose).

```bash
cd basilios
./mvnw spring-boot:run
```

API padrão: `http://localhost:8080`

### Variáveis de ambiente

| Variável | Descrição | Default típico |
|----------|-----------|----------------|
| `DB_URL` | JDBC URL MySQL | `jdbc:mysql://localhost:3306/basiliosData?...` |
| `DB_USERNAME` / `DB_PASSWORD` | Credenciais do banco | `dev` / `dev123` |
| `JWT_SECRET` / `JWT_EXPIRATION` | Assinatura e validade do token | — / `86400000` |
| `RABBITMQ_HOST` / `PORT` / `USERNAME` / `PASSWORD` | Mensageria | `localhost` / `5672` / `guest` |
| `MAIL_HOST` / `MAIL_USERNAME` / `MAIL_PASSWORD` | SMTP (se usado na API) | `smtp.gmail.com` |
| Credenciais AWS S3 | Bucket de imagens | via profile / env de produção |

> Não versionar secrets. Use `.env` local ou o Compose do `basilios-infra`.

## Testes

```bash
cd basilios
./mvnw test
```

## Licença

MIT — veja [LICENSE](LICENSE).
