import type { InputHTMLAttributes, ReactNode } from "react";

type InputProps = InputHTMLAttributes<HTMLInputElement> & {
  error?: string;
  icon?: ReactNode;
  label?: string;
  wrapperClassName?: string;
};

export function Input({
  className = "",
  error,
  icon,
  label,
  wrapperClassName = "",
  ...props
}: InputProps) {
  const labelText = label ?? props.placeholder;

  return (
    <label className={`block ${wrapperClassName}`}>
      {labelText ? (
        <span className="scanit-form-label">
          {labelText}
        </span>
      ) : null}
      <div className="relative">
        {icon ? (
          <div className="pointer-events-none absolute inset-y-0 left-4 flex items-center text-[var(--scanit-text-muted)]">
            {icon}
          </div>
        ) : null}
        <input
          className={[
            "auth-glass-field h-12 w-full rounded-lg px-4 text-[0.9375rem] transition-all duration-150 outline-none",
            "placeholder:text-field-placeholder",
            icon ? "pl-11" : "",
            error
              ? "border-[var(--scanit-danger)] bg-[var(--scanit-danger-softer)] focus:border-[var(--scanit-danger)]"
              : "",
            className,
          ].join(" ")}
          {...props}
        />
      </div>
      {error ? (
        <span className="scanit-validation-error">{error}</span>
      ) : null}
    </label>
  );
}
