import { cookies } from "next/headers";
import { authTokenCookieName } from "@/src/features/auth/model/auth-cookie";
import { getApiBaseUrl } from "@/src/shared/api/client";

const requestHeadersToDrop = [
  "connection",
  "content-length",
  "cookie",
  "host",
  "keep-alive",
  "origin",
  "proxy-authenticate",
  "proxy-authorization",
  "te",
  "trailer",
  "transfer-encoding",
  "upgrade",
];

const responseHeadersToDrop = [
  "connection",
  "content-encoding",
  "content-length",
  "keep-alive",
  "proxy-authenticate",
  "proxy-authorization",
  "te",
  "trailer",
  "transfer-encoding",
  "upgrade",
];

function canHaveBody(method: string) {
  return method !== "GET" && method !== "HEAD";
}

async function createBackendHeaders(request: Request) {
  const headers = new Headers(request.headers);

  requestHeadersToDrop.forEach((header) => headers.delete(header));

  const cookieStore = await cookies();
  const token = cookieStore.get(authTokenCookieName)?.value;

  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  } else {
    headers.delete("Authorization");
  }

  return headers;
}

function createResponseHeaders(headers: Headers) {
  const responseHeaders = new Headers(headers);

  responseHeadersToDrop.forEach((header) => responseHeaders.delete(header));

  return responseHeaders;
}

export async function proxyBackendRequest(request: Request) {
  const requestUrl = new URL(request.url);
  const targetUrl = `${getApiBaseUrl()}${requestUrl.pathname}${requestUrl.search}`;
  const headers = await createBackendHeaders(request);

  try {
    const backendResponse = await fetch(targetUrl, {
      body: canHaveBody(request.method) ? await request.arrayBuffer() : undefined,
      cache: "no-store",
      headers,
      method: request.method,
      redirect: "manual",
    });

    return new Response(backendResponse.body, {
      headers: createResponseHeaders(backendResponse.headers),
      status: backendResponse.status,
      statusText: backendResponse.statusText,
    });
  } catch {
    return Response.json(
      { message: "Unable to reach the backend service." },
      { status: 502 },
    );
  }
}
