type JsonRecord = Record<string, unknown>;

type ApiFetchOptions = {
  authToken?: string;
  getErrorMessage?: (status: number, payload: unknown) => string | undefined;
};

export class ApiError extends Error {
  status?: number;

  constructor(message: string, status?: number) {
    super(message);
    this.name = "ApiError";
    this.status = status;
  }
}

export function getApiBaseUrl() {
  const apiUrl =
    process.env.NEXT_PUBLIC_API_URL?.trim() ||
    process.env.API_URL?.trim() ||
    "http://localhost:8080";

  return apiUrl.replace(/\/+$/, "");
}

export function getApiRequestUrl(path: string) {
  if (/^https?:\/\//i.test(path)) {
    return path;
  }

  if (
    typeof window !== "undefined" &&
    (path.startsWith("/api/") || path.startsWith("/auth/"))
  ) {
    return path;
  }

  return `${getApiBaseUrl()}${path}`;
}

function isJsonRecord(value: unknown): value is JsonRecord {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

function getStringField(payload: unknown, key: string) {
  if (!isJsonRecord(payload)) {
    return undefined;
  }

  const value = payload[key];

  return typeof value === "string" ? value : undefined;
}

function getDefaultErrorMessage(status: number, payload: unknown) {
  const backendMessage =
    getStringField(payload, "message") ??
    getStringField(payload, "error") ??
    getStringField(payload, "detail");

  if (backendMessage) {
    return backendMessage;
  }

  if (status === 400) {
    return "Some details are invalid. Check the highlighted fields and try again.";
  }

  if (status === 401) {
    return "Authentication is required.";
  }

  if (status === 403) {
    return "You do not have permission to perform this action.";
  }

  if (status >= 500) {
    return "The server is having trouble right now. Try again in a moment.";
  }

  return "Request failed. Try again.";
}

async function readJson(response: Response) {
  const text = await response.text();

  if (!text) {
    return undefined;
  }

  try {
    return JSON.parse(text) as unknown;
  } catch {
    return undefined;
  }
}

export async function apiFetch<TResponse>(
  path: string,
  init: RequestInit = {},
  options: ApiFetchOptions = {},
): Promise<TResponse> {
  let response: Response;
  const headers = new Headers(init.headers);

  if (!(init.body instanceof FormData) && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  if (options.authToken && !headers.has("Authorization")) {
    headers.set("Authorization", `Bearer ${options.authToken}`);
  }

  try {
    response = await fetch(getApiRequestUrl(path), {
      ...init,
      headers,
      cache: "no-store",
    });
  } catch {
    throw new ApiError(
      "Unable to reach the server. Check your connection and try again.",
    );
  }

  const payload = await readJson(response);

  if (!response.ok) {
    const message =
      options.getErrorMessage?.(response.status, payload) ??
      getDefaultErrorMessage(response.status, payload);

    throw new ApiError(message, response.status);
  }

  return payload as TResponse;
}

export function createQueryString(
  params: Record<string, string | number | null | undefined>,
) {
  const searchParams = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value !== null && value !== undefined && value !== "") {
      searchParams.set(key, String(value));
    }
  });

  const queryString = searchParams.toString();

  return queryString ? `?${queryString}` : "";
}
