#!/usr/bin/env python3
"""Build the opening and end cards for the EduMind AI demo video (1920x1080)."""
from PIL import Image, ImageDraw, ImageFont

W, H = 1920, 1080
BOLD = "/usr/share/fonts/truetype/noto/NotoSans-Bold.ttf"
ICON = "/home/hatch/workspace/edumind-ai/amazon-listing/edumind-icon-512.png"

def gradient():
    # vertical gradient: dark navy #0F1E3A -> deep purple #4C1D95
    import numpy as np
    top = np.array([15, 30, 58], dtype=float)
    bot = np.array([76, 29, 149], dtype=float)
    t = np.linspace(0, 1, H)[:, None, None]
    row = (top + (bot - top) * t).astype(np.uint8)  # H x 1 x 3
    arr = np.repeat(row, W, axis=1)
    return Image.fromarray(arr, "RGB")

def centered(draw, y, text, font, fill):
    w = draw.textlength(text, font=font)
    draw.text(((W - w) / 2, y), text, font=font, fill=fill)

def card_opening():
    img = gradient()
    d = ImageDraw.Draw(img)
    icon = Image.open(ICON).convert("RGBA").resize((280, 280), Image.LANCZOS)
    img.paste(icon, ((W - 280) // 2, 150), icon)
    centered(d, 500, "Stuck on homework?", ImageFont.truetype(BOLD, 120), (255, 255, 255))
    centered(d, 680, "EduMind AI answers in seconds.", ImageFont.truetype(BOLD, 64), (212, 175, 55))
    img.save("/home/hatch/workspace/edumind-ai/devpost-submission/card-opening.png")

def card_end():
    img = gradient()
    d = ImageDraw.Draw(img)
    icon = Image.open(ICON).convert("RGBA").resize((260, 260), Image.LANCZOS)
    img.paste(icon, ((W - 260) // 2, 120), icon)
    centered(d, 430, "EduMind AI", ImageFont.truetype(BOLD, 130), (255, 255, 255))
    centered(d, 600, "Learn Anything. Solve Everything.", ImageFont.truetype(BOLD, 58), (212, 175, 55))
    centered(d, 730, "Free on Android", ImageFont.truetype(BOLD, 54), (255, 255, 255))
    centered(d, 850, "github.com/shehzadarain252/edumind-ai", ImageFont.truetype(BOLD, 46), (200, 210, 235))
    img.save("/home/hatch/workspace/edumind-ai/devpost-submission/card-end.png")

card_opening()
card_end()
print("cards built")
