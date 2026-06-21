import Link from "next/link";

type CheckEmailPageProps = {
    searchParams: Promise<{
        email?: string;
    }>;
};

export default async function CheckEmailPage({
                                                 searchParams,
                                             }: CheckEmailPageProps) {
    const { email } = await searchParams;

    return (
        <main className="scanit-auth-shell">
            <section className="scanit-app-page">
                <div className="mx-auto max-w-md">
                    <section className="scanit-auth-card p-6 sm:p-7">
                        <p className="text-[0.8125rem] font-semibold text-[var(--scanit-primary)]">
                            Email verification
                        </p>

                        <h1 className="mt-2 font-serif text-[2rem] font-bold leading-tight text-[var(--scanit-text)]">
                            Check your email
                        </h1>

                        <p className="mt-4 text-[0.9375rem] leading-6 text-[var(--scanit-text-secondary)]">
                            We created your account in pending verification state.
                            {email ? (
                                <>
                                    {" "}
                                    A verification link was sent to <strong>{email}</strong>.
                                </>
                            ) : null}
                        </p>

                        <p className="mt-4 text-[0.875rem] leading-6 text-[var(--scanit-text-secondary)]">
                            Your account cannot be used until the email address is verified.
                            The verification link is valid for 24 hours.
                        </p>

                        <Link
                            href="/login?mode=signin"
                            className="scanit-btn scanit-btn-primary mt-6 h-12 w-full"
                        >
                            Back to sign in
                        </Link>
                    </section>
                </div>
            </section>
        </main>
    );
}