import { Fragment } from "react";
import {
  receiptScanSteps,
  type ReceiptScanStep,
} from "@/src/features/receipt-scan/model/receipt-scan-state";
import { CheckIcon } from "@/src/shared/ui/icons/icons";

type ReceiptScanStepperProps = {
  currentStep: ReceiptScanStep;
};

export function ReceiptScanStepper({ currentStep }: ReceiptScanStepperProps) {
  const currentIndex = receiptScanSteps.findIndex(
    (step) => step.id === currentStep,
  );

  return (
    <ol className="mx-auto flex max-w-md items-center justify-center gap-3 sm:max-w-xl sm:gap-4">
      {receiptScanSteps.map((step, index) => {
        const isActive = index === currentIndex;
        const isComplete = index < currentIndex;

        return (
          <Fragment key={step.id}>
            <li className="flex items-center gap-2">
              <span
                className={[
                  "flex h-7 w-7 shrink-0 items-center justify-center rounded-full text-xs font-bold",
                  isActive || isComplete
                    ? "bg-[var(--scanit-primary)] text-white"
                    : "bg-[var(--scanit-border)] text-[var(--scanit-text-secondary)]",
                ].join(" ")}
              >
                {isComplete ? <CheckIcon size={16} /> : index + 1}
              </span>
              <span
                className={[
                  "text-sm font-medium",
                  isActive || isComplete
                    ? "text-[var(--scanit-primary)]"
                    : "text-[var(--scanit-text-secondary)]",
                ].join(" ")}
              >
                {step.label}
              </span>
            </li>
            {index < receiptScanSteps.length - 1 ? (
              <li
                aria-hidden="true"
                className={[
                  "h-px w-8 sm:w-14",
                  index < currentIndex
                    ? "bg-[var(--scanit-primary)]"
                    : "bg-[var(--scanit-border)]",
                ].join(" ")}
              />
            ) : null}
          </Fragment>
        );
      })}
    </ol>
  );
}
