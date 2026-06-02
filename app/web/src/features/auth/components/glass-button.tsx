import type { ButtonHTMLAttributes, ReactNode } from "react";

type GlassButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  children: ReactNode;
  variant?: "primary" | "glass";
};

export function GlassButton({
  children,
  className = "",
  variant = "primary",
  ...props
}: GlassButtonProps) {
  const sharedClasses =
    "inline-flex items-center justify-center rounded-full transition-all duration-300 ease-out active:scale-[0.985] disabled:cursor-not-allowed disabled:opacity-45";

  const variantClasses =
    variant === "primary"
      ? "h-[3.25rem] bg-white px-6 text-sm font-semibold tracking-[-0.01em] text-black shadow-[0_4px_24px_rgba(255,255,255,0.08),0_1px_3px_rgba(255,255,255,0.08)] hover:shadow-[0_12px_44px_rgba(255,255,255,0.14)]"
      : "h-12 border border-white/[0.08] bg-[linear-gradient(180deg,rgba(40,42,48,0.54)_0%,rgba(31,33,39,0.48)_100%)] px-5 text-[0.8125rem] font-medium text-white/78 shadow-[inset_0_1px_0_rgba(255,255,255,0.04)] hover:border-white/[0.12] hover:bg-[linear-gradient(180deg,rgba(44,46,52,0.62)_0%,rgba(34,36,42,0.56)_100%)]";

  return (
    <button className={`${sharedClasses} ${variantClasses} ${className}`} {...props}>
      {children}
    </button>
  );
}
