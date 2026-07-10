import Link from "next/link";
import type { ReactNode } from "react";
import type { User } from "@/src/entities/user/types/user";
import { logout } from "@/src/features/auth/model/actions/logout";
import {
  CardIcon,
  ChartIcon,
  HomeIcon,
  PlusIcon,
  ReceiptIcon,
} from "@/src/shared/ui/icons/icons";

type AppShellNavItem =
  | "budget"
  | "categories"
  | "dashboard"
  | "receipts"
  | "transactions";

type AppShellProps = {
  activeItem?: AppShellNavItem;
  children: ReactNode;
  user: User | null;
};

const navItems: Array<{
  href?: string;
  icon: ReactNode;
  id: AppShellNavItem;
  label: string;
}> = [
  {
    href: "/dashboard",
    icon: <HomeIcon />,
    id: "dashboard",
    label: "Dashboard",
  },
  {
    href: "/dashboard/receipts",
    icon: <ReceiptIcon size={20} />,
    id: "receipts",
    label: "Receipts",
  },
  {
    href: "/dashboard/budget",
    icon: <ChartIcon />,
    id: "budget",
    label: "Budget",
  },
  {
    href: "/dashboard/categories",
    icon: <PlusIcon />,
    id: "categories",
    label: "Categories",
  },
  {
    icon: <CardIcon />,
    id: "transactions",
    label: "Transactions",
  },
];

function getFirstName(user: User | null) {
  return user?.name.trim().split(/\s+/)[0] || "Guest";
}

export function AppShell({
  activeItem = "dashboard",
  children,
  user,
}: AppShellProps) {
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
          {navItems.map((item) => {
            const className = [
              "scanit-app-nav-item",
              item.id === activeItem ? "scanit-app-nav-item-active" : "",
            ].join(" ");

            if (item.href) {
              return (
                <Link key={item.id} href={item.href} className={className}>
                  {item.icon}
                  {item.label}
                </Link>
              );
            }

            return (
              <span key={item.id} className={className}>
                {item.icon}
                {item.label}
              </span>
            );
          })}
        </nav>
      </aside>

      <div className="scanit-app-main">
        <header className="scanit-app-header">
          <p className="scanit-app-greeting">
            {isGuest ? "Welcome to ScanIt" : `Good morning, ${firstName}`}
          </p>
          <div className="flex flex-wrap justify-end gap-3">
            {isGuest ? null : (
              <>
                <Link
                  href="/dashboard/scan"
                  className="scanit-btn scanit-btn-cta hidden h-10 sm:inline-flex"
                >
                  <ReceiptIcon />
                  Scan Receipt
                </Link>
                <Link
                  href="/dashboard/entry"
                  className="scanit-btn scanit-btn-primary hidden h-10 sm:inline-flex"
                >
                  <PlusIcon />
                  Add Entry
                </Link>
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

        {children}
      </div>
    </main>
  );
}
