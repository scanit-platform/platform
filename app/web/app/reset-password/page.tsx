import Link from "next/link";
import { redirect } from "next/navigation";
import { ApiError, apiFetch } from "@/src/shared/api/client";
import { Input } from "@/src/shared/ui/input/input";

type ResetPasswordPageProps = {
  searchParams: Promise<{
    status?: string;
    token?: string;
  }>;
};

async function resetPasswordAction(formData: FormData) {
  "use server";

  const token = String(formData.get("token") ?? "");
  const password = String(formData.get("password") ?? "");
  const confirmPassword = String(formData.get("confirmPassword") ?? "");

  await apiFetch("/api/v1/password-reset-request/reset-password", {
    body: JSON.stringify({
      confirmPassword,
      password,
      token,
    }),
    method: "POST",
  });

  redirect("/reset-password?status=success");
}

export default async function ResetPasswordPage({
  searchParams,
}: ResetPasswordPageProps) {
  const { status, token } = await searchParams;
  let tokenError = "";

  if (token && status !== "success") {
    try {
      await apiFetch(
        `/api/v1/password-reset-request/validate?token=${encodeURIComponent(
          token,
        )}`,
        {
          method: "GET",
        },
      );
    } catch (error) {
      tokenError =
        error instanceof ApiError || error instanceof Error
          ? error.message
          : "This reset link is invalid or expired.";
    }
  }

  return (
    <main className="scanit-auth-shell flex items-center justify-center px-5 py-10">
      <section className="scanit-auth-card w-full max-w-md p-6 sm:p-7">
        <h1 className="font-serif text-[28px] font-bold text-[var(--scanit-text)]">
          Reset Password
        </h1>

        {status === "success" ? (
          <div className="mt-5 rounded-lg border border-[var(--scanit-primary-softer)] bg-[var(--scanit-primary-soft)] px-4 py-3 text-sm font-semibold text-[var(--scanit-primary)]">
            Password reset successfully. You can now sign in.
          </div>
        ) : tokenError || !token ? (
          <div className="scanit-auth-error mt-5 rounded-lg px-4 py-3 text-sm font-semibold">
            {tokenError || "A reset token is required."}
          </div>
        ) : (
          <form action={resetPasswordAction} className="mt-6 space-y-4">
            <input name="token" type="hidden" value={token} />
            <Input
              autoComplete="new-password"
              label="New password"
              name="password"
              placeholder="At least 8 characters"
              required
              type="password"
            />
            <Input
              autoComplete="new-password"
              label="Confirm password"
              name="confirmPassword"
              placeholder="Repeat password"
              required
              type="password"
            />
            <button className="scanit-btn scanit-btn-primary h-12 w-full" type="submit">
              Reset Password
            </button>
          </form>
        )}

        <Link
          className="mt-5 block text-center text-sm font-semibold text-[var(--scanit-primary)]"
          href="/login?mode=signin"
        >
          Back to sign in
        </Link>
      </section>
    </main>
  );
}
