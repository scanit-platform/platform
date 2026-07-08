import {
  formatReceiptFileSize,
} from "@/src/features/receipt-scan/model/receipt-scan-state";
import { FileIcon } from "@/src/shared/ui/icons/icons";

type ReceiptProcessingStepProps = {
  selectedFile: File | null;
};

export function ReceiptProcessingStep({
  selectedFile,
}: ReceiptProcessingStepProps) {
  return (
    <div className="scanit-auth-panel mx-auto flex min-h-[21rem] max-w-3xl flex-col items-center justify-center p-8 text-center">
      <div className="h-14 w-14 animate-spin rounded-full border-4 border-[var(--scanit-primary-softer)] border-t-[var(--scanit-primary)]" />
      <h2 className="mt-6 font-serif text-2xl font-bold text-[var(--scanit-text)]">
        Processing receipt
      </h2>
      <p className="mt-2 max-w-md text-sm text-[var(--scanit-text-secondary)]">
        Extracting merchant, date, line items, and total from your receipt.
      </p>

      {selectedFile ? (
        <div className="mt-6 flex max-w-full items-center gap-3 rounded-lg border border-[var(--scanit-border)] bg-[var(--scanit-soft)] px-4 py-3 text-left">
          <FileIcon className="shrink-0 text-[var(--scanit-primary)]" />
          <div className="min-w-0">
            <p className="truncate text-sm font-semibold text-[var(--scanit-text)]">
              {selectedFile.name}
            </p>
            <p className="text-xs text-[var(--scanit-text-secondary)]">
              {formatReceiptFileSize(selectedFile.size)}
            </p>
          </div>
        </div>
      ) : null}
    </div>
  );
}
