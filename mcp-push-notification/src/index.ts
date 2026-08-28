/**
 * Universal MCP Push Notification Server
 *
 * Cloudflare Worker que expõe um servidor MCP para enviar push notifications
 * em múltiplos idiomas/regiões via Firebase Cloud Messaging (FCM HTTP v1 API).
 *
 * Suporta múltiplos apps Firebase e envia mensagens traduzidas por idioma
 * (lang_XX topics), por país (country_XX topics), por tópico ou por token.
 *
 * Ferramentas MCP:
 *  • register_app        — cadastra um app Firebase com credenciais
 *  • list_apps           — lista apps cadastrados
 *  • send_notification   — envia notificação multi-idioma/região
 *  • register_device     — registra token FCM de dispositivo (com locale e país)
 *  • list_devices        — lista dispositivos registrados
 *  • delete_device       — remove dispositivo
 */

import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { WebStandardStreamableHTTPServerTransport } from "@modelcontextprotocol/sdk/server/webStandardStreamableHttp.js";
import { z } from "zod";
import { sendFcmMessage } from "./fcm.js";
import type { AppConfig, DeviceRecord, NotificationTranslation, SendResult } from "./types.js";

// ─── Env ──────────────────────────────────────────────────────────────────────

interface Env {
  /** KV namespace principal — armazena configs de apps e tokens de dispositivos */
  MCP_KV: KVNamespace;
  /**
   * Token de autenticação Bearer (opcional).
   * Se definido, todas as chamadas ao /mcp devem incluir:
   * Authorization: Bearer <MCP_AUTH_TOKEN>
   */
  MCP_AUTH_TOKEN?: string;
}

// ─── Chaves KV ────────────────────────────────────────────────────────────────

const kvKey = {
  app: (name: string) => `app:${name.toLowerCase()}`,
  device: (appName: string, label: string) =>
    `device:${appName.toLowerCase()}:${label.toLowerCase().replace(/\s+/g, "_")}`,
};

// ─── KV helpers ───────────────────────────────────────────────────────────────

async function getApp(kv: KVNamespace, name: string): Promise<AppConfig | null> {
  return kv.get<AppConfig>(kvKey.app(name), "json");
}

async function listApps(kv: KVNamespace): Promise<AppConfig[]> {
  const { keys } = await kv.list({ prefix: "app:" });
  const apps: AppConfig[] = [];
  for (const k of keys) {
    const app = await kv.get<AppConfig>(k.name, "json");
    if (app) apps.push(app);
  }
  return apps;
}

async function getDevice(
  kv: KVNamespace,
  appName: string,
  label: string
): Promise<DeviceRecord | null> {
  return kv.get<DeviceRecord>(kvKey.device(appName, label), "json");
}

async function listDevices(kv: KVNamespace, appName?: string): Promise<DeviceRecord[]> {
  const prefix = appName ? `device:${appName.toLowerCase()}:` : "device:";
  const { keys } = await kv.list({ prefix });
  const devices: DeviceRecord[] = [];
  for (const k of keys) {
    const d = await kv.get<DeviceRecord>(k.name, "json");
    if (d) devices.push(d);
  }
  return devices;
}

// ─── Lógica de envio multi-idioma ─────────────────────────────────────────────

type TargetType = "all" | "language" | "country" | "topic" | "token";

/**
 * Seleciona a melhor tradução disponível para um locale alvo.
 * Tenta: locale exato → prefixo (pt-BR → pt) → defaultLocale → primeira tradução.
 */
function pickTranslation(
  translations: NotificationTranslation[],
  targetLocale: string,
  defaultLocale?: string
): NotificationTranslation {
  const exact = translations.find((t) => t.locale === targetLocale);
  if (exact) return exact;

  const prefix = targetLocale.split("-")[0];
  const byPrefix = translations.find((t) => t.locale.startsWith(prefix));
  if (byPrefix) return byPrefix;

  if (defaultLocale) {
    const byDefault = translations.find((t) => t.locale === defaultLocale);
    if (byDefault) return byDefault;
  }

  return translations[0];
}

interface SendParams {
  app: AppConfig;
  translations: NotificationTranslation[];
  targetType: TargetType;
  targetValue?: string;
  deeplink?: string;
  url?: string;
  includeAllUsersFallback?: boolean;
  kv: KVNamespace;
}

async function dispatchNotifications(params: SendParams): Promise<SendResult[]> {
  const { app, translations, targetType, targetValue, deeplink, url, includeAllUsersFallback, kv } = params;
  const results: SendResult[] = [];

  const send = async (
    destination: string,
    fcmTargetType: "topic" | "token",
    translation: NotificationTranslation
  ): Promise<SendResult> => {
    try {
      const messageId = await sendFcmMessage(
        app.serviceAccount,
        app.projectId,
        { type: fcmTargetType, value: destination },
        { title: translation.title, body: translation.body, deeplink, url }
      );
      return { destination, locale: translation.locale, messageId, status: "sent" };
    } catch (err) {
      return {
        destination,
        locale: translation.locale,
        status: "error",
        error: err instanceof Error ? err.message : String(err),
      };
    }
  };

  switch (targetType) {
    case "all": {
      // Envia uma mensagem por idioma para o respectivo tópico lang_XX
      for (const translation of translations) {
        const topic = `lang_${translation.locale}`;
        results.push(await send(topic, "topic", translation));
      }
      // Fallback opcional para all_users (usuários sem tópico de idioma)
      if (includeAllUsersFallback) {
        const fallback = pickTranslation(translations, app.defaultLocale);
        results.push(await send("all_users", "topic", fallback));
      }
      break;
    }

    case "language": {
      if (!targetValue) throw new Error("target_value é obrigatório para target_type 'language'");
      const translation = pickTranslation(translations, targetValue, app.defaultLocale);
      results.push(await send(`lang_${targetValue}`, "topic", translation));
      break;
    }

    case "country": {
      if (!targetValue) throw new Error("target_value é obrigatório para target_type 'country'");
      const translation = pickTranslation(translations, app.defaultLocale, app.defaultLocale);
      results.push(await send(`country_${targetValue.toUpperCase()}`, "topic", translation));
      break;
    }

    case "topic": {
      if (!targetValue) throw new Error("target_value é obrigatório para target_type 'topic'");
      const translation = pickTranslation(translations, app.defaultLocale);
      results.push(await send(targetValue, "topic", translation));
      break;
    }

    case "token": {
      if (!targetValue) throw new Error("target_value é obrigatório para target_type 'token'");
      const device = await getDevice(kv, app.name, targetValue);
      if (!device) throw new Error(`Dispositivo "${targetValue}" não encontrado. Use list_devices para ver os disponíveis.`);
      const translation = pickTranslation(translations, device.locale, app.defaultLocale);
      results.push(await send(device.token, "token", translation));
      break;
    }
  }

  return results;
}

// ─── MCP Server ───────────────────────────────────────────────────────────────

function createMcpServer(env: Env): McpServer {
  const kv = env.MCP_KV;

  const server = new McpServer({
    name: "universal-push-notification",
    version: "2.0.0",
  });

  // ── register_app ────────────────────────────────────────────────────────────
  server.tool(
    "register_app",
    `Cadastra (ou atualiza) um app Firebase no servidor MCP.
As credenciais são armazenadas de forma segura no Cloudflare KV.
Execute este comando UMA VEZ por app. Não é necessário repetir para enviar notificações.`,
    {
      name: z
        .string()
        .min(1)
        .regex(/^[a-z0-9_-]+$/)
        .describe('Slug único do app, ex: "dex10", "my_app". Apenas letras minúsculas, números, _ e -.'),
      project_id: z
        .string()
        .describe("ID do projeto Firebase, ex: 'meu-projeto-firebase'"),
      service_account_json: z
        .string()
        .describe(
          "Conteúdo completo do JSON da Service Account do Firebase. Gerado em: Firebase Console → ⚙️ Configurações → Contas de serviço → Gerar nova chave privada."
        ),
      locales: z
        .array(z.string())
        .min(1)
        .describe(
          'Locales suportados pelo app, ex: ["pt", "en", "es", "ja"]. Usados para saber para quais tópicos lang_XX enviar com target_type "all".'
        ),
      default_locale: z
        .string()
        .describe('Locale padrão para fallback, ex: "en"'),
    },
    async ({ name, project_id, service_account_json, locales, default_locale }) => {
      let sa: AppConfig["serviceAccount"];
      try {
        sa = JSON.parse(service_account_json);
      } catch {
        return {
          content: [{ type: "text", text: "❌ service_account_json inválido — não é um JSON válido." }],
        };
      }

      const app: AppConfig = {
        name,
        projectId: project_id,
        serviceAccount: sa,
        locales,
        defaultLocale: default_locale,
        registeredAt: new Date().toISOString(),
      };

      await kv.put(kvKey.app(name), JSON.stringify(app));

      return {
        content: [
          {
            type: "text",
            text: [
              `✅ App **${name}** cadastrado com sucesso!`,
              ``,
              `🔥 **Projeto Firebase:** ${project_id}`,
              `🌍 **Locales:** ${locales.join(", ")}`,
              `📌 **Locale padrão:** ${default_locale}`,
              `📧 **Service Account:** ${sa.client_email}`,
              ``,
              `O app enviará notificações para os tópicos:`,
              locales.map((l) => `  • \`lang_${l}\``).join("\n"),
              `  • \`all_users\` (fallback opcional)`,
            ].join("\n"),
          },
        ],
      };
    }
  );

  // ── list_apps ────────────────────────────────────────────────────────────────
  server.tool(
    "list_apps",
    "Lista todos os apps Firebase cadastrados no servidor MCP.",
    {},
    async () => {
      const apps = await listApps(kv);

      if (apps.length === 0) {
        return {
          content: [{ type: "text", text: "📭 Nenhum app cadastrado. Use register_app para começar." }],
        };
      }

      const rows = [
        "| App | Projeto Firebase | Locales | Padrão | Cadastrado em |",
        "| --- | --- | --- | --- | --- |",
        ...apps.map(
          (a) =>
            `| **${a.name}** | ${a.projectId} | ${a.locales.join(", ")} | ${a.defaultLocale} | ${a.registeredAt} |`
        ),
      ];

      return {
        content: [{ type: "text", text: `🔥 **Apps cadastrados (${apps.length}):**\n\n${rows.join("\n")}` }],
      };
    }
  );

  // ── send_notification ────────────────────────────────────────────────────────
  server.tool(
    "send_notification",
    `Envia push notification multi-idioma/região via FCM.

INSTRUÇÕES PARA O CLAUDE:
1. Antes de chamar esta ferramenta, traduza a mensagem para TODOS os locales do app (use list_apps para verificar).
2. Mostre ao usuário um preview de cada tradução e destino.
3. Então chame esta ferramenta com todas as traduções.

Convenção de tópicos FCM que o app deve assinar:
  • lang_{locale}    ex: lang_pt, lang_en, lang_es, lang_ja
  • country_{CODE}   ex: country_BR, country_US, country_JP
  • all_users        (todos os usuários, fallback)

target_type "all"      → envia 1 mensagem por locale para cada tópico lang_XX
target_type "language" → envia para lang_{target_value}
target_type "country"  → envia para country_{TARGET_VALUE}
target_type "topic"    → envia para um tópico FCM qualquer
target_type "token"    → envia para dispositivo pelo label registrado`,
    {
      app: z.string().describe('Nome do app cadastrado, ex: "dex10"'),
      translations: z
        .array(
          z.object({
            locale: z.string().describe('Código do locale, ex: "pt", "en", "es", "ja"'),
            title: z.string().describe("Título da notificação neste idioma"),
            body: z.string().describe("Corpo/mensagem da notificação neste idioma"),
          })
        )
        .min(1)
        .describe(
          'Lista de traduções. Para target_type "all" forneça uma tradução por locale suportado pelo app.'
        ),
      target_type: z
        .enum(["all", "language", "country", "topic", "token"])
        .describe(
          '"all" envia para todos os idiomas configurados | "language" para um idioma | "country" para um país | "topic" para tópico FCM livre | "token" para dispositivo específico'
        ),
      target_value: z
        .string()
        .optional()
        .describe(
          'Para "language": código do locale (ex: "pt"). Para "country": código ISO 3166-1 (ex: "BR"). Para "topic": nome do tópico FCM. Para "token": label do dispositivo.'
        ),
      include_all_users_fallback: z
        .boolean()
        .optional()
        .default(false)
        .describe(
          'Apenas para target_type "all". Se true, também envia para o tópico "all_users" usando a tradução do locale padrão do app — garante que dispositivos sem tópico de idioma também recebam.'
        ),
      deeplink: z
        .string()
        .optional()
        .describe('Deep link interno do app (ex: "dex10://pokemon/25"). O app navega para essa tela ao tocar na notificação.'),
      url: z
        .string()
        .optional()
        .describe("URL externa a abrir no navegador ao tocar na notificação."),
    },
    async ({ app: appName, translations, target_type, target_value, include_all_users_fallback, deeplink, url }) => {
      const app = await getApp(kv, appName);
      if (!app) {
        return {
          content: [
            {
              type: "text",
              text: `❌ App "${appName}" não encontrado. Use list_apps para ver os apps cadastrados ou register_app para cadastrar.`,
            },
          ],
        };
      }

      let results: SendResult[];
      try {
        results = await dispatchNotifications({
          app,
          translations,
          targetType: target_type as Parameters<typeof dispatchNotifications>[0]["targetType"],
          targetValue: target_value,
          deeplink,
          url,
          includeAllUsersFallback: include_all_users_fallback ?? false,
          kv,
        });
      } catch (err) {
        return {
          content: [{ type: "text", text: `❌ Erro: ${err instanceof Error ? err.message : String(err)}` }],
        };
      }

      const sent = results.filter((r) => r.status === "sent");
      const failed = results.filter((r) => r.status === "error");

      const lines: string[] = [
        `📨 **Resultado do envio — app: ${appName}**`,
        ``,
        `✅ Enviadas: ${sent.length} | ❌ Falhas: ${failed.length}`,
        ``,
        `| Destino | Locale | Status | Message ID |`,
        `| --- | --- | --- | --- |`,
        ...results.map((r) =>
          r.status === "sent"
            ? `| \`${r.destination}\` | ${r.locale ?? "—"} | ✅ | \`${r.messageId}\` |`
            : `| \`${r.destination}\` | ${r.locale ?? "—"} | ❌ | ${r.error} |`
        ),
      ];

      if (deeplink || url) {
        lines.push(``, `🔗 **Extras:** ${deeplink ? `deeplink: \`${deeplink}\`` : ""} ${url ? `url: ${url}` : ""}`);
      }

      return { content: [{ type: "text", text: lines.join("\n") }] };
    }
  );

  // ── register_device ─────────────────────────────────────────────────────────
  server.tool(
    "register_device",
    "Registra (ou atualiza) um token FCM de dispositivo com locale e país.",
    {
      app: z.string().describe('Nome do app, ex: "dex10"'),
      label: z.string().min(1).describe('Label amigável do dispositivo, ex: "pixel_vinithius", "emulator_dev"'),
      token: z.string().min(10).describe("Token FCM do dispositivo"),
      locale: z.string().describe('Locale do dispositivo, ex: "pt", "en", "es"'),
      country_code: z.string().length(2).describe('Código ISO 3166-1 alpha-2 do país, ex: "BR", "US", "ES"'),
    },
    async ({ app: appName, label, token, locale, country_code }) => {
      const appExists = await getApp(kv, appName);
      if (!appExists) {
        return {
          content: [{ type: "text", text: `❌ App "${appName}" não encontrado. Use register_app para cadastrar primeiro.` }],
        };
      }

      const device: DeviceRecord = {
        label,
        token,
        locale,
        countryCode: country_code.toUpperCase(),
        appName,
        registeredAt: new Date().toISOString(),
      };

      await kv.put(kvKey.device(appName, label), JSON.stringify(device));

      return {
        content: [
          {
            type: "text",
            text: [
              `✅ Dispositivo registrado!`,
              ``,
              `🏷️ **Label:** ${label}`,
              `📱 **App:** ${appName}`,
              `🌍 **Locale:** ${locale} | **País:** ${country_code.toUpperCase()}`,
              `🔑 **Token:** \`${token.slice(0, 20)}…\``,
            ].join("\n"),
          },
        ],
      };
    }
  );

  // ── list_devices ────────────────────────────────────────────────────────────
  server.tool(
    "list_devices",
    "Lista dispositivos FCM registrados, opcionalmente filtrado por app.",
    {
      app: z.string().optional().describe("Filtrar por app. Se omitido, lista de todos os apps."),
    },
    async ({ app: appName }) => {
      const devices = await listDevices(kv, appName);

      if (devices.length === 0) {
        return {
          content: [{ type: "text", text: "📭 Nenhum dispositivo registrado." }],
        };
      }

      const rows = [
        "| Label | App | Locale | País | Token (parcial) | Registrado em |",
        "| --- | --- | --- | --- | --- | --- |",
        ...devices.map(
          (d) =>
            `| **${d.label}** | ${d.appName} | ${d.locale} | ${d.countryCode} | \`${d.token.slice(0, 16)}…\` | ${d.registeredAt} |`
        ),
      ];

      return {
        content: [{ type: "text", text: `📱 **Dispositivos (${devices.length}):**\n\n${rows.join("\n")}` }],
      };
    }
  );

  // ── delete_device ────────────────────────────────────────────────────────────
  server.tool(
    "delete_device",
    "Remove um dispositivo FCM registrado.",
    {
      app: z.string().describe("Nome do app"),
      label: z.string().describe("Label do dispositivo a remover"),
    },
    async ({ app: appName, label }) => {
      const key = kvKey.device(appName, label);
      const existing = await kv.get(key);

      if (!existing) {
        return {
          content: [{ type: "text", text: `❌ Dispositivo "${label}" não encontrado no app "${appName}".` }],
        };
      }

      await kv.delete(key);
      return { content: [{ type: "text", text: `🗑️ Dispositivo **${label}** removido do app **${appName}**.` }] };
    }
  );

  return server;
}

// ─── Worker entry point ───────────────────────────────────────────────────────

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    const url = new URL(request.url);

    // CORS preflight
    if (request.method === "OPTIONS") {
      return new Response(null, {
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
          "Access-Control-Allow-Headers": "Content-Type, Accept, Authorization",
          "Access-Control-Max-Age": "86400",
        },
      });
    }

    // Health check
    if (request.method === "GET" && url.pathname === "/") {
      return new Response(
        JSON.stringify({
          name: "universal-mcp-push-notification",
          version: "2.0.0",
          status: "ok",
          endpoint: "/mcp",
          tools: ["register_app", "list_apps", "send_notification", "register_device", "list_devices", "delete_device"],
        }),
        { headers: { "Content-Type": "application/json" } }
      );
    }

    // Endpoint MCP
    if (url.pathname === "/mcp") {
      // Autenticação opcional por Bearer token
      if (env.MCP_AUTH_TOKEN) {
        const authHeader = request.headers.get("Authorization") ?? "";
        const token = authHeader.replace(/^Bearer\s+/i, "").trim();
        if (token !== env.MCP_AUTH_TOKEN) {
          return new Response(JSON.stringify({ error: "Unauthorized" }), {
            status: 401,
            headers: { "Content-Type": "application/json", "WWW-Authenticate": "Bearer" },
          });
        }
      }

      try {
        const server = createMcpServer(env);
        const transport = new WebStandardStreamableHTTPServerTransport({
          sessionIdGenerator: undefined, // stateless — ideal para Workers
        });

        await server.connect(transport);
        const mcpResponse = await transport.handleRequest(request);

        const headers = new Headers(mcpResponse.headers);
        headers.set("Access-Control-Allow-Origin", "*");

        return new Response(mcpResponse.body, { status: mcpResponse.status, headers });
      } catch (err) {
        const message = err instanceof Error ? err.message : String(err);
        return new Response(JSON.stringify({ error: message }), {
          status: 500,
          headers: { "Content-Type": "application/json" },
        });
      }
    }

    return new Response("Not Found", { status: 404 });
  },
};
