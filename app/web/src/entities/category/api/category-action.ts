"use server";

import { revalidatePath } from "next/cache";
import { createCustomCategoryForUser } from "./category-server";

export async function createCategoryAction(formData: FormData) {
    await createCustomCategoryForUser({
        generalCategoryId: String(formData.get("generalCategoryId") ?? ""),
        name: String(formData.get("name") ?? "").trim(),
    });
    revalidatePath("/dashboard/categories");
    revalidatePath("/dashboard/scan")
}