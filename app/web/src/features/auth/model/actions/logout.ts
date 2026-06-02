"use server";

import { redirect } from "next/navigation";
import { clearAuthToken } from "@/src/features/auth/model/session";

export async function logout() {
  await clearAuthToken();
  redirect("/login");
}
