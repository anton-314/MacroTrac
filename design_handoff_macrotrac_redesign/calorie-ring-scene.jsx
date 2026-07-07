const { useMemo } = React;

const COLORS = {
  bg: '#161615',
  surface: '#1E1E1C',
  track: '#2B2B29',
  text: '#EDECE8',
  text2: '#9C9A94',
  text3: '#68665F',
  health: '#7CC47F',
  neutral: '#E3A85A',
  unhealthy: '#E07670',
};

const R = 150;
const CX = 220;
const CY = 220;
const STROKE = 30;
const CIRC = 2 * Math.PI * R;

// Consumed 1520 / goal 2100 kcal, split by tag (kcal).
const GOAL = 2100;
const HEALTH_KCAL = 900;
const NEUTRAL_KCAL = 380;
const UNHEALTHY_KCAL = 240;
const CONSUMED = HEALTH_KCAL + NEUTRAL_KCAL + UNHEALTHY_KCAL;

const HEALTH_LEN = (HEALTH_KCAL / GOAL) * CIRC;
const NEUTRAL_LEN = (NEUTRAL_KCAL / GOAL) * CIRC;
const UNHEALTHY_LEN = (UNHEALTHY_KCAL / GOAL) * CIRC;

function ArcSegment({ color, offset, targetLen, start, end }) {
  const t = useTime();
  const len = interpolate([start, end], [0, targetLen], Easing.easeOutCubic)(clamp(t, start, end));
  return (
    <circle
      cx={CX} cy={CY} r={R} fill="none" stroke={color} strokeWidth={STROKE}
      strokeDasharray={`${len} ${CIRC}`}
      strokeDashoffset={-offset}
      strokeLinecap="butt"
    />
  );
}

function CenterReadout() {
  const t = useTime();
  const kcal = Math.round(interpolate([0.4, 2.0], [0, CONSUMED], Easing.easeOutExpo)(clamp(t, 0.4, 2.0)));
  const remaining = GOAL - CONSUMED;
  const subOpacity = interpolate([2.1, 2.6], [0, 1], Easing.easeOutCubic)(clamp(t, 2.1, 2.6));
  return (
    <div style={{ position: 'absolute', left: 0, top: 0, width: CX * 2, height: CY * 2, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
      <div style={{ fontFamily: "'Newsreader', Georgia, serif", fontWeight: 500, fontSize: 96, lineHeight: 1, color: COLORS.text, letterSpacing: '-0.02em' }}>{kcal}</div>
      <div style={{ fontFamily: "'JetBrains Mono', monospace", fontSize: 20, letterSpacing: '0.22em', color: COLORS.text3, marginTop: 10 }}>KCAL</div>
      <div style={{ fontSize: 22, color: COLORS.text2, marginTop: 14, opacity: subOpacity }}>noch {remaining}</div>
    </div>
  );
}

function CleanSummary() {
  const t = useTime();
  const cleanPct = Math.round((HEALTH_KCAL / CONSUMED) * 100);
  const o = interpolate([2.6, 3.2], [0, 1], Easing.easeOutCubic)(clamp(t, 2.6, 3.2));
  const y = interpolate([2.6, 3.2], [16, 0], Easing.easeOutCubic)(clamp(t, 2.6, 3.2));
  const legend = [
    ['Gesund', COLORS.health],
    ['Neutral', COLORS.neutral],
    ['Ungesund', COLORS.unhealthy],
  ];
  return (
    <div style={{ position: 'absolute', left: 0, top: 560, width: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 20, opacity: o, transform: `translateY(${y}px)` }}>
      <div style={{ fontFamily: "'JetBrains Mono', monospace", fontSize: 20, letterSpacing: '0.16em', color: COLORS.text2 }}>{cleanPct} % CLEAN</div>
      <div style={{ display: 'flex', gap: 32 }}>
        {legend.map(([label, color]) => (
          <div key={label} style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <span style={{ width: 13, height: 13, borderRadius: '50%', background: color, display: 'inline-block' }} />
            <span style={{ fontSize: 18, color: COLORS.text2, fontFamily: "'Hanken Grotesk', sans-serif" }}>{label}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

function RingCard() {
  const t = useTime();
  const cardIn = interpolate([0, 0.4], [0, 1], Easing.easeOutCubic)(clamp(t, 0, 0.4));
  return (
    <div style={{ position: 'absolute', left: '50%', top: '50%', transform: `translate(-50%, -50%) scale(${0.94 + cardIn * 0.06})`, opacity: cardIn, width: 760, height: 780, background: COLORS.surface, borderRadius: 40, border: '1px solid rgba(255,255,255,0.07)', boxShadow: '0 60px 120px -40px rgba(0,0,0,0.7)' }}>
      <div style={{ position: 'relative', width: CX * 2, height: CY * 2, margin: '60px auto 0' }}>
        <svg viewBox={`0 0 ${CX * 2} ${CY * 2}`} width={CX * 2} height={CY * 2}>
          <g transform={`rotate(-90 ${CX} ${CY})`}>
            <circle cx={CX} cy={CY} r={R} fill="none" stroke={COLORS.track} strokeWidth={STROKE} />
            <ArcSegment color={COLORS.health} offset={0} targetLen={HEALTH_LEN} start={0.5} end={1.35} />
            <ArcSegment color={COLORS.neutral} offset={HEALTH_LEN} targetLen={NEUTRAL_LEN} start={1.35} end={1.75} />
            <ArcSegment color={COLORS.unhealthy} offset={HEALTH_LEN + NEUTRAL_LEN} targetLen={UNHEALTHY_LEN} start={1.75} end={2.05} />
          </g>
        </svg>
        <CenterReadout />
      </div>
      <CleanSummary />
    </div>
  );
}

function CalorieRingDemo() {
  return (
    <Stage width={1280} height={900} duration={6.2} background={COLORS.bg} loop autoplay>
      <Sprite start={0} end={6.2} keepMounted>
        <RingCard />
      </Sprite>
      <Sprite start={0} end={0.9}>
        <div style={{ position: 'absolute', left: 0, top: 90, width: '100%', textAlign: 'center', fontFamily: "'JetBrains Mono', monospace", fontSize: 16, letterSpacing: '0.3em', color: COLORS.text3 }}>ÜBERSICHT · KALORIENRING</div>
      </Sprite>
    </Stage>
  );
}

window.CalorieRingDemo = CalorieRingDemo;
