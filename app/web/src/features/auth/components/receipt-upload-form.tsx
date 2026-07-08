"use client";

import { useState } from "react";
import { uploadReceipt } from "@/src/features/auth/api/receipt-api";
import { GlassButton } from "@/src/features/auth/components/glass-button";

export function ReceiptUploadForm() {
    const [file, setFile] = useState<File | null>(null);
    const [pending, setPending] = useState(false);
    const [message, setMessage] = useState("");

    async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();
        if (!file) {
            setMessage("Please select a receipt to upload");
            return
        }

        try {
            setPending(true);
            setMessage("");

            const receipt = await uploadReceipt(file);

            setMessage("Receipt uploaded successfully");
        } catch (e) {
            setMessage(e instanceof Error ? e.message : "Failed to upload receipt");
        } finally {
            setPending(false);
        }
    }

    return (
        <form className="mt-7 flex flex-col gap-6" onSubmit={handleSubmit}>
            {message && (
                <div className="rounded-2xl border border-white/8 bg-[linear-gradient(180deg,rgba(40,42,48,0.4)_0%,rgba(30,32,38,0.3)_100%)] px-4 py-3 text-sm text-white/70">>
                    {message}
                </div>
            )}

            <label className="flex cursor-pointer flex-col items-center justify-center rounded-3xl border border-dashed border-white/10 bg-[linear-gradient(180deg,rgba(40,42,48,0.54)_0%,rgba(31,33,39,0.48)_100%)] p-8 text-center transition hover:border-white/20">
                <ReceiptIcon />

                <span className="mt-3 text-sm font-medium text-white">
                    {file ? file.name : "Choose a receipt"}
                </span>

                <span className="mt-1 text-xs text-white/40">
                    JPG, PNG or PDF
                </span>

                <input
                    accept="image/*,.pdf"
                    className="hidden"
                    type="file"
                    onChange={(e) => setFile(e.target.files?.[0] ?? null)}
                />
            </label>

            <GlassButton className="w-full" disabled={pending || !file} type="submit">
                {pending ? "Uploading" : "Upload a receipt"}
            </GlassButton>
        </form>
    );
}

function ReceiptIcon() {
    return (
        <svg width="32" height="32" fill="none" viewBox="0 0 24 24">
            <path
                d="M7 3h10l2 2v16l-3-2-2 2-2-2-2 2-3-2V5l2-2Z"
                stroke="currentColor"
                strokeWidth="1.5"
                strokeLinecap="round"
                strokeLinejoin="round"
            />
            <path
                d="M9 9h6M9 13h6"
                stroke="currentColor"
                strokeWidth="1.5"
                strokeLinecap="round"
            />
        </svg>
    );
}