import { useId } from "react";

type PremiumLoginBackgroundProps = {
  className?: string;
};

type TrailDefinition = {
  d: string;
  depth: number;
  glowClassName: string;
  groupClassName: string;
  opacity?: number;
  primaryDelay: number;
  primaryDuration: number;
  secondaryDelay: number;
  secondaryDuration: number;
  sharpWidth: number;
  glowWidth: number;
};

const rightTrails: TrailDefinition[] = [
  {
    d: "M985 44C1082 120 1136 224 1182 360C1216 466 1264 610 1398 776",
    depth: 1.18,
    glowClassName: "premium-trail-glow-soft",
    groupClassName:
      "[animation:premium-trail-float-a_24s_ease-in-out_infinite_alternate]",
    opacity: 0.95,
    primaryDelay: -1.8,
    primaryDuration: 5.4,
    secondaryDelay: 1.15,
    secondaryDuration: 8.2,
    sharpWidth: 1.35,
    glowWidth: 12,
  },
  {
    d: "M1048 92C1156 190 1214 302 1262 428C1298 522 1348 628 1448 770",
    depth: 1.04,
    glowClassName: "premium-trail-glow-medium",
    groupClassName:
      "[animation:premium-trail-float-b_28s_ease-in-out_infinite_alternate]",
    opacity: 0.84,
    primaryDelay: -0.95,
    primaryDuration: 6,
    secondaryDelay: 1.9,
    secondaryDuration: 9.4,
    sharpWidth: 1.15,
    glowWidth: 10,
  },
  {
    d: "M930 164C1032 224 1104 330 1142 448C1174 546 1222 638 1330 770",
    depth: 0.72,
    glowClassName: "premium-trail-glow-soft",
    groupClassName:
      "[animation:premium-trail-float-c_26s_ease-in-out_infinite_alternate]",
    opacity: 0.74,
    primaryDelay: -2.4,
    primaryDuration: 11.5,
    secondaryDelay: 2.7,
    secondaryDuration: 15.6,
    sharpWidth: 0.95,
    glowWidth: 8.5,
  },
];

const lowerTrails: TrailDefinition[] = [
  {
    d: "M-120 748C160 664 342 650 524 666C712 682 910 720 1196 690C1318 678 1418 650 1528 616",
    depth: 0.38,
    glowClassName: "premium-trail-glow-soft",
    groupClassName:
      "[animation:premium-trail-drift-low_22s_ease-in-out_infinite_alternate]",
    opacity: 0.78,
    primaryDelay: -4.2,
    primaryDuration: 19.5,
    secondaryDelay: 5.8,
    secondaryDuration: 24.8,
    sharpWidth: 1.1,
    glowWidth: 8,
  },
  {
    d: "M88 796C282 738 468 724 648 736C864 750 1060 786 1260 766C1368 756 1452 730 1548 686",
    depth: 0.24,
    glowClassName: "premium-trail-glow-soft",
    groupClassName:
      "[animation:premium-trail-drift-mid_27s_ease-in-out_infinite_alternate]",
    opacity: 0.58,
    primaryDelay: -7.6,
    primaryDuration: 25.5,
    secondaryDelay: 3.35,
    secondaryDuration: 18.6,
    sharpWidth: 0.92,
    glowWidth: 6.5,
  },
];

export function PremiumLoginBackground({
  className = "",
}: PremiumLoginBackgroundProps) {
  const haloGradientId = useId().replace(/:/g, "");
  const grainMaskId = useId().replace(/:/g, "");
  const horizonGradientId = useId().replace(/:/g, "");
  const horizonGlowId = useId().replace(/:/g, "");
  const sparkGlowStrongId = useId().replace(/:/g, "");
  const sparkGlowSoftId = useId().replace(/:/g, "");
  const trailGradientIdA = useId().replace(/:/g, "");
  const trailGradientIdB = useId().replace(/:/g, "");
  const trailGradientIdC = useId().replace(/:/g, "");
  const trailGradientIdD = useId().replace(/:/g, "");
  const trailGradientIdE = useId().replace(/:/g, "");
  const trailPathIdA = useId().replace(/:/g, "");
  const trailPathIdB = useId().replace(/:/g, "");
  const trailPathIdC = useId().replace(/:/g, "");
  const trailPathIdD = useId().replace(/:/g, "");
  const trailPathIdE = useId().replace(/:/g, "");
  const trailGradientIds = [
    trailGradientIdA,
    trailGradientIdB,
    trailGradientIdC,
    trailGradientIdD,
    trailGradientIdE,
  ];
  const trailPathIds = [
    trailPathIdA,
    trailPathIdB,
    trailPathIdC,
    trailPathIdD,
    trailPathIdE,
  ];

  return (
    <div
      aria-hidden="true"
      className={`pointer-events-none absolute inset-0 overflow-hidden bg-black ${className}`.trim()}
    >
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_76%_24%,rgba(58,114,255,0.08),transparent_18%),radial-gradient(circle_at_86%_58%,rgba(122,78,255,0.08),transparent_22%),radial-gradient(circle_at_72%_82%,rgba(212,98,62,0.06),transparent_18%)]" />

      <svg
        className="absolute inset-0 h-full w-full"
        fill="none"
        preserveAspectRatio="xMidYMid slice"
        viewBox="0 0 1440 900"
      >
        <defs>
          <radialGradient id={haloGradientId} cx="0" cy="0" r="1" gradientTransform="translate(1110 514) rotate(128) scale(456 352)" gradientUnits="userSpaceOnUse">
            <stop stopColor="rgba(89,162,255,0.12)" />
            <stop offset="0.32" stopColor="rgba(106,111,255,0.08)" />
            <stop offset="0.66" stopColor="rgba(178,96,255,0.05)" />
            <stop offset="1" stopColor="rgba(0,0,0,0)" />
          </radialGradient>
          <radialGradient id={horizonGlowId} cx="0" cy="0" r="1" gradientTransform="translate(744 585) rotate(90) scale(34 600)" gradientUnits="userSpaceOnUse">
            <stop stopColor="rgba(212,230,255,0.12)" />
            <stop offset="1" stopColor="rgba(160,190,255,0)" />
          </radialGradient>
          <linearGradient id={horizonGradientId} x1="150" x2="1332" y1="0" y2="0" gradientUnits="userSpaceOnUse">
            <stop offset="0" stopColor="rgba(126,172,255,0)" />
            <stop offset="0.18" stopColor="rgba(152,200,255,0.08)" />
            <stop offset="0.46" stopColor="rgba(208,233,255,0.28)" />
            <stop offset="0.5" stopColor="rgba(242,249,255,0.46)" />
            <stop offset="0.56" stopColor="rgba(208,233,255,0.28)" />
            <stop offset="0.82" stopColor="rgba(152,200,255,0.08)" />
            <stop offset="1" stopColor="rgba(126,172,255,0)" />
          </linearGradient>
          <filter id={sparkGlowStrongId} x="-200%" y="-200%" width="400%" height="400%">
            <feGaussianBlur stdDeviation="5.5" />
          </filter>
          <filter id={sparkGlowSoftId} x="-200%" y="-200%" width="400%" height="400%">
            <feGaussianBlur stdDeviation="3.5" />
          </filter>
          {trailGradientIds.map((id, index) => (
            <linearGradient
              key={id}
              id={id}
              gradientTransform={
                [
                  "rotate(8)",
                  "rotate(22)",
                  "rotate(-10)",
                  "rotate(-4)",
                  "rotate(12)",
                ][index]
              }
              gradientUnits="userSpaceOnUse"
              x1="810"
              x2="1420"
              y1="60"
              y2="860"
            >
              <stop offset="0" stopColor="rgba(98,162,255,0)" />
              <stop offset="0.1" stopColor="#62A7FF" />
              <stop offset="0.3" stopColor="#7EE7FF" />
              <stop offset="0.52" stopColor="#A37CFF" />
              <stop offset="0.74" stopColor="#7EE7FF" />
              <stop offset="0.9" stopColor="#FF8A5C" />
              <stop offset="1" stopColor="rgba(255,138,92,0)" />
            </linearGradient>
          ))}
          <filter id={`${grainMaskId}-blur`} x="-20%" y="-20%" width="140%" height="140%">
            <feGaussianBlur stdDeviation="6" />
          </filter>
        </defs>

        <rect x="0" y="0" width="1440" height="900" fill={`url(#${haloGradientId})`} />

        <g className="opacity-80">
          {rightTrails.map((trail, index) => (
            <LightTrail
              key={`right-${trail.d}`}
              depth={trail.depth}
              d={trail.d}
              glowClassName={trail.glowClassName}
              gradientId={trailGradientIds[index]}
              groupClassName={trail.groupClassName}
              pathId={trailPathIds[index]}
              primaryDelay={trail.primaryDelay}
              primaryDuration={trail.primaryDuration}
              secondaryDelay={trail.secondaryDelay}
              secondaryDuration={trail.secondaryDuration}
              sparkGlowSoftId={sparkGlowSoftId}
              sparkGlowStrongId={sparkGlowStrongId}
              opacity={trail.opacity}
              sharpWidth={trail.sharpWidth}
              glowWidth={trail.glowWidth}
            />
          ))}

          {lowerTrails.map((trail, index) => (
            <LightTrail
              key={`lower-${trail.d}`}
              depth={trail.depth}
              d={trail.d}
              glowClassName={trail.glowClassName}
              gradientId={trailGradientIds[index + rightTrails.length]}
              groupClassName={trail.groupClassName}
              pathId={trailPathIds[index + rightTrails.length]}
              primaryDelay={trail.primaryDelay}
              primaryDuration={trail.primaryDuration}
              secondaryDelay={trail.secondaryDelay}
              secondaryDuration={trail.secondaryDuration}
              sparkGlowSoftId={sparkGlowSoftId}
              sparkGlowStrongId={sparkGlowStrongId}
              opacity={trail.opacity}
              sharpWidth={trail.sharpWidth}
              glowWidth={trail.glowWidth}
            />
          ))}
        </g>

        <g className="[animation:premium-horizon-breathe_14s_ease-in-out_infinite]">
          <ellipse
            cx="744"
            cy="585"
            fill={`url(#${horizonGlowId})`}
            filter={`url(#${grainMaskId}-blur)`}
            rx="600"
            ry="30"
          />
          <path
            d="M82 592C310 560 526 556 744 568C962 580 1176 606 1388 584"
            stroke={`url(#${horizonGradientId})`}
            strokeLinecap="round"
            strokeWidth="1.2"
          />
        </g>
      </svg>

      <div className="absolute inset-0 bg-[radial-gradient(circle_at_82%_48%,rgba(102,152,255,0.08),transparent_24%),radial-gradient(circle_at_74%_74%,rgba(147,112,255,0.06),transparent_22%)] blur-3xl" />
      <div
        className="absolute inset-0 opacity-[0.018] mix-blend-screen"
        style={{
          backgroundImage:
            'url("data:image/svg+xml,%3Csvg viewBox=\'0 0 240 240\' xmlns=\'http://www.w3.org/2000/svg\'%3E%3Cfilter id=\'noise\'%3E%3CfeTurbulence type=\'fractalNoise\' baseFrequency=\'0.82\' numOctaves=\'3\' stitchTiles=\'stitch\'/%3E%3C/filter%3E%3Crect width=\'100%25\' height=\'100%25\' filter=\'url(%23noise)\' opacity=\'0.86\'/%3E%3C/svg%3E")',
          backgroundRepeat: "repeat",
        }}
      />
    </div>
  );
}

type LightTrailProps = {
  depth: number;
  d: string;
  glowClassName: string;
  gradientId: string;
  groupClassName: string;
  pathId: string;
  primaryDelay: number;
  primaryDuration: number;
  secondaryDelay: number;
  secondaryDuration: number;
  sparkGlowSoftId: string;
  sparkGlowStrongId: string;
  opacity?: number;
  sharpWidth: number;
  glowWidth: number;
};

function LightTrail({
  depth,
  d,
  glowClassName,
  gradientId,
  groupClassName,
  pathId,
  primaryDelay,
  primaryDuration,
  secondaryDelay,
  secondaryDuration,
  sparkGlowSoftId,
  sparkGlowStrongId,
  opacity = 1,
  sharpWidth,
  glowWidth,
}: LightTrailProps) {
  const primarySparkLength = 14 + depth * 16;
  const primarySparkRadius = 1.2 + depth * 1.35;
  const secondarySparkLength = 8 + depth * 8;
  const secondarySparkRadius = 0.85 + depth * 0.75;

  return (
    <g className={groupClassName} opacity={opacity}>
      <path
        className={glowClassName}
        d={d}
        stroke={`url(#${gradientId})`}
        strokeLinecap="round"
        strokeLinejoin="round"
        strokeWidth={glowWidth}
      />
      <path
        id={pathId}
        d={d}
        stroke={`url(#${gradientId})`}
        strokeLinecap="round"
        strokeLinejoin="round"
        strokeWidth={sharpWidth}
      />
      <SparkParticle
        blurFilterId={sparkGlowStrongId}
        color="rgba(212,244,255,0.92)"
        delay={primaryDelay}
        duration={primaryDuration}
        glowColor="rgba(118,196,255,0.9)"
        pathId={pathId}
        radius={primarySparkRadius}
        tailLength={primarySparkLength}
      />
      <SparkParticle
        blurFilterId={sparkGlowSoftId}
        color="rgba(186,224,255,0.62)"
        delay={secondaryDelay}
        duration={secondaryDuration}
        glowColor="rgba(164,142,255,0.52)"
        opacity={0.72}
        pathId={pathId}
        radius={secondarySparkRadius}
        tailLength={secondarySparkLength}
      />
    </g>
  );
}

type SparkParticleProps = {
  blurFilterId: string;
  color: string;
  delay: number;
  duration: number;
  glowColor: string;
  opacity?: number;
  pathId: string;
  radius: number;
  tailLength: number;
};

function SparkParticle({
  blurFilterId,
  color,
  delay,
  duration,
  glowColor,
  opacity = 1,
  pathId,
  radius,
  tailLength,
}: SparkParticleProps) {
  return (
    <g opacity={opacity}>
      <g filter={`url(#${blurFilterId})`}>
        <ellipse
          cx={0}
          cy={0}
          fill={glowColor}
          rx={tailLength}
          ry={radius * 1.2}
        />
      </g>
      <ellipse
        cx={-tailLength * 0.22}
        cy={0}
        fill={color}
        opacity={0.8}
        rx={tailLength * 0.85}
        ry={radius * 0.75}
      />
      <circle
        cx={tailLength * 0.6}
        cy={0}
        fill="rgba(255,255,255,0.95)"
        r={radius}
      />
      <animateMotion
        begin={`${delay}s`}
        calcMode="spline"
        dur={`${duration}s`}
        keySplines="0.45 0 0.2 1"
        keyTimes="0;1"
        repeatCount="indefinite"
        rotate="auto"
      >
        <mpath href={`#${pathId}`} />
      </animateMotion>
    </g>
  );
}
