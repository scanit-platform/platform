"use client";

import { useActionState, useState } from "react";
import Link from "next/link";
import { submitAuthForm } from "@/src/features/auth/model/actions/submit-auth-form";
import {
  initialAuthState,
  type AuthMode,
} from "@/src/features/auth/model/auth-state";
import { Button } from "@/src/shared/ui/button/button";
import { Input } from "@/src/shared/ui/input/input";
import { SegmentedControl } from "@/src/shared/ui/segmented-control/segmented-control";

type AuthFormProps = {
  initialMode?: AuthMode;
};

export function AuthForm({ initialMode = "signup" }: AuthFormProps) {
  const [mode, setMode] = useState<AuthMode>(initialMode);
  const [state, formAction, pending] = useActionState(
    submitAuthForm,
    initialAuthState,
  );

  const title = mode === "signup" ? "Create an account" : "Welcome back";
  const subtitle =
    mode === "signup"
      ? "Create your account to start organizing receipts."
      : "Sign in to continue where you left off.";

  return (
    <div className="flex min-h-122 flex-col">
      <div className="mb-7 flex items-center justify-between gap-4">
        <SegmentedControl
          activeIndex={mode === "signup" ? 0 : 1}
          onChange={(index) => setMode(index === 0 ? "signup" : "signin")}
          options={["Sign up", "Sign in"]}
        />
        <Link
          href="/dashboard"
          aria-label="Close panel"
          className="inline-flex h-9 w-9 items-center justify-center rounded-lg border border-transparent text-[var(--scanit-text-muted)] transition-colors duration-150 hover:border-[var(--scanit-border)] hover:bg-[var(--page-bg)] hover:text-[var(--scanit-label)]"
        >
          <CloseIcon />
        </Link>
      </div>

      <div>
        <p className="text-[0.8125rem] font-semibold text-[var(--scanit-primary)]">
          ScanIt account
        </p>
        <h1 className="mt-2 font-serif text-[2rem] font-bold leading-tight text-[var(--scanit-text)] sm:text-[2.15rem]">
          {title}
        </h1>
        <p className="mt-2 max-w-sm text-[0.9375rem] leading-6 text-[var(--scanit-text-secondary)]">
          {subtitle}
        </p>
      </div>

      {state.message ? (
        <div className="scanit-auth-error mt-6 rounded-lg px-4 py-3 text-[0.8125rem] font-medium">
          {state.message}
        </div>
      ) : null}

      <form action={formAction} className="mt-7 flex flex-1 flex-col">
        <input type="hidden" name="mode" value={mode} />
        <div className="min-h-47 space-y-4">
          {mode === "signup" ? (
            <>
              <div className="animate-[auth-field-enter_320ms_cubic-bezier(0.16,1,0.3,1)]">
                <Input
                  autoComplete="name"
                  error={state.fieldErrors.name}
                  label="Full name"
                  name="name"
                  placeholder="John Doe"
                />
              </div>
              <div className="animate-[auth-field-enter_360ms_cubic-bezier(0.16,1,0.3,1)]">
                <Input
                  autoComplete="email"
                  error={state.fieldErrors.email}
                  icon={<MailIcon />}
                  label="Email"
                  name="email"
                  placeholder="john@example.com"
                  type="email"
                />
              </div>
              <div className="animate-[auth-field-enter_400ms_cubic-bezier(0.16,1,0.3,1)]">
                <Input
                  autoComplete="new-password"
                  error={state.fieldErrors.password}
                  label="Password"
                  name="password"
                  placeholder="At least 6 characters"
                  type="password"
                />
              </div>
            </>
          ) : (
            <>
              <div className="animate-[auth-field-enter_320ms_cubic-bezier(0.16,1,0.3,1)]">
                <Input
                  autoComplete="email"
                  error={state.fieldErrors.email}
                  icon={<MailIcon />}
                  label="Email"
                  name="email"
                  placeholder="user@example.com"
                  type="email"
                />
              </div>
              <div className="animate-[auth-field-enter_360ms_cubic-bezier(0.16,1,0.3,1)]">
                <Input
                  autoComplete="current-password"
                  error={state.fieldErrors.password}
                  label="Password"
                  name="password"
                  placeholder="Password"
                  type="password"
                />
              </div>
              <div className="animate-[auth-field-enter_400ms_cubic-bezier(0.16,1,0.3,1)]">
                <Link
                  href="/login"
                  className="ml-auto block w-fit text-[0.8125rem] font-medium text-[var(--scanit-primary)] transition-colors duration-150 hover:text-[var(--scanit-primary-light)]"
                >
                  Forgot password?
                </Link>
              </div>
            </>
          )}
        </div>

        <div className="mt-auto pt-6">
          <Button className="w-full" disabled={pending} type="submit">
            {pending ? <LoadingIcon /> : null}
            {pending
              ? "Processing..."
              : mode === "signup"
                ? "Create an account"
                : "Sign in"}
          </Button>
          <Link
            href="/dashboard"
            className="scanit-btn scanit-btn-secondary mt-3 h-12 w-full"
          >
            Try ScanIt first
          </Link>
        </div>

        <p className="mt-6 text-center text-[0.75rem] leading-6 text-[var(--scanit-text-secondary)]">
          {mode === "signup"
            ? "Guest mode uses sample data. Create an account to keep your workspace."
            : "Guest mode uses sample data. Sign in to keep working with saved receipts."}
          {" "}
          <Link
            href="/dashboard"
            className="font-medium text-[var(--scanit-primary)] underline decoration-[var(--scanit-primary-softer)] underline-offset-3 transition-colors duration-150 hover:text-[var(--scanit-primary-light)]"
          >
            Try without an account
          </Link>
        </p>
      </form>
    </div>
  );
}

function LoadingIcon() {
  return (
    <svg
      aria-hidden="true"
      className="h-4 w-4 animate-spin"
      fill="none"
      viewBox="0 0 24 24"
    >
      <circle
        className="opacity-25"
        cx="12"
        cy="12"
        r="10"
        stroke="currentColor"
        strokeWidth="4"
      />
      <path
        className="opacity-75"
        d="M4 12a8 8 0 0 1 8-8"
        stroke="currentColor"
        strokeLinecap="round"
        strokeWidth="4"
      />
    </svg>
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
