import { apiFetch } from "@/src/shared/api/client";
import {
  getAuthToken,
  getAuthenticatedUserOrRedirect,
} from "@/src/features/auth/model/session";
import { searchBudgetLimits } from "@/src/entities/budget/api/budget-service";
import type { BudgetLimit } from "@/src/entities/budget/types/budget";
import type {
  CustomCategory,
  GeneralCategory,
} from "@/src/entities/category/types/category";
import { searchReceipts } from "@/src/entities/receipt/api/receipts-service";
import type { Receipt } from "@/src/entities/receipt/types/receipt";
import { DashboardOverview } from "@/src/widgets/dashboard-overview/ui/dashboard-overview";

type DashboardViewProps = {
  mode?: string | string[];
};

function getCurrentPeriod() {
  return new Date().toISOString().slice(0, 7);
}

export async function DashboardView({ mode }: DashboardViewProps) {
  const user = await getAuthenticatedUserOrRedirect();
  const authToken = await getAuthToken();
  const authOptions = authToken ? { authToken } : {};
  const [receipts, budgets, generalCategories, customCategories] = await Promise.all([
    searchReceipts({ userId: user.id }, authOptions).catch<Receipt[]>(() => []),
    searchBudgetLimits(
      {
        period: getCurrentPeriod(),
        userId: user.id,
      },
      authOptions,
    ).catch<BudgetLimit[]>(() => []),
    apiFetch<GeneralCategory[]>("/api/categories/general", {
      method: "GET",
    }).catch<GeneralCategory[]>(() => []),
    authToken
      ? apiFetch<CustomCategory[]>(
          "/api/categories/custom",
          {
            method: "GET",
          },
          { authToken },
        ).catch<CustomCategory[]>(() => [])
      : Promise.resolve([]),
  ]);

  return (
    <DashboardOverview
      budgets={budgets}
      customCategories={customCategories}
      generalCategories={generalCategories}
      mode={mode}
      receipts={receipts}
      user={user}
    />
  );
}
