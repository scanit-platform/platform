"use client";

import type { FormEvent } from "react";
import { useMemo, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { ApiError } from "@/src/shared/api/client";
import { createReceipt } from "@/src/entities/receipt/api/receipts-service";
import type { Receipt } from "@/src/entities/receipt/types/receipt";
import type {
  CustomCategory,
  GeneralCategory,
} from "@/src/entities/category/types/category";
import { Button } from "@/src/shared/ui/button/button";
import { Input } from "@/src/shared/ui/input/input";
import { CheckIcon, PlusIcon } from "@/src/shared/ui/icons/icons";
import { revalidateDashboard } from "@/src/entities/receipt/api/receipt-actions";

type ManualReceiptFormProps = {
  customCategories: CustomCategory[];
  generalCategories: GeneralCategory[];
  userId: number;
};

export function todayIsoDate() {
  return new Date().toISOString().slice(0, 10);
}

export function ManualReceiptForm({
  customCategories,
  generalCategories,
  userId,
}: ManualReceiptFormProps) {
  const router = useRouter();
  const [vendorName, setVendorName] = useState("");
  const [totalAmount, setTotalAmount] = useState("");
  const [transactionAmount, setTransactionAmount] = useState("");
  const [transactionDate, setTransactionDate] = useState(todayIsoDate);
  const [generalCategoryId, setGeneralCategoryId] = useState(
    generalCategories[0]?.id ?? "",
  );
  const [customCategoryId, setCustomCategoryId] = useState("");
  const [error, setError] = useState("");
  const [savedReceipt, setSavedReceipt] = useState<Receipt | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const filteredCustomCategories = useMemo(
    () =>
      customCategories.filter(
        (category) => category.generalCategoryId === generalCategoryId,
      ),
    [customCategories, generalCategoryId],
  );

  const resetForm = () => {
    setVendorName("");
    setTotalAmount("");
    setTransactionAmount("");
    setTransactionDate(todayIsoDate());
    setCustomCategoryId("");
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError("");
    setSavedReceipt(null);

    const parsedTotal = Number(totalAmount);
    const parsedTransactionAmount = transactionAmount
      ? Number(transactionAmount)
      : parsedTotal;

    if (!vendorName.trim()) {
      setError("Merchant name is required.");
      return;
    }

    if (!transactionDate) {
      setError("Transaction date is required.");
      return;
    }

    if (!Number.isFinite(parsedTotal) || parsedTotal <= 0) {
      setError("Total amount must be greater than zero.");
      return;
    }

    if (
      transactionAmount &&
      (!Number.isFinite(parsedTransactionAmount) || parsedTransactionAmount < 0)
    ) {
      setError("Subtotal must be zero or greater.");
      return;
    }

    if (!generalCategoryId) {
      setError("Choose a category.");
      return;
    }

    setIsSubmitting(true);

    try {
      const receipt = await createReceipt({
        customCategoryId: customCategoryId || null,
        generalCategoryId,
        totalAmount: parsedTotal,
        transactionAmount: parsedTransactionAmount,
        transactionDate,
        userId,
        vendorName: vendorName.trim(),
      });

      setSavedReceipt(receipt);
      resetForm();
      await revalidateDashboard();
      router.refresh();
    } catch (submitError) {
      setError(
        submitError instanceof ApiError || submitError instanceof Error
          ? submitError.message
          : "Could not save this entry. Try again.",
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form className="scanit-auth-card p-5 sm:p-6" onSubmit={handleSubmit}>
      {savedReceipt ? (
        <div className="mb-5 rounded-lg border border-[var(--scanit-primary-softer)] bg-[var(--scanit-primary-soft)] px-4 py-3 text-sm font-semibold text-[var(--scanit-primary)]">
          <span className="inline-flex items-center gap-2">
            <CheckIcon />
            Entry saved as receipt #{savedReceipt.id}.
          </span>
        </div>
      ) : null}

      {error ? (
        <div className="scanit-auth-error mb-5 rounded-lg px-4 py-3 text-sm font-semibold">
          {error}
        </div>
      ) : null}

      <div className="grid gap-4 sm:grid-cols-2">
        <Input
          label="Vendor"
          name="vendorName"
          onChange={(event) => setVendorName(event.target.value)}
          placeholder="Supervalu"
          value={vendorName}
        />
        <Input
          label="Date"
          name="transactionDate"
          onChange={(event) => setTransactionDate(event.target.value)}
          type="date"
          value={transactionDate}
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

      <div className="mt-4 grid gap-4 sm:grid-cols-2">
        <label className="block">
          <span className="scanit-form-label">Category</span>
          <select
            className="auth-glass-field h-12 w-full rounded-lg px-4 text-[0.9375rem] outline-none"
            onChange={(event) => {
              setGeneralCategoryId(event.target.value);
              setCustomCategoryId("");
            }}
            value={generalCategoryId}
          >
            {generalCategories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.icon ? `${category.icon} ` : ""}
                {category.name}
              </option>
            ))}
          </select>
        </label>

        <label className="block">
          <span className="scanit-form-label">Subcategory</span>
          <select
            className="auth-glass-field h-12 w-full rounded-lg px-4 text-[0.9375rem] outline-none"
            onChange={(event) => setCustomCategoryId(event.target.value)}
            value={customCategoryId}
          >
            <option value="">None</option>
            {filteredCustomCategories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
        </label>
      </div>

      <div className="mt-6 flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
        <Link
          className="scanit-btn scanit-btn-secondary h-11 w-full sm:w-auto"
          href="/dashboard"
        >
          Cancel
        </Link>
        <Button
          className="h-11 w-full sm:w-auto"
          disabled={isSubmitting}
          type="submit"
        >
          <PlusIcon />
          {isSubmitting ? "Saving..." : "Save Entry"}
        </Button>
      </div>
    </form>
  );
}
