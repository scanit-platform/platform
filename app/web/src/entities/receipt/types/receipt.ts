export type ReceiptOcrStatus =
  | "PENDING"
  | "PROCESSING"
  | "COMPLETED"
  | "FAILED"
  | "CANCELLED";

export type Receipt = {
  customCategoryId: string | null;
  generalCategoryId: string | null;
  id: number;
  imageUrl: string;
  ocrStatus: ReceiptOcrStatus;
  totalAmount: number | null;
  transactionAmount: number | null;
  transactionDate: string;
  userId: number;
  vendorName: string;
};

export type CreateReceiptInput = {
  customCategoryId?: string | null;
  generalCategoryId?: string | null;
  imageUrl?: string;
  ocrStatus?: ReceiptOcrStatus;
  totalAmount: number;
  transactionAmount?: number | null;
  transactionDate: string;
  userId: number;
  vendorName: string;
};

export type ReceiptItem = {
  amount: number;
  id: string;
  name: string;
  quantity: number;
};

export type ExtractedReceipt = {
  currency: "USD";
  customCategoryId?: string;
  date: string;
  generalCategoryId: string;
  id: string;
  items: ReceiptItem[];
  merchant: string;
  sourceFileName?: string;
  total: number;
};
