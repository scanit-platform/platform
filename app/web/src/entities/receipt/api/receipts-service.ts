import {
  ApiError,
  apiFetch,
  createQueryString,
  getApiRequestUrl,
} from "@/src/shared/api/client";
import type {
  CreateReceiptInput,
  Receipt,
} from "@/src/entities/receipt/types/receipt";

export type ReceiptSearchParams = {
  transactionDate?: string;
  userId: number;
  vendorName?: string;
};

type AuthenticatedRequestOptions = {
  authToken?: string;
};

function getAuthOptions(options: AuthenticatedRequestOptions = {}) {
  return options.authToken ? { authToken: options.authToken } : {};
}

export function getReceipts(options: AuthenticatedRequestOptions = {}) {
  return apiFetch<Receipt[]>(
    "/api/receipt",
    {
      method: "GET",
    },
    getAuthOptions(options),
  );
}

export function searchReceipts(
  params: ReceiptSearchParams,
  options: AuthenticatedRequestOptions = {},
) {
  return apiFetch<Receipt[]>(
    `/api/receipt/search${createQueryString(params)}`,
    {
      method: "GET",
    },
    getAuthOptions(options),
  );
}

export function createReceipt(
  input: CreateReceiptInput,
  options: AuthenticatedRequestOptions = {},
) {
  return apiFetch<Receipt>(
    "/api/receipt",
    {
      body: JSON.stringify({
        customCategoryId: input.customCategoryId ?? null,
        generalCategoryId: input.generalCategoryId ?? null,
        id: null,
        imageUrl: input.imageUrl ?? "manual-entry",
        ocrStatus: input.ocrStatus ?? "COMPLETED",
        totalAmount: input.totalAmount,
        transactionAmount: input.transactionAmount ?? input.totalAmount,
        transactionDate: input.transactionDate,
        userId: input.userId,
        vendorName: input.vendorName,
      }),
      method: "POST",
    },
    getAuthOptions(options),
  );
}

export function deleteReceipt(
  id: number,
  options: AuthenticatedRequestOptions = {},
) {
  return apiFetch<void>(
    `/api/receipt/${id}`,
    {
      method: "DELETE",
    },
    getAuthOptions(options),
  );
}

export function getReceiptDownloadUrl(receiptId: number) {
  return `/api/receipt/${receiptId}/download`;
}

export function uploadReceipt({
  file,
  onProgress,
  userId,
}: {
  file: File;
  onProgress?: (progress: number) => void;
  userId: number;
}) {
  return new Promise<Receipt>((resolve, reject) => {
    const formData = new FormData();
    formData.append("userId", String(userId));
    formData.append("file", file);

    const request = new XMLHttpRequest();
    request.open("POST", getApiRequestUrl("/api/receipt/upload"));

    request.upload.onprogress = (event) => {
      if (event.lengthComputable && onProgress) {
        onProgress(Math.round((event.loaded / event.total) * 100));
      }
    };

    request.onerror = () => {
      reject(
        new ApiError(
          "Unable to upload the receipt. Check your connection and try again.",
        ),
      );
    };

    request.onload = () => {
      let payload: unknown;

      try {
        payload = request.responseText
          ? (JSON.parse(request.responseText) as unknown)
          : undefined;
      } catch {
        payload = undefined;
      }

      if (request.status < 200 || request.status >= 300) {
        const message =
          typeof payload === "object" &&
          payload !== null &&
          "message" in payload &&
          typeof payload.message === "string"
            ? payload.message
            : "Receipt upload failed. Try again.";

        reject(new ApiError(message, request.status));
        return;
      }

      onProgress?.(100);
      resolve(payload as Receipt);
    };

    request.send(formData);
  });
}
