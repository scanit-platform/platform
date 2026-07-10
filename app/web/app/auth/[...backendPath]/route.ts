import { proxyBackendRequest } from "@/src/shared/api/backend-proxy";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

export function GET(request: Request) {
  return proxyBackendRequest(request);
}

export function POST(request: Request) {
  return proxyBackendRequest(request);
}
