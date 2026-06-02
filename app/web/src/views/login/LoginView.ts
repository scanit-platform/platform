import { createElement } from "react";
import type { AuthMode } from "@/src/features/auth/model/auth-state";
import { AuthLayout } from "@/src/widgets/auth-layout/ui/auth-layout";

type LoginViewProps = {
  initialMode?: AuthMode;
};

export function LoginView({ initialMode }: LoginViewProps) {
  return createElement(AuthLayout, {
    initialMode,
  });
}
