import Link from "next/link";
import { apiFetch } from "@/src/shared/api/client";
import { getAuthToken, getAuthenticatedUserOrRedirect } from "@/src/features/auth/model/session";
import {
  getReceiptDownloadUrl,
  searchReceipts,
} from "@/src/entities/receipt/api/receipts-service";
import type { Receipt } from "@/src/entities/receipt/types/receipt";
import type {
  CustomCategory,
  GeneralCategory,
} from "@/src/entities/category/types/category";
import { AppShell } from "@/src/widgets/app-shell/ui/app-shell";
import { FileIcon, PlusIcon, ReceiptIcon } from "@/src/shared/ui/icons/icons";

const moneyFormatter = new Intl.NumberFormat("en-US", {
  currency: "USD",
  style: "currency",
});

function formatDate(value: string) {
  const date = new Date(`${value}T00:00:00`);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat("en-US", {
    day: "numeric",
    month: "short",
    year: "numeric",
  }).format(date);
}

function getAmount(receipt: Receipt) {
  return receipt.totalAmount ?? receipt.transactionAmount ?? 0;
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

export async function ReceiptsView() {
  const user = await getAuthenticatedUserOrRedirect();
  const authToken = await getAuthToken();
  const authOptions = authToken ? { authToken } : {};
  const [receipts, categories] = await Promise.all([
    searchReceipts({ userId: user.id }, authOptions).catch(() => []),
    getCategories(authToken ?? ""),
  ]);

  const sortedReceipts = [...receipts].sort((left, right) =>
    right.transactionDate.localeCompare(left.transactionDate),
  );

  return (
    <AppShell activeItem="receipts" user={user}>
      <section className="scanit-app-page">
        <div className="mx-auto max-w-7xl">
          <div className="mb-6 flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
            <div>
              <p className="text-sm font-semibold text-[var(--scanit-primary)]">
                Real backend data
              </p>
              <h1 className="mt-1 font-serif text-[28px] font-bold text-[var(--scanit-text)]">
                Receipts
              </h1>
            </div>
            <div className="flex flex-col gap-2 sm:flex-row">
              <Link
                className="scanit-btn scanit-btn-cta h-11"
                href="/dashboard/scan"
              >
                <ReceiptIcon />
                Scan Receipt
              </Link>
              <Link
                className="scanit-btn scanit-btn-primary h-11"
                href="/dashboard/entry"
              >
                <PlusIcon />
                Add Entry
              </Link>
            </div>
          </div>

          <div className="space-y-3">
            {sortedReceipts.length > 0 ? (
              sortedReceipts.map((receipt) => (
                <article
                  className="scanit-auth-card flex flex-col gap-4 p-5 sm:flex-row sm:items-center sm:justify-between"
                  key={receipt.id}
                >
                  <div className="flex min-w-0 items-center gap-4">
                    <span className="scanit-auth-icon-tile flex h-11 w-11 shrink-0 items-center justify-center">
                      <FileIcon />
                    </span>
                    <div className="min-w-0">
                      <div className="flex flex-wrap items-center gap-2">
                        <h2 className="truncate font-bold text-[var(--scanit-text)]">
                          {receipt.vendorName}
                        </h2>
                        <span className="rounded-md bg-[var(--scanit-primary-soft)] px-2 py-1 text-xs font-bold text-[var(--scanit-primary)]">
                          {receipt.ocrStatus}
                        </span>
                      </div>
                      <p className="mt-1 text-sm text-[var(--scanit-text-secondary)]">
                        {getCategoryLabel(
                          receipt,
                          categories.generalCategories,
                          categories.customCategories,
                        )}
                      </p>
                    </div>
                  </div>
                  <div className="flex items-center justify-between gap-6 sm:justify-end">
                    <p className="text-sm text-[var(--scanit-text-secondary)]">
                      {formatDate(receipt.transactionDate)}
                    </p>
                    <p className="font-serif text-xl font-bold text-[var(--scanit-text)]">
                      {moneyFormatter.format(getAmount(receipt))}
                    </p>
                    {receipt.imageUrl && receipt.imageUrl !== "manual-entry" ? (
                      <a
                        className="text-sm font-semibold text-[var(--scanit-primary)]"
                        href={getReceiptDownloadUrl(receipt.id)}
                      >
                        Download
                      </a>
                    ) : null}
                  </div>
                </article>
              ))
            ) : (
              <section className="scanit-auth-card p-8 text-center">
                <h2 className="font-serif text-2xl font-bold text-[var(--scanit-text)]">
                  No receipts yet
                </h2>
                <p className="mx-auto mt-2 max-w-md text-sm text-[var(--scanit-text-secondary)]">
                  Scan a receipt or add a manual entry to start building your
                  expense history.
                </p>
              </section>
            )}
          </div>
        </div>
      </section>
    </AppShell>
  );
}
