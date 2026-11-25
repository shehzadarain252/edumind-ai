# EduMind AI — Backend

Secure Gemini AI backend for the EduMind AI Android app (Node.js / Express).

## Security

- The Gemini API key lives **only** in `backend/.env` (git-ignored).
- It is **never** embedded in the Android app, never logged, and never
  returned to any client.
- If the key is missing, every AI endpoint returns HTTP 503
  `{ "error": "AI_NOT_CONFIGURED" }` and the app shows a clear
  "AI not configured" state instead of inventing an answer.

## Setup

```bash
cd backend
npm install
cp .env.example .env
# Edit .env and set GEMINI_API_KEY to your key from Google AI Studio
# (free tier, no billing): https://aistudio.google.com/app/apikey
npm start
```

The server listens on `http://localhost:3000` by default.

## Endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/api/health` | Status + whether AI is configured |
| POST | `/api/chat` | Chat / solve (messages, subject, level, systemHint) |
| POST | `/api/tools` | One of the 20 AI study tools (toolId, input, level) |
| POST | `/api/summarize` | Summarise text (text, style: brief/detailed/chapters) |
| POST | `/api/analyze-image` | Camera/gallery image analysis (imageBase64, mimeType, question) |
| POST | `/api/document` | Multipart document upload (file + task). Tasks: summarize, questions, quiz, ask |

Document extraction: PDF, DOCX, TXT, PPTX (real text extraction) and images
(Gemini vision). Legacy binary `.doc` / `.ppt` files return an honest
`422 UNSUPPORTED_FORMAT` error instead of a fake result. Uploads are held
in memory only (20 MB limit) — no temp files are left on disk.

## ads.txt

`public/ads.txt` contains the publisher declaration for the app's
associated website. It is served at `/ads.txt` when the backend is hosted.
Do not show it as advertising content inside the app.

## Deploying

Host this backend on any Node.js host (Render, Railway, Fly.io, a VPS…),
set the `GEMINI_API_KEY` environment variable there (do not upload `.env`),
then set the app's `BACKEND_URL` in the Android project's
`local.properties` (see the main README.md).
