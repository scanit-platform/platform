"use server";

import { getAuthToken } from "@/src/features/auth/model/session";
import { uploadReceipt } from "@/src/features/auth/api/receipt-api";

export async function submitReceipt(file: File, userId: number) {
    const token = await getAuthToken();

    if (!token) {
        throw new Error("Authentication required");
    }

    return uploadReceipt(file, userId, token);
}