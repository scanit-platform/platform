"use client";

    import { useState } from "react";
    import Link from "next/link";
    import { useRouter } from "next/navigation";
    import type { Receipt } from "@/src/entities/receipt/types/receipt";
    import { CheckIcon } from "@/src/shared/ui/icons/icons";
    import { Input } from "@/src/shared/ui/input/input";
    import { Button } from "@/src/shared/ui/button/button";
    import { ApiError } from "@/src/shared/api/client";

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
        const [vendorName, setVendorName] = useState(receipt.vendorName ?? "");
        const [totalAmount, setTotalAmount] = useState(
            receipt.totalAmount != null ? String(receipt.totalAmount) : ""
        );
        const [transactionAmount, setTransactionAmount] = useState(
            receipt.transactionAmount != null ? String(receipt.transactionAmount) : ""
        );
        const [transactionDate, setTransactionDate] = useState(
            receipt.transactionDate ?? ""
        );
        const [isSaving, setIsSaving] = useState(false);
        const [error, setError] = useState("");

        async function handleSave() {
                setError("");
            if (!vendorName.trim()) { setError("Vendor name is required."); return; }
            if (!totalAmount) { setError("Total amount is required."); return; }
            if (!transactionDate) { setError("Transaction date is required."); return; }

            setIsSaving(true);

            console.log("handleSave fired - sending:",{
               vendorName: vendorName.trim(),
               totalAmount: parseFloat(totalAmount),
               transactionAmount: transactionAmount
                            ? parseFloat(transactionAmount)
                            : parseFloat(totalAmount),
                transactionDate,
            });

            try {
                const res = await fetch(`/api/receipt/${receipt.id}`, {
                    method: "PUT",
                    credentials: "include",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({
                        vendorName: vendorName.trim(),
                        totalAmount: parseFloat(totalAmount),
                        transactionAmount: transactionAmount
                            ? parseFloat(transactionAmount)
                            : parseFloat(totalAmount),
                        transactionDate,
                    }),
                });

                console.log("PUT status:", res.status);

                if (!res.ok) {
                    const body = await res.json().catch(() => ({}));
                    throw new Error(body.message ?? "Failed to save receipt.");
                }

                const updatedReceipt = await res.json();
                console.log("PUT response:", updatedReceipt);
                onSaveAction?.(updatedReceipt);
                onVerifyAction?.();
                router.refresh();

            } catch (err) {
                console.error("handleSave error:", err);
                setError(
                    err instanceof ApiError || err instanceof Error
                        ? err.message
                        : "Could not save receipt. Try again.",
                );
            } finally {
                setIsSaving(false);
            }
        }

        return (
        <div className="space-y-5">
            {/* Success banner */}
            <div className="rounded-lg border border-(--scanit-primary-softer) bg-(--scanit-primary-soft) px-4 py-3 text-sm font-semibold text-(--scanit-primary)">
        <span className="inline-flex items-center gap-2">
          <CheckIcon />
          Receipt #{receipt.id} saved successfully.
        </span>
            </div>

            <div className="grid gap-5 sm:grid-cols-2">
                {/* Receipt image */}
                <div className="flex flex-col gap-2">
          <span className="text-[0.75rem] font-medium text-(--scanit-text-muted) uppercase tracking-wide">
            Receipt Image
          </span>
                    {receipt.imageUrl ? (
                        <div className="overflow-y-auto rounded-xl border border-(--scanit-border) bg-(--surface-1) max-h-[50vh]">
                            <img
                                src={receipt.imageUrl}
                                alt={`Receipt from ${receipt.vendorName ?? "unknown vendor"}`}
                                className="w-full object-contain"
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

                {/* Receipt data from entity */}
                <div className="flex flex-col gap-3">
          <span className="text-[0.75rem] font-medium text-(--scanit-text-muted) uppercase tracking-wide">
            Receipt Data
          </span>

                <div className={"grid grid-cols-2 gap-x-3 gap-y-4"}>

                    <ReceiptField label="Scanned Vendor" value={receipt.vendorName} />
                    <Input
                        label="Update Vendor"
                        name="vendorName"
                        onChange={(event) => setVendorName(event.target.value)}
                        placeholder="Supervalu"
                        value={vendorName}
                    />


                    <ReceiptField
                        label="Scanned Total Amount"
                        value={receipt.totalAmount != null
                            ? `€${Number(receipt.totalAmount).toFixed(2)}`
                            : null}
                    />
                        <Input
                            inputMode="decimal"
                            label="Update Total Amount"
                            min="0"
                            name="totalAmount"
                            onChange={(event) => setTotalAmount(event.target.value)}
                            placeholder="47.82"
                            step="0.01"
                            type="number"
                            value={totalAmount}
                        />



                    <ReceiptField
                        label="Scanned Transaction Amount"
                        value={receipt.transactionAmount != null
                            ? `€${Number(receipt.transactionAmount).toFixed(2)}`
                            : null}
                    />
                        <Input
                            inputMode="decimal"
                            label="Update Transaction Amount"
                            min="0"
                            name="transactionAmount"
                            onChange={(event) => setTransactionAmount(event.target.value)}
                            placeholder="Optional"
                            step="0.01"
                            type="number"
                            value={transactionAmount}
                        />

                    <ReceiptField label="Scanned Date" value={formatDisplayDate(receipt.transactionDate)} />
                        <Input className={"justify-end"}
                            label="Updated Date"
                            name="transactionDate"
                            onChange={(event) => setTransactionDate(event.target.value)}
                            type="date"
                            value={transactionDate}
                            placeholder="13-07-2026"
                        />
                    </div>

                    {/* OCR status indicator */}
                    <div className={`rounded-lg px-3 py-2 text-[0.8125rem] font-medium ${
                        receipt.ocrStatus === "COMPLETED"
                            ? "bg-green-50 border border-green-200 text-green-700"
                            : "bg-red-50 border border-red-200 text-red-700"
                    }`}>
                        {receipt.ocrStatus === "COMPLETED"
                            ? "✓ OCR extraction complete — ready to verify"
                            : "✗ OCR extraction failed — manual entry required"}
                    </div>
                </div>
            </div>

            {/* Action buttons */}
            <div className="flex flex-col gap-3 sm:flex-row sm:justify-end pt-2">
                {onScanAnotherAction && (
                    <button
                        onClick={onScanAnotherAction}
                        className="scanit-btn scanit-btn-secondary h-11 w-full sm:w-auto"
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
                    {isSaving ? "Saving…" : "Save & Verify →"}
                </Button>
            </div>
        </div>
    );
}

// Simple field display
function ReceiptField({
                          label,
                          value,
                      }: {
    label: string;
    value: string | number | null | undefined;
}) {
    const hasValue = value != null && value !== "";
    return (
        <div className="flex flex-col gap-1">
            <span className="text-[0.8rem] px-2 py-1 font-medium text-(--scanit-label)">
             {label}
            </span>
            <div className={`grow rounded-lg border  px-3 py-2 ${
                hasValue
                    ? "border-(--scanit-border) bg-(--surface-1) text-(--scanit-text)"
                    : "border-red-200 bg-red-50 text-red-400"
            }`}>
                {hasValue ? String(value) : "Not available"}
            </div>
        </div>
    );
}

function formatDisplayDate(dateStr: string | null | undefined): string | null {
        if (!dateStr) return null;
        const [year, month, day] = dateStr.split("-");
        if (!year || !month || !day) return dateStr;
        return `${day}/${month}/${year}`;
}