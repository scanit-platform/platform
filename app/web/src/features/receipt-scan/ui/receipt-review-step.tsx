import { ReceiptReviewCard } from "@/src/entities/receipt/ui/receipt-review-card";
import type { Receipt } from "@/src/entities/receipt/types/receipt";
import { Button } from "@/src/shared/ui/button/button";
import { CheckIcon, RefreshIcon } from "@/src/shared/ui/icons/icons";

type ReceiptReviewStepProps = {
  isConfirmed: boolean;
  onConfirm: () => void;
  onReset: () => void;
  receipt: Receipt | null;
};

export function ReceiptReviewStep({
  isConfirmed,
  onConfirm,
  onReset,
  receipt,
}: ReceiptReviewStepProps) {
  if (!receipt) {
    return null;
  }

  return (
    <div className="mx-auto max-w-3xl">
      {isConfirmed ? (
        <div className="mb-4 rounded-lg border border-[var(--scanit-primary-softer)] bg-[var(--scanit-primary-soft)] px-4 py-3 text-sm font-semibold text-[var(--scanit-primary)]">
          Receipt uploaded and saved.
        </div>
      ) : null}

      <ReceiptReviewCard receipt={receipt} />

      <div className="mt-5 flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
        <Button
          className="h-11 w-full px-4 sm:w-auto"
          onClick={onReset}
          type="button"
          variant="secondary"
        >
          <RefreshIcon />
          Scan Another Receipt
        </Button>
        <Button
          className="h-11 w-full px-4 sm:w-auto"
          disabled={isConfirmed}
          onClick={onConfirm}
          type="button"
        >
          <CheckIcon />
          {isConfirmed ? "Saved" : "Done"}
        </Button>
      </div>
    </div>
  );
}
