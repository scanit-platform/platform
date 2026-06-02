type JsonRecord = Record<string, unknown>;

type ApiFetchOptions = {
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

function getApiBaseUrl() {
  const apiUrl = process.env.VITE_API_URL?.trim();

  if (!apiUrl) {
    throw new ApiError("API URL is not configured. Set VITE_API_URL.");
  }

  return apiUrl.replace(/\/+$/, "");
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
  init: RequestInit,
  options: ApiFetchOptions = {},
): Promise<TResponse> {
  let response: Response;

  try {
    response = await fetch(`${getApiBaseUrl()}${path}`, {
      ...init,
      headers: {
        "Content-Type": "application/json",
        ...init.headers,
      },
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
