import type { ExtractedReceipt } from "@/src/entities/receipt/types/receipt";

const mockReceiptItems = [
  { amount: 12.48, id: "milk-bread-eggs", name: "Milk, bread, eggs", quantity: 1 },
  { amount: 8.75, id: "fresh-produce", name: "Fresh produce", quantity: 1 },
  { amount: 5.99, id: "coffee-beans", name: "Coffee beans", quantity: 1 },
  { amount: 18.98, id: "household-items", name: "Household items", quantity: 2 },
];

export function createMockExtractedReceipt(
  sourceFileName?: string,
): ExtractedReceipt {
  return {
    currency: "USD",
    customCategoryId: "groceries",
    date: "2026-06-04",
    generalCategoryId: "food",
    id: "mock-receipt-001",
    items: mockReceiptItems,
    merchant: "Tesco Express",
    sourceFileName,
    total: 46.2,
  };
}
