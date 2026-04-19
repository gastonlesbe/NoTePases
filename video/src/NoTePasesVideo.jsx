import {
  AbsoluteFill,
  Easing,
  Img,
  interpolate,
  Sequence,
  spring,
  staticFile,
  useCurrentFrame,
  useVideoConfig,
} from 'remotion';
import { strings } from './i18n';

// ── Palette ──────────────────────────────────────────────────────────────────
const NAVY  = '#1A1F3A';
const TEAL  = '#00D4AA';
const GOLD  = '#FFD166';
const WHITE = '#FFFFFF';
const LIGHT = '#F0F4F8';

// ── Helpers ───────────────────────────────────────────────────────────────────

function eased(frame, start, end) {
  return interpolate(frame, [start, end], [0, 1], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
    easing: Easing.out(Easing.cubic),
  });
}

function fadeIn(frame, delay = 0, duration = 20) {
  return interpolate(frame, [delay, delay + duration], [0, 1], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
  });
}

// ── Phone frame ───────────────────────────────────────────────────────────────

function PhoneFrame({ src, progress, scale = 1, x = 0, y = 0 }) {
  const opacity = interpolate(progress, [0, 0.15], [0, 1], {
    extrapolateLeft: 'clamp', extrapolateRight: 'clamp',
  });
  const translateY = interpolate(progress, [0, 0.35], [80, 0], {
    extrapolateLeft: 'clamp', extrapolateRight: 'clamp',
    easing: Easing.out(Easing.back(1.4)),
  });

  return (
    <div style={{
      position: 'absolute',
      left: '50%', top: '50%',
      transform: `translate(calc(-50% + ${x}px), calc(-50% + ${y}px + ${translateY}px)) scale(${scale})`,
      opacity,
      width: 280, height: 590,
      borderRadius: 36,
      boxShadow: '0 40px 120px rgba(0,0,0,0.7), 0 0 0 3px rgba(255,255,255,0.12)',
      overflow: 'hidden',
      background: '#000',
    }}>
      <div style={{
        position: 'absolute', top: 0, left: '50%',
        transform: 'translateX(-50%)',
        width: 90, height: 22, background: '#000',
        borderRadius: '0 0 14px 14px', zIndex: 10,
      }} />
      <Img src={src} style={{ width: '100%', height: '100%', objectFit: 'cover', display: 'block' }} />
    </div>
  );
}

// ── Badge ────────────────────────────────────────────────────────────────────

function Badge({ text, color = TEAL, frame, delay = 0 }) {
  const progress = eased(frame, delay, delay + 25);
  return (
    <div style={{
      position: 'absolute', bottom: 80, left: '50%',
      transform: `translateX(-50%) scale(${interpolate(progress, [0, 1], [0.7, 1])})`,
      opacity: progress,
      background: color, color: NAVY,
      fontFamily: 'Inter, sans-serif', fontWeight: 700, fontSize: 22,
      padding: '10px 28px', borderRadius: 50,
      whiteSpace: 'nowrap', letterSpacing: 0.5,
      boxShadow: `0 4px 24px ${color}88`,
    }}>
      {text}
    </div>
  );
}

// ── Scenes ────────────────────────────────────────────────────────────────────

function SceneIntro({ localFrame, t }) {
  const { fps } = useVideoConfig();
  const logoScale = spring({ frame: localFrame, fps, config: { damping: 14, stiffness: 120, mass: 0.8 } });
  const titleOpacity = fadeIn(localFrame, 25, 30);
  const titleY = interpolate(localFrame, [25, 55], [30, 0], { extrapolateLeft: 'clamp', extrapolateRight: 'clamp', easing: Easing.out(Easing.cubic) });
  const taglineOpacity = fadeIn(localFrame, 55, 25);

  return (
    <AbsoluteFill style={{ background: NAVY, alignItems: 'center', justifyContent: 'center', flexDirection: 'column' }}>
      <div style={{ position: 'absolute', width: 500, height: 500, borderRadius: '50%', background: `radial-gradient(circle, ${TEAL}22 0%, transparent 70%)`, top: '50%', left: '50%', transform: 'translate(-50%, -50%)' }} />
      <div style={{ transform: `scale(${logoScale})`, marginBottom: 32 }}>
        <Img src={staticFile('screenshots/alarm.jpg')} style={{ width: 120, height: 120, borderRadius: 28, boxShadow: `0 0 40px ${TEAL}66` }} />
      </div>
      <div style={{ fontFamily: 'Inter, sans-serif', fontWeight: 800, fontSize: 72, color: WHITE, opacity: titleOpacity, transform: `translateY(${titleY}px)`, letterSpacing: -1 }}>
        {t.appName}
      </div>
      <div style={{ fontFamily: 'Inter, sans-serif', fontWeight: 400, fontSize: 26, color: TEAL, opacity: taglineOpacity, marginTop: 12, letterSpacing: 1, textAlign: 'center', maxWidth: 700 }}>
        {t.tagline}
      </div>
    </AbsoluteFill>
  );
}

function SceneHook({ localFrame, t }) {
  const line1 = fadeIn(localFrame, 0, 20);
  const line1Y = interpolate(localFrame, [0, 20], [20, 0], { extrapolateLeft: 'clamp', extrapolateRight: 'clamp', easing: Easing.out(Easing.cubic) });
  const line2 = fadeIn(localFrame, 30, 20);
  const line2Y = interpolate(localFrame, [30, 50], [20, 0], { extrapolateLeft: 'clamp', extrapolateRight: 'clamp', easing: Easing.out(Easing.cubic) });
  const line3 = fadeIn(localFrame, 65, 20);
  const line3Y = interpolate(localFrame, [65, 85], [20, 0], { extrapolateLeft: 'clamp', extrapolateRight: 'clamp', easing: Easing.out(Easing.cubic) });

  return (
    <AbsoluteFill style={{ background: `linear-gradient(135deg, ${NAVY} 0%, #0D1126 100%)`, alignItems: 'center', justifyContent: 'center', flexDirection: 'column', padding: '0 160px' }}>
      <div style={{ textAlign: 'center' }}>
        <div style={{ fontFamily: 'Inter, sans-serif', fontWeight: 700, fontSize: 54, color: WHITE, opacity: line1, transform: `translateY(${line1Y}px)`, marginBottom: 16, lineHeight: 1.2 }}>{t.hookLine1}</div>
        <div style={{ fontFamily: 'Inter, sans-serif', fontWeight: 700, fontSize: 54, color: GOLD, opacity: line2, transform: `translateY(${line2Y}px)`, marginBottom: 32, lineHeight: 1.2 }}>{t.hookLine2}</div>
        <div style={{ fontFamily: 'Inter, sans-serif', fontWeight: 400, fontSize: 32, color: TEAL, opacity: line3, transform: `translateY(${line3Y}px)` }}>{t.hookLine3}</div>
      </div>
    </AbsoluteFill>
  );
}

function ScenePhone({ localFrame, src, label, labelColor = TEAL, description, side = 'center' }) {
  const progress = eased(localFrame, 0, 90);
  const descOpacity = fadeIn(localFrame, 40, 25);
  const descY = interpolate(localFrame, [40, 65], [20, 0], { extrapolateLeft: 'clamp', extrapolateRight: 'clamp', easing: Easing.out(Easing.cubic) });
  const xOffset = side === 'left' ? -300 : side === 'right' ? 300 : 0;

  return (
    <AbsoluteFill style={{ background: `linear-gradient(160deg, ${NAVY} 0%, #121629 100%)`, overflow: 'hidden' }}>
      <div style={{ position: 'absolute', width: 700, height: 700, borderRadius: '50%', background: `radial-gradient(circle, ${labelColor}18 0%, transparent 65%)`, top: '50%', left: '50%', transform: 'translate(-50%, -50%)' }} />

      <PhoneFrame src={src} progress={progress} x={xOffset} y={-20} />

      {side !== 'center' && (
        <div style={{
          position: 'absolute', top: '50%',
          [side === 'left' ? 'right' : 'left']: 120,
          transform: 'translateY(-50%)', opacity: descOpacity, maxWidth: 480,
        }}>
          <div style={{ transform: `translateY(${descY}px)`, fontFamily: 'Inter, sans-serif', fontWeight: 700, fontSize: 42, color: WHITE, lineHeight: 1.25, marginBottom: 20 }}>{description?.title}</div>
          <div style={{ transform: `translateY(${descY}px)`, fontFamily: 'Inter, sans-serif', fontWeight: 400, fontSize: 26, color: LIGHT, lineHeight: 1.5, opacity: 0.85 }}>{description?.body}</div>
        </div>
      )}

      {side === 'center' && description && (
        <div style={{ position: 'absolute', top: 80, left: '50%', transform: `translateX(-50%) translateY(${descY}px)`, opacity: descOpacity, textAlign: 'center', whiteSpace: 'nowrap' }}>
          <div style={{ fontFamily: 'Inter, sans-serif', fontWeight: 700, fontSize: 36, color: WHITE }}>{description?.title}</div>
        </div>
      )}

      <Badge text={label} color={labelColor} frame={localFrame} delay={50} />
    </AbsoluteFill>
  );
}

function SceneCTA({ localFrame, t }) {
  const { fps } = useVideoConfig();
  const bgOpacity = fadeIn(localFrame, 0, 20);
  const titleScale = spring({ frame: localFrame, fps, config: { damping: 12, stiffness: 100 } });
  const sub1 = fadeIn(localFrame, 35, 20);
  const sub2 = fadeIn(localFrame, 55, 20);
  const imgOpacity = fadeIn(localFrame, 70, 25);

  return (
    <AbsoluteFill style={{ background: `linear-gradient(135deg, ${NAVY} 0%, #0D1126 100%)`, alignItems: 'center', justifyContent: 'center', flexDirection: 'column', opacity: bgOpacity }}>
      <div style={{ position: 'absolute', width: 800, height: 800, borderRadius: '50%', border: `2px solid ${TEAL}30`, top: '50%', left: '50%', transform: 'translate(-50%, -50%)' }} />
      <div style={{ position: 'absolute', width: 550, height: 550, borderRadius: '50%', border: `1px solid ${TEAL}20`, top: '50%', left: '50%', transform: 'translate(-50%, -50%)' }} />

      <div style={{ fontFamily: 'Inter, sans-serif', fontWeight: 800, fontSize: 76, color: WHITE, letterSpacing: -1, textAlign: 'center', lineHeight: 1.1, transform: `scale(${titleScale})`, marginBottom: 24 }}>
        {t.appName}
      </div>
      <div style={{ fontFamily: 'Inter, sans-serif', fontWeight: 400, fontSize: 30, color: TEAL, opacity: sub1, marginBottom: 12, letterSpacing: 0.5, textAlign: 'center' }}>{t.ctaLine1}</div>
      <div style={{ fontFamily: 'Inter, sans-serif', fontWeight: 600, fontSize: 22, color: GOLD, opacity: sub2, letterSpacing: 1, textAlign: 'center' }}>{t.ctaLine2}</div>

      <div style={{ marginTop: 48, opacity: imgOpacity, background: WHITE, borderRadius: 16, padding: 16, boxShadow: `0 0 40px ${TEAL}44` }}>
        <Img src={staticFile('screenshots/alarm.jpg')} style={{ width: 120, height: 120, borderRadius: 12, display: 'block' }} />
      </div>

      <div style={{ position: 'absolute', bottom: 40, fontFamily: 'Inter, sans-serif', fontWeight: 400, fontSize: 18, color: `${WHITE}60`, letterSpacing: 1, opacity: sub2 }}>
        play.google.com/store/apps/details?id=com.gaston.lesbegueris.notepases
      </div>
    </AbsoluteFill>
  );
}

// ── Main composition ──────────────────────────────────────────────────────────

export const NoTePasesVideo = ({ lang = 'es' }) => {
  const frame = useCurrentFrame();
  const t = strings[lang];

  const scenes = [
    { start: 0,    dur: 120 },  // Intro
    { start: 120,  dur: 180 },  // Hook
    { start: 300,  dur: 200 },  // Alarm
    { start: 500,  dur: 180 },  // Map tracking
    { start: 680,  dur: 160 },  // Map ready
    { start: 840,  dur: 160 },  // Favorites
    { start: 1000, dur: 160 },  // Recents
    { start: 1160, dur: 160 },  // Add dialog
    { start: 1320, dur: 180 },  // CTA
  ];

  return (
    <AbsoluteFill style={{ background: NAVY }}>
      <Sequence from={scenes[0].start} durationInFrames={scenes[0].dur}>
        <SceneIntro localFrame={frame - scenes[0].start} t={t} />
      </Sequence>
      <Sequence from={scenes[1].start} durationInFrames={scenes[1].dur}>
        <SceneHook localFrame={frame - scenes[1].start} t={t} />
      </Sequence>
      <Sequence from={scenes[2].start} durationInFrames={scenes[2].dur}>
        <ScenePhone localFrame={frame - scenes[2].start} src={staticFile('screenshots/alarm.jpg')} label={t.labelAlarm} labelColor={TEAL} description={{ title: t.descMapTracking.title }} side="center" />
      </Sequence>
      <Sequence from={scenes[3].start} durationInFrames={scenes[3].dur}>
        <ScenePhone localFrame={frame - scenes[3].start} src={staticFile('screenshots/map_tracking.jpg')} label={t.labelMapTracking} labelColor={TEAL} description={t.descMapTracking} side="right" />
      </Sequence>
      <Sequence from={scenes[4].start} durationInFrames={scenes[4].dur}>
        <ScenePhone localFrame={frame - scenes[4].start} src={staticFile('screenshots/map_ready.jpg')} label={t.labelMapReady} labelColor={GOLD} description={t.descMapReady} side="left" />
      </Sequence>
      <Sequence from={scenes[5].start} durationInFrames={scenes[5].dur}>
        <ScenePhone localFrame={frame - scenes[5].start} src={staticFile('screenshots/favorites.jpg')} label={t.labelFavorites} labelColor={GOLD} description={t.descFavorites} side="right" />
      </Sequence>
      <Sequence from={scenes[6].start} durationInFrames={scenes[6].dur}>
        <ScenePhone localFrame={frame - scenes[6].start} src={staticFile('screenshots/recents.jpg')} label={t.labelRecents} labelColor={TEAL} description={t.descRecents} side="left" />
      </Sequence>
      <Sequence from={scenes[7].start} durationInFrames={scenes[7].dur}>
        <ScenePhone localFrame={frame - scenes[7].start} src={staticFile('screenshots/add_dialog.jpg')} label={t.labelAddDialog} labelColor={TEAL} side="center" description={{ title: t.descFavorites.title }} />
      </Sequence>
      <Sequence from={scenes[8].start} durationInFrames={scenes[8].dur}>
        <SceneCTA localFrame={frame - scenes[8].start} t={t} />
      </Sequence>
    </AbsoluteFill>
  );
};
