# Builds EN store screenshots from raw phone captures (1080x2340):
# phone 1080x1920 and tablet 1920x1080 (used for 7" and 10").
# Crops the status bar and the bottom ad/navigation area, adds captions.
# The map capture's real "current distance" is replaced by a demo value (privacy).
import os
import sys
from PIL import Image, ImageDraw, ImageFont, ImageFilter

HERE = os.path.dirname(os.path.abspath(__file__))
RAW = sys.argv[1] if len(sys.argv) > 1 else HERE
BOLD = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
REG = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
POPPINS = "/home/gaston/StudioProjects/NoTePases/app/src/main/res/font/poppins_regular.ttf"
TEAL = (20, 217, 196)
CARD = (11, 29, 51)

SHOTS = [
    ("cap_map.png", "1_map", "Wakes you before your stop", "Pick a destination and an alert radius"),
    ("cap_favs.png", "2_favorites", "Your favorite destinations", "Home, work, the bus terminal: one tap away"),
    ("cap_recents.png", "3_recent", "Recent destinations", "Repeat a trip without typing it again"),
    ("cap_menu.png", "4_menu", "Your way", "Units, appearance and help"),
]
FOOT = "GPS alarm: wake up before your stop"


def bg(w, h):
    top, bot = (9, 20, 38), (14, 44, 66)
    g = Image.new("RGB", (w, h))
    d = ImageDraw.Draw(g)
    for y in range(h):
        t = y / (h - 1)
        d.line([(0, y), (w, y)], fill=tuple(int(a + (b - a) * t) for a, b in zip(top, bot)))
    return g


def load(src):
    raw = Image.open(os.path.join(RAW, src)).convert("RGB")
    if src == "cap_map.png":
        d = ImageDraw.Draw(raw)
        d.rectangle([575, 280, 890, 350], fill=CARD)
        f = ImageFont.truetype(POPPINS, 46)
        t = "1250 m"
        w = d.textlength(t, font=f)
        d.text((733 - w / 2, 283), t, font=f, fill=(255, 255, 255))
    return raw.crop((0, 95, 1080, 2050))


def paste_phone(im, app, x, y, ph):
    pw = int(app.width * ph / app.height)
    app = app.resize((pw, ph), Image.LANCZOS)
    W, H = im.size
    shadow = Image.new("L", (W, H), 0)
    ImageDraw.Draw(shadow).rounded_rectangle([x - 4, y + 10, x + pw + 4, y + ph + 18], 40, fill=160)
    im.paste((0, 0, 0), (0, 0), shadow.filter(ImageFilter.GaussianBlur(18)))
    ImageDraw.Draw(im).rounded_rectangle([x - 10, y - 10, x + pw + 9, y + ph + 9], 44, fill=(225, 232, 240))
    mask = Image.new("L", (pw, ph), 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, pw - 1, ph - 1], 36, fill=255)
    im.paste(app, (x, y), mask)
    return pw


def centered(d, txt, f, cx, y, col, maxw):
    # shrink the font until the line fits
    while d.textlength(txt, font=f) >= maxw:
        f = ImageFont.truetype(f.path, f.size - 2)
    w = d.textlength(txt, font=f)
    d.text((cx - w / 2, y), txt, font=f, fill=col)


def phone(src, name, title, sub):
    W, H = 1080, 1920
    app = load(src)
    im = bg(W, H)
    ph = 1400
    pw = int(app.width * ph / app.height)
    paste_phone(im, app, (W - pw) // 2, 330, ph)
    d = ImageDraw.Draw(im)
    centered(d, title, ImageFont.truetype(BOLD, 68), W / 2, 90, (255, 255, 255), W - 60)
    centered(d, sub, ImageFont.truetype(BOLD, 40), W / 2, 190, TEAL, W - 60)
    centered(d, FOOT, ImageFont.truetype(REG, 34), W / 2, 1800, (200, 215, 230), W - 60)
    out = f"gpsalert_en_{name}.png"
    im.save(os.path.join(HERE, out), optimize=True)
    print(out)


def tablet(src, name, title, sub):
    W, H = 1920, 1080
    app = load(src)
    im = bg(W, H)
    ph = 940
    pw = int(app.width * ph / app.height)
    x = W - pw - 230
    paste_phone(im, app, x, 60, ph)
    d = ImageDraw.Draw(im)
    cx, maxw = (x - 40) / 2 + 20, x - 120
    ft = ImageFont.truetype(BOLD, 64)
    # wrap title to two lines if needed
    words, lines, cur = title.split(), [], ""
    for wd in words:
        t = (cur + " " + wd).strip()
        if d.textlength(t, font=ft) > maxw and cur:
            lines.append(cur); cur = wd
        else:
            cur = t
    lines.append(cur)
    y = 360 - 40 * (len(lines) - 1)
    for ln in lines:
        centered(d, ln, ft, cx, y, (255, 255, 255), maxw); y += 84
    fs = ImageFont.truetype(BOLD, 36)
    centered(d, sub, fs, cx, y + 30, TEAL, maxw)
    centered(d, FOOT, ImageFont.truetype(REG, 30), cx, y + 130, (200, 215, 230), maxw)
    out = f"gpsalert_en_tablet_{name}.png"
    im.save(os.path.join(HERE, out), optimize=True)
    print(out)


for s in SHOTS:
    phone(*s)
    tablet(*s)
