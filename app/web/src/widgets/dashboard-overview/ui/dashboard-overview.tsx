import Link from "next/link";
import type { User } from "@/src/entities/user/types/user";
import { logout } from "@/src/features/auth/model/actions/logout";

type DashboardOverviewProps = {
  mode?: string | string[];
  user: User | null;
};

const metrics = [
  {
    label: "Total Spent",
    value: "$2,847.50",
    detail: "vs $2,542 last month",
    badge: "+12%",
    badgeTone: "danger",
  },
  {
    label: "Budget Remaining",
    value: "$1,152.50",
    detail: "of $4,000 this month",
    badge: "68%",
    badgeTone: "success",
  },
  {
    label: "Days Remaining",
    value: "18",
    detail: "of 31 days in May",
  },
];

const spendingBars = [
  { label: "Groceries", height: "72%", value: "$920" },
  { label: "Dining", height: "44%", value: "$420" },
  { label: "Travel", height: "58%", value: "$610" },
  { label: "Utilities", height: "35%", value: "$265" },
  { label: "Office", height: "49%", value: "$390" },
  { label: "Other", height: "31%", value: "$242" },
];

const transactions = [
  { merchant: "Tesco Express", amount: "$46.20", category: "Groceries" },
  { merchant: "Luas Travel", amount: "$12.80", category: "Transport" },
  { merchant: "The Green Cafe", amount: "$18.40", category: "Dining" },
  { merchant: "Office Supplies", amount: "$86.90", category: "Work" },
];

function getFirstName(user: User | null) {
  return user?.name.trim().split(/\s+/)[0] || "Guest";
}

function getStatusMessage(mode: string | string[] | undefined, isGuest: boolean) {
  if (isGuest) {
    return "Guest mode uses sample data. Create an account or sign in before adding real receipts so your workspace can be saved.";
  }

  if (mode === "signin") {
    return "You are signed in. Your ScanIt workspace is connected to your account.";
  }

  if (mode === "signup") {
    return "Your account was created successfully. Your ScanIt workspace is ready.";
  }

  return "Your ScanIt workspace is connected to your account.";
}

export function DashboardOverview({ mode, user }: DashboardOverviewProps) {
  const isGuest = !user;
  const firstName = getFirstName(user);

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
          <p className="scanit-app-greeting">
            {isGuest ? "Welcome to ScanIt" : `Good morning, ${firstName}`}
          </p>
          <div className="flex gap-3">
            {isGuest ? (
              null
            ) : (
              <>
                <span className="scanit-btn scanit-btn-cta hidden h-10 sm:inline-flex">
                  <ReceiptIcon />
                  Scan Receipt
                </span>
                <span className="scanit-btn scanit-btn-primary hidden h-10 sm:inline-flex">
                  <PlusIcon />
                  Add Entry
                </span>
                <form action={logout}>
                  <button
                    type="submit"
                    className="scanit-btn scanit-btn-secondary h-10"
                  >
                    Log out
                  </button>
                </form>
              </>
            )}
          </div>
        </header>

        <section className="scanit-app-page">
          <div className="mx-auto max-w-7xl">
            <div className="mb-6 flex flex-col justify-between gap-3 sm:flex-row sm:items-end">
              <div>
                <p className="text-sm font-semibold text-[var(--scanit-primary)]">
                  {isGuest ? "Sample workspace" : user.email}
                </p>
                <h1 className="mt-1 font-serif text-[28px] font-bold text-[var(--scanit-text)]">
                  Dashboard
                </h1>
              </div>
              {isGuest ? (
                <div className="flex gap-2">
                  <Link
                    href="/login?mode=signin"
                    className="scanit-btn scanit-btn-secondary h-10"
                  >
                    Sign in
                  </Link>
                  <Link
                    href="/login?mode=signup"
                    className="scanit-btn scanit-btn-primary h-10"
                  >
                    Create account
                  </Link>
                </div>
              ) : null}
            </div>

            <div
              className={[
                "mb-6 rounded-lg border px-4 py-3 text-sm font-medium",
                isGuest
                  ? "border-[var(--scanit-warning-soft)] bg-[var(--scanit-warning-softer)] text-[var(--scanit-warning-text)]"
                  : "border-[var(--scanit-primary-softer)] bg-[var(--scanit-primary-soft)] text-[var(--scanit-primary)]",
              ].join(" ")}
            >
              {getStatusMessage(mode, isGuest)}
            </div>

            <div className="grid gap-5 lg:grid-cols-3">
              {metrics.map((metric) => (
                <article key={metric.label} className="scanit-auth-card p-5">
                  <div className="flex items-start justify-between gap-4">
                    <p className="text-sm font-medium text-[var(--scanit-label)]">
                      {metric.label}
                    </p>
                    {metric.badge ? (
                      <span
                        className={[
                          "rounded-full px-3 py-1 text-xs font-bold",
                          metric.badgeTone === "danger"
                            ? "text-[var(--scanit-danger)]"
                            : "bg-[var(--scanit-primary-softer)] text-[var(--scanit-primary)]",
                        ].join(" ")}
                      >
                        {metric.badge}
                      </span>
                    ) : null}
                  </div>
                  <p className="mt-4 font-serif text-4xl font-bold text-[var(--scanit-text)]">
                    {metric.value}
                  </p>
                  <p className="mt-2 text-sm text-[var(--scanit-text-secondary)]">
                    {metric.detail}
                  </p>
                </article>
              ))}
            </div>

            <div className="mt-8 grid gap-5 lg:grid-cols-[minmax(0,1.35fr)_minmax(20rem,0.65fr)]">
              <section className="scanit-auth-card p-5">
                <div className="mb-5 flex items-center justify-between gap-4">
                  <h2 className="font-serif text-xl font-bold text-[var(--scanit-text)]">
                    Monthly Spending
                  </h2>
                  <span className="rounded-lg border border-[var(--scanit-border)] bg-[var(--scanit-soft)] px-4 py-2 text-sm font-medium">
                    May 2025
                  </span>
                </div>
                <div className="grid min-h-72 grid-cols-6 items-end gap-3">
                  {spendingBars.map((bar) => (
                    <div
                      key={bar.label}
                      className="flex h-72 flex-col items-center justify-end gap-2"
                    >
                      <p className="text-xs font-semibold text-[var(--scanit-label)]">
                        {bar.value}
                      </p>
                      <div className="flex h-52 w-full items-end rounded-lg bg-[var(--scanit-soft)] p-1">
                        <div
                          className="w-full rounded-md bg-[var(--scanit-primary)]"
                          style={{ height: bar.height }}
                        />
                      </div>
                      <p className="max-w-full truncate text-xs text-[var(--scanit-text-secondary)]">
                        {bar.label}
                      </p>
                    </div>
                  ))}
                </div>
              </section>

              <section className="scanit-auth-card p-5">
                <h2 className="font-serif text-xl font-bold text-[var(--scanit-text)]">
                  Recent Activity
                </h2>
                <div className="mt-5 space-y-3">
                  {transactions.map((transaction) => (
                    <div
                      key={transaction.merchant}
                      className="flex items-center justify-between gap-4 rounded-lg border border-[var(--scanit-border)] px-4 py-3"
                    >
                      <div>
                        <p className="font-semibold text-[var(--scanit-text)]">
                          {transaction.merchant}
                        </p>
                        <p className="text-sm text-[var(--scanit-text-secondary)]">
                          {transaction.category}
                        </p>
                      </div>
                      <p className="font-serif text-lg font-bold">
                        {transaction.amount}
                      </p>
                    </div>
                  ))}
                </div>
                {isGuest ? (
                  <Link
                    href="/login?mode=signup"
                    className="scanit-btn scanit-btn-primary mt-5 h-11 w-full"
                  >
                    Save my workspace
                  </Link>
                ) : null}
              </section>
            </div>
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
