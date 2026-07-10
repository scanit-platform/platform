import type { User } from "@/src/entities/user/types/user";
import { ReceiptScanFlow } from "@/src/features/receipt-scan/ui/receipt-scan-flow";
import { AppShell } from "@/src/widgets/app-shell/ui/app-shell";

type ReceiptScanProps = {
  user: User | null;
};

export function ReceiptScan({ user }: ReceiptScanProps) {
  return (
    <AppShell activeItem="receipts" user={user}>
      <section className="scanit-app-page">
        <div className="mx-auto max-w-5xl">
          <h1 className="mb-7 text-center font-serif text-[28px] font-bold text-[var(--scanit-text)]">
            Scan a Receipt
          </h1>
          <ReceiptScanFlow userId={user?.id} />
        </div>
      </section>
    </AppShell>
  );
}
