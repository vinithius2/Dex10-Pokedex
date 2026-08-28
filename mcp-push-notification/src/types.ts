/**
 * Tipos compartilhados do MCP Push Notification Server
 */

export interface ServiceAccount {
  type: string;
  project_id: string;
  private_key_id: string;
  private_key: string;
  client_email: string;
  client_id: string;
  auth_uri: string;
  token_uri: string;
}

/** Configuração de um app Firebase registrado no servidor */
export interface AppConfig {
  /** Slug único do app, ex: "dex10", "myapp" */
  name: string;
  /** ID do projeto Firebase */
  projectId: string;
  /** Service Account completa (armazenada encriptada no KV) */
  serviceAccount: ServiceAccount;
  /**
   * Locales suportados pelo app, ex: ["pt", "en", "es", "ja"].
   * Usados para saber para quais tópicos lang_XX enviar quando target = "all".
   */
  locales: string[];
  /** Locale padrão para fallback, ex: "en" */
  defaultLocale: string;
  registeredAt: string;
}

/** Registro de dispositivo com token FCM, locale e país */
export interface DeviceRecord {
  /** Label amigável, ex: "pixel_vinithius" */
  label: string;
  /** Token FCM do dispositivo */
  token: string;
  /** Locale do dispositivo, ex: "pt", "en" */
  locale: string;
  /** Código ISO 3166-1 alpha-2 do país, ex: "BR", "US" */
  countryCode: string;
  /** Nome do app ao qual este device pertence */
  appName: string;
  registeredAt: string;
}

/** Uma tradução de notificação para um idioma específico */
export interface NotificationTranslation {
  locale: string;
  title: string;
  body: string;
}

/** Tipos de destino para envio de notificação */
export type TargetType = "all" | "language" | "country" | "topic" | "token";

/** Resultado do envio de uma mensagem FCM individual */
export interface SendResult {
  /** Tópico ou token que recebeu a mensagem */
  destination: string;
  /** Locale da tradução enviada */
  locale?: string;
  /** Message ID retornado pelo FCM */
  messageId?: string;
  status: "sent" | "error";
  error?: string;
}
