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

export type UpdateReceiptInput = {
    transactionAmount: number;
    transactionDate: string;
    totalAmount: number;
    vendorName: string;
};

type AuthenticatedRequestOptions = {
    authToken?: string;
};

function getAuthOptions(
    options: AuthenticatedRequestOptions = {},
) {
    return options.authToken
        ? { authToken: options.authToken }
        : {};
}

export function getReceipts(
    options: AuthenticatedRequestOptions = {},
) {
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
                customCategoryId:
                    input.customCategoryId ?? null,

                generalCategoryId:
                    input.generalCategoryId ?? null,

                id: null,

                imageUrl:
                    input.imageUrl ?? "manual-entry",

                ocrStatus:
                    input.ocrStatus ?? "COMPLETED",

                totalAmount:
                input.totalAmount,

                transactionAmount:
                    input.transactionAmount ??
                    input.totalAmount,

                transactionDate:
                input.transactionDate,

                userId:
                input.userId,

                vendorName:
                input.vendorName,
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

export function getReceiptById(
    id: number,
    options: AuthenticatedRequestOptions = {},
) {
    return apiFetch<Receipt>(
        `/api/receipt/${id}`,
        {
            method: "GET",
        },
        getAuthOptions(options),
    );
}

export function updateReceipt(
    id: number,
    input: UpdateReceiptInput,
    options: AuthenticatedRequestOptions = {},
) {
    return apiFetch<Receipt>(
        `/api/receipt/${id}`,
        {
            body: JSON.stringify(input),
            method: "PUT",
        },
        getAuthOptions(options),
    );
}

export function saveDuplicateAsNew(
    id: number,
    options: AuthenticatedRequestOptions = {},
) {
    return apiFetch<Receipt>(
        `/api/receipt/${id}/save-as-new`,
        {
            method: "POST",
        },
        getAuthOptions(options),
    );
}

export function getReceiptDownloadUrl(
    receiptId: number,
) {
    return `/api/receipt/${receiptId}/download`;
}

/*
 * Run OCR after the receipt
 * has been uploaded to S3.
 */
export function extractReceipt({
                                   key,
                                   signal,
                                   userId,
                               }: {
    key: string;
    signal?: AbortSignal;
    userId: number;
}) {
    return apiFetch<Receipt>(
        "/api/receipt/extract",
        {
            body: JSON.stringify({
                key,
                userId,
            }),
            method: "POST",
            signal,
        },
    );
}

/*
 * Upload a receipt with progress reporting
 * and cancellation through AbortSignal.
 */
export function uploadReceipt({
                                  file,
                                  onProgress,
                                  signal,
                                  userId,
                              }: {
    file: File;
    onProgress?: (progress: number) => void;
    signal?: AbortSignal;
    userId: number;
}) {
    return new Promise<Receipt>(
        (resolve, reject) => {
            if (signal?.aborted) {
                reject(
                    new DOMException(
                        "Receipt upload cancelled.",
                        "AbortError",
                    ),
                );

                return;
            }

            const formData = new FormData();

            formData.append(
                "userId",
                String(userId),
            );

            formData.append(
                "file",
                file,
            );

            const request =
                new XMLHttpRequest();

            request.open(
                "POST",
                getApiRequestUrl(
                    "/api/receipt/upload",
                ),
            );

            const abortRequest = () => {
                request.abort();
            };

            const removeAbortListener = () => {
                signal?.removeEventListener(
                    "abort",
                    abortRequest,
                );
            };

            signal?.addEventListener(
                "abort",
                abortRequest,
                {
                    once: true,
                },
            );

            request.upload.onprogress = (
                event,
            ) => {
                if (
                    event.lengthComputable &&
                    onProgress
                ) {
                    const progress = Math.round(
                        (event.loaded / event.total) *
                        100,
                    );

                    onProgress(progress);
                }
            };

            request.onabort = () => {
                removeAbortListener();

                reject(
                    new DOMException(
                        "Receipt upload cancelled.",
                        "AbortError",
                    ),
                );
            };

            request.onerror = () => {
                removeAbortListener();

                reject(
                    new ApiError(
                        "Unable to upload the receipt. Check your connection and try again.",
                    ),
                );
            };

            request.onload = () => {
                removeAbortListener();

                let payload: unknown;

                try {
                    payload = request.responseText
                        ? (JSON.parse(
                            request.responseText,
                        ) as unknown)
                        : undefined;
                } catch {
                    payload = undefined;
                }

                if (
                    request.status < 200 ||
                    request.status >= 300
                ) {
                    const message =
                        typeof payload === "object" &&
                        payload !== null &&
                        "message" in payload &&
                        typeof payload.message ===
                        "string"
                            ? payload.message
                            : "Receipt upload failed. Try again.";

                    reject(
                        new ApiError(
                            message,
                            request.status,
                        ),
                    );

                    return;
                }

                onProgress?.(100);

                resolve(
                    payload as Receipt,
                );
            };

            request.send(formData);
        },
    );
}