import { AuthExperience } from "@/src/features/auth/components/auth-experience";

type HomePageProps = {
  searchParams: Promise<{
    provider?: string | string[] | undefined;
  }>;
};

const providerNames = {
  google: "Google",
  apple: "Apple",
} as const;

function getProviderNotice(
  provider: string | string[] | undefined,
): string | undefined {
  if (typeof provider !== "string") {
    return undefined;
  }

  const label = providerNames[provider as keyof typeof providerNames];

  if (!label) {
    return undefined;
  }

  return `${label} auth is not connected in this demo yet.`;
}

export default async function HomePage({ searchParams }: HomePageProps) {
  const { provider } = await searchParams;

  return <AuthExperience providerNotice={getProviderNotice(provider)} />;
}
