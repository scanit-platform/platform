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
    <div className="relative inline-grid grid-cols-2 rounded-lg border border-(--scanit-border) bg-page-bg p-1">
      <div
        aria-hidden="true"
        className="absolute bottom-1 top-1 rounded-md bg-(--scanit-primary) shadow-(--scanit-shadow) transition-transform duration-300"
        style={{
          left: "3px",
          width: "calc(50% - 3px)",
          transform: `translateX(calc(${activeIndex * 100}% + ${activeIndex * 3}px))`,
        }}
      />
      {options.map((option, index) => (
        <button
          key={option}
          aria-pressed={index === activeIndex}
          type="button"
          onClick={() => onChange(index)}
          className={`relative z-10 rounded-full px-5 py-[0.45rem] text-[0.8125rem] tracking-[0.01em] transition-colors duration-300 ${
            index === activeIndex
              ? "text-white"
              : "text-white/32 hover:text-white/56"
          }`}
        >
          {option}
        </button>
      ))}
    </div>
  );
}
