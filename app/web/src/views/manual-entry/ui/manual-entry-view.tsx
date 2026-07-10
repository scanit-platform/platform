import { apiFetch } from "@/src/shared/api/client";
import { getAuthToken, getAuthenticatedUserOrRedirect } from "@/src/features/auth/model/session";
import type {
  CustomCategory,
  GeneralCategory,
} from "@/src/entities/category/types/category";
import { ManualReceiptForm } from "@/src/features/receipt-manual-entry/ui/manual-receipt-form";
import { AppShell } from "@/src/widgets/app-shell/ui/app-shell";

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

export async function ManualEntryView() {
  const user = await getAuthenticatedUserOrRedirect();
  const authToken = await getAuthToken();
  const { customCategories, generalCategories } = await getCategories(
    authToken ?? "",
  );

  return (
    <AppShell activeItem="receipts" user={user}>
      <section className="scanit-app-page">
        <div className="mx-auto max-w-4xl">
          <div className="mb-6">
            <p className="text-sm font-semibold text-[var(--scanit-primary)]">
              Manual expense
            </p>
            <h1 className="mt-1 font-serif text-[28px] font-bold text-[var(--scanit-text)]">
              Add Entry
            </h1>
          </div>
          <ManualReceiptForm
            customCategories={customCategories}
            generalCategories={generalCategories}
            userId={user.id}
          />
        </div>
      </section>
    </AppShell>
  );
}
