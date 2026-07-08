import type {
  ChangeEvent,
  DragEvent,
  KeyboardEvent,
  MouseEvent,
} from "react";
import { useRef, useState } from "react";
import {
  formatReceiptFileSize,
  receiptAcceptedInputTypes,
  receiptPhotoInputTypes,
} from "@/src/features/receipt-scan/model/receipt-scan-state";
import { Button } from "@/src/shared/ui/button/button";
import {
  CameraIcon,
  FileIcon,
  ReceiptIcon,
  UploadIcon,
} from "@/src/shared/ui/icons/icons";

type ReceiptUploadStepProps = {
  error: string;
  onFileSelected: (file: File | null) => void;
  onStartProcessing: () => void;
  selectedFile: File | null;
};

export function ReceiptUploadStep({
  error,
  onFileSelected,
  onStartProcessing,
  selectedFile,
}: ReceiptUploadStepProps) {
  const cameraInputRef = useRef<HTMLInputElement>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [isDragging, setIsDragging] = useState(false);

  const openFilePicker = () => fileInputRef.current?.click();
  const openCameraPicker = () => cameraInputRef.current?.click();

  const handleFileInputChange = (event: ChangeEvent<HTMLInputElement>) => {
    onFileSelected(event.target.files?.[0] ?? null);
    event.target.value = "";
  };

  const handleDrop = (event: DragEvent<HTMLDivElement>) => {
    event.preventDefault();
    setIsDragging(false);
    onFileSelected(event.dataTransfer.files?.[0] ?? null);
  };

  const handleDragOver = (event: DragEvent<HTMLDivElement>) => {
    event.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = (event: DragEvent<HTMLDivElement>) => {
    if (event.currentTarget.contains(event.relatedTarget as Node)) {
      return;
    }

    setIsDragging(false);
  };

  const handleKeyDown = (event: KeyboardEvent<HTMLDivElement>) => {
    if (event.key === "Enter" || event.key === " ") {
      event.preventDefault();
      openFilePicker();
    }
  };

  const handleActionClick = (
    event: MouseEvent<HTMLButtonElement>,
    action: () => void,
  ) => {
    event.stopPropagation();
    action();
  };

  return (
    <div className="mx-auto max-w-3xl">
      <input
        ref={cameraInputRef}
        accept={receiptPhotoInputTypes}
        capture="environment"
        className="hidden"
        onChange={handleFileInputChange}
        type="file"
      />
      <input
        ref={fileInputRef}
        accept={receiptAcceptedInputTypes}
        className="hidden"
        onChange={handleFileInputChange}
        type="file"
      />

      <div
        aria-label="Upload receipt"
        className={[
          "scanit-auth-panel flex min-h-[21rem] cursor-pointer flex-col items-center justify-center border-dashed p-8 text-center transition-colors",
          isDragging
            ? "border-[var(--scanit-primary)] bg-[var(--scanit-primary-soft)]"
            : "border-[var(--scanit-border-strong)]",
          error ? "border-[var(--scanit-danger)]" : "",
        ].join(" ")}
        onClick={openFilePicker}
        onDragLeave={handleDragLeave}
        onDragOver={handleDragOver}
        onDrop={handleDrop}
        onKeyDown={handleKeyDown}
        role="button"
        tabIndex={0}
      >
        <div className="scanit-auth-icon-tile flex h-14 w-14 items-center justify-center">
          <ReceiptIcon size={28} />
        </div>
        <h2 className="mt-6 text-base font-bold text-[var(--scanit-text)]">
          Drop receipt here
        </h2>
        <p className="mt-2 text-sm text-[var(--scanit-text-secondary)]">
          or tap to browse your files
        </p>
        <p className="mt-6 text-xs font-medium text-[var(--scanit-text-secondary)]">
          JPG, PNG, or PDF - Max 10MB
        </p>
        <div className="mt-6 flex flex-wrap justify-center gap-3">
          <Button
            className="h-10 px-4"
            onClick={(event) => handleActionClick(event, openCameraPicker)}
            type="button"
          >
            <CameraIcon />
            Take Photo
          </Button>
          <Button
            className="h-10 px-4"
            onClick={(event) => handleActionClick(event, openFilePicker)}
            type="button"
            variant="secondary"
          >
            <UploadIcon />
            Upload File
          </Button>
        </div>
      </div>

      {error ? (
        <p className="mt-3 rounded-lg border border-[var(--scanit-danger-soft)] bg-[var(--scanit-danger-softer)] px-4 py-3 text-sm font-semibold text-[var(--scanit-danger-text)]">
          {error}
        </p>
      ) : null}

      {selectedFile ? (
        <div className="scanit-auth-panel mt-4 flex flex-col gap-4 p-4 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex min-w-0 items-center gap-3">
            <span className="scanit-auth-icon-tile flex h-11 w-11 shrink-0 items-center justify-center">
              <FileIcon />
            </span>
            <div className="min-w-0">
              <p className="truncate font-semibold text-[var(--scanit-text)]">
                {selectedFile.name}
              </p>
              <p className="text-sm text-[var(--scanit-text-secondary)]">
                {formatReceiptFileSize(selectedFile.size)}
              </p>
            </div>
          </div>
          <Button
            className="h-10 w-full px-4 sm:w-auto"
            onClick={onStartProcessing}
            type="button"
          >
            <CameraIcon />
            Scan Receipt
          </Button>
        </div>
      ) : null}
    </div>
  );
}
