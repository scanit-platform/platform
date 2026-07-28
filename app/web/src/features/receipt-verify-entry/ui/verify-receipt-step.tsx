"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";

import {
    deleteReceipt,
    getReceiptById,
    saveDuplicateAsNew,
    updateReceipt,
} from "@/src/entities/receipt/api/receipts-service";
import type { Receipt } from "@/src/entities/receipt/types/receipt";
import { ApiError } from "@/src/shared/api/client";
import { Button } from "@/src/shared/ui/button/button";
import { CheckIcon } from "@/src/shared/ui/icons/icons";
import { Input } from "@/src/shared/ui/input/input";

export function VerifyReceiptStep({
                                      receipt,
                                      onScanAnotherAction,
                                      onVerifyAction,
                                      onSaveAction,
                                  }: {
    receipt: Receipt;
    onScanAnotherAction?: () => void;
    onVerifyAction?: () => void;
    onSaveAction?: (updated: Receipt) => void;
}) {
    const router = useRouter();

    const [currentReceipt, setCurrentReceipt] =
        useState<Receipt>(receipt);

    const [existingReceipt, setExistingReceipt] =
        useState<Receipt | null>(null);

    const [vendorName, setVendorName] =
        useState(receipt.vendorName ?? "");

    const [totalAmount, setTotalAmount] =
        useState(
            receipt.totalAmount != null
                ? String(receipt.totalAmount)
                : "",
        );

    const [transactionAmount, setTransactionAmount] =
        useState(
            receipt.transactionAmount != null
                ? String(receipt.transactionAmount)
                : "",
        );

    const [transactionDate, setTransactionDate] =
        useState(receipt.transactionDate ?? "");

    const [isSaving, setIsSaving] =
        useState(false);

    const [
        isDuplicateActionRunning,
        setIsDuplicateActionRunning,
    ] = useState(false);

    const [error, setError] =
        useState("");

    useEffect(() => {
        setCurrentReceipt(receipt);
        setExistingReceipt(null);

        setVendorName(
            receipt.vendorName ?? "",
        );

        setTotalAmount(
            receipt.totalAmount != null
                ? String(receipt.totalAmount)
                : "",
        );

        setTransactionAmount(
            receipt.transactionAmount != null
                ? String(receipt.transactionAmount)
                : "",
        );

        setTransactionDate(
            receipt.transactionDate ?? "",
        );

        setError("");
    }, [receipt]);

    const isDuplicateReview =
        currentReceipt.ocrStatus ===
        "DUPLICATE_REVIEW";

    async function handleSave() {
        setError("");
        setExistingReceipt(null);

        const trimmedVendorName =
            vendorName.trim();

        const parsedTotalAmount =
            Number(totalAmount);

        const parsedTransactionAmount =
            transactionAmount
                ? Number(transactionAmount)
                : parsedTotalAmount;

        if (!trimmedVendorName) {
            setError(
                "Vendor name is required.",
            );

            return;
        }

        if (
            !totalAmount ||
            !Number.isFinite(parsedTotalAmount) ||
            parsedTotalAmount < 0
        ) {
            setError(
                "Enter a valid total amount.",
            );

            return;
        }

        if (
            !Number.isFinite(
                parsedTransactionAmount,
            ) ||
            parsedTransactionAmount < 0
        ) {
            setError(
                "Enter a valid transaction amount.",
            );

            return;
        }

        if (!transactionDate) {
            setError(
                "Transaction date is required.",
            );

            return;
        }

        setIsSaving(true);

        try {
            const updatedReceipt =
                await updateReceipt(
                    currentReceipt.id,
                    {
                        vendorName:
                        trimmedVendorName,

                        totalAmount:
                        parsedTotalAmount,

                        transactionAmount:
                        parsedTransactionAmount,

                        transactionDate,
                    },
                );

            setCurrentReceipt(
                updatedReceipt,
            );

            onSaveAction?.(
                updatedReceipt,
            );

            /*
             * Если backend нашёл дубликат,
             * не переходим к следующему шагу.
             * Вместо этого показываем warning.
             */
            if (
                updatedReceipt.ocrStatus ===
                "DUPLICATE_REVIEW"
            ) {
                return;
            }

            onVerifyAction?.();
            router.refresh();
        } catch (err) {
            setError(
                getErrorMessage(
                    err,
                    "Could not save receipt. Try again.",
                ),
            );
        } finally {
            setIsSaving(false);
        }
    }

    async function handleCancelDuplicate() {
        setError("");
        setIsDuplicateActionRunning(true);

        try {
            await deleteReceipt(
                currentReceipt.id,
            );

            if (onScanAnotherAction) {
                onScanAnotherAction();
            } else {
                router.push(
                    "/dashboard",
                );
            }

            router.refresh();
        } catch (err) {
            setError(
                getErrorMessage(
                    err,
                    "Could not discard the duplicate receipt.",
                ),
            );
        } finally {
            setIsDuplicateActionRunning(false);
        }
    }

    async function handleViewExisting() {
        setError("");

        const existingReceiptId =
            currentReceipt
                .duplicateOfReceiptId;

        if (existingReceiptId == null) {
            setError(
                "The matching receipt could not be identified.",
            );

            return;
        }

        setIsDuplicateActionRunning(true);

        try {
            const matchingReceipt =
                await getReceiptById(
                    existingReceiptId,
                );

            setExistingReceipt(
                matchingReceipt,
            );
        } catch (err) {
            setError(
                getErrorMessage(
                    err,
                    "Could not load the existing receipt.",
                ),
            );
        } finally {
            setIsDuplicateActionRunning(false);
        }
    }

    async function handleSaveAsNew() {
        setError("");
        setIsDuplicateActionRunning(true);

        try {
            const savedReceipt =
                await saveDuplicateAsNew(
                    currentReceipt.id,
                );

            setCurrentReceipt(
                savedReceipt,
            );

            setExistingReceipt(null);

            onSaveAction?.(
                savedReceipt,
            );

            onVerifyAction?.();
            router.refresh();
        } catch (err) {
            setError(
                getErrorMessage(
                    err,
                    "Could not save the receipt as new.",
                ),
            );
        } finally {
            setIsDuplicateActionRunning(false);
        }
    }

    return (
        <div className="space-y-5">
            <div className="rounded-lg border border-(--scanit-primary-softer) bg-(--scanit-primary-soft) px-4 py-3 text-sm font-semibold text-(--scanit-primary)">
        <span className="inline-flex items-center gap-2">
          <CheckIcon />

          Receipt #{currentReceipt.id} uploaded successfully.
        </span>
            </div>

            {error && (
                <div
                    className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700"
                    role="alert"
                >
                    {error}
                </div>
            )}

            {isDuplicateReview && (
                <div className="space-y-4 rounded-xl border border-amber-300 bg-amber-50 p-4">
                    <div>
                        <h3 className="font-semibold text-amber-900">
                            Possible duplicate receipt
                        </h3>

                        <p className="mt-1 text-sm text-amber-800">
                            This looks like a receipt from{" "}
                            <strong>
                                {currentReceipt.vendorName}
                            </strong>{" "}
                            on{" "}
                            <strong>
                                {formatDisplayDate(
                                    currentReceipt.transactionDate,
                                ) ?? "an unknown date"}
                            </strong>{" "}
                            for{" "}
                            <strong>
                                {formatEuro(
                                    currentReceipt.totalAmount,
                                )}
                            </strong>
                            . Save anyway?
                        </p>
                    </div>

                    <div className="flex flex-col gap-2 sm:flex-row">
                        <Button
                            className="w-full sm:w-auto"
                            disabled={
                                isDuplicateActionRunning
                            }
                            onClick={
                                handleCancelDuplicate
                            }
                        >
                            Cancel and discard
                        </Button>

                        <button
                            className="scanit-btn scanit-btn-secondary h-11 w-full sm:w-auto"
                            disabled={
                                isDuplicateActionRunning
                            }
                            onClick={
                                handleViewExisting
                            }
                            type="button"
                        >
                            View existing
                        </button>

                        <button
                            className="scanit-btn scanit-btn-secondary h-11 w-full sm:w-auto"
                            disabled={
                                isDuplicateActionRunning
                            }
                            onClick={
                                handleSaveAsNew
                            }
                            type="button"
                        >
                            Save as new
                        </button>
                    </div>
                </div>
            )}

            {existingReceipt && (
                <div className="rounded-xl border border-(--scanit-border) bg-(--surface-1) p-4">
                    <h3 className="font-semibold text-(--scanit-text)">
                        Existing receipt
                    </h3>

                    <div className="mt-3 grid gap-3 sm:grid-cols-3">
                        <ReceiptField
                            label="Vendor"
                            value={
                                existingReceipt.vendorName
                            }
                        />

                        <ReceiptField
                            label="Date"
                            value={formatDisplayDate(
                                existingReceipt.transactionDate,
                            )}
                        />

                        <ReceiptField
                            label="Total"
                            value={formatEuro(
                                existingReceipt.totalAmount,
                            )}
                        />
                    </div>
                </div>
            )}

            <div className="grid gap-5 sm:grid-cols-2">
                <div className="flex flex-col gap-2">
          <span className="text-[0.75rem] font-medium uppercase tracking-wide text-(--scanit-text-muted)">
            Receipt Image
          </span>

                    {currentReceipt.imageUrl ? (
                        <div className="max-h-[50vh] overflow-y-auto rounded-xl border border-(--scanit-border) bg-(--surface-1)">
                            <img
                                alt={`Receipt from ${
                                    currentReceipt.vendorName ??
                                    "unknown vendor"
                                }`}
                                className="w-full object-contain"
                                src={
                                    currentReceipt.imageUrl
                                }
                            />
                        </div>
                    ) : (
                        <div className="flex h-40 items-center justify-center rounded-xl border border-dashed border-(--scanit-border) bg-(--surface-1)">
                            <p className="text-[0.8125rem] text-(--scanit-text-muted)">
                                No image available
                            </p>
                        </div>
                    )}
                </div>

                <div className="flex flex-col gap-3">
          <span className="text-[0.75rem] font-medium uppercase tracking-wide text-(--scanit-text-muted)">
            Receipt Data
          </span>

                    <div className="grid grid-cols-2 gap-x-3 gap-y-4">
                        <ReceiptField
                            label="Scanned Vendor"
                            value={
                                currentReceipt.vendorName
                            }
                        />

                        <Input
                            label="Update Vendor"
                            name="vendorName"
                            onChange={(event) =>
                                setVendorName(
                                    event.target.value,
                                )
                            }
                            placeholder="Supervalu"
                            value={vendorName}
                        />

                        <ReceiptField
                            label="Scanned Total Amount"
                            value={formatEuro(
                                currentReceipt.totalAmount,
                            )}
                        />

                        <Input
                            inputMode="decimal"
                            label="Update Total Amount"
                            min="0"
                            name="totalAmount"
                            onChange={(event) =>
                                setTotalAmount(
                                    event.target.value,
                                )
                            }
                            placeholder="47.82"
                            step="0.01"
                            type="number"
                            value={totalAmount}
                        />

                        <ReceiptField
                            label="Scanned Transaction Amount"
                            value={formatEuro(
                                currentReceipt.transactionAmount,
                            )}
                        />

                        <Input
                            inputMode="decimal"
                            label="Update Transaction Amount"
                            min="0"
                            name="transactionAmount"
                            onChange={(event) =>
                                setTransactionAmount(
                                    event.target.value,
                                )
                            }
                            placeholder="Optional"
                            step="0.01"
                            type="number"
                            value={
                                transactionAmount
                            }
                        />

                        <ReceiptField
                            label="Scanned Date"
                            value={formatDisplayDate(
                                currentReceipt.transactionDate,
                            )}
                        />

                        <Input
                            className="justify-end"
                            label="Updated Date"
                            name="transactionDate"
                            onChange={(event) =>
                                setTransactionDate(
                                    event.target.value,
                                )
                            }
                            placeholder="13-07-2026"
                            type="date"
                            value={transactionDate}
                        />
                    </div>

                    <OcrStatusIndicator
                        status={
                            currentReceipt.ocrStatus
                        }
                    />
                </div>
            </div>

            {!isDuplicateReview && (
                <div className="flex flex-col gap-3 pt-2 sm:flex-row sm:justify-end">
                    {onScanAnotherAction && (
                        <button
                            className="scanit-btn scanit-btn-secondary h-11 w-full sm:w-auto"
                            onClick={
                                onScanAnotherAction
                            }
                            type="button"
                        >
                            Scan another
                        </button>
                    )}

                    <Link
                        className="scanit-btn scanit-btn-secondary h-11 w-full sm:w-auto"
                        href="/dashboard"
                    >
                        Back to Dashboard
                    </Link>

                    <Button
                        className="h-11 w-full sm:w-auto"
                        disabled={isSaving}
                        onClick={handleSave}
                    >
                        {isSaving
                            ? "Saving…"
                            : "Save & Verify →"}
                    </Button>
                </div>
            )}
        </div>
    );
}

function OcrStatusIndicator({
                                status,
                            }: {
    status: Receipt["ocrStatus"];
}) {
    if (
        status === "DUPLICATE_REVIEW"
    ) {
        return (
            <div className="rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-[0.8125rem] font-medium text-amber-700">
                Possible duplicate — review required
            </div>
        );
    }

    if (status === "COMPLETED") {
        return (
            <div className="rounded-lg border border-green-200 bg-green-50 px-3 py-2 text-[0.8125rem] font-medium text-green-700">
                ✓ OCR extraction complete — ready to verify
            </div>
        );
    }

    return (
        <div className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-[0.8125rem] font-medium text-red-700">
            ✗ OCR extraction failed — manual entry required
        </div>
    );
}

function ReceiptField({
                          label,
                          value,
                      }: {
    label: string;
    value:
        | string
        | number
        | null
        | undefined;
}) {
    const hasValue =
        value != null &&
        value !== "";

    return (
        <div className="flex flex-col gap-1">
      <span className="px-2 py-1 text-[0.8rem] font-medium text-(--scanit-label)">
        {label}
      </span>

            <div
                className={`grow rounded-lg border px-3 py-2 ${
                    hasValue
                        ? "border-(--scanit-border) bg-(--surface-1) text-(--scanit-text)"
                        : "border-red-200 bg-red-50 text-red-400"
                }`}
            >
                {hasValue
                    ? String(value)
                    : "Not available"}
            </div>
        </div>
    );
}

function formatDisplayDate(
    dateStr:
        | string
        | null
        | undefined,
): string | null {
    if (!dateStr) {
        return null;
    }

    const [year, month, day] =
        dateStr.split("-");

    if (
        !year ||
        !month ||
        !day
    ) {
        return dateStr;
    }

    return `${day}/${month}/${year}`;
}

function formatEuro(
    value:
        | number
        | null
        | undefined,
): string {
    if (value == null) {
        return "Not available";
    }

    return `€${Number(value).toFixed(2)}`;
}

function getErrorMessage(
    error: unknown,
    fallback: string,
): string {
    if (
        error instanceof ApiError ||
        error instanceof Error
    ) {
        return error.message;
    }

    return fallback;
}