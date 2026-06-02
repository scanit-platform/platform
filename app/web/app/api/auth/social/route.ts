import { NextResponse } from "next/server";

const supportedProviders = new Set(["google", "apple"]);

export async function GET(request: Request) {
  const requestUrl = new URL(request.url);
  const provider = requestUrl.searchParams.get("provider");

  if (!provider || !supportedProviders.has(provider)) {
    return NextResponse.redirect(new URL("/", requestUrl));
  }

  return NextResponse.redirect(new URL(`/?provider=${provider}`, requestUrl));
}
