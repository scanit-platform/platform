import type { ButtonHTMLAttributes, ReactNode } from "react";

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  children: ReactNode;
  variant?: "primary" | "glass";
};

export function Button({
  children,
  className = "",
  variant = "primary",
  ...props
}: ButtonProps) {
  const sharedClasses =
    "scanit-btn active:scale-[0.985] disabled:cursor-not-allowed disabled:opacity-60";

  const variantClasses =
    variant === "primary"
      ? "scanit-btn-primary h-[3.25rem]"
      : "scanit-btn-secondary h-12";

  return (
    <button className={`${sharedClasses} ${variantClasses} ${className}`} {...props}>
      {children}
    </button>
  );
}
