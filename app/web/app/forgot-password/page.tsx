import Link from "next/link";
import { Input } from "@/src/shared/ui/input/input";

type ForgotPasswordPageProps = {
  searchParams?: { [key: string]: string | string[] | undefined };
};

export default function ForgotPasswordPage({ searchParams }: ForgotPasswordPageProps) {
  const email = searchParams?.email as string | undefined;
  const status = searchParams?.status as string | undefined;

  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-[var(--scanit-background)]">
      <div className="w-full max-w-md rounded-lg bg-[var(--scanit-card)] p-8 shadow-md">
        <h1 className="mb-6 text-center text-2xl font-bold text-[var(--scanit-text)]">Forgot Password</h1>
        {status === "success" ? (
          <div className="text-center text-green-600">
            If an account with the email <strong>{email}</strong> exists, a password reset link has been sent.
          </div>
        ) : (
          <form method="POST" action="/api/auth/forgot-password">
           <div className="animate-[auth-field-enter_320ms_cubic-bezier(0.16,1,0.3,1)]">
                               <Input
                                   autoComplete="email"
                                      defaultValue={email}
                                   icon={<MailIcon />}
                                   label="Email"
                                   name="email"
                                   placeholder="user@example.com"
                                   type="email"
                               />
                             </div>
            <button className="scanit-btn active:scale-[0.985] disabled:cursor-not-allowed disabled:opacity-60 scanit-btn-primary h-[3.25rem] w-full" type="submit">Send Reset Link</button>
          </form>
        )}
        <div className="mt-4 text-center">
          <Link href="/login" className="text-sm text-[var(--scanit-primary)] hover:underline">
            Back to Login
          </Link>
        </div>
      </div>
    </div>
  );
}

function MailIcon() {
  return (
      <svg
          aria-hidden="true"
          fill="none"
          height="16"
          viewBox="0 0 24 24"
          width="16"
      >
        <path
            d="m4 7 8 6 8-6"
            stroke="currentColor"
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth="1.5"
        />
        <rect
            height="14"
            rx="3"
            stroke="currentColor"
            strokeWidth="1.5"
            width="18"
            x="3"
            y="5"
        />
      </svg>
  );
}