import type { User } from "@/src/entities/user/types/user";
import { ReceiptScanFlow } from "@/src/features/receipt-scan/ui/receipt-scan-flow";
import { AppShell } from "@/src/widgets/app-shell/ui/app-shell";
import type { GeneralCategory } from "@/src/entities/category/types/category";

type ReceiptScanProps = {
  user: User | null;
  generalCategories: GeneralCategory[];
};

export function ReceiptScan({ user, generalCategories }: ReceiptScanProps) {
  return (
    <AppShell activeItem="receipts" user={user}>
      <section className="scanit-app-page">
        <div className="mx-auto max-w-5xl">
          <h1 className="mb-7 text-center font-serif text-[28px] font-bold text-[var(--scanit-text)]">
            Scan a Receipt
          </h1>
          <ReceiptScanFlow
            userId={user?.id}
            generalCategories={generalCategories}
          />
        </div>
      </section>
    </AppShell>
  );
}
