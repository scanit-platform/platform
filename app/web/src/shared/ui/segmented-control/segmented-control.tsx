type SegmentedControlProps = {
  activeIndex: number;
  onChange: (index: number) => void;
  options: [string, string];
};

export function SegmentedControl({
  activeIndex,
  onChange,
  options,
}: SegmentedControlProps) {
  return (
    <div className="relative inline-grid grid-cols-2 rounded-lg border border-[var(--scanit-border)] bg-[var(--page-bg)] p-1">
      <div
        aria-hidden="true"
        className="absolute bottom-1 top-1 rounded-md bg-[var(--scanit-primary)] shadow-[var(--scanit-shadow)] transition-transform duration-300"
        style={{
          left: "4px",
          width: "calc(50% - 4px)",
          transform: `translateX(calc(${activeIndex * 100}% + ${activeIndex * 4}px))`,
        }}
      />
      {options.map((option, index) => (
        <button
          key={option}
          aria-pressed={index === activeIndex}
          type="button"
          onClick={() => onChange(index)}
          className={`relative z-10 rounded-md px-5 py-2 text-[0.875rem] font-semibold transition-colors duration-300 ${
            index === activeIndex
              ? "text-white"
              : "text-(--scanit-text-secondary) hover:text-(--scanit-text)"
          }`}
        >
          {option}
        </button>
      ))}
    </div>
  );
}
