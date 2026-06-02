import Link from "next/link";

type DashboardPageProps = {
  searchParams: Promise<{
    mode?: string | string[] | undefined;
  }>;
};

function getModeLabel(mode: string | string[] | undefined) {
  if (mode === "signin") {
    return "signed in";
  }

  return "created an account";
}

export default async function DashboardPage({
  searchParams,
}: DashboardPageProps) {
  const { mode } = await searchParams;

  return (
    <main className="relative min-h-screen overflow-hidden bg-page-bg px-6 py-10 text-page-foreground">
      <div className="pointer-events-none absolute inset-0">
        <div className="absolute inset-x-[-10%] top-[-12%] h-128 rounded-full bg-[radial-gradient(circle_at_center,rgba(175,52,24,0.24),rgba(55,120,215,0.06)_55%,transparent_80%)] blur-3xl" />
      </div>
      <div className="relative mx-auto flex min-h-[calc(100vh-5rem)] max-w-5xl items-center justify-center">
        <section className="w-full max-w-2xl rounded-4xl border border-white/8 bg-white/4 p-8 shadow-[0_30px_90px_rgba(0,0,0,0.45)] backdrop-blur-2xl sm:p-12">
          <p className="text-xs font-medium uppercase tracking-[0.28em] text-white/45">
            Authentication Flow
          </p>
          <h1 className="mt-5 max-w-xl text-4xl font-semibold tracking-[-0.04em] text-white sm:text-5xl">
            You {getModeLabel(mode)} successfully.
          </h1>
          <p className="mt-5 max-w-xl text-base leading-7 text-white/62">
            This route is a minimal post-auth landing page so the server action
            has a real destination inside the App Router.
          </p>
          <div className="mt-10 flex flex-wrap gap-3">
            <Link
              href="/"
              className="inline-flex h-12 items-center justify-center rounded-full bg-white px-6 text-sm font-semibold tracking-[-0.01em] text-black transition-transform duration-200 hover:scale-[1.01]"
            >
              Back to auth
            </Link>
            <a
              href="mailto:team@example.com"
              className="inline-flex h-12 items-center justify-center rounded-full border border-white/12 bg-white/3 px-6 text-sm font-medium text-white/76 transition-colors duration-200 hover:bg-white/6"
            >
              Contact support
            </a>
          </div>
        </section>
      </div>
    </main>
  );
}
