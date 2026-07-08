"use client";

import { useCallback, useEffect, useState } from "react";
import { createMockExtractedReceipt } from "@/src/entities/receipt/model/mock-receipt";
import type { ExtractedReceipt } from "@/src/entities/receipt/types/receipt";
import {
  receiptProcessingDelayMs,
  type ReceiptScanStep,
  validateReceiptFile,
} from "@/src/features/receipt-scan/model/receipt-scan-state";

export function useReceiptScanFlow() {
  const [step, setStep] = useState<ReceiptScanStep>("upload");
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [fileError, setFileError] = useState("");
  const [receipt, setReceipt] = useState<ExtractedReceipt | null>(null);
  const [isConfirmed, setIsConfirmed] = useState(false);

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
      setStep("upload");
      return;
    }

    setFileError("");
    setSelectedFile(file);
    setReceipt(null);
    setIsConfirmed(false);
    setStep("upload");
  }, []);

  const startProcessing = useCallback(() => {
    if (!selectedFile) {
      setFileError("Choose a receipt before starting the scan.");
      return;
    }

    setFileError("");
    setReceipt(null);
    setIsConfirmed(false);
    setStep("processing");
  }, [selectedFile]);

  const resetFlow = useCallback(() => {
    setStep("upload");
    setSelectedFile(null);
    setFileError("");
    setReceipt(null);
    setIsConfirmed(false);
  }, []);

  const confirmReceipt = useCallback(() => {
    setIsConfirmed(true);
  }, []);

  useEffect(() => {
    if (step !== "processing" || !selectedFile) {
      return;
    }

    const processingTimer = window.setTimeout(() => {
      setReceipt(createMockExtractedReceipt(selectedFile.name));
      setStep("review");
    }, receiptProcessingDelayMs);

    return () => window.clearTimeout(processingTimer);
  }, [selectedFile, step]);

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
  };
}
