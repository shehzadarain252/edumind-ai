/**
 * EduMind AI — secure Gemini backend (Node.js / Express).
 *
 * SECURITY:
 * - The Gemini API key is read ONLY from the environment (backend/.env).
 *   It is NEVER embedded in the Android app, NEVER logged, and NEVER
 *   returned to any client. The backend/.env file is git-ignored.
 * - If GEMINI_API_KEY is missing, every AI endpoint returns HTTP 503 with
 *   { error: "AI_NOT_CONFIGURED" } and the Android app shows a clear
 *   "AI not configured" state — it never invents a pretend answer.
 * - Uploads are held in memory only (no temp files on disk), with a 20 MB
 *   limit. Document text extraction failures return an honest error
 *   instead of a fake result.
 */
require("dotenv").config();
const express = require("express");
const cors = require("cors");
const multer = require("multer");
const { GoogleGenerativeAI } = require("@google/generative-ai");
const pdfParse = require("pdf-parse");
const mammoth = require("mammoth");
const JSZip = require("jszip");

const PORT = process.env.PORT || 3000;
const GEMINI_API_KEY = process.env.GEMINI_API_KEY || "";
const GEMINI_MODEL = process.env.GEMINI_MODEL || "gemini-2.5-flash";
const MAX_UPLOAD_BYTES = 20 * 1024 * 1024;

const app = express();
app.use(cors());
app.use(express.json({ limit: "10mb" }));
app.use(express.urlencoded({ extended: true, limit: "10mb" }));

// Serve the ads.txt publisher declaration for the associated website.
app.use(express.static("public"));

// ---------- Gemini client (created lazily; never logs the key) ----------
let genAI = null;
function getModel() {
  if (!GEMINI_API_KEY) return null;
  if (!genAI) genAI = new GoogleGenerativeAI(GEMINI_API_KEY);
  return genAI.getGenerativeModel({ model: GEMINI_MODEL });
}

function aiConfigured() {
  return Boolean(GEMINI_API_KEY);
}

// ---------- Rate limiting (simple in-memory, per IP) ----------
const rateWindowMs = 60 * 1000;
const rateMax = 60;
const rateMap = new Map(); // ip -> { count, resetAt }
function rateLimit(req, res, next) {
  const ip = req.ip || "unknown";
  const now = Date.now();
  const entry = rateMap.get(ip);
  if (!entry || now > entry.resetAt) {
    rateMap.set(ip, { count: 1, resetAt: now + rateWindowMs });
    return next();
  }
  entry.count += 1;
  if (entry.count > rateMax) {
    return res.status(429).json({ error: "RATE_LIMITED", message: "Too many requests. Please wait a minute and try again." });
  }
  return next();
}
app.use("/api/", rateLimit);

// ---------- Answer format (every AI response follows this structure) ----------
const ANSWER_FORMAT = `Answer in this exact structure when it fits the question:
1. Direct Answer
2. Detailed Explanation
3. Step-by-Step Solution
4. Relevant Formulas
5. Examples
6. Important Points
7. Final Conclusion
Omit a section only if it truly does not apply. Be accurate and clear. If you are unsure about a fact, say so instead of inventing it. Never invent references, statistics, or quotations.`;

const BASE_SYSTEM = `You are EduMind AI, a friendly and accurate AI tutor for students of all levels (school to postgraduate). ${ANSWER_FORMAT}`;

// ---------- Tool hints (mirrors the Android app's 20 tools) ----------
const TOOL_HINTS = {
  "homework-solver": "Solve the homework question with a clear step-by-step solution and a final answer.",
  "math-solver": "Solve the maths problem step by step, showing every formula and working clearly.",
  "essay-writer": "Write a structured essay with an introduction, body paragraphs and a conclusion. Keep it original and academic.",
  "assignment-assistant": "Produce a clear assignment outline with sections, key points to cover, and suggested ideas.",
  "quiz-generator": "Generate a practice quiz with numbered questions, four options each, and an answer key with brief explanations.",
  "notes-generator": "Convert the input into concise, structured study notes with headings, bullet points and key terms.",
  "flashcard-maker": "Generate flashcards as Q: / A: pairs, one per line, covering the most important facts of the topic.",
  "pdf-summarizer": "Summarise the text clearly: main idea, key points, and a short conclusion. Do not invent content that is not in the text.",
  "grammar-checker": "Correct the grammar and spelling, then list the corrections made. Keep the original meaning unchanged.",
  "translation": "Translate the text accurately into the requested language and note any words that have no direct translation.",
  "exam-prep": "Create a realistic exam preparation plan with daily topics, revision techniques and practice questions.",
  "study-planner": "Create a balanced weekly study schedule with subjects, times and breaks.",
  "concept-explainer": "Explain the concept simply with an everyday analogy, then add the precise definition and an example.",
  "research-assistant": "Produce a research brief: background, key questions, main arguments for and against, and areas to investigate.",
  "programming-assistant": "Provide working code with comments, then explain how it works and how to run it.",
  "code-explainer": "Explain the code line by line in plain language, then summarise what the whole program does.",
  "presentation-outline": "Create a slide-by-slide presentation outline with a title and bullet points for each slide.",
  "question-paper": "Generate an exam-style question paper with sections, marks, and a marking scheme with answers.",
  "vocabulary-builder": "List each word with its meaning, part of speech, and an example sentence.",
  "reading-assistant": "Summarise the passage, list its key ideas, and explain any difficult words or sentences.",
};

function notConfigured(res) {
  return res.status(503).json({
    error: "AI_NOT_CONFIGURED",
    message: "Gemini AI is not configured on this backend. Add GEMINI_API_KEY to backend/.env and restart the server.",
  });
}

async function generateText(prompt, extraParts) {
  const model = getModel();
  const parts = [{ text: prompt }];
  if (extraParts) parts.push(...extraParts);
  const result = await model.generateContent({ contents: [{ role: "user", parts }] });
  return result.response.text();
}

function safeErrorMessage(err) {
  // Never include credentials or raw upstream bodies in the response.
  const msg = (err && err.message) || "Unknown error";
  if (/quota|429|RESOURCE_EXHAUSTED/i.test(msg)) return "The AI service is temporarily busy (quota). Please try again in a little while.";
  if (/api key|API_KEY|401|403/i.test(msg)) return "The AI service rejected the request (key/configuration problem).";
  return "The AI request failed. Please try again.";
}

// ---------- Health ----------
app.get("/api/health", (req, res) => {
  res.json({ status: "ok", aiConfigured: aiConfigured(), model: aiConfigured() ? GEMINI_MODEL : null });
});

// ---------- Chat / solve ----------
app.post("/api/chat", async (req, res) => {
  if (!aiConfigured()) return notConfigured(res);
  const { messages, subject, level, systemHint } = req.body || {};
  if (!Array.isArray(messages) || messages.length === 0) {
    return res.status(400).json({ error: "BAD_REQUEST", message: "messages must be a non-empty array." });
  }
  const history = messages
    .map((m) => `${m.role === "model" ? "EduMind AI" : "Student"}: ${m.text}`)
    .join("\n");
  const context = [
    subject ? `Subject: ${subject}.` : "",
    level ? `Education level: ${level}.` : "",
    systemHint ? `Task: ${systemHint}` : "",
  ].filter(Boolean).join(" ");
  const prompt = `${BASE_SYSTEM}\n${context}\n\nConversation so far:\n${history}\n\nEduMind AI:`;
  try {
    const answer = await generateText(prompt);
    res.json({ answer, subject: subject || null, model: GEMINI_MODEL });
  } catch (err) {
    console.error("chat error:", err.message);
    res.status(502).json({ error: "AI_ERROR", message: safeErrorMessage(err) });
  }
});

// ---------- Study tools ----------
app.post("/api/tools", async (req, res) => {
  if (!aiConfigured()) return notConfigured(res);
  const { toolId, input, level } = req.body || {};
  if (!toolId || typeof input !== "string" || !input.trim()) {
    return res.status(400).json({ error: "BAD_REQUEST", message: "toolId and a non-empty input are required." });
  }
  const hint = TOOL_HINTS[toolId] || "Help the student with this request clearly and accurately.";
  const prompt = `${BASE_SYSTEM}\nTask: ${hint}\n${level ? `Education level: ${level}.` : ""}\n\nStudent input:\n${input.trim()}\n\nEduMind AI:`;
  try {
    const answer = await generateText(prompt);
    res.json({ answer, subject: toolId, model: GEMINI_MODEL });
  } catch (err) {
    console.error("tool error:", err.message);
    res.status(502).json({ error: "AI_ERROR", message: safeErrorMessage(err) });
  }
});

// ---------- Summarize ----------
app.post("/api/summarize", async (req, res) => {
  if (!aiConfigured()) return notConfigured(res);
  const { text, style } = req.body || {};
  if (typeof text !== "string" || !text.trim()) {
    return res.status(400).json({ error: "BAD_REQUEST", message: "text is required." });
  }
  const styleText = style === "chapters"
    ? "Give a chapter-by-chapter summary with the key points of each chapter."
    : style === "brief"
      ? "Give a short summary (a few sentences) plus the three most important points."
      : "Give a detailed summary: main idea, key points, and a short conclusion.";
  const prompt = `${BASE_SYSTEM}\n${styleText}\n\nText to summarise:\n${text.slice(0, 60000)}\n\nEduMind AI:`;
  try {
    const answer = await generateText(prompt);
    res.json({ answer, subject: "summary", model: GEMINI_MODEL });
  } catch (err) {
    console.error("summarize error:", err.message);
    res.status(502).json({ error: "AI_ERROR", message: safeErrorMessage(err) });
  }
});

// ---------- Image analysis (camera scanner / gallery) ----------
app.post("/api/analyze-image", async (req, res) => {
  if (!aiConfigured()) return notConfigured(res);
  const { imageBase64, mimeType, question } = req.body || {};
  if (typeof imageBase64 !== "string" || !imageBase64) {
    return res.status(400).json({ error: "BAD_REQUEST", message: "imageBase64 is required." });
  }
  const mime = mimeType || "image/jpeg";
  const prompt = `${BASE_SYSTEM}\nFirst, read the question in this image carefully (it may be a textbook page, handwritten question, equation, diagram or exam paper). Then solve it using the answer structure above. ${question ? `The student also wrote: ${question}` : ""}\n\nEduMind AI:`;
  try {
    const answer = await generateText(prompt, [
      { inlineData: { mimeType: mime, data: imageBase64 } },
    ]);
    res.json({ answer, subject: null, model: GEMINI_MODEL });
  } catch (err) {
    console.error("analyze-image error:", err.message);
    res.status(502).json({ error: "AI_ERROR", message: safeErrorMessage(err) });
  }
});

// ---------- Document upload ----------
const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: MAX_UPLOAD_BYTES },
});

function extensionOf(filename) {
  return (filename || "").split(".").pop().toLowerCase();
}

async function extractText(buffer, filename, mimeType) {
  const ext = extensionOf(filename);
  if (ext === "txt") return buffer.toString("utf-8");
  if (ext === "pdf") {
    const data = await pdfParse(buffer);
    return data.text || "";
  }
  if (ext === "docx") {
    const result = await mammoth.extractRawText({ buffer });
    return result.value || "";
  }
  if (ext === "pptx") {
    // PPTX is a zip of slide XML — extract <a:t> text runs (real extraction).
    const zip = await JSZip.loadAsync(buffer);
    const slideFiles = Object.keys(zip.files)
      .filter((name) => /^ppt\/slides\/slide\d+\.xml$/.test(name))
      .sort();
    const texts = [];
    for (const name of slideFiles) {
      const xml = await zip.files[name].async("string");
      const runs = [...xml.matchAll(/<a:t>([^<]*)<\/a:t>/g)].map((m) => m[1]);
      if (runs.length) texts.push(runs.join(" "));
    }
    return texts.join("\n");
  }
  if (["jpg", "jpeg", "png", "webp"].includes(ext)) return null; // image -> vision path
  // Legacy binary formats (.doc, .ppt) cannot be read reliably here.
  throw Object.assign(new Error("UNSUPPORTED_FORMAT"), { code: "UNSUPPORTED_FORMAT" });
}

app.post("/api/document", upload.single("file"), async (req, res) => {
  if (!aiConfigured()) return notConfigured(res);
  if (!req.file) {
    return res.status(400).json({ error: "BAD_REQUEST", message: "No file uploaded." });
  }
  const rawTask = String(req.body.task || "summarize");
  const [task, question] = rawTask.split("::");
  const ext = extensionOf(req.file.originalname);
  try {
    const extracted = await extractText(req.file.buffer, req.file.originalname, req.file.mimetype);
    if (extracted === null) {
      // Image document — send it to Gemini vision.
      const prompt = `${BASE_SYSTEM}\nRead this document image carefully, then ${taskDescription(task, question)}\n\nEduMind AI:`;
      const answer = await generateText(prompt, [
        { inlineData: { mimeType: req.file.mimetype || "image/jpeg", data: req.file.buffer.toString("base64") } },
      ]);
      return res.json({ answer, subject: "document", model: GEMINI_MODEL });
    }
    const text = (extracted || "").trim();
    if (!text) {
      return res.status(422).json({
        error: "NO_TEXT_EXTRACTED",
        message: "No readable text could be extracted from this file. It may be a scanned image or an empty document.",
      });
    }
    const prompt = `${BASE_SYSTEM}\n${taskDescription(task, question)}\n\nDocument text:\n${text.slice(0, 60000)}\n\nEduMind AI:`;
    const answer = await generateText(prompt);
    res.json({ answer, subject: "document", model: GEMINI_MODEL });
  } catch (err) {
    if (err.code === "UNSUPPORTED_FORMAT") {
      return res.status(422).json({
        error: "UNSUPPORTED_FORMAT",
        message: `The .${ext} format cannot be read here. Please use a PDF, DOCX, TXT, PPTX or image file.`,
      });
    }
    console.error("document error:", err.message);
    res.status(502).json({ error: "AI_ERROR", message: safeErrorMessage(err) });
  }
});

function taskDescription(task, question) {
  switch (task) {
    case "questions":
      return "Extract the important questions from this document and answer each one.";
    case "quiz":
      return "Generate a practice test from this document with questions and an answer key.";
    case "ask":
      return question
        ? `Answer this question about the document: ${question}`
        : "Answer questions about this document.";
    case "summarize":
    default:
      return "Summarise this document: main idea, key points, and a short conclusion. Do not invent content that is not in the document.";
  }
}

// ---------- Errors ----------
app.use((err, req, res, next) => {
  if (err && err.code === "LIMIT_FILE_SIZE") {
    return res.status(413).json({ error: "FILE_TOO_LARGE", message: "File is larger than 20 MB." });
  }
  console.error("server error:", err && err.message);
  res.status(500).json({ error: "SERVER_ERROR", message: "Something went wrong on the server." });
});

app.listen(PORT, () => {
  console.log(`EduMind AI backend listening on port ${PORT}`);
  console.log(`Gemini AI configured: ${aiConfigured() ? "yes" : "NO — set GEMINI_API_KEY in backend/.env"}`);
  if (aiConfigured()) console.log(`Gemini model: ${GEMINI_MODEL}`);
});
