/**
 * FCM HTTP v1 API helper.
 * Autentica via Service Account (JWT → OAuth2) e envia notificações.
 */

import type { ServiceAccount } from "./types.js";

export type { ServiceAccount };

export interface FcmMessage {
  title: string;
  body: string;
  deeplink?: string;
  url?: string;
}

export type FcmTarget =
  | { type: "topic"; value: string }
  | { type: "token"; value: string };

// ─── JWT / OAuth2 ────────────────────────────────────────────────────────────

function base64urlEncode(data: ArrayBuffer | string): string {
  const bytes =
    typeof data === "string"
      ? new TextEncoder().encode(data)
      : new Uint8Array(data);
  let binary = "";
  for (let i = 0; i < bytes.length; i++) {
    binary += String.fromCharCode(bytes[i]);
  }
  return btoa(binary).replace(/\+/g, "-").replace(/\//g, "_").replace(/=/g, "");
}

function pemToArrayBuffer(pem: string): ArrayBuffer {
  const base64 = pem
    .replace(/-----BEGIN PRIVATE KEY-----/, "")
    .replace(/-----END PRIVATE KEY-----/, "")
    .replace(/\s+/g, "");
  const binary = atob(base64);
  const buffer = new ArrayBuffer(binary.length);
  const view = new Uint8Array(buffer);
  for (let i = 0; i < binary.length; i++) {
    view[i] = binary.charCodeAt(i);
  }
  return buffer;
}

async function signJwt(sa: ServiceAccount): Promise<string> {
  const now = Math.floor(Date.now() / 1000);

  const header = base64urlEncode(JSON.stringify({ alg: "RS256", typ: "JWT" }));
  const payload = base64urlEncode(
    JSON.stringify({
      iss: sa.client_email,
      scope: "https://www.googleapis.com/auth/firebase.messaging",
      aud: "https://oauth2.googleapis.com/token",
      iat: now,
      exp: now + 3600,
    })
  );

  const signingInput = `${header}.${payload}`;
  const keyData = pemToArrayBuffer(sa.private_key);

  const cryptoKey = await crypto.subtle.importKey(
    "pkcs8",
    keyData,
    { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false,
    ["sign"]
  );

  const signature = await crypto.subtle.sign(
    "RSASSA-PKCS1-v1_5",
    cryptoKey,
    new TextEncoder().encode(signingInput)
  );

  return `${signingInput}.${base64urlEncode(signature)}`;
}

async function getAccessToken(sa: ServiceAccount): Promise<string> {
  const jwt = await signJwt(sa);

  const resp = await fetch("https://oauth2.googleapis.com/token", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      grant_type: "urn:ietf:params:oauth:grant-type:jwt-bearer",
      assertion: jwt,
    }),
  });

  if (!resp.ok) {
    const err = await resp.text();
    throw new Error(`Falha ao obter access token OAuth2: ${err}`);
  }

  const { access_token } = (await resp.json()) as { access_token: string };
  return access_token;
}

// ─── Envio FCM ───────────────────────────────────────────────────────────────

export async function sendFcmMessage(
  sa: ServiceAccount,
  projectId: string,
  target: FcmTarget,
  message: FcmMessage
): Promise<string> {
  const accessToken = await getAccessToken(sa);

  // Payload de dados extras (deeplink / url)
  const data: Record<string, string> = {};
  if (message.deeplink) data["deeplink"] = message.deeplink;
  if (message.url) data["url"] = message.url;

  const targetField =
    target.type === "topic"
      ? { topic: target.value }
      : { token: target.value };

  const body = {
    message: {
      ...targetField,
      notification: { title: message.title, body: message.body },
      ...(Object.keys(data).length > 0 ? { data } : {}),
      android: {
        priority: "HIGH",
        notification: { channel_id: "default_channel" },
      },
    },
  };

  const url = `https://fcm.googleapis.com/v1/projects/${projectId}/messages:send`;
  const resp = await fetch(url, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    },
    body: JSON.stringify(body),
  });

  if (!resp.ok) {
    const err = await resp.text();
    throw new Error(`FCM error ${resp.status}: ${err}`);
  }

  const result = (await resp.json()) as { name: string };
  return result.name.split("/").pop() ?? result.name;
}
