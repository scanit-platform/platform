import { createElement } from "react";
import { getAuthenticatedUser } from "@/src/features/auth/model/session";
import { DashboardOverview } from "@/src/widgets/dashboard-overview/ui/dashboard-overview";

type DashboardViewProps = {
  mode?: string | string[];
};

export async function DashboardView({ mode }: DashboardViewProps) {
  const user = await getAuthenticatedUser();

  return createElement(DashboardOverview, {
    mode,
    user,
  });
}
