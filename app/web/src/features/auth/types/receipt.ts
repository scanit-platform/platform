export type Receipt = {
    id: number;
    vendorName: string;
    transactionAmount: number | null;
    totalAmount: number | null;
    imageUrl: string;
    ocrStatus: string;
    userId: number;
};