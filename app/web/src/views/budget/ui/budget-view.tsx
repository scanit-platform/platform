import { revalidatePath } from "next/cache";
import { apiFetch } from "@/src/shared/api/client";
import {
  getAuthToken,
  getAuthenticatedUserOrRedirect,
} from "@/src/features/auth/model/session";
import {
  deleteBudgetLimit,
  searchBudgetLimits,
} from "@/src/entities/budget/api/budget-service";
import { searchReceipts } from "@/src/entities/receipt/api/receipts-service";
import type { BudgetLimit } from "@/src/entities/budget/types/budget";
import type { Receipt } from "@/src/entities/receipt/types/receipt";
import type {
  CustomCategory,
  GeneralCategory,
} from "@/src/entities/category/types/category";
import { AppShell } from "@/src/widgets/app-shell/ui/app-shell";

const moneyFormatter = new Intl.NumberFormat("en-IE", {
  currency: "EUR",
  maximumFractionDigits: 0,
  style: "currency",
});

function getCurrentPeriod() {
  return new Date().toISOString().slice(0, 7);
}

function getReceiptAmount(receipt: Receipt) {
  return receipt.totalAmount ?? receipt.transactionAmount ?? 0;
}

function getSpentForBudget(budget: BudgetLimit, receipts: Receipt[]) {
  return receipts
    .filter((receipt) => {
      const sameGeneralCategory =
        receipt.generalCategoryId === budget.generalCategoryId;
      const sameCustomCategory =
        !budget.customCategoryId ||
        receipt.customCategoryId === budget.customCategoryId;

      return sameGeneralCategory && sameCustomCategory;
    })
    .reduce((total, receipt) => total + getReceiptAmount(receipt), 0);
}

function getCategoryLabel(
  budget: BudgetLimit,
  generalCategories: GeneralCategory[],
  customCategories: CustomCategory[],
) {
  const customCategory = customCategories.find(
    (category) => category.id === budget.customCategoryId,
  );

  if (customCategory) {
    return customCategory.name;
  }

  return (
    generalCategories.find((category) => category.id === budget.generalCategoryId)
      ?.name ?? "Budget"
  );
}

async function getCategories(authToken: string) {
  const [generalCategories, customCategories] = await Promise.all([
    apiFetch<GeneralCategory[]>("/api/categories/general", {
      method: "GET",
    }),
    apiFetch<CustomCategory[]>(
      "/api/categories/custom",
      {
        method: "GET",
      },
      { authToken },
    ).catch(() => []),
  ]);

  return { customCategories, generalCategories };
}

async function createBudgetAction(formData: FormData) {
  "use server";

  const user = await getAuthenticatedUserOrRedirect();
  const authToken = await getAuthToken();
  const generalCategoryId = String(formData.get("generalCategoryId") ?? "");
  const customCategoryId = String(formData.get("customCategoryId") ?? "");
  const monthlyLimit = Number(formData.get("monthlyLimit") ?? 0);
  const period = String(formData.get("period") ?? getCurrentPeriod());

  await apiFetch(
    "/api/budget",
    {
      body: JSON.stringify({
        customCategoryId: customCategoryId || null,
        generalCategoryId,
        monthlyLimit,
        period,
        userId: user.id,
      }),
      method: "POST",
    },
    authToken ? { authToken } : {},
  );

  revalidatePath("/dashboard/budget");
}

async function deleteBudgetAction(formData: FormData) {
  "use server";

  const id = Number(formData.get("id"));
  const authToken = await getAuthToken();

  if (Number.isFinite(id)) {
    await deleteBudgetLimit(id, authToken ? { authToken } : {});
    revalidatePath("/dashboard/budget");
  }
}

export async function BudgetView() {
  const user = await getAuthenticatedUserOrRedirect();
  const authToken = await getAuthToken();
  const authOptions = authToken ? { authToken } : {};
  const period = getCurrentPeriod();
  const [budgets, receipts, categories] = await Promise.all([
    searchBudgetLimits({ period, userId: user.id }, authOptions).catch(() => []),
    searchReceipts({ userId: user.id }, authOptions).catch(() => []),
    getCategories(authToken ?? ""),
  ]);

  const totalBudget = budgets.reduce(
    (total, budget) => total + Number(budget.monthlyLimit),
    0,
  );

  return (
    <AppShell activeItem="budget" user={user}>
      <section className="scanit-app-page">
        <div className="mx-auto max-w-7xl">
          <div className="mb-6 flex flex-col justify-between gap-4 lg:flex-row lg:items-end">
            <div>
              <p className="text-sm font-semibold text-[var(--scanit-primary)]">
                {period}
              </p>
              <h1 className="mt-1 font-serif text-[28px] font-bold text-[var(--scanit-text)]">
                Budget Settings
              </h1>
              <p className="mt-3 text-sm text-[var(--scanit-text-secondary)]">
                Monthly budget:{" "}
                <span className="font-bold text-[var(--scanit-text)]">
                  {moneyFormatter.format(totalBudget)}
                </span>
              </p>
            </div>
            <form
              action={createBudgetAction}
              className="scanit-auth-card grid gap-3 p-4 sm:grid-cols-[minmax(10rem,1fr)_minmax(8rem,0.7fr)_minmax(7rem,0.5fr)_auto]"
            >
              <select
                className="auth-glass-field h-11 rounded-lg px-3 text-sm"
                name="generalCategoryId"
                required
              >
                {categories.generalCategories.map((category) => (
                  <option key={category.id} value={category.id}>
                    {category.name}
                  </option>
                ))}
              </select>
              <input
                className="auth-glass-field h-11 rounded-lg px-3 text-sm"
                defaultValue={period}
                name="period"
                pattern="\d{4}-\d{2}"
                required
              />
              <input
                className="auth-glass-field h-11 rounded-lg px-3 text-sm"
                min="1"
                name="monthlyLimit"
                placeholder="Limit"
                required
                step="0.01"
                type="number"
              />
              <button className="scanit-btn scanit-btn-primary h-11" type="submit">
                Add
              </button>
            </form>
          </div>

          <div className="space-y-4">
            {budgets.length > 0 ? (
              budgets.map((budget) => {
                const spent = getSpentForBudget(budget, receipts);
                const monthlyLimit = Number(budget.monthlyLimit);
                const percent = monthlyLimit > 0 ? (spent / monthlyLimit) * 100 : 0;
                const isOverBudget = percent > 100;

                return (
                  <article className="scanit-auth-card p-5" key={budget.id}>
                    <div className="flex items-start justify-between gap-4">
                      <div>
                        <h2 className="font-bold text-[var(--scanit-text)]">
                          {getCategoryLabel(
                            budget,
                            categories.generalCategories,
                            categories.customCategories,
                          )}
                        </h2>
                        <p
                          className={[
                            "mt-3 font-bold",
                            isOverBudget
                              ? "text-[var(--scanit-danger)]"
                              : "text-[var(--scanit-text)]",
                          ].join(" ")}
                        >
                          {moneyFormatter.format(spent)} spent
                        </p>
                      </div>
                      <div className="text-right">
                        <span
                          className={[
                            "rounded-full px-3 py-1 text-xs font-bold",
                            isOverBudget
                              ? "bg-[var(--scanit-danger-softer)] text-[var(--scanit-danger-text)]"
                              : "bg-[var(--scanit-primary-softer)] text-[var(--scanit-primary)]",
                          ].join(" ")}
                        >
                          {Math.round(percent)}%
                        </span>
                        <p className="mt-3 text-sm text-[var(--scanit-text-secondary)]">
                          {isOverBudget
                            ? `Over by ${moneyFormatter.format(spent - monthlyLimit)}`
                            : `${moneyFormatter.format(monthlyLimit - spent)} remaining`}
                        </p>
                      </div>
                    </div>
                    <div className="mt-4 h-2 rounded-full bg-[var(--scanit-soft)]">
                      <div
                        className={[
                          "h-full rounded-full",
                          isOverBudget
                            ? "bg-[var(--scanit-danger)]"
                            : "bg-[var(--scanit-primary)]",
                        ].join(" ")}
                        style={{ width: `${Math.min(percent, 100)}%` }}
                      />
                    </div>
                    <div className="mt-3 flex items-center justify-between text-sm text-[var(--scanit-text-secondary)]">
                      <span>{moneyFormatter.format(monthlyLimit)} budget</span>
                      <form action={deleteBudgetAction}>
                        <input name="id" type="hidden" value={budget.id} />
                        <button
                          className="font-semibold text-[var(--scanit-danger)]"
                          type="submit"
                        >
                          Delete
                        </button>
                      </form>
                    </div>
                  </article>
                );
              })
            ) : (
              <section className="scanit-auth-card p-8 text-center">
                <h2 className="font-serif text-2xl font-bold text-[var(--scanit-text)]">
                  No budgets yet
                </h2>
                <p className="mx-auto mt-2 max-w-md text-sm text-[var(--scanit-text-secondary)]">
                  Add a monthly category limit to start tracking spend against
                  your budget.
                </p>
              </section>
            )}
          </div>
        </div>
      </section>
    </AppShell>
  );
}
