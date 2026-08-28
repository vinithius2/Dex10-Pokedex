# Universal MCP Push Notification Server

Servidor MCP hospedado no **Cloudflare Workers** que envia push notifications multi-idioma e multi-região para **qualquer app Android/iOS** via **FCM HTTP v1 API**.

## Como funciona com o Claude

```
Usuário: "Envie uma notificação sobre o evento de Pokémon lendário"
         ↓
Claude: Traduz para todos os idiomas do app (pt, en, es, ja...)
Claude: Mostra preview:
  🇧🇷 pt → lang_pt: "Evento Pokémon Lendário! Capture Mewtwo..."
  🇺🇸 en → lang_en: "Legendary Pokémon Event! Catch Mewtwo..."
  🇪🇸 es → lang_es: "¡Evento Pokémon Legendario! ¡Atrapa a Mewtwo..."
  🇯🇵 ja → lang_ja: "伝説ポケモンイベント！ミュウツーを捕まえよう..."
Claude: Chama send_notification com todas as traduções
         ↓
Cloudflare Worker → FCM → Cada dispositivo recebe no SEU idioma
```

## Arquitetura

```
Claude Code (MCP Client)
    │  Bearer Token Auth
    │  POST /mcp
    ▼
Cloudflare Worker (MCP Server)
    ├── Cloudflare KV ──► configs de apps (Firebase credentials)
    │                 └─► tokens FCM de dispositivos (label + locale + país)
    │
    │  FCM HTTP v1 API (1 chamada por idioma)
    ▼
Firebase Cloud Messaging
    ├── lang_pt  ──► dispositivos em Português
    ├── lang_en  ──► dispositivos em Inglês
    ├── lang_es  ──► dispositivos em Espanhol
    ├── lang_ja  ──► dispositivos em Japonês
    ├── country_BR ──► dispositivos no Brasil
    └── all_users  ──► todos (fallback)
```

## Ferramentas MCP

| Ferramenta | Descrição |
|---|---|
| `register_app` | Cadastra um app Firebase (credenciais + locales suportados) |
| `list_apps` | Lista apps cadastrados |
| `send_notification` | Envia notificação multi-idioma/região (Claude traduz automaticamente) |
| `register_device` | Registra token FCM com locale e país |
| `list_devices` | Lista dispositivos registrados |
| `delete_device` | Remove um dispositivo |

## Setup

### 1. Pré-requisitos

```bash
npm install -g wrangler
wrangler login
```

### 2. Criar KV Namespace

```bash
cd mcp-push-notification
npm install

wrangler kv namespace create MCP_KV
wrangler kv namespace create MCP_KV --preview
```

Atualize o `wrangler.toml` com os IDs gerados:

```toml
[[kv_namespaces]]
binding = "MCP_KV"
id = "SEU_KV_ID"
preview_id = "SEU_KV_PREVIEW_ID"
```

### 3. Configurar autenticação (recomendado)

```bash
# Gera um token seguro
openssl rand -hex 32

# Configura no Cloudflare
wrangler secret put MCP_AUTH_TOKEN
```

### 4. Deploy

```bash
npm run deploy
# URL do Worker: https://universal-mcp-push-notification.SEU-SUBDOMINIO.workers.dev
```

### 5. Configurar no Claude Code

`~/.claude/claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "push-notifications": {
      "type": "http",
      "url": "https://universal-mcp-push-notification.SEU-SUBDOMINIO.workers.dev/mcp",
      "headers": {
        "Authorization": "Bearer SEU_MCP_AUTH_TOKEN"
      }
    }
  }
}
```

### 6. Cadastrar um app (uma vez)

No Claude Code:
```
Cadastre o app "dex10" com os locales pt, en, es e ja, locale padrão en, e o seguinte service account JSON: {...}
```

Ou via ferramenta `register_app` diretamente.

O Service Account JSON é obtido em:
> Firebase Console → ⚙️ Configurações do Projeto → Contas de serviço → Gerar nova chave privada

---

## Configuração no App Android (Kotlin)

O app precisa se inscrever nos tópicos FCM na inicialização:

```kotlin
import com.google.firebase.messaging.FirebaseMessaging
import java.util.Locale

fun subscribeToFcmTopics() {
    val messaging = FirebaseMessaging.getInstance()

    // Tópico do idioma do dispositivo (ex: lang_pt, lang_en)
    val locale = Locale.getDefault().language  // "pt", "en", "es", "ja"
    messaging.subscribeToTopic("lang_$locale")

    // Tópico do país do dispositivo (ex: country_BR, country_US)
    val country = Locale.getDefault().country  // "BR", "US", "ES", "JP"
    if (country.isNotEmpty()) {
        messaging.subscribeToTopic("country_$country")
    }

    // Tópico geral (recebe fallback de "all users")
    messaging.subscribeToTopic("all_users")
}
```

Chame `subscribeToFcmTopics()` no `onCreate` da `Application` ou `MainActivity`.

---

## Exemplos de uso no Claude

### Notificação para todos os usuários (multi-idioma automático)

```
Envie uma notificação para todos os usuários do app dex10 sobre o evento especial
de captura de Pokémon lendários. Use target "all" e inclua um fallback para all_users.
O deep link deve ser "dex10://events/legendary".
```

Claude vai:
1. Chamar `list_apps` para ver os locales de "dex10" (pt, en, es, ja)
2. Traduzir a mensagem para os 4 idiomas
3. Mostrar o preview
4. Chamar `send_notification` → envia 4 mensagens (uma por tópico de idioma) + 1 fallback

### Notificação para um país específico

```
Avise apenas os usuários do Brasil sobre a manutenção programada amanhã às 22h.
```

Claude chama `send_notification` com `target_type: "country"`, `target_value: "BR"`.

### Notificação para um idioma específico

```
Envie uma mensagem apenas em japonês sobre o novo Pokémon regional.
```

Claude chama `send_notification` com `target_type: "language"`, `target_value: "ja"`.

### Notificação para dispositivo de teste

```
Mande uma notificação de teste para o meu Pixel (label "pixel_vinithius").
```

Claude chama `send_notification` com `target_type: "token"`, `target_value: "pixel_vinithius"`.

---

## Variáveis e secrets

| Nome | Tipo | Descrição |
|---|---|---|
| `MCP_KV` | KV binding | Armazena configs de apps e tokens de dispositivos |
| `MCP_AUTH_TOKEN` | secret (opcional) | Token Bearer para autenticar o cliente MCP |

As credenciais Firebase (Service Account) são armazenadas **dentro do KV** via `register_app` — não precisam de secrets separados por app.

## Desenvolvimento local

```bash
npm run dev
# Worker em http://localhost:8787
# Configure no Claude Code com URL: http://localhost:8787/mcp
```
