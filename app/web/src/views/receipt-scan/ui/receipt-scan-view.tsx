import { getAuthenticatedUserOrRedirect } from "@/src/features/auth/model/session";
import { ReceiptScan } from "@/src/widgets/receipt-scan/ui/receipt-scan";
import { getGeneralCategories } from "@/src/entities/category/api/categories-service";

export async function ReceiptScanView() {
  const user = await getAuthenticatedUserOrRedirect();
  const generalCategories = await getGeneralCategories();

  return <ReceiptScan
            user={user}
            generalCategories={generalCategories}
         />;
}
