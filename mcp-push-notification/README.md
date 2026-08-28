# Dex10 MCP Push Notification Server

Servidor MCP hospedado no **Cloudflare Workers** que permite enviar push notifications para o app **Dex10 Pokédex** via **Firebase Cloud Messaging (FCM) HTTP v1 API** — diretamente do Claude Code.

## Arquitetura

```
Claude Code
    │
    │  MCP Protocol (HTTP)
    ▼
Cloudflare Worker  ─── Cloudflare KV (tokens)
    │
    │  FCM HTTP v1 API
    ▼
Firebase Cloud Messaging
    │
    ▼
App Dex10 (Android)
```

## Ferramentas MCP disponíveis

| Ferramenta | Descrição |
|---|---|
| `send_notification` | Envia notificação para tópico FCM ou dispositivo específico |
| `register_token` | Registra token FCM de um dispositivo com um label amigável |
| `list_tokens` | Lista todos os tokens registrados |
| `delete_token` | Remove um token pelo label |

## Setup

### 1. Pré-requisitos

```bash
npm install -g wrangler
wrangler login
```

### 2. Criar KV Namespace

```bash
cd mcp-push-notification
wrangler kv namespace create FCM_TOKENS_KV
wrangler kv namespace create FCM_TOKENS_KV --preview
```

Copie os IDs gerados e atualize o `wrangler.toml`:

```toml
[[kv_namespaces]]
binding = "FCM_TOKENS_KV"
id = "SEU_KV_ID_AQUI"
preview_id = "SEU_KV_PREVIEW_ID_AQUI"
```

### 3. Configurar o projeto Firebase

No `wrangler.toml`, atualize:

```toml
[vars]
FCM_PROJECT_ID = "seu-projeto-firebase"
```

### 4. Configurar a Service Account (secret)

No [Firebase Console](https://console.firebase.google.com):
1. Projeto → ⚙️ Configurações → Contas de serviço
2. **Gerar nova chave privada** → baixe o JSON
3. Configure o secret no Cloudflare:

```bash
wrangler secret put FCM_SERVICE_ACCOUNT
# Cole o conteúdo completo do JSON quando solicitado
```

### 5. Instalar dependências e fazer deploy

```bash
npm install
npm run deploy
```

O deploy retorna a URL do Worker, ex: `https://dex10-mcp-push-notification.SEU-SUBDOMINIO.workers.dev`

### 6. Configurar no Claude Code

Adicione ao seu `~/.claude/claude_desktop_config.json` (ou ao `claude.json` do projeto):

```json
{
  "mcpServers": {
    "dex10-push": {
      "type": "http",
      "url": "https://dex10-mcp-push-notification.SEU-SUBDOMINIO.workers.dev/mcp"
    }
  }
}
```

> Reinicie o Claude Code para carregar o servidor MCP.

## Uso

### Enviar para todos os usuários (tópico)

O app precisa se inscrever no tópico `all_users` (ou outro de sua escolha). No app Android, basta chamar:

```kotlin
FirebaseMessaging.getInstance().subscribeToTopic("all_users")
```

No Claude Code:

```
Envie uma notificação para todos os usuários avisando sobre o novo evento de captura de Pokémon lendários
```

### Registrar um dispositivo de teste

```
Registre o token FCM "dXNlcl9kZXZpY2VfdG9rZW4..." com o label "pixel_dev"
```

### Enviar para um dispositivo específico

```
Envie uma notificação de teste para o dispositivo "pixel_dev"
```

### Navegar para um Pokémon com deep link

```
Envie uma notificação sobre Mewtwo com deeplink "dex10://pokemon/150"
```

## Variáveis de ambiente

| Nome | Tipo | Descrição |
|---|---|---|
| `FCM_PROJECT_ID` | `[vars]` | ID do projeto Firebase |
| `FCM_SERVICE_ACCOUNT` | secret | JSON da Service Account do Firebase |
| `FCM_TOKENS_KV` | KV binding | Namespace para tokens de dispositivos |

## Desenvolvimento local

```bash
npm run dev
```

O Worker sobe em `http://localhost:8787`. Para testar:

```bash
curl http://localhost:8787/
```

Para usar localmente com o Claude Code, configure a URL `http://localhost:8787/mcp`.
