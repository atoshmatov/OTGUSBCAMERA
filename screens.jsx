// OTG Camera — all screens for the prototype
// Dark, mobile-first, IG-Live × OBS-lite aesthetic.

// ─────────────────────────────────────────────────────────────
// Design tokens
// ─────────────────────────────────────────────────────────────
const OTG = {
  bg:        '#000000',
  surface:   '#0d0d0d',
  surface2:  '#161616',
  hairline:  'rgba(255,255,255,0.08)',
  hairline2: 'rgba(255,255,255,0.14)',
  text:      '#fafafa',
  textDim:   'rgba(255,255,255,0.62)',
  textMute:  'rgba(255,255,255,0.38)',
  accent:    '#ff3b30',
  accentDim: 'rgba(255,59,48,0.18)',
  good:      '#22c55e',
  warn:      '#f5a524',
  font:      "'Manrope', system-ui, sans-serif",
  mono:      "'JetBrains Mono', ui-monospace, monospace",
};

// ─────────────────────────────────────────────────────────────
// Phone shell — dark, no Material chrome; status bar tinted black
// ─────────────────────────────────────────────────────────────
const PHONE_W = 380;
const PHONE_H = 800;

function StatusBar({ light = true, transparent = false }) {
  const c = light ? '#fff' : '#000';
  return (
    <div style={{
      height: 36, display: 'flex', alignItems: 'center',
      justifyContent: 'space-between', padding: '0 22px',
      position: 'relative', flexShrink: 0,
      background: transparent ? 'transparent' : OTG.bg,
      fontFamily: OTG.font, color: c, fontSize: 14, fontWeight: 600,
      letterSpacing: 0.2,
    }}>
      <span>9:30</span>
      <div style={{
        position: 'absolute', left: '50%', top: 9, transform: 'translateX(-50%)',
        width: 20, height: 20, borderRadius: 99, background: '#000',
        boxShadow: 'inset 0 0 0 1px rgba(255,255,255,0.06)',
      }} />
      <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
        {/* signal */}
        <svg width="16" height="11" viewBox="0 0 16 11" fill={c}>
          <rect x="0" y="7" width="3" height="4" rx="0.5"/>
          <rect x="4.5" y="4.5" width="3" height="6.5" rx="0.5"/>
          <rect x="9" y="2" width="3" height="9" rx="0.5"/>
          <rect x="13.5" y="0" width="3" height="11" rx="0.5" opacity="0.4"/>
        </svg>
        {/* wifi */}
        <svg width="15" height="11" viewBox="0 0 15 11" fill={c}>
          <path d="M7.5 11l3.6-4.5a5.5 5.5 0 00-7.2 0L7.5 11z"/>
          <path d="M7.5 4.2a8 8 0 015.7 2.3l1.4-1.7a10 10 0 00-14.2 0l1.4 1.7a8 8 0 015.7-2.3z" opacity="0.55"/>
        </svg>
        {/* battery */}
        <svg width="22" height="11" viewBox="0 0 22 11">
          <rect x="0.5" y="0.5" width="19" height="10" rx="2.5" fill="none" stroke={c} strokeOpacity="0.5"/>
          <rect x="2" y="2" width="14" height="7" rx="1" fill={c}/>
          <rect x="20" y="3.5" width="1.5" height="4" rx="0.5" fill={c} opacity="0.5"/>
        </svg>
      </div>
    </div>
  );
}

function HomeBar({ light = true }) {
  return (
    <div style={{
      height: 22, display: 'flex', alignItems: 'flex-start',
      justifyContent: 'center', paddingTop: 8, flexShrink: 0,
    }}>
      <div style={{
        width: 120, height: 4, borderRadius: 2,
        background: light ? '#fff' : '#000', opacity: 0.85,
      }} />
    </div>
  );
}

function Phone({ children, label, light = true, transparentStatus = false, statusInside = false }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 14 }}>
      <div style={{
        width: PHONE_W, height: PHONE_H, borderRadius: 42,
        padding: 9, background: '#1a1a1a',
        boxShadow: '0 0 0 1.5px #2a2a2a, 0 40px 80px -20px rgba(0,0,0,0.6)',
        position: 'relative', flexShrink: 0,
      }}>
        <div style={{
          width: '100%', height: '100%', borderRadius: 34, overflow: 'hidden',
          background: OTG.bg, display: 'flex', flexDirection: 'column',
          fontFamily: OTG.font, color: OTG.text, position: 'relative',
        }}>
          {!statusInside && <StatusBar light={light} transparent={transparentStatus} />}
          <div style={{ flex: 1, position: 'relative', overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
            {statusInside && (
              <div style={{ position: 'absolute', top: 0, left: 0, right: 0, zIndex: 50 }}>
                <StatusBar light={light} transparent />
              </div>
            )}
            {children}
          </div>
          <HomeBar light={light} />
        </div>
      </div>
      {label && (
        <div style={{
          fontFamily: OTG.mono, fontSize: 12, color: 'rgba(0,0,0,0.55)',
          letterSpacing: 0.4, textTransform: 'uppercase',
        }}>{label}</div>
      )}
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// Reusable icons (line, 24px, stroke 1.6)
// ─────────────────────────────────────────────────────────────
const Icon = ({ d, size = 22, stroke = 'currentColor', fill = 'none', sw = 1.6 }) => (
  <svg width={size} height={size} viewBox="0 0 24 24" fill={fill} stroke={stroke}
       strokeWidth={sw} strokeLinecap="round" strokeLinejoin="round">
    {typeof d === 'string' ? <path d={d}/> : d}
  </svg>
);
const I = {
  usb:      <Icon d="M12 2v9M12 11l-4-4M12 11l4-3M9 13h6v4a3 3 0 01-3 3 3 3 0 01-3-3v-4zM12 20v2"/>,
  cam:      <Icon d="M3 7h4l2-3h6l2 3h4v12H3z M12 16a4 4 0 100-8 4 4 0 000 8z"/>,
  rec:      <Icon d="M3 6h13l3 6-3 6H3z M16 12h.01"/>,
  mic:      <Icon d="M12 2a3 3 0 00-3 3v7a3 3 0 006 0V5a3 3 0 00-3-3z M5 11a7 7 0 0014 0 M12 18v3"/>,
  micOff:   <Icon d="M9 9V5a3 3 0 015.92-.7 M15 12V8 M5 11a7 7 0 0011.5 5.4 M12 18v3 M3 3l18 18"/>,
  flash:    <Icon d="M13 2L4 14h7l-1 8 9-12h-7z"/>,
  flashOff: <Icon d="M13 2L8 8.5 M20 10h-7l1-8 M11 14l-1 8 6-8 M3 3l18 18"/>,
  grid:     <Icon d="M3 3h18v18H3z M3 9h18 M3 15h18 M9 3v18 M15 3v18"/>,
  settings: <Icon d="M12 15a3 3 0 100-6 3 3 0 000 6z M19 12a7 7 0 00-.2-1.7l2-1.5-2-3.4-2.3.9a7 7 0 00-3-1.7L13 2h-4l-.5 2.6a7 7 0 00-3 1.7L3.2 5.4l-2 3.4 2 1.5A7 7 0 003 12c0 .6.1 1.2.2 1.7l-2 1.5 2 3.4 2.3-.9a7 7 0 003 1.7L9 22h4l.5-2.6a7 7 0 003-1.7l2.3.9 2-3.4-2-1.5c.1-.5.2-1.1.2-1.7z"/>,
  close:    <Icon d="M6 6l12 12 M18 6l-12 12"/>,
  chevR:    <Icon d="M9 6l6 6-6 6"/>,
  chevL:    <Icon d="M15 6l-6 6 6 6"/>,
  check:    <Icon d="M5 12l5 5L20 7"/>,
  copy:     <Icon d="M8 8h11v13H8z M5 5h11v3 M5 5v11h3"/>,
  refresh:  <Icon d="M3 12a9 9 0 0115-6.7L21 8 M21 3v5h-5 M21 12a9 9 0 01-15 6.7L3 16 M3 21v-5h5"/>,
  user:     <Icon d="M12 12a4 4 0 100-8 4 4 0 000 8z M4 21a8 8 0 0116 0"/>,
  zoom:     <Icon d="M11 11a4 4 0 100-8 4 4 0 000 8z M21 21l-6.5-6.5 M9 7v4M7 9h4" sw={1.6}/>,
  switch:   <Icon d="M17 3l3 3-3 3 M20 6H8a4 4 0 00-4 4 M7 21l-3-3 3-3 M4 18h12a4 4 0 004-4"/>,
  play:     <Icon d="M6 4l14 8-14 8z" fill="currentColor"/>,
  pause:    <Icon d="M7 4h3v16H7z M14 4h3v16h-3z" fill="currentColor"/>,
  cast:     <Icon d="M3 16v3h3 M3 12v0a8 8 0 018 8 M3 8v0a12 12 0 0112 12 M14 5h7v12h-9 M3 5h3"/>,
  dot:      <Icon d="M12 12h.01" sw={6}/>,
  link:     <Icon d="M10 14a4 4 0 005.7 0l3-3a4 4 0 00-5.7-5.7l-1 1 M14 10a4 4 0 00-5.7 0l-3 3a4 4 0 005.7 5.7l1-1"/>,
  signal:   <Icon d="M2 20l4-4 M8 18l4-8 M14 14l4-12"/>,
  resize:   <Icon d="M4 9V4h5 M20 9V4h-5 M4 15v5h5 M20 15v5h-5"/>,
};

// Striped video placeholder
function VideoPlaceholder({ label = 'CAMERA FEED', children, dim = false }) {
  return (
    <div style={{
      position: 'absolute', inset: 0,
      background: `
        repeating-linear-gradient(135deg, #0e0e0e 0 16px, #131313 16px 32px)
      `,
      overflow: 'hidden',
    }}>
      <div style={{
        position: 'absolute', inset: 0,
        background: 'radial-gradient(ellipse at center, rgba(255,255,255,0.04) 0%, transparent 70%)',
      }}/>
      {!dim && (
        <div style={{
          position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%, -50%)',
          fontFamily: OTG.mono, fontSize: 11, color: 'rgba(255,255,255,0.28)',
          letterSpacing: 2, textAlign: 'center', lineHeight: 1.8,
        }}>
          <div>┌─────────────┐</div>
          <div>│ {label.padEnd(11)} │</div>
          <div>└─────────────┘</div>
          <div style={{ marginTop: 8, fontSize: 9, opacity: 0.7 }}>1920 × 1080 · 30 FPS</div>
        </div>
      )}
      {children}
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// 1. SPLASH / USB OTG CONNECT
// ─────────────────────────────────────────────────────────────
function ScreenSplash({ state = 'waiting' /* waiting | detected */ }) {
  return (
    <div style={{
      flex: 1, display: 'flex', flexDirection: 'column',
      padding: '24px 28px 28px', background: OTG.bg,
    }}>
      {/* brand */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 'auto', paddingTop: 16 }}>
        <div style={{
          width: 28, height: 28, borderRadius: 8,
          background: OTG.accent, display: 'flex',
          alignItems: 'center', justifyContent: 'center',
        }}>
          <div style={{ width: 8, height: 8, borderRadius: 99, background: '#000' }}/>
        </div>
        <div style={{ fontWeight: 700, fontSize: 17, letterSpacing: -0.3 }}>otgcamera</div>
      </div>

      {/* USB illustration */}
      <div style={{
        flex: 1, display: 'flex', flexDirection: 'column',
        alignItems: 'center', justifyContent: 'center', gap: 32,
      }}>
        <div style={{ position: 'relative', width: 200, height: 200 }}>
          {/* pulse rings (waiting only) */}
          {state === 'waiting' && [0,1,2].map(i => (
            <div key={i} style={{
              position: 'absolute', inset: 0, borderRadius: '50%',
              border: '1px solid rgba(255,255,255,0.08)',
              transform: `scale(${0.5 + i*0.25})`,
            }}/>
          ))}
          {state === 'detected' && (
            <div style={{
              position: 'absolute', inset: 0, borderRadius: '50%',
              background: 'radial-gradient(circle, rgba(34,197,94,0.18) 0%, transparent 60%)',
            }}/>
          )}
          {/* central icon */}
          <div style={{
            position: 'absolute', top: '50%', left: '50%',
            transform: 'translate(-50%, -50%)',
            width: 96, height: 96, borderRadius: 28,
            background: OTG.surface2, border: `1px solid ${OTG.hairline2}`,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            color: state === 'detected' ? OTG.good : OTG.text,
          }}>
            <Icon d="M9 2v5M15 2v5M7 7h10v3a2 2 0 01-2 2H9a2 2 0 01-2-2V7zM12 12v6M9 18h6v2a3 3 0 01-3 3 3 3 0 01-3-3v-2z" size={48} sw={1.4}/>
          </div>
        </div>

        <div style={{ textAlign: 'center' }}>
          <div style={{ fontSize: 22, fontWeight: 600, letterSpacing: -0.4, marginBottom: 8 }}>
            {state === 'waiting' ? 'USB kamerani ulang' : 'Kamera topildi'}
          </div>
          <div style={{ fontSize: 14, color: OTG.textDim, lineHeight: 1.5, maxWidth: 280 }}>
            {state === 'waiting'
              ? 'OTG kabel orqali kamerani telefonga ulang. Avtomatik aniqlanadi.'
              : 'Sony FDR-AX53 · UVC 1.5 · 1080p tayyor'}
          </div>
        </div>

        {state === 'waiting' && (
          <div style={{
            display: 'flex', alignItems: 'center', gap: 8,
            padding: '8px 14px', borderRadius: 99,
            background: OTG.surface, border: `1px solid ${OTG.hairline}`,
            fontFamily: OTG.mono, fontSize: 11, color: OTG.textDim,
          }}>
            <div style={{
              width: 6, height: 6, borderRadius: 99, background: OTG.warn,
              animation: 'pulse 1.4s ease-in-out infinite',
            }}/>
            kutilmoqda...
          </div>
        )}
      </div>

      {/* bottom action */}
      <button style={{
        height: 56, borderRadius: 16, border: 'none',
        background: state === 'detected' ? OTG.text : OTG.surface2,
        color: state === 'detected' ? '#000' : OTG.textDim,
        fontFamily: OTG.font, fontSize: 16, fontWeight: 600,
        letterSpacing: -0.2,
      }}>
        {state === 'detected' ? 'Davom etish' : 'Yordam · qo\'llanma'}
      </button>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// 2. CAMERA SELECT
// ─────────────────────────────────────────────────────────────
function ScreenCameraSelect() {
  const cams = [
    { name: 'Sony FDR-AX53', sub: 'UVC 1.5 · 1080p60 · USB-C', active: true, dot: OTG.good },
    { name: 'Logitech C920', sub: 'UVC 1.1 · 1080p30 · USB-A', active: false, dot: OTG.good },
    { name: 'Phone back camera', sub: 'Internal · 4K30', active: false, dot: OTG.textMute },
    { name: 'Phone front camera', sub: 'Internal · 1080p30', active: false, dot: OTG.textMute },
  ];
  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', background: OTG.bg }}>
      {/* header */}
      <div style={{
        padding: '20px 22px 24px', display: 'flex', alignItems: 'center', gap: 12,
      }}>
        <button style={{
          width: 38, height: 38, borderRadius: 12, border: 'none',
          background: OTG.surface2, color: OTG.text,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>{I.chevL}</button>
        <div style={{ flex: 1 }}>
          <div style={{ fontSize: 19, fontWeight: 700, letterSpacing: -0.4 }}>Kamera tanlash</div>
          <div style={{ fontSize: 12, color: OTG.textDim, fontFamily: OTG.mono, marginTop: 2 }}>
            4 device · 2 active
          </div>
        </div>
        <button style={{
          width: 38, height: 38, borderRadius: 12, border: 'none',
          background: OTG.surface2, color: OTG.text,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>{I.refresh}</button>
      </div>

      {/* list */}
      <div style={{ padding: '0 16px', display: 'flex', flexDirection: 'column', gap: 8, flex: 1 }}>
        {cams.map((c, i) => (
          <div key={i} style={{
            display: 'flex', alignItems: 'center', gap: 14,
            padding: '14px 16px', borderRadius: 16,
            background: c.active ? OTG.surface2 : OTG.surface,
            border: c.active ? `1px solid ${OTG.hairline2}` : `1px solid transparent`,
          }}>
            <div style={{
              width: 44, height: 44, borderRadius: 12,
              background: c.active ? '#1f1f1f' : '#0f0f0f',
              border: `1px solid ${OTG.hairline}`,
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              color: c.active ? OTG.text : OTG.textDim,
            }}>{I.cam}</div>
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ fontSize: 15, fontWeight: 600, letterSpacing: -0.2 }}>{c.name}</div>
              <div style={{ fontSize: 12, color: OTG.textDim, fontFamily: OTG.mono, marginTop: 2 }}>{c.sub}</div>
            </div>
            <div style={{
              width: 18, height: 18, borderRadius: 99,
              background: c.active ? OTG.accent : 'transparent',
              border: c.active ? 'none' : `1.5px solid ${OTG.hairline2}`,
              display: 'flex', alignItems: 'center', justifyContent: 'center',
            }}>
              {c.active && <div style={{ width: 6, height: 6, borderRadius: 99, background: '#fff' }}/>}
            </div>
          </div>
        ))}
      </div>

      {/* bottom action */}
      <div style={{ padding: 16, paddingTop: 8 }}>
        <button style={{
          width: '100%', height: 54, borderRadius: 16, border: 'none',
          background: OTG.text, color: '#000',
          fontFamily: OTG.font, fontSize: 16, fontWeight: 700,
          letterSpacing: -0.2, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
        }}>
          Davom etish {I.chevR}
        </button>
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// 3. LIVE PREVIEW (idle) — minimalist variant
// ─────────────────────────────────────────────────────────────
function ScreenLivePreview({ variant = 'minimal', controlsPos = 'bottom', state = 'idle' }) {
  const isLive = state === 'live';
  const isConn = state === 'connecting';

  return (
    <div style={{ flex: 1, position: 'relative', background: '#000' }}>
      <VideoPlaceholder label="LIVE VIEW">
        {/* subtle vignette to make UI legible */}
        <div style={{
          position: 'absolute', inset: 0, pointerEvents: 'none',
          background: 'linear-gradient(180deg, rgba(0,0,0,0.55) 0%, transparent 25%, transparent 65%, rgba(0,0,0,0.7) 100%)',
        }}/>
      </VideoPlaceholder>

      {/* TOP overlay */}
      <div style={{
        position: 'absolute', top: 44, left: 0, right: 0, zIndex: 10,
        display: 'flex', alignItems: 'center', padding: '0 16px', gap: 10,
      }}>
        {/* stream status pill */}
        <div style={{
          display: 'flex', alignItems: 'center', gap: 8,
          padding: '7px 12px 7px 10px', borderRadius: 99,
          background: isLive ? OTG.accent : 'rgba(0,0,0,0.55)',
          backdropFilter: 'blur(12px)',
          border: isLive ? 'none' : `1px solid ${OTG.hairline2}`,
        }}>
          <div style={{
            width: 8, height: 8, borderRadius: 99,
            background: isLive ? '#fff' : (isConn ? OTG.warn : OTG.textMute),
            animation: isLive ? 'pulse 1.2s ease-in-out infinite' : 'none',
          }}/>
          <div style={{
            fontFamily: OTG.mono, fontSize: 11, fontWeight: 700,
            letterSpacing: 1.2, color: '#fff',
          }}>
            {isLive ? 'ON AIR' : isConn ? 'CONNECTING' : 'READY'}
          </div>
          {isLive && (
            <div style={{ fontFamily: OTG.mono, fontSize: 11, color: 'rgba(255,255,255,0.85)', paddingLeft: 6, borderLeft: '1px solid rgba(255,255,255,0.35)' }}>
              00:12:48
            </div>
          )}
        </div>

        <div style={{ flex: 1 }}/>

        {/* viewers (when live) */}
        {isLive && (
          <div style={{
            display: 'flex', alignItems: 'center', gap: 6,
            padding: '7px 12px', borderRadius: 99,
            background: 'rgba(0,0,0,0.55)', backdropFilter: 'blur(12px)',
            border: `1px solid ${OTG.hairline2}`,
            color: '#fff', fontFamily: OTG.mono, fontSize: 11, fontWeight: 700,
          }}>
            <div style={{ opacity: 0.8 }}>{I.user}</div> 1.2K
          </div>
        )}

        {/* close */}
        <button style={{
          width: 36, height: 36, borderRadius: 99, border: 'none',
          background: 'rgba(0,0,0,0.55)', backdropFilter: 'blur(12px)',
          color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>{I.close}</button>
      </div>

      {/* TOP-RIGHT compact stats (when streaming) */}
      {isLive && variant !== 'side' && (
        <div style={{
          position: 'absolute', top: 96, right: 16, zIndex: 9,
          display: 'flex', flexDirection: 'column', gap: 6,
          padding: '10px 12px', borderRadius: 12,
          background: 'rgba(0,0,0,0.5)', backdropFilter: 'blur(14px)',
          border: `1px solid ${OTG.hairline}`,
          fontFamily: OTG.mono, fontSize: 10, color: 'rgba(255,255,255,0.85)',
          letterSpacing: 0.3,
        }}>
          <Stat k="BITRATE" v="4 200 kbps" trend={OTG.good}/>
          <Stat k="FPS" v="29.97"/>
          <Stat k="RES" v="1920×1080"/>
          <Stat k="RTMP" v="OK" valColor={OTG.good}/>
        </div>
      )}

      {/* SIDE rail variant (right) */}
      {(controlsPos === 'right' || variant === 'side') && (
        <SideRail isLive={isLive}/>
      )}

      {/* GRID overlay (third-rule lines) */}
      {variant === 'dense' && (
        <svg style={{ position: 'absolute', inset: 0, pointerEvents: 'none', zIndex: 4 }}>
          <line x1="33%" y1="0" x2="33%" y2="100%" stroke="rgba(255,255,255,0.08)" strokeWidth="1"/>
          <line x1="66%" y1="0" x2="66%" y2="100%" stroke="rgba(255,255,255,0.08)" strokeWidth="1"/>
          <line x1="0" y1="33%" x2="100%" y2="33%" stroke="rgba(255,255,255,0.08)" strokeWidth="1"/>
          <line x1="0" y1="66%" x2="100%" y2="66%" stroke="rgba(255,255,255,0.08)" strokeWidth="1"/>
        </svg>
      )}

      {/* BOTTOM control rail */}
      {controlsPos === 'bottom' && (
        <BottomRail isLive={isLive} isConn={isConn} variant={variant}/>
      )}
    </div>
  );
}

function Stat({ k, v, valColor, trend }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 10, justifyContent: 'space-between', minWidth: 130 }}>
      <span style={{ color: 'rgba(255,255,255,0.45)' }}>{k}</span>
      <span style={{ display: 'flex', alignItems: 'center', gap: 4, color: valColor || '#fff', fontWeight: 600 }}>
        {trend && <span style={{ width: 4, height: 4, borderRadius: 99, background: trend }}/>}
        {v}
      </span>
    </div>
  );
}

function SideRail({ isLive }) {
  const btns = [
    { i: I.flash, label: 'Flash' },
    { i: I.mic,   label: 'Mic' },
    { i: I.grid,  label: 'Grid', on: true },
    { i: I.switch,label: 'Switch' },
    { i: I.zoom,  label: 'Zoom' },
  ];
  return (
    <div style={{
      position: 'absolute', right: 12, top: '50%', transform: 'translateY(-50%)',
      zIndex: 8, display: 'flex', flexDirection: 'column', gap: 10,
      padding: '12px 8px', borderRadius: 24,
      background: 'rgba(0,0,0,0.45)', backdropFilter: 'blur(14px)',
      border: `1px solid ${OTG.hairline}`,
    }}>
      {btns.map((b, i) => (
        <div key={i} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}>
          <button style={{
            width: 42, height: 42, borderRadius: 99, border: 'none',
            background: b.on ? '#fff' : 'transparent',
            color: b.on ? '#000' : '#fff',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>{b.i}</button>
        </div>
      ))}
    </div>
  );
}

function BottomRail({ isLive, isConn, variant }) {
  const topBtns = [
    { i: I.flash, label: 'flash' },
    { i: I.mic, label: 'mic' },
    { i: I.grid, label: 'grid', on: variant === 'dense' },
    { i: I.zoom, label: '1.0×', text: true },
  ];
  return (
    <div style={{
      position: 'absolute', bottom: 16, left: 0, right: 0, zIndex: 10,
      padding: '0 16px', display: 'flex', flexDirection: 'column', gap: 14,
    }}>
      {/* top row — utility */}
      {variant !== 'minimal' && (
        <div style={{
          display: 'flex', justifyContent: 'space-between', gap: 8,
          padding: '8px 10px', borderRadius: 99,
          background: 'rgba(0,0,0,0.55)', backdropFilter: 'blur(14px)',
          border: `1px solid ${OTG.hairline}`,
        }}>
          {topBtns.map((b, i) => (
            <button key={i} style={{
              flex: 1, height: 38, borderRadius: 99, border: 'none',
              background: b.on ? '#fff' : 'transparent',
              color: b.on ? '#000' : '#fff',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              fontFamily: b.text ? OTG.mono : OTG.font,
              fontSize: b.text ? 12 : 14, fontWeight: 700,
            }}>{b.text ? b.label : b.i}</button>
          ))}
        </div>
      )}

      {/* main row */}
      <div style={{
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        padding: '0 6px',
      }}>
        {/* left aux: switch cam */}
        <button style={{
          width: 50, height: 50, borderRadius: 99, border: `1px solid ${OTG.hairline2}`,
          background: 'rgba(0,0,0,0.5)', backdropFilter: 'blur(12px)', color: '#fff',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>{I.switch}</button>

        {/* primary: stream */}
        <button style={{
          width: 78, height: 78, borderRadius: 99, border: '4px solid rgba(255,255,255,0.9)',
          background: isLive ? OTG.accent : 'transparent',
          padding: 0, position: 'relative',
          boxShadow: isLive ? `0 0 0 4px rgba(255,59,48,0.25)` : 'none',
        }}>
          <div style={{
            position: 'absolute', inset: isLive ? 22 : 4,
            borderRadius: isLive ? 6 : 99,
            background: OTG.accent,
            transition: 'all 0.2s',
          }}/>
          {isConn && (
            <div style={{
              position: 'absolute', inset: -4, borderRadius: 99,
              border: `3px solid ${OTG.warn}`, borderTopColor: 'transparent',
              animation: 'spin 1s linear infinite',
            }}/>
          )}
        </button>

        {/* right aux: snapshot */}
        <button style={{
          width: 50, height: 50, borderRadius: 99, border: `1px solid ${OTG.hairline2}`,
          background: 'rgba(0,0,0,0.5)', backdropFilter: 'blur(12px)', color: '#fff',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>{I.cam}</button>
      </div>

      {/* label under primary */}
      <div style={{
        textAlign: 'center', fontFamily: OTG.mono, fontSize: 10,
        letterSpacing: 1.5, color: 'rgba(255,255,255,0.55)', marginTop: -8,
      }}>
        {isLive ? 'TAP TO STOP' : isConn ? 'CONNECTING…' : 'TAP TO GO LIVE'}
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// 4. RTMP SETTINGS
// ─────────────────────────────────────────────────────────────
function ScreenRTMPSettings() {
  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', background: OTG.bg, overflow: 'auto' }}>
      <div style={{ padding: '20px 22px 14px', display: 'flex', alignItems: 'center', gap: 12 }}>
        <button style={{
          width: 38, height: 38, borderRadius: 12, border: 'none',
          background: OTG.surface2, color: OTG.text,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>{I.chevL}</button>
        <div style={{ flex: 1, fontSize: 19, fontWeight: 700, letterSpacing: -0.4 }}>Stream sozlamalari</div>
      </div>

      <div style={{ padding: '0 16px 16px', display: 'flex', flexDirection: 'column', gap: 16 }}>

        <Section title="RTMP Server">
          <Field label="Server URL" value="rtmp://live.uztv.uz/stream" mono action="paste"/>
          <Field label="Stream key" value="••••••••• xK29-4lP" mono action="copy"/>
          <Field label="Profile" value="Default · UzTV" arrow/>
        </Section>

        <Section title="Video">
          <SliderRow label="Bitrate" v="4 200" unit="kbps" pos={0.55}/>
          <PickerRow label="Resolution" v="1920 × 1080" sub="Full HD"/>
          <PickerRow label="Frame rate" v="30 fps"/>
          <PickerRow label="Codec" v="H.264 · High"/>
        </Section>

        <Section title="Audio">
          <SliderRow label="Bitrate" v="128" unit="kbps" pos={0.45}/>
          <PickerRow label="Source" v="USB camera mic"/>
        </Section>

        <Section title="Advanced">
          <ToggleRow label="Auto-reconnect" on/>
          <ToggleRow label="Adaptive bitrate" on/>
          <ToggleRow label="Hardware encoder" on={false}/>
        </Section>

        <button style={{
          height: 54, borderRadius: 16, border: 'none', marginTop: 4,
          background: OTG.accent, color: '#fff',
          fontFamily: OTG.font, fontSize: 16, fontWeight: 700, letterSpacing: -0.2,
        }}>Sozlamalarni saqlash</button>
      </div>
    </div>
  );
}

function Section({ title, children }) {
  return (
    <div>
      <div style={{
        fontFamily: OTG.mono, fontSize: 10, letterSpacing: 1.5,
        color: OTG.textMute, padding: '6px 6px 10px', textTransform: 'uppercase',
      }}>{title}</div>
      <div style={{
        background: OTG.surface, borderRadius: 16,
        border: `1px solid ${OTG.hairline}`, overflow: 'hidden',
      }}>{children}</div>
    </div>
  );
}

function Field({ label, value, mono, action }) {
  return (
    <div style={{
      padding: '12px 16px', borderBottom: `1px solid ${OTG.hairline}`,
      display: 'flex', flexDirection: 'column', gap: 4,
    }}>
      <div style={{ fontSize: 11, color: OTG.textMute, fontFamily: OTG.mono, letterSpacing: 0.5 }}>{label}</div>
      <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
        <div style={{
          flex: 1, fontFamily: mono ? OTG.mono : OTG.font,
          fontSize: 14, color: OTG.text, fontWeight: 500,
          overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
        }}>{value}</div>
        {action === 'copy' && (
          <button style={{ background: 'transparent', border: 'none', color: OTG.textDim, padding: 4 }}>{I.copy}</button>
        )}
        {action === 'paste' && (
          <span style={{ fontFamily: OTG.mono, fontSize: 11, color: OTG.accent, padding: 4 }}>PASTE</span>
        )}
      </div>
    </div>
  );
}

function SliderRow({ label, v, unit, pos = 0.5 }) {
  return (
    <div style={{ padding: '14px 16px', borderBottom: `1px solid ${OTG.hairline}` }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: 12 }}>
        <span style={{ fontSize: 14, fontWeight: 500 }}>{label}</span>
        <span style={{ fontFamily: OTG.mono, fontSize: 13, color: OTG.text }}>
          {v} <span style={{ color: OTG.textMute }}>{unit}</span>
        </span>
      </div>
      <div style={{ height: 4, borderRadius: 99, background: 'rgba(255,255,255,0.08)', position: 'relative' }}>
        <div style={{ position: 'absolute', left: 0, top: 0, bottom: 0, width: `${pos*100}%`, background: OTG.text, borderRadius: 99 }}/>
        <div style={{
          position: 'absolute', left: `calc(${pos*100}% - 9px)`, top: -7,
          width: 18, height: 18, borderRadius: 99, background: '#fff',
          boxShadow: '0 2px 8px rgba(0,0,0,0.4)',
        }}/>
      </div>
    </div>
  );
}

function PickerRow({ label, v, sub }) {
  return (
    <div style={{
      padding: '14px 16px', borderBottom: `1px solid ${OTG.hairline}`,
      display: 'flex', alignItems: 'center', gap: 12,
    }}>
      <span style={{ flex: 1, fontSize: 14, fontWeight: 500 }}>{label}</span>
      <div style={{ textAlign: 'right' }}>
        <div style={{ fontFamily: OTG.mono, fontSize: 13, color: OTG.text }}>{v}</div>
        {sub && <div style={{ fontSize: 10, color: OTG.textMute, marginTop: 2 }}>{sub}</div>}
      </div>
      <div style={{ color: OTG.textMute }}>{I.chevR}</div>
    </div>
  );
}

function ToggleRow({ label, on }) {
  return (
    <div style={{
      padding: '14px 16px', borderBottom: `1px solid ${OTG.hairline}`,
      display: 'flex', alignItems: 'center', gap: 12,
    }}>
      <span style={{ flex: 1, fontSize: 14, fontWeight: 500 }}>{label}</span>
      <div style={{
        width: 44, height: 26, borderRadius: 99,
        background: on ? OTG.accent : '#2a2a2a',
        padding: 3, boxSizing: 'border-box', position: 'relative',
        transition: 'background 0.2s',
      }}>
        <div style={{
          width: 20, height: 20, borderRadius: 99, background: '#fff',
          marginLeft: on ? 18 : 0, transition: 'margin 0.2s',
        }}/>
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// 6. STREAM PLAYER (receive)
// ─────────────────────────────────────────────────────────────
function ScreenPlayer() {
  return (
    <div style={{ flex: 1, background: '#000', position: 'relative' }}>
      <VideoPlaceholder label="LIVE STREAM">
        <div style={{
          position: 'absolute', inset: 0, pointerEvents: 'none',
          background: 'linear-gradient(180deg, rgba(0,0,0,0.5) 0%, transparent 30%, transparent 60%, rgba(0,0,0,0.85) 100%)',
        }}/>
      </VideoPlaceholder>

      {/* top */}
      <div style={{
        position: 'absolute', top: 44, left: 16, right: 16, zIndex: 10,
        display: 'flex', alignItems: 'center', gap: 10,
      }}>
        <button style={{
          width: 38, height: 38, borderRadius: 12, border: 'none',
          background: 'rgba(0,0,0,0.55)', backdropFilter: 'blur(12px)',
          color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>{I.chevL}</button>
        <div style={{
          display: 'flex', alignItems: 'center', gap: 8,
          padding: '7px 12px 7px 10px', borderRadius: 99,
          background: OTG.accent, color: '#fff',
        }}>
          <div style={{ width: 8, height: 8, borderRadius: 99, background: '#fff',
            animation: 'pulse 1.2s ease-in-out infinite' }}/>
          <div style={{ fontFamily: OTG.mono, fontSize: 11, fontWeight: 700, letterSpacing: 1.2 }}>LIVE</div>
        </div>
        <div style={{ flex: 1 }}/>
        <button style={{
          width: 38, height: 38, borderRadius: 12, border: 'none',
          background: 'rgba(0,0,0,0.55)', backdropFilter: 'blur(12px)',
          color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>{I.cast}</button>
      </div>

      {/* bottom — title + scrub */}
      <div style={{
        position: 'absolute', bottom: 18, left: 0, right: 0, zIndex: 10,
        padding: '0 18px', display: 'flex', flexDirection: 'column', gap: 14,
      }}>
        <div>
          <div style={{ fontFamily: OTG.mono, fontSize: 10, color: 'rgba(255,255,255,0.55)', letterSpacing: 1.5 }}>
            CHANNEL · UZTV LIVE
          </div>
          <div style={{ fontSize: 20, fontWeight: 700, letterSpacing: -0.4, marginTop: 4 }}>
            Toshkent kechki yangiliklar
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginTop: 8, fontFamily: OTG.mono, fontSize: 11, color: 'rgba(255,255,255,0.65)' }}>
            <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}>{I.user} 12.4K</span>
            <span>·</span>
            <span>4 200 kbps</span>
            <span>·</span>
            <span style={{ color: OTG.good }}>● HD</span>
          </div>
        </div>

        <div style={{
          display: 'flex', alignItems: 'center', gap: 10,
          padding: '6px', borderRadius: 99,
          background: 'rgba(0,0,0,0.55)', backdropFilter: 'blur(12px)',
          border: `1px solid ${OTG.hairline}`,
        }}>
          <button style={{
            width: 44, height: 44, borderRadius: 99, border: 'none',
            background: '#fff', color: '#000',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>{I.pause}</button>
          <div style={{ flex: 1, height: 3, borderRadius: 99, background: 'rgba(255,255,255,0.18)', position: 'relative' }}>
            <div style={{ position: 'absolute', inset: 0, width: '92%', background: OTG.accent, borderRadius: 99 }}/>
            <div style={{ position: 'absolute', right: '8%', top: -5, width: 12, height: 12, borderRadius: 99, background: OTG.accent }}/>
          </div>
          <div style={{ fontFamily: OTG.mono, fontSize: 11, color: '#fff', paddingRight: 10 }}>LIVE</div>
          <button style={{
            width: 36, height: 36, borderRadius: 99, border: 'none',
            background: 'transparent', color: '#fff',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>{I.resize}</button>
        </div>
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// 7. SETTINGS
// ─────────────────────────────────────────────────────────────
function ScreenSettings() {
  return (
    <div style={{ flex: 1, background: OTG.bg, overflow: 'auto', display: 'flex', flexDirection: 'column' }}>
      <div style={{ padding: '20px 22px 8px', display: 'flex', alignItems: 'center', gap: 12 }}>
        <button style={{
          width: 38, height: 38, borderRadius: 12, border: 'none',
          background: OTG.surface2, color: OTG.text,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>{I.chevL}</button>
        <div style={{ flex: 1, fontSize: 19, fontWeight: 700, letterSpacing: -0.4 }}>Sozlamalar</div>
      </div>

      {/* profile */}
      <div style={{ padding: '8px 16px 18px' }}>
        <div style={{
          display: 'flex', alignItems: 'center', gap: 14,
          padding: '16px', borderRadius: 16,
          background: OTG.surface, border: `1px solid ${OTG.hairline}`,
        }}>
          <div style={{
            width: 48, height: 48, borderRadius: 99,
            background: 'linear-gradient(135deg, #ff3b30 0%, #7a1c17 100%)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontWeight: 700, color: '#fff', fontSize: 17,
          }}>JR</div>
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: 15, fontWeight: 600 }}>Jasur Rahimov</div>
            <div style={{ fontSize: 12, color: OTG.textDim, fontFamily: OTG.mono, marginTop: 2 }}>jasur@uztv.uz</div>
          </div>
          <div style={{ color: OTG.textMute }}>{I.chevR}</div>
        </div>
      </div>

      <div style={{ padding: '0 16px 16px', display: 'flex', flexDirection: 'column', gap: 16 }}>
        <Section title="App">
          <PickerRow label="Til · Language" v="O'zbekcha"/>
          <PickerRow label="Tema" v="Dark"/>
          <PickerRow label="Boshqaruv joyi" v="Pastda"/>
        </Section>

        <Section title="Stream">
          <PickerRow label="Default profile" v="UzTV Live"/>
          <PickerRow label="Storage" v="Internal · 24 GB free"/>
          <ToggleRow label="Save local copy" on/>
        </Section>

        <Section title="Device">
          <PickerRow label="USB OTG" v="UVC 1.5" sub="auto-connect"/>
          <ToggleRow label="Keep screen on" on/>
          <ToggleRow label="Battery saver" on={false}/>
        </Section>

        <div style={{
          fontFamily: OTG.mono, fontSize: 10, color: OTG.textMute,
          textAlign: 'center', padding: '8px 0 4px', letterSpacing: 1,
        }}>otgcamera · v1.2.0 · build 248</div>
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// Export
// ─────────────────────────────────────────────────────────────
Object.assign(window, {
  OTG, Phone, PHONE_W, PHONE_H,
  ScreenSplash, ScreenCameraSelect, ScreenLivePreview,
  ScreenRTMPSettings, ScreenPlayer, ScreenSettings, Icon, I,
});
