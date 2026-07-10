import { proxyBackendRequest } from "@/src/shared/api/backend-proxy";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

export function GET(request: Request) {
  return proxyBackendRequest(request);
}

export function POST(request: Request) {
  return proxyBackendRequest(request);
}

export function PUT(request: Request) {
  return proxyBackendRequest(request);
}

export function PATCH(request: Request) {
  return proxyBackendRequest(request);
}

export function DELETE(request: Request) {
  return proxyBackendRequest(request);
}
