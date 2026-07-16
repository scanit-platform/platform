"use client";

import { ReceiptProcessingStep } from "@/src/features/receipt-scan/ui/receipt-processing-step";
import { ReceiptReviewStep } from "@/src/features/receipt-scan/ui/receipt-review-step";
import { ReceiptScanStepper } from "@/src/features/receipt-scan/ui/receipt-scan-stepper";
import { ReceiptUploadStep } from "@/src/features/receipt-scan/ui/receipt-upload-step";
import { useReceiptScanFlow } from "@/src/features/receipt-scan/model/use-receipt-scan-flow";
import {ValidateReceiptStep} from "@/src/features/receipt-validate-entry/ui/validate-receipt-step";

type ReceiptScanFlowProps = {
    userId?: number;
};

export function ReceiptScanFlow({ userId }: ReceiptScanFlowProps) {
    const scanFlow = useReceiptScanFlow(userId);

    return (
        <div>
            <ReceiptScanStepper currentStep={scanFlow.step} />

            <div className="mt-8">
                {scanFlow.step === "upload" ? (
                    <ReceiptUploadStep
                        error={scanFlow.fileError}
                        onFileSelected={scanFlow.selectReceiptFile}
                        onStartProcessing={scanFlow.startProcessing}
                        selectedFile={scanFlow.selectedFile}
                    />
                ) : null}

          {scanFlow.step === "validate" && scanFlow.receipt ? (
               <ValidateReceiptStep
                          receipt={scanFlow.receipt}
                          onScanAnotherAction={scanFlow.resetFlow}
                          onVerifyAction={() => scanFlow.setStep("review")}
                          onSaveAction={(updated) => scanFlow.setUpdatedReceipt(updated)}
                      />
          ) : null}

              {scanFlow.step === "review" ? (
          <ReceiptReviewStep
            isConfirmed={scanFlow.isConfirmed}
            onConfirm={scanFlow.confirmReceipt}
            onReset={scanFlow.resetFlow}
            onBackAction={() => scanFlow.setStep("validate")}
            receipt={scanFlow.updatedReceipt ?? scanFlow.receipt}
          />
        ) : null}
      </div>
    </div>
  );
}
