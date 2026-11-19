# Builds 1080x1920 PT store screenshots from raw phone captures (1080x2340).
# Crops the status bar and the bottom ad/navigation area, adds a caption on top.
import os
import sys
from PIL import Image, ImageDraw, ImageFont, ImageFilter

HERE = os.path.dirname(os.path.abspath(__file__))
RAW = sys.argv[1] if len(sys.argv) > 1 else HERE
BOLD = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
REG = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
TEAL = (20, 217, 196)
W, H = 1080, 1920

SHOTS = [
    ("cap_favs.png", "gpsalert_pt_1_favoritos.png", "Seus destinos favoritos", "Casa, trabalho, rodoviária: a um toque"),
    ("cap_recents.png", "gpsalert_pt_2_ultimos.png", "Últimos destinos", "Repita a viagem sem digitar de novo"),
    ("cap_menu.png", "gpsalert_pt_3_menu.png", "Do seu jeito", "Unidade de medida, aparência e ajuda"),
]
FOOT = "Alarme GPS: acorda você antes do seu ponto"


def bg():
    top, bot = (9, 20, 38), (14, 44, 66)
    g = Image.new("RGB", (W, H))
    d = ImageDraw.Draw(g)
    for y in range(H):
        t = y / (H - 1)
        d.line([(0, y), (W, y)], fill=tuple(int(a + (b - a) * t) for a, b in zip(top, bot)))
    return g


def make(src, dst, title, sub):
    raw = Image.open(os.path.join(RAW, src)).convert("RGB")
    # keep app area: below status bar (y=95) to above the ad banner (y=2050)
    app = raw.crop((0, 95, 1080, 2050))
    ph = 1400
    pw = int(app.width * ph / app.height)
    app = app.resize((pw, ph), Image.LANCZOS)
    im = bg()
    x, y = (W - pw) // 2, 330
    shadow = Image.new("L", (W, H), 0)
    ImageDraw.Draw(shadow).rounded_rectangle([x - 4, y + 10, x + pw + 4, y + ph + 18], 40, fill=160)
    im.paste((0, 0, 0), (0, 0), shadow.filter(ImageFilter.GaussianBlur(18)))
    frame = ImageDraw.Draw(im)
    frame.rounded_rectangle([x - 10, y - 10, x + pw + 9, y + ph + 9], 44, fill=(225, 232, 240))
    mask = Image.new("L", (pw, ph), 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, pw - 1, ph - 1], 36, fill=255)
    im.paste(app, (x, y), mask)
    d = ImageDraw.Draw(im)
    ft, fs, ff = ImageFont.truetype(BOLD, 68), ImageFont.truetype(BOLD, 40), ImageFont.truetype(REG, 34)
    for txt, f, yy, col in ((title, ft, 90, (255, 255, 255)), (sub, fs, 190, TEAL), (FOOT, ff, 1800, (200, 215, 230))):
        w = d.textlength(txt, font=f)
        assert w < W - 60, (txt, w)
        d.text(((W - w) / 2, yy), txt, font=f, fill=col)
    im.save(os.path.join(HERE, dst), optimize=True)
    print(dst)


for s in SHOTS:
    make(*s)
