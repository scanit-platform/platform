import { redirect } from "next/navigation";
import {
    resendVerification,
    verifyEmail,
} from "@/src/features/auth/api/auth-api";
import { storeAuthToken } from "@/src/features/auth/model/session";

type VerifyEmailPageProps = {
    searchParams: Promise<{
        token?: string;
        resent?: string;
    }>;
};

async function resendVerificationAction(formData: FormData) {
    "use server";

    const email = String(formData.get("email") ?? "").trim();

    if (email) {
        await resendVerification({ email });
    }

    redirect("/verify-email?resent=true");
}

export default async function VerifyEmailPage({
                                                  searchParams,
                                              }: VerifyEmailPageProps) {
    const { token, resent } = await searchParams;

    if (token) {
        let errorMessage = "";

        try {
            const response = await verifyEmail(token);
            const tokenError = await storeAuthToken(response);

            if (tokenError) {
                errorMessage = tokenError;
            } else {
                redirect("/dashboard?verified=true");
            }
        } catch (error) {
            errorMessage =
                error instanceof Error
                    ? error.message
                    : "Verification failed. Request a new verification email.";
        }

        return (
            <VerificationFailed
                message={errorMessage}
                resent={resent === "true"}
            />
        );
    }

    return (
        <VerificationFailed
            message="Verification token is missing."
            resent={resent === "true"}
        />
    );
}

function VerificationFailed({
                                message,
                                resent,
                            }: {
    message: string;
    resent: boolean;
}) {
    return (
        <main className="scanit-auth-shell">
            <section className="scanit-app-page">
                <div className="mx-auto max-w-md">
                    <section className="scanit-auth-card p-6 sm:p-7">
                        <p className="text-[0.8125rem] font-semibold text-[var(--scanit-primary)]">
                            Email verification
                        </p>

                        <h1 className="mt-2 font-serif text-[2rem] font-bold leading-tight text-[var(--scanit-text)]">
                            Verification link problem
                        </h1>

                        <div className="scanit-auth-error mt-6 rounded-lg px-4 py-3 text-[0.8125rem] font-medium">
                            {message}
                        </div>

                        {resent ? (
                            <p className="mt-4 text-[0.875rem] leading-6 text-[var(--scanit-text-secondary)]">
                                A new verification email has been requested. Check your inbox.
                            </p>
                        ) : null}

                        <p className="mt-4 text-[0.875rem] leading-6 text-[var(--scanit-text-secondary)]">
                            If the verification link expired, enter your email address and
                            request a new verification email.
                        </p>

                        <form action={resendVerificationAction} className="mt-6 space-y-4">
                            <label className="block text-[0.875rem] font-medium text-[var(--scanit-label)]">
                                Email address
                            </label>

                            <input
                                className="w-full rounded-xl border border-[var(--scanit-border)] bg-white px-4 py-3 text-[0.9375rem]"
                                name="email"
                                placeholder="you@example.com"
                                type="email"
                                required
                            />

                            <button
                                className="scanit-btn scanit-btn-primary h-12 w-full"
                                type="submit"
                            >
                                Resend verification email
                            </button>
                        </form>
                    </section>
                </div>
            </section>
        </main>
    );
}