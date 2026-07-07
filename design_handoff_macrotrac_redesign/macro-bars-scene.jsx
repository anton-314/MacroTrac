const COLORS = {
  bg: '#161615',
  surface: '#1E1E1C',
  text: '#EDECE8',
  text2: '#9C9A94',
  text3: '#68665F',
  protein: '#82C8F5',
  carbs: '#82D48A',
  fat: '#E8A4D4',
};

function trackColor(hex) {
  // 20% tint of the bar color over the surface, matching the app track style.
  return hex + '33';
}

function MacroBar({ label, unit, current, goal, color, start, end, y }) {
  const t = useTime();
  const pct = clamp((t - start) / (end - start), 0, 1);
  const eased = Easing.easeOutCubic(pct);
  const value = Math.round(current * eased);
  const widthPct = (current / goal) * 100 * eased;

  return (
    <div style={{ position: 'absolute', left: 0, top: y, width: '100%', display: 'flex', flexDirection: 'column', gap: 10 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
        <span style={{ fontFamily: "'Hanken Grotesk', sans-serif", fontWeight: 600, fontSize: 26, color: COLORS.text }}>{label}</span>
        <span style={{ fontFamily: "'JetBrains Mono', monospace", fontSize: 20, color: COLORS.text2 }}>{value} / {goal} {unit}</span>
      </div>
      <div style={{ height: 22, borderRadius: 99, background: trackColor(color), overflow: 'hidden' }}>
        <div style={{ height: '100%', width: `${widthPct}%`, background: color, borderRadius: 99 }} />
      </div>
    </div>
  );
}

function BarsCard() {
  const t = useTime();
  const cardIn = interpolate([0, 0.4], [0, 1], Easing.easeOutCubic)(clamp(t, 0, 0.4));
  return (
    <div style={{ position: 'absolute', left: '50%', top: '50%', transform: `translate(-50%, -50%) scale(${0.95 + cardIn * 0.05})`, opacity: cardIn, width: 820, height: 520, background: COLORS.surface, borderRadius: 40, border: '1px solid rgba(255,255,255,0.07)', boxShadow: '0 60px 120px -40px rgba(0,0,0,0.7)', padding: '70px 70px' }}>
      <div style={{ position: 'relative', width: '100%', height: 360 }}>
        <MacroBar label="Protein" unit="g" current={118} goal={160} color={COLORS.protein} start={0.4} end={1.3} y={0} />
        <MacroBar label="Kohlenhydrate" unit="g" current={176} goal={230} color={COLORS.carbs} start={0.55} end={1.45} y={130} />
        <MacroBar label="Fett" unit="g" current={52} goal={70} color={COLORS.fat} start={0.7} end={1.6} y={260} />
      </div>
    </div>
  );
}

function MacroBarsDemo() {
  return (
    <Stage width={1280} height={780} duration={5.4} background={COLORS.bg} loop autoplay>
      <Sprite start={0} end={5.4} keepMounted>
        <BarsCard />
      </Sprite>
      <Sprite start={0} end={0.9}>
        <div style={{ position: 'absolute', left: 0, top: 90, width: '100%', textAlign: 'center', fontFamily: "'JetBrains Mono', monospace", fontSize: 16, letterSpacing: '0.3em', color: COLORS.text3 }}>ÜBERSICHT · MAKRO-BALKEN</div>
      </Sprite>
    </Stage>
  );
}

window.MacroBarsDemo = MacroBarsDemo;
