"use client";

import { ReceiptProcessingStep } from "@/src/features/receipt-scan/ui/receipt-processing-step";
import { ReceiptReviewStep } from "@/src/features/receipt-scan/ui/receipt-review-step";
import { ReceiptScanStepper } from "@/src/features/receipt-scan/ui/receipt-scan-stepper";
import { ReceiptUploadStep } from "@/src/features/receipt-scan/ui/receipt-upload-step";
import { useReceiptScanFlow } from "@/src/features/receipt-scan/model/use-receipt-scan-flow";
import {VerifyReceiptStep} from "@/src/features/receipt-verify-entry/ui/verify-receipt-step";
import { useRouter } from "next/navigation";
import { revalidateDashboard } from "@/src/entities/receipt/api/receipt-actions";
import type { GeneralCategory } from "@/src/entities/category/types/category";

type ReceiptScanFlowProps = {
    userId?: number;
    generalCategories?: GeneralCategory[];
};

export function ReceiptScanFlow(
    { userId, generalCategories }: ReceiptScanFlowProps
    ) {
    const router = useRouter();
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

                {scanFlow.step === "processing" ? (
                    <ReceiptProcessingStep
                        onCancel={scanFlow.cancelProcessing}
                        progress={scanFlow.uploadProgress}
                        selectedFile={scanFlow.selectedFile}
                    />
                ) : null}

          {scanFlow.step === "verify" && scanFlow.receipt ? (
               <VerifyReceiptStep
                          receipt={scanFlow.receipt}
                          onScanAnotherAction={scanFlow.resetFlow}
                          onVerifyAction={() => scanFlow.setStep("review")}
                          onSaveAction={async (updated) => {
                              scanFlow.setUpdatedReceipt(updated);
                              await revalidateDashboard();
                          }}
                      generalCategories={generalCategories}
               />
          ) : null}

              {scanFlow.step === "review" ? (
          <ReceiptReviewStep
            isConfirmed={scanFlow.isConfirmed}
            onConfirm={() => {
                scanFlow.confirmReceipt();
                router.refresh();
            }}
            onReset={scanFlow.resetFlow}
            onBackAction={() => scanFlow.setStep("verify")}
            receipt={scanFlow.updatedReceipt ?? scanFlow.receipt}
          />
        ) : null}
      </div>
    </div>
  );
}
