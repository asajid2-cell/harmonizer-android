# LOOPS — Harmonizer mobile: native translation of the web design

Grand goal: faithfully translate the Harmonizer web design (Internet Discotheque index +
Harmonizer Lab) into native mobile Compose screens. Keep the EXACT existing design language;
only the **layout** translates to mobile-native (stack, scroll, thumb-reach). Verify on device
`R58M4244YVF` via adb screenshots (ui-craft workflow). Tandem (Codex) = design co-reviewer.

Source of truth (web): `harmonizer/frontend/index.html` (+ inline CSS) and
`harmonizer/frontend/harmonizer.html` + `modern.css`.

---

## CONTRACT (change-controlled)

### Grand Goal Contract
- **GG1** (machine) — `./gradlew assembleDebug` exits 0 and APK installs on R58M4244YVF.
- **GG2** (observable) — App start destination is a NEW Discotheque home screen (not UploadScreen);
  adb screenshot shows it on launch.
- **GG3** (observable) — From home, tapping Harmonizer opens the Harmonizer Lab; adb screenshots
  show the navigation works; back returns to home.
- **GG4** (observable) — Lab screen renders the faithful hero: lime eyebrow `*** WELCOME TO THE
  HARMONIZER LAB ***`, pulsing cyan `HASHTAG INFINITE LOOPS`, orange lede, cyan/purple callouts,
  3 glowing badges; plus `ENGINE` magenta mode-grid and lime `LOAD A TRACK` panel; track load →
  player still works.
- **GG5** (machine/observable) — No layout breakage at the device viewport across home + lab +
  experimental-expanded + song-list states: no clipped/overflowing text, every primary action
  on-screen, interactive targets ≥ 48dp. (ui-craft destruct discipline, adapted to device shots.)
- **GG6** (HUMAN-GATE) — Visual fidelity: user + Codex confirm both screens read as the Internet
  Discotheque + Harmonizer Lab. NOT autonomously claimable.
- **GG7** (regression) — Existing engine flows intact: `./gradlew testDebugUnitTest` green; manual
  smoke of load-track → process → player.

### Design tokens (extracted from source — the translation must hit these)
Index: bg `#000` + starfield dots (1px white) @50/80px grids + faint scanlines; title
"MY VERY OWN INTERNET DISCOTHEQUE" Times serif bold uppercase ls .32em color `#1d55ff`
shadow `0 0 18px rgba(61,128,255,.65)`; icon frame 64px radius 6 border white .18 bg `#0d0d0d`
inset bevel + outer glow, per-app accent (Harmonizer `#5cff8a`/`#ff7ac8`); label MS-Sans 10px
upper ls .1em; taskbar 44px bg `#0d0d0d` border-top `#2b2b2b`, START bevel `#151515`, center
"An Ode to You", tray "also done, with errors on page" + clock; credits ID Chief /
コンシャスTHOUGHTS / Aloe Island Posse (orange/purple/lime mono).
Lab: same starfield; eyebrow lime `#00FF00` bold ls .1em glow `0 0 10px`; h1 cyan `#00FFDE`
upper ls .15em glow `0 0 20/40px` pulse; lede orange `#FF6600` 600 ls .05em; callouts cyan li +
purple `#FF00FF` strong (glow) + cyan arrow, mono; badges 3px border + glow (purple/orange/cyan);
panel border 3px lime `#00FF00` bg `#000`, h2 lime upper ls .1em; section-title cyan .7rem upper
ls .16em; mode-grid; mode-card border 2px purple bg `#000`, active = filled purple + black text,
icon 2rem, h3 .9rem bold, p .7rem.

### Loop plan
- **L0** Baseline — current app builds green, installs, capture start-screen shot. (foundation)
- **L1** Foundation + Home scaffold — serif title font, starfield brightened to source, new
  `DiscothequeHomeScreen` wired as nav start destination, `IconFrame`/`Taskbar` components.
  Verifier: builds + installs + launches to home (adb shot shows a home, not upload), no crash.
- **L2** Home faithful layout — 15-app neon icon grid w/ accents, glowing blue serif title,
  taskbar, credits, ASCII flourish. Verifier: render + destruct (≥48dp, no clip) + GG6 gate.
- **L3** Lab rebuild — faithful hero (eyebrow/title/lede/callouts/badges) + `ENGINE` grid +
  `LOAD A TRACK`, upload functionality preserved. Verifier: render + load-track flow +
  destruct + GG6 gate.
- **L4** Synthesis — end-to-end home→lab→load→player; Codex adversarial design review; user
  visual sign-off. Verifier: GG1-GG7 end to end.

---

## PROGRESS

### Baseline
- (pending) `./gradlew assembleDebug` running.
- Device `R58M4244YVF` connected (`adb devices` = device). NOTE: secure keyguard makes
  `screencap` return 0 bytes — user must keep phone unlocked for visual verification; install
  works while locked.
- Start git state: harmonizer-android is NOT a git repo (env). Checkpoint policy = ledger +
  file diffs (no commits sanctioned / no repo).

### Loop status
- L0 baseline: **done** — `assembleDebug` exit 0; installed on R58M4244YVF; baseline shot
  `_design/dev/baseline.png` (current start = magenta upload screen, no home/lab hero).
- L1 foundation + home scaffold: **done** — serif title font (FontFamily.Serif), starfield
  brightened (`GridBackground(bright=true)`), `model/DiscothequeApp.kt` catalog (15 apps +
  featured Harmonizer, real mascot PNGs bundled to res/drawable, per-app CSS accents),
  `ui/home/DiscothequeHomeScreen.kt` (IconTile + Taskbar), nav start dest = Home, Harmonizer→Lab,
  others→web Intent. Verifier: build exit 0, installs, launches to home, no crash —
  `_design/dev/l1_home.png` shows the Discotheque home. trusted.
- L2 home faithful layout: **done** — glowing blue serif title (switched to `Shadow` text-shadow,
  not box halo), HARMONIZER hero w/ green glow, 14-tile 3-col neon grid w/ mascots+accents,
  PROGRAMS label, credits (ID Chief/コンシャスTHOUGHTS/Aloe Island Posse), taskbar (START menu +
  An Ode to You + live clock). Shot `_design/dev/l2_home.png`. GG6 (visual) = pending user/Codex.
- L3 lab rebuild: **done (pending GG6)** — `LabHero` in UploadScreen: lime eyebrow, pulsing cyan
  HASHTAG INFINITE LOOPS (animated Shadow), orange lede, cyan/magenta callouts, 3 glowing badges;
  LOAD A TRACK lime heading + ~~~ divider; mode label SELECT MODE→ENGINE (cyan); back link →
  Discotheque. Upload/mode/source/recent logic untouched. Shot `_design/dev/l3_lab.png`.
  FIX: added `.statusBarsPadding()` (top bar was under system status bar). GG6 = pending.
- L3.1 lab ASCII hero-visual (user tweak "include these guys"): **done** — added `HeroVisual`
  (orbit rings: spinning cyan+magenta ovals via Canvas) + `.ascii-woman` (orange) + two
  `.ascii-dancer-side` (cyan + pink) to LabHero. Shot `_design/dev/l3c_lab_ascii.png`.
- L3.2 hero motion (user tweak "they need to jump and move in the same way"): **done** — ported
  the exact modern.css keyframes: side-bounce (2.5s translateY -8px, pink +0.4 phase), woman-dance
  (2s rotate ±3°), ring-rotate (20s 360°). Verified motion across spaced frames m1/m3
  (`_design/dev/m{1,3}_crop.png`) — rings spin, woman sways. GG7 unit tests green (exit 0).
- L3.3 Codex fidelity audit + fixes: **done** — tandem partner Codex audited native-vs-web and
  caught a blind spot: I'd added neonGlow to icon frames / lime panels / selected mode-card that
  the web does NOT have (CSS box-shadow:none; frames black-beveled). Applied faithful fixes:
  flat icon frames + 58dp mascots; ModeCard full-magenta border + no glow; lime panels 3dp + no
  glow; home title unified to one string; lab h1 38→30sp; hero order = callouts→dancers→badges→
  orbit/woman; badges 3dp + badge-glow pulse; taskbar AN ODE TO YOU uppercase. Shots
  `_design/dev/f_{home,lab_top,lab_mid}.png`. Ledger: tandems/harmonizer-mobile-ui-translation.
- L4 synthesis: running — GG1 build green; GG2/GG3 nav verified (home start, Harmonizer→Lab→back);
  GG5 no clipping at device viewport across home/lab/scrolled; GG7 unit tests rerunning.
  Remaining: load-track smoke + user GG6 sign-off.

### Learnings
- Source mobile web index = the Win98 `.mobile-landing` window + ASCII gallery; desktop = icon
  grid + taskbar. We translate the DESKTOP icon-grid concept (richer) to a native scrollable home.
- App palette already matches (NeonCyan/Lime/Magenta/Orange in theme/Color.kt); Manrope + IBM
  Plex Mono bundled; `GridBackground` exists (currently dim .06/.03 — source is brighter).
- Current `UploadScreen` = the de-facto lab; it lacks the whole web hero. Rebuild adds hero, keeps
  the working upload/mode/source/recent logic + ViewModel as-is.
