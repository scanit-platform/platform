"use client";

import { useCallback, useState } from "react";
import { ApiError } from "@/src/shared/api/client";
import { uploadReceipt } from "@/src/entities/receipt/api/receipts-service";
import type { Receipt } from "@/src/entities/receipt/types/receipt";
import {
  type ReceiptScanStep,
  validateReceiptFile,
} from "@/src/features/receipt-scan/model/receipt-scan-state";

export function useReceiptScanFlow(userId: number | undefined) {
  const [step, setStep] = useState<ReceiptScanStep>("upload");
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [fileError, setFileError] = useState("");
  const [receipt, setReceipt] = useState<Receipt | null>(null);
  const [isConfirmed, setIsConfirmed] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);

  const selectReceiptFile = useCallback((file: File | null) => {
    if (!file) {
      return;
    }

    const validationError = validateReceiptFile(file);

    if (validationError) {
      setFileError(validationError);
      setSelectedFile(null);
      setReceipt(null);
      setIsConfirmed(false);
      setUploadProgress(0);
      setStep("upload");
      return;
    }

    setFileError("");
    setSelectedFile(file);
    setReceipt(null);
    setIsConfirmed(false);
    setUploadProgress(0);
    setStep("upload");
  }, []);

  const startProcessing = useCallback(async () => {
    if (!selectedFile) {
      setFileError("Choose a receipt before starting the scan.");
      return;
    }

    if (!userId) {
      setFileError("Sign in before uploading receipts.");
      return;
    }

    setFileError("");
    setReceipt(null);
    setIsConfirmed(false);
    setUploadProgress(0);
    setStep("processing");

    try {
      const uploadedReceipt = await uploadReceipt({
        file: selectedFile,
        onProgress: setUploadProgress,
        userId,
      });

      setReceipt(uploadedReceipt);
      setStep("validate");
    } catch (error) {
      setFileError(
        error instanceof ApiError || error instanceof Error
          ? error.message
          : "Receipt upload failed. Try again.",
      );
      setStep("upload");
    }
  }, [selectedFile, userId]);

  const resetFlow = useCallback(() => {
    setStep("upload");
    setSelectedFile(null);
    setFileError("");
    setReceipt(null);
    setIsConfirmed(false);
    setUploadProgress(0);
  }, []);

  const confirmReceipt = useCallback(() => {
    setIsConfirmed(true);
  }, []);

  return {
    confirmReceipt,
    fileError,
    isConfirmed,
    receipt,
    resetFlow,
    selectReceiptFile,
    selectedFile,
    startProcessing,
    step,
    uploadProgress,
  };
}
