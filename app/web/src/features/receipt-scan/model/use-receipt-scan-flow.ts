"use client";

import { useCallback, useRef, useState } from "react";
import { ApiError } from "@/src/shared/api/client";
import {
  extractReceipt,
  uploadReceipt,
} from "@/src/entities/receipt/api/receipts-service";
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

  const uploadControllerRef = useRef<AbortController | null>(null);

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

    const controller = new AbortController();
    uploadControllerRef.current = controller;

    setFileError("");
    setReceipt(null);
    setIsConfirmed(false);
    setUploadProgress(0);
    setStep("processing");

    try {
      const uploadedReceipt = await uploadReceipt({
        file: selectedFile,
        onProgress: setUploadProgress,
        signal: controller.signal,
        userId,
      });

      const key = uploadedReceipt.imageUrl.substring(
          uploadedReceipt.imageUrl.lastIndexOf("/") + 1,
      );

      const extractedReceipt = await extractReceipt({
        key,
        signal: controller.signal,
        userId,
      });

      setReceipt(extractedReceipt);
      setStep("review");
    } catch (error) {
      const wasCancelled =
          error instanceof DOMException &&
          error.name === "AbortError";

      setFileError(
          wasCancelled
              ? "Receipt processing was cancelled."
              : error instanceof ApiError || error instanceof Error
                  ? error.message
                  : "Receipt upload failed. Try again.",
      );

      setUploadProgress(0);
      setStep("upload");
    } finally {
      if (uploadControllerRef.current === controller) {
        uploadControllerRef.current = null;
      }
    }
  }, [selectedFile, userId]);

  const cancelProcessing = useCallback(() => {
    uploadControllerRef.current?.abort();
  }, []);

  const resetFlow = useCallback(() => {
    uploadControllerRef.current?.abort();
    uploadControllerRef.current = null;

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
    cancelProcessing,
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