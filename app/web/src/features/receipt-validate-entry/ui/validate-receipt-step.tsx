"use client";

    import { useState } from "react";
    import Link from "next/link";
    import { useRouter } from "next/navigation";
    import type { Receipt } from "@/src/entities/receipt/types/receipt";
    import { CheckIcon } from "@/src/shared/ui/icons/icons";
    import { Input } from "@/src/shared/ui/input/input";
    import { Button } from "@/src/shared/ui/button/button";
    import { ApiError } from "@/src/shared/api/client";

    export function ValidateReceiptStep({
                                            receipt,
                                            onScanAnother,
                                        }: {
        receipt: Receipt;
        onScanAnother?: () => void;
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

                if (!res.ok) {
                    const body = await res.json().catch(() => ({}));
                    throw new Error(body.message ?? "Failed to save receipt.");
                }
                router.push(`/verify/${receipt.id}`);

            } catch (err) {
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
            <div className="rounded-lg border border-[var(--scanit-primary-softer)] bg-[var(--scanit-primary-soft)] px-4 py-3 text-sm font-semibold text-[var(--scanit-primary)]">
        <span className="inline-flex items-center gap-2">
          <CheckIcon />
          Receipt #{receipt.id} saved successfully.
        </span>
            </div>

            <div className="grid gap-5 sm:grid-cols-2">
                {/* Receipt image */}
                <div className="flex flex-col gap-2">
          <span className="text-[0.75rem] font-medium text-[var(--scanit-text-muted)] uppercase tracking-wide">
            Receipt Image
          </span>
                    {receipt.imageUrl ? (
                        <div className="overflow-y-auto rounded-xl border border-[var(--scanit-border)] bg-[var(--surface-1)] max-h-[50vh]">
                            <img
                                src={receipt.imageUrl}
                                alt={`Receipt from ${receipt.vendorName ?? "unknown vendor"}`}
                                className="w-full object-contain"
                            />
                        </div>
                    ) : (
                        <div className="flex h-40 items-center justify-center rounded-xl border border-dashed border-[var(--scanit-border)] bg-[var(--surface-1)]">
                            <p className="text-[0.8125rem] text-[var(--scanit-text-muted)]">
                                No image available
                            </p>
                        </div>
                    )}
                </div>

                {/* Receipt data from entity */}
                <div className="flex flex-col gap-3">
          <span className="text-[0.75rem] font-medium text-[var(--scanit-text-muted)] uppercase tracking-wide">
            Receipt Data
          </span>
                <div className={"flex flex-row gap-3 justify-evenly"}>
                    <ReceiptField label="Scanned Vendor" value={receipt.vendorName} />
                    <Input
                        label="Update Vendor"
                        name="vendorName"
                        //onChange={(event) => setVendorName(event.target.value)}
                        placeholder="Supervalu"
                       // value={vendorName}
                    />
                </div>

                    <div className={"flex flex-row gap-3 justify-evenly"}>
                    <ReceiptField
                        label="Total amount"
                        value={receipt.totalAmount != null
                            ? `€${Number(receipt.totalAmount).toFixed(2)}`
                            : null}
                    />
                        <Input
                            inputMode="decimal"
                            label="Total amount"
                            min="0"
                            name="totalAmount"
                            onChange={(event) => setTotalAmount(event.target.value)}
                            placeholder="47.82"
                            step="0.01"
                            type="number"
                            value={totalAmount}
                        />
                    </div>

                    <div className={"flex flex-row gap-3 justify-evenly"}>
                    <ReceiptField
                        label="Transaction amount"
                        value={receipt.transactionAmount != null
                            ? `€${Number(receipt.transactionAmount).toFixed(2)}`
                            : null}
                    />
                        <Input
                            inputMode="decimal"
                            label="Subtotal"
                            min="0"
                            name="transactionAmount"
                            onChange={(event) => setTransactionAmount(event.target.value)}
                            placeholder="Optional"
                            step="0.01"
                            type="number"
                            value={transactionAmount}
                        />
                    </div>
                    <div className={"flex flex-row gap-3 justify-evenly"}>
                    <ReceiptField label="Date" value={receipt.transactionDate} />
                        <Input
                            label="Date"
                            name="transactionDate"
                            onChange={(event) => setTransactionDate(event.target.value)}
                            type="date"
                            value={transactionDate}
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
                {onScanAnother && (
                    <button
                        onClick={onScanAnother}
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
      <span className="text-[0.75rem] font-medium text-[var(--scanit-label)]">
        {label}
      </span>
            <div className={`rounded-lg border px-3 py-2 text-[0.875rem] ${
                hasValue
                    ? "border-[var(--scanit-border)] bg-[var(--surface-1)] text-[var(--scanit-text)]"
                    : "border-red-200 bg-red-50 text-red-400"
            }`}>
                {hasValue ? String(value) : "Not available"}
            </div>
        </div>
    );
}