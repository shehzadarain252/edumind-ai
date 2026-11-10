# EduMind AI — Android App

**EduMind AI — "Learn Anything. Solve Everything."**

A complete, production-ready Android education application (Kotlin + Jetpack
Compose, Material 3). Students can ask any academic question, scan textbook
questions with the camera, upload documents, and use 20 AI study tools —
all powered by Google Gemini through a secure backend.

- **Platform:** Android (minSdk 26, targetSdk 34)
- **UI:** Jetpack Compose, Material 3, light + dark + system theme
- **Navigation:** bottom navigation only (Home, Ask AI, Subjects, Study Tools,
  Profile). There is deliberately **no sidebar / navigation drawer** and no
  options menu inside a sidebar.
- **AI:** Google Gemini via the secure backend in `backend/` — the Gemini API
  key is **never** embedded in the APK.
- **Ads:** Google AdMob (interstitial + rewarded only — no banner ads).

## Project structure

```
edumind-ai/
├── app/                          Android app (Kotlin + Compose)
│   ├── build.gradle.kts          Dependencies (Compose, CameraX, Retrofit,
│   │                             Room, DataStore, Mobile Ads, UMP, Coil)
│   └── src/main/
│       ├── AndroidManifest.xml   Permissions + AdMob App ID (only one registered)
│       ├── java/com/edumind/ai/
│       │   ├── MainActivity.kt / EduMindApp.kt
│       │   ├── data/             SubjectsData (16 categories), ToolsData (20 tools),
│       │   │                     Models, SettingsRepository, Room DB, Retrofit API
│       │   ├── ads/              AdMobManager (interstitial + rewarded), ConsentManager (UMP)
│       │   ├── ui/theme/         Premium blue/purple/white, light + dark
│       │   ├── ui/navigation/    Routes + NavHost (bottom bar only)
│       │   ├── ui/screens/       All 25 screens (see below)
│       │   ├── ui/components/    AnswerView, RobotMascot, shared components
│       │   ├── ui/viewmodel/     AskAi, Tools, Scanner, Document, Planner, Settings
│       │   └── util/             AnswerFormatter, FileUtils, ImageUtils
│       └── res/                  strings, themes, colors, launcher icon, file provider
├── backend/                      Secure Gemini backend (Node.js / Express)
│   ├── server.js                 /api/health, /api/chat, /api/tools,
│   │                             /api/summarize, /api/analyze-image, /api/document
│   ├── .env.example              Placeholder — copy to .env and set your key
│   ├── public/ads.txt            Publisher declaration for the associated website
│   └── README.md                 Backend setup + endpoints
└── README.md                     This file
```

## Screens (all 25)

Splash, Onboarding, Home Dashboard, Ask AI, Camera Scanner, Gallery Upload,
Document Upload, All Subjects, Subject Details, AI Study Tools, 20 individual
Tool screens, AI Chat History, Saved Answers, Favorites, Study Planner, User
Settings, Help and Support, Feedback, Privacy Policy, Terms and Conditions,
Disclaimer, About Us, Contact Us, FAQ, AI Usage Guidelines.

Guest access — no account or login is required for basic AI features.

## Subjects (16 categories)

Mathematics, English and Languages, Physics, Chemistry, Biology, Computer
Science and IT, Artificial Intelligence, Engineering, Business and
Management, Accounting and Finance, Economics, Medical and Health Sciences,
Social Sciences, History and Geography, Law and Legal Studies, Other Academic
Subjects. Every subcategory from the specification is in
`data/SubjectsData.kt` — add new subjects there without rewriting the app.

## AI study tools (20)

Homework Solver, Mathematics Solver, Essay Writer, Assignment Assistant, Quiz
Generator, Notes Generator, Flashcard Maker, PDF Summarizer, Grammar Checker,
Translation, Exam Preparation, Study Planner, Concept Explainer, Research
Assistant, Programming Assistant, Code Explainer, Presentation Outline
Generator, Question Paper Generator, Vocabulary Builder, Reading Assistant.
Each tool has its own input screen, loading state and output screen wired to
the backend. See `data/ToolsData.kt`.

## 1. Open in Android Studio

1. Open Android Studio (Hedgehog or newer, with JDK 17).
2. **Open** the `edumind-ai` folder (the Gradle project root).
3. Let Gradle sync. If it asks, accept the Gradle wrapper (8.7) and AGP 8.5.2.
4. Run on an emulator (API 26+) or a real device.

## 2. Set up the Gemini backend (required for AI answers)

The app never contains a Gemini API key. The key lives on the backend only.

```bash
cd backend
npm install
cp .env.example .env
# Edit .env and set GEMINI_API_KEY.
# Get a free key at Google AI Studio (free tier, no billing):
#   https://aistudio.google.com/app/apikey
npm start
```

The backend listens on `http://localhost:3000`. For a real device, host the
backend somewhere reachable (Render, Railway, Fly.io, a VPS…) and set the
`GEMINI_API_KEY` environment variable on the host — do **not** upload `.env`.

Check it works: open `http://localhost:3000/api/health` — it should report
`"aiConfigured": true`.

If the key is missing, every AI endpoint returns HTTP 503
`AI_NOT_CONFIGURED` and the app shows a clear "AI is not configured" state.
It never invents a pretend answer, and it never pretends a document was
processed when extraction or AI fails.

## 3. Point the app at your backend

In the Android project root (`edumind-ai/`), create or edit
`local.properties` and add:

```
BACKEND_URL=https://your-backend.example.com
```

(For the Android emulator talking to a backend on your own computer, use
`http://10.0.2.2:3000`.) The default placeholder URL is
`https://your-backend.example.com` — with that value the app shows the
"AI not configured" state instead of calling a fake endpoint.

## 4. AdMob setup

- The **primary AdMob App ID is already registered** in
  `AndroidManifest.xml` (`com.google.android.gms.ads.APPLICATION_ID`). It is
  the **only** App ID registered in the app.
- Interstitial and rewarded ad unit IDs are wired in `ads/AdMobManager.kt`.
- **Additional App ID provided by the owner is reference-only.** It appears
  only as a code comment in `AdMobManager.kt`. Do **not** register it in the
  manifest or anywhere else in the app. Confirm the correct App ID in the
  AdMob dashboard before release.
- **Test ads in debug builds:** when `BuildConfig.DEBUG` is true, the app
  uses Google's official test ad unit IDs automatically. Real ad units are
  used only in release builds. Never click live ads during testing.
- **Rules enforced in code:** no banner ads; interstitials load before they
  are needed, show only at natural transitions (leaving a tool screen), never
  during scanning, AI generation or uploads, with a 3-minute frequency limit;
  rewarded ads are voluntary only, never required for essential answers, with
  the reward verified by the SDK callback.
- **UMP consent:** `ads/ConsentManager.kt` requests consent info on first
  launch and shows the consent form where required. Users can reopen it from
  Profile → "Ad Choices / Privacy Choices".
- **ads.txt:** `backend/public/ads.txt` contains the publisher declaration
  for the app's associated website. Serve it at `/ads.txt` when the backend
  is hosted. Do not show it as advertising content inside the app.

## 5. Build a debug APK

In Android Studio: **Build → Build App Bundle(s) / APK(s) → Build APK(s)**,
or from the terminal:

```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

Install it on a device to test. Debug builds use test ads.

## 6. Build a signed release AAB (for Google Play)

1. In Android Studio: **Build → Generate Signed App Bundle / APK**.
2. Create a **new keystore** (or use your existing one) and keep it safe —
   you need the same keystore for every future update. Never share its
   password or file publicly.
3. Choose **Android App Bundle**, build type **release**.
4. Output: `app/build/outputs/bundle/release/app-release.aab`.

## 7. Publish order (Play Console first)

1. **Google Play Console first:** create a Google Play developer account
   (one-time $25 fee — the account owner must create it; it needs their
   payment and identity verification). Create the app, upload the signed
   release **AAB**, fill in the store listing, content rating, privacy
   policy and data-safety form, then submit for review.
2. **Then Amazon Appstore / other stores** (if desired) using the same
   release build. Google does not restrict publishing on other stores.
3. Before release, confirm the correct AdMob App ID in the AdMob dashboard
   and link the app there.

## What still needs the owner

- **Gemini API key:** set `GEMINI_API_KEY` in `backend/.env` (or as an
  environment variable on the host). Without it, AI answers show the
  "AI not configured" state.
- **Backend hosting + `BACKEND_URL`** in `local.properties`.
- **Google Play developer account** ($25, owner-created) for publishing.
- **AdMob dashboard:** confirm the correct App ID and create/verify the ad
  units before release. (Test ads are used automatically in debug builds.)

## Honesty rules built into the app

- No fake buttons, no dummy AI responses, no simulated uploads.
- If the backend or the Gemini key is missing, the app says so clearly.
- If document text extraction fails, the app reports the failure honestly.
- AI answers follow the structure: Direct Answer, Detailed Explanation,
  Step-by-Step Solution, Formulas, Examples, Important Points, Final
  Conclusion — and the Disclaimer states that AI answers may contain errors
  and should be verified for important academic work.
- Contact / Feedback forms validate input and store messages in an on-device
  outbox. No fake contact details are shown anywhere in the app.
