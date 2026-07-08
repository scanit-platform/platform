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
