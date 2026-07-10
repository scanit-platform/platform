import type { Receipt } from "@/src/entities/receipt/types/receipt";

type ReceiptReviewCardProps = {
  receipt: Receipt;
};

const moneyFormatter = new Intl.NumberFormat("en-US", {
  currency: "USD",
  style: "currency",
});

function formatReceiptDate(value: string) {
  const date = new Date(`${value}T00:00:00`);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat("en-US", {
    day: "numeric",
    month: "long",
    year: "numeric",
  }).format(date);
}

export function ReceiptReviewCard({ receipt }: ReceiptReviewCardProps) {
  const amount = receipt.totalAmount ?? receipt.transactionAmount ?? 0;

  return (
    <article className="scanit-auth-card overflow-hidden">
      <div className="border-b border-[var(--scanit-border)] p-5 sm:p-6">
        <div className="grid gap-4 sm:grid-cols-3">
          <div>
            <p className="text-xs font-bold uppercase tracking-[0.08em] text-[var(--scanit-text-muted)]">
              Merchant
            </p>
            <p className="mt-1 font-serif text-2xl font-bold text-[var(--scanit-text)]">
              {receipt.vendorName}
            </p>
          </div>
          <div>
            <p className="text-xs font-bold uppercase tracking-[0.08em] text-[var(--scanit-text-muted)]">
              Date
            </p>
            <p className="mt-2 font-semibold text-[var(--scanit-text)]">
              {formatReceiptDate(receipt.transactionDate)}
            </p>
          </div>
          <div>
            <p className="text-xs font-bold uppercase tracking-[0.08em] text-[var(--scanit-text-muted)]">
              Total
            </p>
            <p className="mt-1 font-serif text-2xl font-bold text-[var(--scanit-primary)]">
              {moneyFormatter.format(amount)}
            </p>
          </div>
        </div>
      </div>

      <div className="p-5 sm:p-6">
        <div className="grid gap-3 text-sm sm:grid-cols-2">
          <div className="rounded-lg border border-[var(--scanit-border)] bg-[var(--scanit-soft)] px-4 py-3">
            <p className="font-semibold text-[var(--scanit-label)]">
              OCR status
            </p>
            <p className="mt-1 font-bold text-[var(--scanit-text)]">
              {receipt.ocrStatus}
            </p>
          </div>
          <div className="rounded-lg border border-[var(--scanit-border)] bg-[var(--scanit-soft)] px-4 py-3">
            <p className="font-semibold text-[var(--scanit-label)]">
              Receipt ID
            </p>
            <p className="mt-1 font-bold text-[var(--scanit-text)]">
              #{receipt.id}
            </p>
          </div>
        </div>

        {receipt.imageUrl ? (
          <p className="mt-4 text-sm text-[var(--scanit-text-secondary)]">
            File:{" "}
            <span className="font-semibold text-[var(--scanit-text)]">
              uploaded successfully
            </span>
          </p>
        ) : null}
      </div>
    </article>
  );
}
