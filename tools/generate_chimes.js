// Offline chime synthesiser for Sukoon.
// Renders soft, warm "dun dun dun" mallet tones to app/src/main/res/raw/*.wav.
// Run:  node tools/generate_chimes.js
//
// These are deliberately NOT shimmering bells. Each note is a rounded marimba/kalimba-like
// "dun": a warm fundamental with a couple of quiet harmonics, a soft attack and a short, dark
// decay (heavily low-passed, no detune, only a touch of room). Cues are short sequences of these
// soft notes so they read as a gentle "dun dun dun", not a strummed chord.

const fs = require("fs");
const path = require("path");

const SR = 44100;
const OUT_DIR = path.join(__dirname, "..", "app", "src", "main", "res", "raw");

// Warm, mostly-harmonic partials → a rounded wooden "dun" rather than a metallic ring.
const PARTIALS = [
  { r: 0.5, a: 0.18, d: 0.7 }, // soft sub for body
  { r: 1.0, a: 1.0, d: 1.0 },
  { r: 2.0, a: 0.24, d: 1.7 },
  { r: 3.0, a: 0.07, d: 2.4 },
];

function renderNote(out, f0, startSec, decaySec, gain, attackMs) {
  const start = Math.floor(startSec * SR);
  const len = Math.floor((decaySec + 0.15) * SR);
  const att = Math.max(1, Math.floor((attackMs / 1000) * SR));
  const rate = 6.9 / decaySec;
  for (let i = 0; i < len; i++) {
    const idx = start + i;
    if (idx >= out.length) break;
    const t = i / SR;
    const attack = i < att ? 0.5 - 0.5 * Math.cos((Math.PI * i) / att) : 1;
    let s = 0;
    for (const p of PARTIALS) {
      s += p.a * Math.exp(-t * rate * p.d) * Math.sin(2 * Math.PI * f0 * p.r * t);
    }
    out[idx] += attack * s * gain;
  }
}

function lowpass(buf, cutoff) {
  const a = 1 - Math.exp((-2 * Math.PI * cutoff) / SR);
  let y = 0;
  for (let i = 0; i < buf.length; i++) {
    y += a * (buf[i] - y);
    buf[i] = y;
  }
}

// A small, subtle mono room reverb (a few combs + an allpass) — warmth, not a wash.
function reverb(input, { wet = 0.14, roomsize = 0.5, damp = 0.4 } = {}) {
  const combTuning = [1116, 1188, 1277, 1356];
  const apTuning = [556, 441];
  const fb = roomsize * 0.28 + 0.6;
  const damp1 = damp * 0.4;
  const damp2 = 1 - damp1;
  const n = input.length;
  const mkComb = (s) => ({ buf: new Float64Array(s), idx: 0, store: 0 });
  const mkAp = (s) => ({ buf: new Float64Array(s), idx: 0 });
  const combs = combTuning.map(mkComb);
  const aps = apTuning.map(mkAp);
  const out = new Float64Array(n);
  for (let i = 0; i < n; i++) {
    const inp = input[i] * 0.015;
    let acc = 0;
    for (const c of combs) {
      const y = c.buf[c.idx];
      c.store = y * damp2 + c.store * damp1;
      c.buf[c.idx] = inp + c.store * fb;
      if (++c.idx >= c.buf.length) c.idx = 0;
      acc += y;
    }
    for (const a of aps) {
      const bufout = a.buf[a.idx];
      const o = -acc + bufout;
      a.buf[a.idx] = acc + bufout * 0.5;
      if (++a.idx >= a.buf.length) a.idx = 0;
      acc = o;
    }
    out[i] = input[i] * (1 - wet) + acc * wet;
  }
  return out;
}

function finish(buf, peak) {
  // soft fade in/out to avoid edge clicks
  const fade = Math.floor(0.004 * SR);
  for (let i = 0; i < fade; i++) {
    const g = i / fade;
    buf[i] *= g;
    buf[buf.length - 1 - i] *= g;
  }
  let p = 0;
  for (const v of buf) p = Math.max(p, Math.abs(v));
  if (p === 0) return buf;
  const norm = peak / p;
  for (let i = 0; i < buf.length; i++) {
    buf[i] = Math.tanh(buf[i] * norm) * 0.92; // gentle, well below full scale
  }
  return buf;
}

function writeWav(name, mono) {
  const n = mono.length;
  const bps = 2;
  const dataSize = n * bps; // mono
  const buf = Buffer.alloc(44 + dataSize);
  buf.write("RIFF", 0);
  buf.writeUInt32LE(36 + dataSize, 4);
  buf.write("WAVE", 8);
  buf.write("fmt ", 12);
  buf.writeUInt32LE(16, 16);
  buf.writeUInt16LE(1, 20);
  buf.writeUInt16LE(1, 22); // mono
  buf.writeUInt32LE(SR, 24);
  buf.writeUInt32LE(SR * bps, 28);
  buf.writeUInt16LE(bps, 32);
  buf.writeUInt16LE(16, 34);
  buf.write("data", 36);
  buf.writeUInt32LE(dataSize, 40);
  let off = 44;
  for (let i = 0; i < n; i++) {
    buf.writeInt16LE(Math.round(Math.max(-1, Math.min(1, mono[i])) * 32767), off);
    off += 2;
  }
  fs.mkdirSync(OUT_DIR, { recursive: true });
  fs.writeFileSync(path.join(OUT_DIR, name), buf);
  console.log(`  ${name}  (${(buf.length / 1024).toFixed(0)} KB)`);
}

function build(name, totalSec, notes, { cutoff = 2600, peak = 0.5, rev = {} } = {}) {
  const buf = new Float64Array(Math.floor(totalSec * SR));
  for (const note of notes) {
    renderNote(buf, note.f, note.start ?? 0, note.decay ?? 0.55, note.gain ?? 1.0, note.attack ?? 5);
  }
  lowpass(buf, cutoff);
  const out = reverb(buf, rev);
  writeWav(name, finish(out, peak));
}

// Warm, mid/low note set (Hz)
const G3 = 196.0, A3 = 220.0, C4 = 261.63, E4 = 329.63, G4 = 392.0, A4 = 440.0, C5 = 523.25;

console.log("Rendering chimes →", OUT_DIR);

// Get ready — two soft, even "dun dun".
build("chime_ready.wav", 1.0, [
  { f: A3, start: 0.0, decay: 0.5, gain: 1.0 },
  { f: A3, start: 0.2, decay: 0.55, gain: 0.95 },
], { cutoff: 2300, peak: 0.46 });

// Begin a round — a gently rising "dun dun dun".
build("chime_work.wav", 1.1, [
  { f: G3, start: 0.0, decay: 0.5, gain: 0.95 },
  { f: C4, start: 0.16, decay: 0.5, gain: 0.95 },
  { f: E4, start: 0.32, decay: 0.6, gain: 1.0 },
], { cutoff: 2900, peak: 0.5 });

// Rest — a soft descending "dun dun".
build("chime_rest.wav", 1.0, [
  { f: E4, start: 0.0, decay: 0.5, gain: 0.95 },
  { f: C4, start: 0.2, decay: 0.6, gain: 1.0 },
], { cutoff: 2300, peak: 0.46 });

// Final-3s tick — a single soft, short "dun".
build("chime_tick.wav", 0.45, [
  { f: A4, start: 0.0, decay: 0.22, gain: 0.7 },
], { cutoff: 2600, peak: 0.30, rev: { wet: 0.08 } });

// All done — a gentle ascending "dun dun dun dun ~" that resolves on a longer note.
build("chime_done.wav", 2.2, [
  { f: C4, start: 0.0, decay: 0.5, gain: 0.9 },
  { f: E4, start: 0.18, decay: 0.5, gain: 0.9 },
  { f: G4, start: 0.36, decay: 0.55, gain: 0.92 },
  { f: C5, start: 0.54, decay: 1.3, gain: 1.0 },
], { cutoff: 3200, peak: 0.52, rev: { wet: 0.18, roomsize: 0.6 } });

console.log("Done.");
