"use client";

import { useActionState, useState } from "react";
import Link from "next/link";
import { submitAuthForm } from "@/src/features/auth/actions/submit-auth-form";
import {
  initialAuthState,
  type AuthMode,
} from "@/src/features/auth/model/auth-state";
import { GlassButton } from "@/src/features/auth/components/glass-button";
import { GlassInput } from "@/src/features/auth/components/glass-input";
import { SegmentedControl } from "@/src/features/auth/components/segmented-control";

type AuthFormProps = {
  providerNotice?: string;
};

export function AuthForm({ providerNotice }: AuthFormProps) {
  const [mode, setMode] = useState<AuthMode>("signup");
  const [state, formAction, pending] = useActionState(
    submitAuthForm,
    initialAuthState,
  );

  const title = mode === "signup" ? "Create an account" : "Welcome back";
  const subtitle =
    mode === "signup"
      ? "Enter your details to get started."
      : "Sign in to continue where you left off.";
  const socialLinkClasses =
    "inline-flex h-12 w-full items-center justify-center gap-2.5 rounded-full border border-white/[0.08] bg-[linear-gradient(180deg,rgba(40,42,48,0.54)_0%,rgba(31,33,39,0.48)_100%)] px-5 text-[0.8125rem] font-medium text-white/78 shadow-[inset_0_1px_0_rgba(255,255,255,0.04)] transition-all duration-300 hover:border-white/[0.12] hover:bg-[linear-gradient(180deg,rgba(44,46,52,0.62)_0%,rgba(34,36,42,0.56)_100%)]";

  return (
    <div className="flex min-h-122 flex-col">
      <div className="mb-8 flex items-center justify-between gap-4">
        <SegmentedControl
          activeIndex={mode === "signup" ? 0 : 1}
          onChange={(index) => setMode(index === 0 ? "signup" : "signin")}
          options={["Sign up", "Sign in"]}
        />
        <button
          type="button"
          aria-label="Close panel"
          className="inline-flex h-9 w-9 items-center justify-center rounded-full text-white/24 transition-colors duration-200 hover:bg-white/5 hover:text-white/55"
        >
          <CloseIcon />
        </button>
      </div>

      <div>
        <p className="text-[0.68rem] font-medium uppercase tracking-[0.32em] text-white/36">
          Modern Identity
        </p>
        <h1 className="mt-3.5 text-[2rem] font-semibold leading-none tracking-[-0.045em] text-white sm:text-[2.15rem]">
          {title}
        </h1>
        <p className="mt-2.5 max-w-sm text-[0.84rem] leading-6 text-white/34">
          {subtitle}
        </p>
      </div>

      {providerNotice ? (
        <div className="mt-6 rounded-2xl border border-white/8 bg-[linear-gradient(180deg,rgba(40,42,48,0.4)_0%,rgba(30,32,38,0.3)_100%)] px-4 py-3 text-[0.78rem] text-white/62">
          {providerNotice}
        </div>
      ) : null}

      {state.message ? (
        <div className="mt-6 rounded-2xl border border-red-300/20 bg-[linear-gradient(180deg,rgba(76,30,36,0.5)_0%,rgba(58,24,30,0.4)_100%)] px-4 py-3 text-[0.78rem] text-red-100/88">
          {state.message}
        </div>
      ) : null}

      <form action={formAction} className="mt-7 flex flex-1 flex-col">
        <input type="hidden" name="mode" value={mode} />
        <div className="min-h-47 space-y-2">
          {mode === "signup" ? (
            <>
              <div className="animate-[auth-field-enter_320ms_cubic-bezier(0.16,1,0.3,1)]">
                <GlassInput
                  autoComplete="name"
                  error={state.fieldErrors.name}
                  name="name"
                  placeholder="Full name"
                />
              </div>
              <div className="animate-[auth-field-enter_360ms_cubic-bezier(0.16,1,0.3,1)]">
                <GlassInput
                  autoComplete="email"
                  error={state.fieldErrors.email}
                  icon={<MailIcon />}
                  name="email"
                  placeholder="Enter your email"
                  type="email"
                />
              </div>
              <div className="animate-[auth-field-enter_400ms_cubic-bezier(0.16,1,0.3,1)]">
                <GlassInput
                  autoComplete="new-password"
                  error={state.fieldErrors.password}
                  name="password"
                  placeholder="Password"
                  type="password"
                />
              </div>
            </>
          ) : (
            <>
              <div className="animate-[auth-field-enter_320ms_cubic-bezier(0.16,1,0.3,1)]">
                <GlassInput
                  autoComplete="email"
                  error={state.fieldErrors.email}
                  icon={<MailIcon />}
                  name="email"
                  placeholder="Enter your email"
                  type="email"
                />
              </div>
              <div className="animate-[auth-field-enter_360ms_cubic-bezier(0.16,1,0.3,1)]">
                <GlassInput
                  autoComplete="current-password"
                  error={state.fieldErrors.password}
                  name="password"
                  placeholder="Password"
                  type="password"
                />
              </div>
              <div className="animate-[auth-field-enter_400ms_cubic-bezier(0.16,1,0.3,1)]">
                <Link
                  href="/"
                  className="ml-auto block w-fit text-[0.75rem] text-white/34 transition-colors duration-200 hover:text-white/54"
                >
                  Forgot password?
                </Link>
              </div>
            </>
          )}
        </div>

        <div className="mt-auto pt-6">
          <GlassButton className="w-full" disabled={pending} type="submit">
            {pending
              ? "Processing..."
              : mode === "signup"
                ? "Create an account"
                : "Sign in"}
          </GlassButton>
        </div>

        <div className="relative my-6">
          <div className="absolute inset-0 flex items-center">
            <div className="w-full border-t border-white/4" />
          </div>
          <div className="relative flex justify-center">
            <span className="bg-transparent px-4 text-[0.64rem] font-medium uppercase tracking-[0.18em] text-white/20">
              Or continue with
            </span>
          </div>
        </div>

        <div className="grid grid-cols-1 gap-2 sm:grid-cols-2">
          <a
            className={socialLinkClasses}
            href="/api/auth/social?provider=google"
          >
            <GoogleIcon />
            Google
          </a>
          <a
            className={socialLinkClasses}
            href="/api/auth/social?provider=apple"
          >
            <AppleIcon />
            Apple
          </a>
        </div>

        <p className="mt-6 text-center text-[0.7rem] leading-6 text-white/22">
          {mode === "signup"
            ? "By creating an account, you agree to our "
            : "By signing in, you agree to our "}
          <Link
            href="/"
            className="text-white/36 underline decoration-white/18 underline-offset-3 transition-colors duration-200 hover:text-white/52"
          >
            Terms &amp; Service
          </Link>
        </p>
      </form>
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

function CloseIcon() {
  return (
    <svg
      aria-hidden="true"
      fill="none"
      height="15"
      viewBox="0 0 24 24"
      width="15"
    >
      <path
        d="M6 6 18 18M18 6 6 18"
        stroke="currentColor"
        strokeLinecap="round"
        strokeWidth="1.8"
      />
    </svg>
  );
}

function GoogleIcon() {
  return (
    <svg aria-hidden="true" height="16" viewBox="0 0 24 24" width="16">
      <path
        d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09Z"
        fill="#4285F4"
      />
      <path
        d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84A10.98 10.98 0 0 0 12 23Z"
        fill="#34A853"
      />
      <path
        d="M5.84 14.09A6.96 6.96 0 0 1 5.49 12c0-.72.13-1.43.35-2.09V7.07H2.18A10.96 10.96 0 0 0 1 12c0 1.78.43 3.45 1.18 4.93l3.66-2.84Z"
        fill="#FBBC05"
      />
      <path
        d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84C6.71 7.31 9.14 5.38 12 5.38Z"
        fill="#EA4335"
      />
    </svg>
  );
}

function AppleIcon() {
  return (
    <svg
      aria-hidden="true"
      fill="currentColor"
      height="16"
      viewBox="0 0 24 24"
      width="16"
    >
      <path d="M17.05 20.28c-.98.95-2.05.8-3.08.35-1.09-.46-2.09-.48-3.24 0-1.44.62-2.2.44-3.06-.35C2.79 15.25 3.51 7.59 9.05 7.31c1.35.07 2.29.74 3.08.8 1.18-.24 2.31-.93 3.57-.84 1.51.12 2.65.72 3.4 1.8-3.12 1.87-2.38 5.98.48 7.13-.57 1.5-1.31 2.99-2.54 4.09ZM12.03 7.25c-.15-2.23 1.66-4.07 3.74-4.25.29 2.58-2.34 4.5-3.74 4.25Z" />
    </svg>
  );
}
