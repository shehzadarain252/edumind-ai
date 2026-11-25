#!/usr/bin/env python3
"""Build the EduMind AI demo video from listing screenshots + TTS narration."""
import math, os, subprocess, sys

BASE = "/home/hatch/workspace/edumind-ai/devpost-submission"
LISTING = "/home/hatch/workspace/edumind-ai/amazon-listing"
SEG = os.path.join(BASE, "segments")
NAR = os.path.join(BASE, "narration")
FONT = "/usr/share/fonts/truetype/noto/NotoSans-Bold.ttf"

SEGMENTS = [
    # (image, narration, on-screen title or None)
    (os.path.join(BASE, "card-opening.png"), "nar-01-hook.mp3", None),
    (os.path.join(LISTING, "screenshot-01-home.png"), "nar-02-home.mp3", "Home \u2014 No sign-up needed"),
    (os.path.join(LISTING, "screenshot-02-ask-ai.png"), "nar-03-askai.mp3", "Ask AI \u2014 Powered by Gemini"),
    (os.path.join(LISTING, "screenshot-03-subjects.png"), "nar-04-subjects.mp3", "16 Subjects \u2014 School to University"),
    (os.path.join(LISTING, "screenshot-04-study-tools.png"), "nar-05-tools.mp3", "20 AI Study Tools \u2014 Scan it or upload it"),
    (os.path.join(LISTING, "screenshot-05-profile.png"), "nar-06-profile.mp3", "Free \u2014 Guest access, your way"),
    (os.path.join(BASE, "card-end.png"), "nar-07-close.mp3", None),
]

def run(cmd, **kw):
    print("+", " ".join(cmd[:6]), "..." if len(cmd) > 6 else "")
    r = subprocess.run(cmd, capture_output=True, text=True, **kw)
    if r.returncode != 0:
        print(r.stdout[-2000:])
        print(r.stderr[-3000:])
        sys.exit(f"FAILED: {' '.join(cmd[:4])}")
    return r

def probe_dur(path):
    r = run(["ffprobe", "-v", "error", "-show_entries", "format=duration",
             "-of", "csv=p=0", path])
    return float(r.stdout.strip())

os.makedirs(SEG, exist_ok=True)

# --- title text files (no trailing newline) ---
title_files = []
for i, (_, _, title) in enumerate(SEGMENTS, 1):
    if title:
        p = os.path.join(BASE, f"title-{i:02d}.txt")
        open(p, "w", encoding="utf-8").write(title)
        title_files.append(p)
    else:
        title_files.append(None)

seg_durs, seg_frames = [], []
for i, (img, nar, title) in enumerate(SEGMENTS, 1):
    nar_dur = probe_dur(os.path.join(NAR, nar))
    frames = math.ceil((nar_dur + 0.5) * 24)
    seg_dur = frames / 24.0
    seg_durs.append(seg_dur)
    seg_frames.append(frames)
    print(f"seg{i}: nar={nar_dur:.3f}s frames={frames} dur={seg_dur:.4f}s")

# --- render video segments (Ken Burns zoompan + optional static title) ---
vf_base = ("scale=2112:1188,zoompan=z='1+0.0002*on':x='iw/2-(iw/zoom/2)'"
           ":y='ih/2-(ih/zoom/2)':d=1:s=1920x1080:fps=24")
for i, (img, nar, title) in enumerate(SEGMENTS, 1):
    out = os.path.join(SEG, f"seg{i:02d}.mp4")
    vf = vf_base
    if title_files[i - 1]:
        vf += (f",drawtext=fontfile={FONT}:textfile={title_files[i-1]}"
               f":fontsize=46:fontcolor=white:box=1:boxcolor=black@0.55"
               f":boxborderw=24:x=(w-text_w)/2:y=h-110")
    run(["ffmpeg", "-v", "error", "-y", "-loop", "1", "-framerate", "24", "-i", img,
         "-vf", vf, "-frames:v", str(seg_frames[i - 1]),
         "-c:v", "libx264", "-pix_fmt", "yuv420p", "-r", "24", out])
    print(f"seg{i} video done")

# --- audio: normalize each narration to 48kHz mono, pad to segment duration ---
wav_list = os.path.join(BASE, "audio-list.txt")
with open(wav_list, "w") as f:
    for i, (img, nar, title) in enumerate(SEGMENTS, 1):
        wav = os.path.join(SEG, f"seg{i:02d}.wav")
        run(["ffmpeg", "-v", "error", "-y", "-i", os.path.join(NAR, nar),
             "-af", f"aresample=48000,apad=whole_dur={seg_durs[i-1]:.4f}",
             "-ac", "1", "-ar", "48000", wav])
        f.write(f"file '{wav}'\n")
full_wav = os.path.join(BASE, "audio-full.wav")
run(["ffmpeg", "-v", "error", "-y", "-f", "concat", "-safe", "0", "-i", wav_list,
     "-c", "copy", full_wav])
full_aac = os.path.join(BASE, "audio-full.m4a")
run(["ffmpeg", "-v", "error", "-y", "-i", full_wav, "-c:a", "aac", "-b:a", "192k", full_aac])

# --- concat video segments, mux with audio ---
vid_list = os.path.join(BASE, "video-list.txt")
with open(vid_list, "w") as f:
    for i in range(1, len(SEGMENTS) + 1):
        f.write(f"file '{os.path.join(SEG, f'seg{i:02d}.mp4')}'\n")
video_only = os.path.join(BASE, "video-only.mp4")
run(["ffmpeg", "-v", "error", "-y", "-f", "concat", "-safe", "0", "-i", vid_list,
     "-c", "copy", video_only])
final = os.path.join(BASE, "edumind-ai-demo-video.mp4")
run(["ffmpeg", "-v", "error", "-y", "-i", video_only, "-i", full_aac,
     "-c:v", "copy", "-c:a", "copy", "-shortest", final])
r = run(["ffprobe", "-v", "error", "-show_entries",
         "format=duration:stream=codec_name,width,height,r_frame_rate",
         "-of", "default=noprint_wrappers=1", final])
print(r.stdout)
print("TOTAL video seconds:", sum(seg_durs))
print("DONE:", final)
