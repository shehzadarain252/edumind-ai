# EduMind AI — Demo Video Script (Devpost / Global AI Builder's Challenge)

- **Video:** `edumind-ai-demo-video.mp4` — 1920×1080, 24fps, H.264 + AAC, ~2:00–2:30
- **Voice:** one narrator throughout — Helio (`avocado_v2:MAI_03`), speed 100, English
- **Visuals:** ONLY the real listing assets in `~/workspace/edumind-ai/amazon-listing/`
  (5 screenshots + icon + banner). No generated app UI, no fake screens.
- **Honesty rules:** only features that exist in the app. No invented numbers.
  (The profile screenshot shows sample stats — never quote them as real user data.)

## Segments

### 1 — Hook (opening card, PIL-built)
- **Visual:** dark blue/purple card, app icon, "Stuck on homework?" + "EduMind AI answers in seconds."
- **On-screen title:** baked into the card.
- **Narration:** "Every day, students get stuck. On homework. On a concept they have read three times and still do not understand. On a page that makes no sense at all. EduMind AI answers in seconds. It is free, and it is built for school, college and university students."

### 2 — Home (screenshot-01-home.png)
- **On-screen title:** "Home — No sign-up needed"
- **Narration:** "Open EduMind AI, and there is no account to create, no sign-up wall in your way. Just learning. Sixteen subjects, from mathematics to physics, biology, English and history, all in one place. Pick up where you left off, and let your daily goals keep you on track. Five tabs at the bottom keep everything one tap away: Home, Ask AI, Subjects, Study Tools, and Profile."

### 3 — Ask AI (screenshot-02-ask-ai.png)
- **On-screen title:** "Ask AI — Powered by Gemini"
- **Narration:** "This is the heart of the app. Ask anything, in your own words, and your AI tutor, Lumina, explains it step by step, in plain language, with examples you can actually use. No confusing symbols. And if you still do not get it, just ask again. It remembers the conversation, so every answer builds on the last one."

### 4 — Subjects (screenshot-03-subjects.png)
- **On-screen title:** "16 Subjects — School to University"
- **Narration:** "Sixteen subjects, one app. From school maths to university medicine. Every subject has its own tools, and you learn at your own pace, from school level right up to university level."

### 5 — Study tools, camera scanner, document upload (screenshot-04-study-tools.png)
- **On-screen title:** "20 AI Study Tools — Scan it or upload it"
- **Narration:** "Twenty built-in study tools carry you from homework to exam revision. Homework Solver, Maths Solver, Essay Writer, Quiz Generator, Notes Generator, Flashcard Maker, and fourteen more. Turn any topic into a quiz. Memorise key facts with flashcards. Summarise long notes in seconds. Stuck on a printed question? Point the camera at it. Got a PDF, a Word document or a photo of your notes? Upload it, and ask questions about it."

### 6 — Profile / guest access (screenshot-05-profile.png)
- **On-screen title:** "Free — Guest access, your way"
- **Narration:** "And it is yours, your way. Start as a guest, switch between light, dark and system themes, and keep your streak alive as you earn XP. Save your favourite answers, keep your chat history, and revise later. It is free to use, with ads only at natural breaks, never while you are reading an answer."

### 7 — Close (end card, PIL-built)
- **Visual:** dark blue/purple card, app icon, "EduMind AI", "Learn Anything. Solve Everything.", "Free on Android", "github.com/shehzadarain252/edumind-ai"
- **On-screen title:** baked into the card.
- **Narration:** "EduMind AI. Learn anything. Solve everything. Download it free from the GitHub release page. The link is below."

## Build notes
- Each video segment = its narration file's measured duration (rounded up to 0.1s) + 0.5s tail.
- Ken Burns: slow zoompan (zoom 1.00 → ~1.10) on every visual; cards get the same treatment.
- Audio: normalize every narration file to 48kHz before concatenating (never resample across concat boundaries).
- Verify: ffprobe final (120–180s, 1920×1080, 24fps, H.264 + AAC); read one rendered frame to check title readability; Whisper-transcribe the first 10s of mixed audio to confirm narration is present.
