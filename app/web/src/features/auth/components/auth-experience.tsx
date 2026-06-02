import Link from "next/link";
import { AuthForm } from "@/src/features/auth/components/auth-form";
import { PremiumLoginBackground } from "@/src/shared/ui/premium-login-background";

type AuthExperienceProps = {
  providerNotice?: string;
};

export function AuthExperience({ providerNotice }: AuthExperienceProps) {
  return (
    <main className="relative min-h-screen overflow-hidden bg-page-bg text-page-foreground">
      <PremiumLoginBackground />

      <div className="relative z-10 mx-auto flex min-h-screen max-w-7xl flex-col px-5 py-5 sm:px-8 lg:px-12">
        <header className="flex items-center justify-between gap-4">
          <Link
            href="/"
            className="font-serif text-xl tracking-[0.08em] text-white/78"
          >
            NOVA
          </Link>
          <p className="hidden text-[0.72rem] uppercase tracking-[0.32em] text-white/30 sm:block">
            Secure access, reimagined
          </p>
        </header>

        <div className="relative flex flex-1 items-center justify-center py-8 lg:py-12">
          <div className="pointer-events-none absolute inset-0 hidden lg:block">
            <div className="absolute left-0 top-1/2 max-w-sm -translate-y-1/2">
              <p className="text-[0.72rem] font-medium uppercase tracking-[0.34em] text-white/30">
                Modern authentication experience
              </p>
              <h2 className="mt-6 font-serif text-6xl leading-[0.92] tracking-[-0.045em] text-white/92">
                Quiet glass.
                <br />
                Warm orbit.
                <br />
                Minimal friction.
              </h2>
              <p className="mt-6 max-w-sm text-sm leading-7 text-white/42">
                Ported from Figma into a server-first Next.js route with
                reusable auth primitives and a real submission path.
              </p>
            </div>
          </div>

          <section
            className="relative w-full max-w-103 rounded-[1.75rem] border border-white/8 bg-[linear-gradient(160deg,rgba(18,20,26,0.58),rgba(22,24,30,0.64))] p-6 shadow-[0_60px_120px_-30px_rgba(0,0,0,0.6),0_28px_70px_-20px_rgba(0,0,0,0.42),inset_0_1px_0_rgba(255,255,255,0.05),inset_0_-1px_0_rgba(0,0,0,0.2)] backdrop-blur-[44px] sm:p-7"
            style={{
              animation:
                "auth-card-enter 1100ms cubic-bezier(0.16,1,0.3,1) forwards",
            }}
          >
            <div className="pointer-events-none absolute left-6 right-6 top-0 h-px rounded-full bg-[linear-gradient(90deg,transparent_5%,rgba(255,255,255,0.04)_32%,rgba(255,255,255,0.08)_50%,rgba(255,255,255,0.04)_68%,transparent_95%)]" />
            <AuthForm providerNotice={providerNotice} />
          </section>
        </div>
      </div>
    </main>
  );
}
