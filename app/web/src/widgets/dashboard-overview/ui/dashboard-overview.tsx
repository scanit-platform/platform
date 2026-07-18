import Link from "next/link";
import type { BudgetLimit } from "@/src/entities/budget/types/budget";
import type {
  CustomCategory,
  GeneralCategory,
} from "@/src/entities/category/types/category";
import type { Receipt } from "@/src/entities/receipt/types/receipt";
import type { User } from "@/src/entities/user/types/user";
import { PlusIcon, ReceiptIcon } from "@/src/shared/ui/icons/icons";
import { AppShell } from "@/src/widgets/app-shell/ui/app-shell";

type DashboardOverviewProps = {
  budgets: BudgetLimit[];
  customCategories: CustomCategory[];
  generalCategories: GeneralCategory[];
  mode?: string | string[];
  receipts: Receipt[];
  user: User | null;
};

const moneyFormatter = new Intl.NumberFormat("en-IE", {
  currency: "EUR",
  style: "currency",
});

function getStatusMessage(mode: string | string[] | undefined, isGuest: boolean) {
  if (isGuest) {
    return "Sign in before adding real receipts so your workspace can be saved.";
  }

  if (mode === "signin") {
    return "You are signed in. Your ScanIt workspace is connected to your account.";
  }

  if (mode === "signup") {
    return "Your account was created successfully. Your ScanIt workspace is ready.";
  }

  return "Your ScanIt workspace is connected to your account.";
}

function getCurrentPeriod() {
  return new Date().toISOString().slice(0, 7);
}

function getDaysRemainingInMonth() {
  const now = new Date();
  const endOfMonth = new Date(now.getFullYear(), now.getMonth() + 1, 0);

  return Math.max(endOfMonth.getDate() - now.getDate(), 0);
}

function getReceiptAmount(receipt: Receipt) {
  return receipt.totalAmount ?? receipt.transactionAmount ?? 0;
}

function isReceiptInCurrentPeriod(receipt: Receipt) {
  if (!receipt.transactionDate) return false;
  return receipt.transactionDate.startsWith(getCurrentPeriod());
}

function getCategoryLabel(
  receipt: Receipt,
  generalCategories: GeneralCategory[],
  customCategories: CustomCategory[],
) {
  const customCategory = customCategories.find(
    (category) => category.id === receipt.customCategoryId,
  );

  if (customCategory) {
    return customCategory.name;
  }

  return (
    generalCategories.find((category) => category.id === receipt.generalCategoryId)
      ?.name ?? "Uncategorized"
  );
}

function buildSpendingBars(
  receipts: Receipt[],
  generalCategories: GeneralCategory[],
) {
  const totalsByCategory = new Map<string, number>();

  receipts.filter(isReceiptInCurrentPeriod).forEach((receipt) => {
    const categoryId = receipt.generalCategoryId ?? "uncategorized";
    totalsByCategory.set(
      categoryId,
      (totalsByCategory.get(categoryId) ?? 0) + getReceiptAmount(receipt),
    );
  });

  const rows = [...totalsByCategory.entries()]
    .map(([categoryId, total]) => ({
      label:
        generalCategories.find((category) => category.id === categoryId)?.name ??
        "Uncategorized",
      total,
    }))
    .sort((left, right) => right.total - left.total)
    .slice(0, 6);

  const maxTotal = Math.max(...rows.map((row) => row.total), 1);

  return rows.map((row) => ({
    ...row,
    height: `${Math.max((row.total / maxTotal) * 100, 8)}%`,
  }));
}

export function DashboardOverview({
  budgets,
  customCategories,
  generalCategories,
  mode,
  receipts,
  user,
}: DashboardOverviewProps) {
  const isGuest = !user;
  const monthlyReceipts = receipts.filter(isReceiptInCurrentPeriod);
  const totalSpent = monthlyReceipts.reduce(
    (total, receipt) => total + getReceiptAmount(receipt),
    0,
  );
  const totalBudget = budgets.reduce(
    (total, budget) => total + Number(budget.monthlyLimit),
    0,
  );
  const remainingBudget = totalBudget - totalSpent;
  const spendingBars = buildSpendingBars(receipts, generalCategories);
  const recentReceipts = [...receipts]
    .sort((left, right) => right.transactionDate.localeCompare(left.transactionDate))
    .slice(0, 4);

  const metrics = [
    {
      detail: `${monthlyReceipts.length} receipts this month`,
      label: "Total Spent",
      value: moneyFormatter.format(totalSpent),
    },
    {
      detail:
        totalBudget > 0
          ? `of ${moneyFormatter.format(totalBudget)} this month`
          : "No budget set",
      label: "Budget Remaining",
      tone: remainingBudget < 0 ? "danger" : "success",
      value: moneyFormatter.format(remainingBudget),
    },
    {
      detail: "in the current month",
      label: "Days Remaining",
      value: String(getDaysRemainingInMonth()),
    },
  ];

  return (
    <AppShell activeItem="dashboard" user={user}>
      <section className="scanit-app-page">
        <div className="mx-auto max-w-7xl">
          <div className="mb-6 flex flex-col justify-between gap-3 sm:flex-row sm:items-end">
            <div>
              <p className="text-sm font-semibold text-(--scanit-primary)">
                {isGuest ? "Guest workspace" : user.email}
              </p>
              <h1 className="mt-1 font-serif text-[28px] font-bold text-(--scanit-text)">
                Dashboard
              </h1>
            </div>
            {isGuest ? (
              <div className="flex gap-2">
                <Link
                  href="/login?mode=signin"
                  className="scanit-btn scanit-btn-secondary h-10"
                >
                  Sign in
                </Link>
                <Link
                  href="/login?mode=signup"
                  className="scanit-btn scanit-btn-primary h-10"
                >
                  Create account
                </Link>
              </div>
            ) : null}
          </div>

          <div
            className={[
              "mb-6 rounded-lg border px-4 py-3 text-sm font-medium",
              isGuest
                ? "border-(--scanit-warning-soft) bg-(--scanit-warning-softer) text-(--scanit-warning-text)"
                : "border-(--scanit-primary-softer) bg-(--scanit-primary-soft) text-(--scanit-primary)",
            ].join(" ")}
          >
            {getStatusMessage(mode, isGuest)}
          </div>

          <div className="mb-6 grid gap-4 md:grid-cols-2">
            <Link
              className="scanit-auth-card group flex min-h-36 items-center justify-between gap-5 p-5 transition-colors hover:border-(--scanit-primary)"
              href={isGuest ? "/login?mode=signin" : "/dashboard/scan"}
            >
              <div>
                <span className="scanit-auth-icon-tile mb-4 flex h-11 w-11 items-center justify-center">
                  <ReceiptIcon />
                </span>
                <h2 className="font-serif text-2xl font-bold text-(--scanit-text)">
                  Scan Receipt
                </h2>
                <p className="mt-2 text-sm text-(--scanit-text-secondary)">
                  Upload an image or PDF and save the receipt to your account.
                </p>
              </div>
              <span className="scanit-btn scanit-btn-cta h-11 shrink-0">
                Start
              </span>
            </Link>

            <Link
              className="scanit-auth-card group flex min-h-36 items-center justify-between gap-5 p-5 transition-colors hover:border-(--scanit-primary)"
              href={isGuest ? "/login?mode=signin" : "/dashboard/entry"}
            >
              <div>
                <span className="scanit-auth-icon-tile mb-4 flex h-11 w-11 items-center justify-center">
                  <PlusIcon />
                </span>
                <h2 className="font-serif text-2xl font-bold text-(--scanit-text)">
                  Add Entry
                </h2>
                <p className="mt-2 text-sm text-(--scanit-text-secondary)">
                  Create a manual receipt or expense entry from a short form.
                </p>
              </div>
              <span className="scanit-btn scanit-btn-primary h-11 shrink-0">
                Add
              </span>
            </Link>
          </div>

          <div className="grid gap-5 lg:grid-cols-3">
            {metrics.map((metric) => (
              <article key={metric.label} className="scanit-auth-card p-5">
                <p className="text-sm font-medium text-(--scanit-label)">
                  {metric.label}
                </p>
                <p
                  className={[
                    "mt-4 font-serif text-4xl font-bold",
                    metric.tone === "danger"
                      ? "text-(--scanit-danger)"
                      : "text-(--scanit-text)",
                  ].join(" ")}
                >
                  {metric.value}
                </p>
                <p className="mt-2 text-sm text-(--scanit-text-secondary)">
                  {metric.detail}
                </p>
              </article>
            ))}
          </div>

          <div className="mt-8 grid gap-5 lg:grid-cols-[minmax(0,1.35fr)_minmax(20rem,0.65fr)]">
            <section className="scanit-auth-card p-5">
              <div className="mb-5 flex items-center justify-between gap-4">
                <h2 className="font-serif text-xl font-bold text-(--scanit-text)">
                  Monthly Spending
                </h2>
                <span className="rounded-lg border border-(--scanit-border) bg-(--scanit-soft) px-4 py-2 text-sm font-medium">
                  {getCurrentPeriod()}
                </span>
              </div>
              {spendingBars.length > 0 ? (
                <div className="grid min-h-72 grid-cols-2 items-end gap-3 sm:grid-cols-3 lg:grid-cols-6">
                  {spendingBars.map((bar) => (
                    <div
                      key={bar.label}
                      className="flex h-72 flex-col items-center justify-end gap-2"
                    >
                      <p className="text-xs font-semibold text-(--scanit-label)">
                        {moneyFormatter.format(bar.total)}
                      </p>
                      <div className="flex h-52 w-full items-end rounded-lg bg-(--scanit-soft) p-1">
                        <div
                          className="w-full rounded-md bg-(--scanit-primary)"
                          style={{ height: bar.height }}
                        />
                      </div>
                      <p className="max-w-full truncate text-xs text-(--scanit-text-secondary)">
                        {bar.label}
                      </p>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="rounded-lg border border-(--scanit-border) bg-(--scanit-soft) px-4 py-6 text-center text-sm text-(--scanit-text-secondary)">
                  No spending data for this month yet.
                </p>
              )}
            </section>

            <section className="scanit-auth-card p-5">
              <h2 className="font-serif text-xl font-bold text-(--scanit-text)">
                Recent Activity
              </h2>
              <div className="mt-5 space-y-3">
                {recentReceipts.length > 0 ? (
                  recentReceipts.map((receipt) => (
                    <div
                      key={receipt.id}
                      className="flex items-center justify-between gap-4 rounded-lg border border-(--scanit-border) px-4 py-3"
                    >
                      <div className="min-w-0">
                        <p className="truncate font-semibold text-(--scanit-text)">
                          {receipt.vendorName}
                        </p>
                        <p className="text-sm text-(--scanit-text-secondary)">
                          {getCategoryLabel(
                            receipt,
                            generalCategories,
                            customCategories,
                          )}
                        </p>
                      </div>
                      <p className="font-serif text-lg font-bold">
                        {moneyFormatter.format(getReceiptAmount(receipt))}
                      </p>
                    </div>
                  ))
                ) : (
                  <p className="rounded-lg border border-(--scanit-border) bg-(--scanit-soft) px-4 py-6 text-center text-sm text-(--scanit-text-secondary)">
                    No receipts yet.
                  </p>
                )}
              </div>
            </section>
          </div>
        </div>
      </section>
    </AppShell>
  );
}
