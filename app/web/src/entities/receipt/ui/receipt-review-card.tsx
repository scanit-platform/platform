import type { ExtractedReceipt } from "@/src/entities/receipt/types/receipt";

type ReceiptReviewCardProps = {
  receipt: ExtractedReceipt;
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
  return (
    <article className="scanit-auth-card overflow-hidden">
      <div className="border-b border-[var(--scanit-border)] p-5 sm:p-6">
        <div className="grid gap-4 sm:grid-cols-3">
          <div>
            <p className="text-xs font-bold uppercase tracking-[0.08em] text-[var(--scanit-text-muted)]">
              Merchant
            </p>
            <p className="mt-1 font-serif text-2xl font-bold text-[var(--scanit-text)]">
              {receipt.merchant}
            </p>
          </div>
          <div>
            <p className="text-xs font-bold uppercase tracking-[0.08em] text-[var(--scanit-text-muted)]">
              Date
            </p>
            <p className="mt-2 font-semibold text-[var(--scanit-text)]">
              {formatReceiptDate(receipt.date)}
            </p>
          </div>
          <div>
            <p className="text-xs font-bold uppercase tracking-[0.08em] text-[var(--scanit-text-muted)]">
              Total
            </p>
            <p className="mt-1 font-serif text-2xl font-bold text-[var(--scanit-primary)]">
              {moneyFormatter.format(receipt.total)}
            </p>
          </div>
        </div>
      </div>

      <div className="p-5 sm:p-6">
        <h2 className="font-serif text-xl font-bold text-[var(--scanit-text)]">
          Items
        </h2>
        <div className="mt-4 divide-y divide-[var(--scanit-border)] rounded-lg border border-[var(--scanit-border)]">
          {receipt.items.map((item) => (
            <div
              key={item.id}
              className="grid grid-cols-[minmax(0,1fr)_4rem_5rem] items-center gap-3 px-4 py-3 text-sm"
            >
              <p className="min-w-0 truncate font-semibold text-[var(--scanit-text)]">
                {item.name}
              </p>
              <p className="text-center text-[var(--scanit-text-secondary)]">
                x{item.quantity}
              </p>
              <p className="text-right font-bold text-[var(--scanit-text)]">
                {moneyFormatter.format(item.amount)}
              </p>
            </div>
          ))}
        </div>

        {receipt.sourceFileName ? (
          <p className="mt-4 text-sm text-[var(--scanit-text-secondary)]">
            Source file:{" "}
            <span className="font-semibold text-[var(--scanit-text)]">
              {receipt.sourceFileName}
            </span>
          </p>
        ) : null}
      </div>
    </article>
  );
}
