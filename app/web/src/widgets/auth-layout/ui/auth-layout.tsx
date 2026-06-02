import Link from "next/link";
import type { AuthMode } from "@/src/features/auth/model/auth-state";
import { AuthForm } from "@/src/features/auth/ui/auth-form";

type AuthLayoutProps = {
  initialMode?: AuthMode;
};

export function AuthLayout({ initialMode }: AuthLayoutProps) {
  return (
    <main className="scanit-auth-shell">
      <aside className="scanit-app-sidebar hidden md:flex">
        <Link href="/dashboard" className="scanit-app-brand">
          <span aria-hidden="true">📊</span>
          <span>
            Scan <span className="text-[var(--scanit-primary)]">It</span>
          </span>
        </Link>
        <nav className="scanit-app-nav" aria-label="Application navigation">
          <Link
            href="/dashboard"
            className="scanit-app-nav-item scanit-app-nav-item-active"
          >
            <HomeIcon />
            Dashboard
          </Link>
          <span className="scanit-app-nav-item">
            <ReceiptIcon />
            Receipts
          </span>
          <span className="scanit-app-nav-item">
            <ChartIcon />
            Budget
          </span>
          <span className="scanit-app-nav-item">
            <CardIcon />
            Transactions
          </span>
        </nav>
      </aside>

      <div className="scanit-app-main">
        <header className="scanit-app-header">
          <p className="scanit-app-greeting">👋 Welcome to ScanIt</p>
          <div className="hidden gap-3 sm:flex">
            <span className="scanit-btn scanit-btn-cta">
              <ReceiptIcon />
              Scan Receipt
            </span>
            <span className="scanit-btn scanit-btn-primary">
              <PlusIcon />
              Add Entry
            </span>
          </div>
        </header>

        <section className="scanit-app-page">
          <div className="mx-auto max-w-md">
            <h1 className="mb-6 font-serif text-[28px] font-bold">
              Authentication
            </h1>
            <section
              className="scanit-auth-card p-6 sm:p-7"
              style={{
                animation:
                  "auth-card-enter 500ms cubic-bezier(0.16,1,0.3,1) forwards",
              }}
            >
              <AuthForm initialMode={initialMode} />
            </section>
          </div>
        </section>
      </div>
    </main>
  );
}

function HomeIcon() {
  return (
    <svg
      aria-hidden="true"
      fill="none"
      height="20"
      viewBox="0 0 24 24"
      width="20"
    >
      <path
        d="m3 12 9-9 9 9M5 10v10h5v-6h4v6h5V10"
        stroke="currentColor"
        strokeLinecap="round"
        strokeLinejoin="round"
        strokeWidth="2"
      />
    </svg>
  );
}

function ReceiptIcon() {
  return (
    <svg
      aria-hidden="true"
      fill="none"
      height="18"
      viewBox="0 0 24 24"
      width="18"
    >
      <path
        d="M7 3h10a2 2 0 0 1 2 2v17l-3-2-2 2-2-2-2 2-2-2-3 2V5a2 2 0 0 1 2-2Z"
        stroke="currentColor"
        strokeLinecap="round"
        strokeLinejoin="round"
        strokeWidth="2"
      />
      <path
        d="M9 8h6M9 12h6M9 16h3"
        stroke="currentColor"
        strokeLinecap="round"
        strokeWidth="2"
      />
    </svg>
  );
}

function PlusIcon() {
  return (
    <svg
      aria-hidden="true"
      fill="none"
      height="18"
      viewBox="0 0 24 24"
      width="18"
    >
      <path
        d="M12 5v14M5 12h14"
        stroke="currentColor"
        strokeLinecap="round"
        strokeWidth="2"
      />
    </svg>
  );
}

function ChartIcon() {
  return (
    <svg
      aria-hidden="true"
      fill="none"
      height="20"
      viewBox="0 0 24 24"
      width="20"
    >
      <path
        d="M4 19V5M4 19h16M8 15v-4M12 15V8M16 15v-6"
        stroke="currentColor"
        strokeLinecap="round"
        strokeLinejoin="round"
        strokeWidth="2"
      />
    </svg>
  );
}

function CardIcon() {
  return (
    <svg
      aria-hidden="true"
      fill="none"
      height="20"
      viewBox="0 0 24 24"
      width="20"
    >
      <path
        d="M3 8h18M7 15h1m4 0h1M5 5h14a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2Z"
        stroke="currentColor"
        strokeLinecap="round"
        strokeLinejoin="round"
        strokeWidth="2"
      />
    </svg>
  );
}
