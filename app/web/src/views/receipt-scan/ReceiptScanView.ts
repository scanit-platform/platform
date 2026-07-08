import { createElement } from "react";
import { getAuthenticatedUser } from "@/src/features/auth/model/session";
import { ReceiptScan } from "@/src/widgets/receipt-scan/ui/receipt-scan";

export async function ReceiptScanView() {
  const user = await getAuthenticatedUser();

  return createElement(ReceiptScan, {
    user,
  });
}
