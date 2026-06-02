import { LoginView } from "@/src/views/login/LoginView";
import type { AuthMode } from "@/src/features/auth/model/auth-state";

type HomePageProps = {
  searchParams: Promise<{
    mode?: string | string[] | undefined;
  }>;
};

function getInitialMode(mode: string | string[] | undefined): AuthMode {
  const value = Array.isArray(mode) ? mode[0] : mode;

  return value === "signin" ? "signin" : "signup";
}

export default async function HomePage({ searchParams }: HomePageProps) {
  const { mode } = await searchParams;

  return <LoginView initialMode={getInitialMode(mode)} />;
}
