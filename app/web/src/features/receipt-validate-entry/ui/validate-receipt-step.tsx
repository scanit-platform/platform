"use client";

import Link from "next/link";
import type { Receipt } from "@/src/entities/receipt/types/receipt";
import { CheckIcon, PlusIcon } from "@/src/shared/ui/icons/icons";

export function ValidateReceiptStep({
                                        receipt,
                                  onScanAnother,
}: {
    receipt: Receipt;
onScanAnother?: () => void;
}) {
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

                    <ReceiptField label="Vendor" value={receipt.vendorName} />
                    <ReceiptField
                        label="Total amount"
                        value={receipt.totalAmount != null
                            ? `€${Number(receipt.totalAmount).toFixed(2)}`
                            : null}
                    />
                    <ReceiptField
                        label="Transaction amount"
                        value={receipt.transactionAmount != null
                            ? `€${Number(receipt.transactionAmount).toFixed(2)}`
                            : null}
                    />
                    <ReceiptField label="Date" value={receipt.transactionDate} />
                    <ReceiptField label="OCR Status" value={receipt.ocrStatus} />

                    {/* OCR status indicator */}
                    <div className={`rounded-lg px-3 py-2 text-[0.8125rem] font-medium ${
                        receipt.ocrStatus === "COMPLETED"
                            ? "bg-green-50 border border-green-200 text-green-700"
                            : receipt.ocrStatus === "FAILED"
                                ? "bg-red-50 border border-red-200 text-red-700"
                                : "bg-amber-50 border border-amber-200 text-amber-700"
                    }`}>
                        {receipt.ocrStatus === "COMPLETED"
                            ? "✓ OCR extraction complete — ready to verify"
                            : receipt.ocrStatus === "FAILED"
                                ? "✗ OCR extraction failed — manual entry required"
                                : "⏳ OCR processing in progress…"}
                    </div>
                </div>
            </div>

            {/* Action buttons */}
            <div className="flex flex-col gap-3 sm:flex-row sm:justify-end pt-2">
                <Link
                    className="scanit-btn scanit-btn-secondary h-11 w-full sm:w-auto"
                    href="/dashboard"
                >
                    Back to Dashboard
                </Link>
                {receipt.ocrStatus === "COMPLETED" && (
                    <Link
                        className="scanit-btn scanit-btn-primary h-11 w-full sm:w-auto inline-flex items-center justify-center gap-2"
                        href={`/verify/${receipt.id}`}
                    >
                        Validate Receipt →
                    </Link>
                )}
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