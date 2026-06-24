import { createElement } from "react";
import type { AuthMode } from "@/src/features/auth/model/auth-state";
import { AuthLayout } from "@/src/widgets/auth-layout/ui/auth-layout";

type LoginViewProps = {
  initialMode?: AuthMode;
  verified?: boolean;
};

export function LoginView({ initialMode, verified }: LoginViewProps) {
  return createElement(AuthLayout, {
    initialMode,
    verified,
  });
}
