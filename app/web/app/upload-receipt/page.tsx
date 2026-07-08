import { ReceiptUploadForm } from "@/src/features/auth/components/receipt-upload-form";

export function uploadReceiptPage() {
    return (
        <div className="flex min-h-screen flex-col items-center justify-center bg-[var(--scanit-background)]">
            <div className="w-full max-w-md rounded-lg bg-[var(--scanit-card)] p-8 shadow-md">
                <h1 className="mb-2 text-center text-2xl font-bold text-[var(--scanit-text)]">
                    Upload Receipt
                </h1>
                <p className="mb-6 text-center text-sm text-[var(--scanit-muted)]">
                    Upload a receipt image to extract its details.
                </p>

                <ReceiptUploadForm />
            </div>
        </div>
    )
}