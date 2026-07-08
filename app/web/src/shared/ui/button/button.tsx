import type { ButtonHTMLAttributes, ReactNode } from "react";

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  children: ReactNode;
  variant?: "primary" | "secondary" | "cta" | "glass";
};

export function Button({
  children,
  className = "",
  variant = "primary",
  ...props
}: ButtonProps) {
  const sharedClasses =
    "scanit-btn active:scale-[0.985] disabled:cursor-not-allowed disabled:opacity-60";

  const variantClasses = {
    cta: "scanit-btn-cta h-[3.25rem]",
    glass: "scanit-btn-secondary h-12",
    primary: "scanit-btn-primary h-[3.25rem]",
    secondary: "scanit-btn-secondary h-12",
  }[variant];

  return (
    <button
      className={`${sharedClasses} ${variantClasses} ${className}`}
      {...props}
    >
      {children}
    </button>
  );
}
