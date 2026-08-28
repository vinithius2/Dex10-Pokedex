/**
 * Dex10 MCP Push Notification Server
 *
 * Cloudflare Worker que expõe um servidor MCP para enviar push notifications
 * ao app Dex10 Pokédex via Firebase Cloud Messaging (FCM) HTTP v1 API.
 *
 * Ferramentas disponíveis:
 *  • send_notification   — envia para um tópico ou token específico
 *  • register_token      — salva um token FCM no KV (identificado por label)
 *  • list_tokens         — lista tokens registrados
 *  • delete_token        — remove um token do KV
 */

import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { WebStandardStreamableHTTPServerTransport } from "@modelcontextprotocol/sdk/server/webStandardStreamableHttp.js";
import { z } from "zod";
import { sendFcmNotification, type ServiceAccount } from "./fcm.js";

// ─── Tipos ────────────────────────────────────────────────────────────────────

interface Env {
  /** KV namespace para armazenar tokens FCM de dispositivos */
  FCM_TOKENS_KV: KVNamespace;
  /** JSON completo da Service Account do Firebase (secret) */
  FCM_SERVICE_ACCOUNT: string;
  /** ID do projeto Firebase (var de ambiente) */
  FCM_PROJECT_ID: string;
}

interface StoredToken {
  token: string;
  label: string;
  registeredAt: string;
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

function parseServiceAccount(raw: string): ServiceAccount {
  try {
    return JSON.parse(raw) as ServiceAccount;
  } catch {
    throw new Error(
      "FCM_SERVICE_ACCOUNT inválido — certifique-se de que é um JSON válido."
    );
  }
}

function buildTokenKey(label: string): string {
  return `token:${label.toLowerCase().replace(/\s+/g, "_")}`;
}

// ─── MCP Server factory ───────────────────────────────────────────────────────

function createMcpServer(env: Env): McpServer {
  const server = new McpServer({
    name: "dex10-push-notifications",
    version: "1.0.0",
  });

  // ── Ferramenta: send_notification ─────────────────────────────────────────
  server.tool(
    "send_notification",
    "Envia uma push notification para o app Dex10 Pokédex via FCM.",
    {
      title: z.string().describe("Título da notificação"),
      body: z.string().describe("Corpo/mensagem da notificação"),
      target_type: z
        .enum(["topic", "token_label"])
        .describe(
          'Tipo de destino: "topic" para um tópico FCM (ex: "all_users") ou "token_label" para um dispositivo específico pelo label registrado'
        ),
      target_value: z
        .string()
        .describe(
          'Valor do destino: nome do tópico (ex: "all_users") ou label do token (ex: "pixel_vinithius")'
        ),
      deeplink: z
        .string()
        .optional()
        .describe(
          'Deep link interno do app para navegar ao receber (ex: "dex10://pokemon/25")'
        ),
      url: z
        .string()
        .optional()
        .describe("URL externa a abrir ao tocar na notificação"),
    },
    async ({ title, body, target_type, target_value, deeplink, url }) => {
      const sa = parseServiceAccount(env.FCM_SERVICE_ACCOUNT);

      let fcmTarget: { type: "topic" | "token"; value: string };

      if (target_type === "topic") {
        fcmTarget = { type: "topic", value: target_value };
      } else {
        // token_label → busca no KV
        const key = buildTokenKey(target_value);
        const stored = await env.FCM_TOKENS_KV.get<StoredToken>(key, "json");
        if (!stored) {
          return {
            content: [
              {
                type: "text",
                text: `❌ Token com label "${target_value}" não encontrado. Use a ferramenta list_tokens para ver os tokens disponíveis.`,
              },
            ],
          };
        }
        fcmTarget = { type: "token", value: stored.token };
      }

      const { messageId } = await sendFcmNotification(
        sa,
        env.FCM_PROJECT_ID,
        fcmTarget,
        { title, body, deeplink, url }
      );

      const targetDesc =
        target_type === "topic"
          ? `tópico "${target_value}"`
          : `dispositivo "${target_value}"`;

      return {
        content: [
          {
            type: "text",
            text: [
              `✅ Notificação enviada com sucesso para ${targetDesc}!`,
              ``,
              `📩 **Título:** ${title}`,
              `📝 **Corpo:** ${body}`,
              deeplink ? `🔗 **Deep link:** ${deeplink}` : "",
              url ? `🌐 **URL:** ${url}` : "",
              ``,
              `🆔 Message ID: \`${messageId}\``,
            ]
              .filter(Boolean)
              .join("\n"),
          },
        ],
      };
    }
  );

  // ── Ferramenta: register_token ─────────────────────────────────────────────
  server.tool(
    "register_token",
    "Registra ou atualiza um token FCM de dispositivo no KV, identificado por um label amigável.",
    {
      label: z
        .string()
        .min(1)
        .describe(
          'Label amigável para identificar o dispositivo (ex: "pixel_vinithius", "emulador_dev")'
        ),
      token: z.string().min(10).describe("Token FCM do dispositivo"),
    },
    async ({ label, token }) => {
      const key = buildTokenKey(label);
      const stored: StoredToken = {
        token,
        label,
        registeredAt: new Date().toISOString(),
      };
      await env.FCM_TOKENS_KV.put(key, JSON.stringify(stored));

      return {
        content: [
          {
            type: "text",
            text: `✅ Token registrado com sucesso!\n\n🏷️ **Label:** ${label}\n🔑 **Token:** \`${token.slice(0, 20)}…\`\n📅 **Registrado em:** ${stored.registeredAt}`,
          },
        ],
      };
    }
  );

  // ── Ferramenta: list_tokens ────────────────────────────────────────────────
  server.tool(
    "list_tokens",
    "Lista todos os tokens FCM de dispositivos registrados no KV.",
    {},
    async () => {
      const { keys } = await env.FCM_TOKENS_KV.list({ prefix: "token:" });

      if (keys.length === 0) {
        return {
          content: [
            {
              type: "text",
              text: "📭 Nenhum token registrado ainda. Use register_token para adicionar dispositivos.",
            },
          ],
        };
      }

      const rows: string[] = ["| Label | Token (parcial) | Registrado em |", "| --- | --- | --- |"];

      for (const key of keys) {
        const stored = await env.FCM_TOKENS_KV.get<StoredToken>(
          key.name,
          "json"
        );
        if (stored) {
          rows.push(
            `| ${stored.label} | \`${stored.token.slice(0, 20)}…\` | ${stored.registeredAt} |`
          );
        }
      }

      return {
        content: [
          {
            type: "text",
            text: `📱 **Tokens FCM registrados (${keys.length}):**\n\n${rows.join("\n")}`,
          },
        ],
      };
    }
  );

  // ── Ferramenta: delete_token ───────────────────────────────────────────────
  server.tool(
    "delete_token",
    "Remove um token FCM do KV pelo seu label.",
    {
      label: z.string().describe("Label do token a remover"),
    },
    async ({ label }) => {
      const key = buildTokenKey(label);
      const existing = await env.FCM_TOKENS_KV.get(key);

      if (!existing) {
        return {
          content: [
            {
              type: "text",
              text: `❌ Token com label "${label}" não encontrado.`,
            },
          ],
        };
      }

      await env.FCM_TOKENS_KV.delete(key);

      return {
        content: [
          {
            type: "text",
            text: `🗑️ Token "${label}" removido com sucesso.`,
          },
        ],
      };
    }
  );

  return server;
}

// ─── Worker entry point ───────────────────────────────────────────────────────

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    // CORS preflight
    if (request.method === "OPTIONS") {
      return new Response(null, {
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
          "Access-Control-Allow-Headers": "Content-Type, Accept",
          "Access-Control-Max-Age": "86400",
        },
      });
    }

    // Rota de health check
    if (request.method === "GET" && new URL(request.url).pathname === "/") {
      return new Response(
        JSON.stringify({
          name: "dex10-mcp-push-notification",
          version: "1.0.0",
          status: "ok",
          tools: [
            "send_notification",
            "register_token",
            "list_tokens",
            "delete_token",
          ],
        }),
        { headers: { "Content-Type": "application/json" } }
      );
    }

    // Rota MCP via Web Standard Streamable HTTP (nativo para Workers)
    if (new URL(request.url).pathname === "/mcp") {
      try {
        const server = createMcpServer(env);
        const transport = new WebStandardStreamableHTTPServerTransport({
          sessionIdGenerator: undefined, // stateless — ideal para Workers
        });

        await server.connect(transport);
        const mcpResponse = await transport.handleRequest(request);

        // Adiciona cabeçalhos CORS na resposta MCP
        const headers = new Headers(mcpResponse.headers);
        headers.set("Access-Control-Allow-Origin", "*");

        return new Response(mcpResponse.body, {
          status: mcpResponse.status,
          headers,
        });
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
