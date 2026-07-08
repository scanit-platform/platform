export type ReceiptScanStep = "upload" | "processing" | "review";

export const receiptScanSteps: Array<{
  id: ReceiptScanStep;
  label: string;
}> = [
  { id: "upload", label: "Upload" },
  { id: "processing", label: "Processing" },
  { id: "review", label: "Review" },
];

export const maxReceiptFileSizeBytes = 10 * 1024 * 1024;
export const receiptAcceptedInputTypes =
  ".jpg,.jpeg,.png,.pdf,image/jpeg,image/png,application/pdf";
export const receiptPhotoInputTypes = "image/jpeg,image/png";
export const receiptProcessingDelayMs = 1600;

const acceptedMimeTypes = new Set([
  "application/pdf",
  "image/jpeg",
  "image/png",
]);

const acceptedExtensions = [".jpg", ".jpeg", ".png", ".pdf"];

export function validateReceiptFile(file: File) {
  const fileName = file.name.toLowerCase();
  const hasAcceptedMime = file.type ? acceptedMimeTypes.has(file.type) : false;
  const hasAcceptedExtension = acceptedExtensions.some((extension) =>
    fileName.endsWith(extension),
  );

  if (!hasAcceptedMime && !hasAcceptedExtension) {
    return "Choose a JPG, PNG, or PDF receipt.";
  }

  if (file.size > maxReceiptFileSizeBytes) {
    return "Receipt must be 10MB or smaller.";
  }

  return "";
}

export function formatReceiptFileSize(size: number) {
  if (size >= 1024 * 1024) {
    return `${(size / 1024 / 1024).toFixed(1)} MB`;
  }

  if (size >= 1024) {
    return `${Math.round(size / 1024)} KB`;
  }

  return `${size} B`;
}
