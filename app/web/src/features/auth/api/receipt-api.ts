import { apiFetch } from "@/src/shared/api/client";
import type { Receipt } from "@/src/features/auth/types/receipt";

export async function uploadReceipt(file: File, userId: number, token: string): Promise<Receipt> {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("userId", userId.toString());

    return apiFetch("/api/receipt/upload", {
        method: "POST",
        headers: { Authorization: `Bearer ${token}`,},
        body: formData,
    });
}