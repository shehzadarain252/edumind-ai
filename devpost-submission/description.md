# EduMind AI — Devpost Submission Description
### Global AI Builder's Challenge (21–28 November 2026)
### PREPARATION ONLY — do not submit until the Devpost account is reinstated

---

## Project description (paste into Devpost)

**EduMind AI — Learn Anything. Solve Everything.**

EduMind AI is a free Android app that helps any student learn anything. Pick from
16 subjects, ask the Gemini-powered AI tutor in plain language, scan a printed
question with the camera, or upload a document and ask questions about it. Twenty
built-in study tools — quizzes, summaries, flashcards and more — turn one app into
a full study companion, with guest access so there is no sign-up wall.

**The problem:** every day, students get stuck — on homework, on a concept they
have read three times and still do not understand, on a page that makes no sense.
Private tutoring is expensive and not available to everyone. EduMind AI gives every
student a patient tutor in their pocket, for free.

**How AI is used:** the app's AI tutor, Lumina, is powered by Google Gemini
(gemini-3.5-flash-lite) through a secure Node.js backend hosted on Render. The
Gemini API key lives on the backend only — it is never embedded in the APK. Every
answer follows a clear structure: direct answer first, then a simple explanation,
step-by-step working, formulas where they matter, examples, and the key points to
remember. If the backend or key is missing, the app says so clearly instead of
inventing an answer.

**What it does:**
- Ask AI — chat with Lumina, your AI tutor, in plain language, with follow-up
  questions and chat history
- 16 subjects — Mathematics, English and Languages, Physics, Chemistry, Biology,
  Computer Science and IT, Artificial Intelligence, Engineering, Business and
  Management, Accounting and Finance, Economics, Medical and Health Sciences,
  Social Sciences, History and Geography, Law and Legal Studies, and Other
  Academic Subjects — from school level to university level
- 20 AI study tools — Homework Solver, Mathematics Solver, Essay Writer,
  Assignment Assistant, Quiz Generator, Notes Generator, Flashcard Maker, PDF
  Summarizer, Grammar Checker, Translation, Exam Preparation, Study Planner,
  Concept Explainer, Research Assistant, Programming Assistant, Code Explainer,
  Presentation Outline Generator, Question Paper Generator, Vocabulary Builder,
  Reading Assistant
- Camera scanner — point the camera at a printed question
- Document upload — upload a PDF, Word document or slides and ask questions
  about it (processed in memory only, never stored on our servers)
- Guest access — no account or login needed; light, dark and system themes;
  saved answers, favourites, daily goals, streaks and XP
- 25 screens, bottom navigation only, Material 3, premium blue / purple / white

**Built with:** Android (Kotlin + Jetpack Compose), Google Gemini AI,
Node.js / Express backend on Render, Firebase Analytics + Crashlytics, AdMob.

**Try it:** the app is already built and live. Download the APK from the GitHub
release below and watch the 2-minute demo video.

## What was improved for this challenge

<!-- TODO — FILL IN DURING 21–28 NOV 2026 BEFORE SUBMITTING.
     Describe the real improvement made to the existing app during the
     challenge week, e.g. onboarding / study-tool flow changes. Do not
     invent anything — only what was actually changed and released. -->
[PENDING — to be filled in after the improvement is made during Nov 21–28, 2026]

## Development disclosure

EduMind AI is an existing project, first built in November 2026 and already
public on GitHub (latest release v1.0.2). For this challenge, the existing app
was improved during the event window (see "What was improved for this
challenge" above), and a new demo video was recorded for this submission.
No part of the app was built by another person or team for this submission.

## AI disclosure

AI is used in two ways in this project. First, the product itself is AI-powered:
the in-app tutor and all 20 study tools call Google Gemini (gemini-3.5-flash-lite)
through our own backend. Second, AI assistants were used during development to
help write code, documentation and this submission text; all code was reviewed,
and all claims in this description were checked against the real app.

---

## Submission checklist (for the parent agent — do NOT submit from here)

- [ ] Devpost account reinstated (appeal email sent 2026-11-13; check reply)
- [ ] Register for the Global AI Builder's Challenge (deadline Nov 28, 2026, 3:00 PM PST)
- [ ] Make the real improvement during Nov 21–28 and fill in the placeholder above
- [ ] GitHub repo link: https://github.com/shehzadarain252/edumind-ai
- [ ] Demo video: `edumind-ai-demo-video.mp4` in this folder (2–3 min)
- [ ] Backend (live): https://edumind-ai-jbv7.onrender.com
- [ ] Latest release: v1.0.2 (2026-11-13)
