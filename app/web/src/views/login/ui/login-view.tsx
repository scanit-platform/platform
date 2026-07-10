import type { AuthMode } from "@/src/features/auth/model/auth-state";
import { AuthLayout } from "@/src/widgets/auth-layout/ui/auth-layout";

type LoginViewProps = {
  initialMode?: AuthMode;
  verified?: boolean;
};

export function LoginView({ initialMode, verified }: LoginViewProps) {
  return <AuthLayout initialMode={initialMode} verified={verified} />;
}
