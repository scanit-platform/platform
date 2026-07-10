import { LoginView } from "@/src/views/login/ui/login-view";
import type { AuthMode } from "@/src/features/auth/model/auth-state";

type HomePageProps = {
  searchParams: Promise<{
    mode?: string | string[] | undefined;
    verified?: string;
  }>;
};

function getInitialMode(mode: string | string[] | undefined): AuthMode {
  const value = Array.isArray(mode) ? mode[0] : mode;

  return value === "signin" ? "signin" : "signup";
}

export default async function HomePage({ searchParams }: HomePageProps) {
  const { mode, verified } = await searchParams;

  return (<LoginView initialMode={getInitialMode(mode)} 
        verified={verified === "true"} />);
}
