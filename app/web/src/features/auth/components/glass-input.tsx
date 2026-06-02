import type { InputHTMLAttributes, ReactNode } from "react";

type GlassInputProps = InputHTMLAttributes<HTMLInputElement> & {
  error?: string;
  icon?: ReactNode;
  wrapperClassName?: string;
};

export function GlassInput({
  className = "",
  error,
  icon,
  wrapperClassName = "",
  ...props
}: GlassInputProps) {
  return (
    <label className={`block ${wrapperClassName}`}>
      <span className="sr-only">{props.placeholder}</span>
      <div className="relative">
        {icon ? (
          <div className="pointer-events-none absolute inset-y-0 left-4 flex items-center text-white/28">
            {icon}
          </div>
        ) : null}
        <input
          className={[
            "auth-glass-field h-12 w-full rounded-[0.9rem] px-4 text-[0.875rem] transition-all duration-300 outline-none",
            "placeholder:text-field-placeholder",
            icon ? "pl-11" : "",
            error
              ? "border-red-400/60 bg-[linear-gradient(180deg,rgba(64,28,34,0.58)_0%,rgba(48,22,28,0.5)_100%)] focus:border-red-300/80 focus:shadow-[0_0_0_4px_rgba(255,120,120,0.1)]"
              : "",
            className,
          ].join(" ")}
          {...props}
        />
      </div>
      {error ? (
        <span className="mt-2 block text-[0.7rem] tracking-[0.01em] text-red-200/85">
          {error}
        </span>
      ) : null}
    </label>
  );
}
