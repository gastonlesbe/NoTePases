# GPS Alert feature graphics (1024x500) for ES / EN / PT.
# Plain gradient + vector shapes drawn here (no AI imagery, no emoji, no real map).
import os
import sys
from PIL import Image, ImageDraw, ImageFont, ImageFilter

W, H = 1024, 500
OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "gpsalert_feature_{}.png")
BOLD = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
REG = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
TEAL = (20, 217, 196)
CORAL = (255, 92, 92)

TEXT = {
    "es": ("Alarma de Llegada", "Dormite tranquilo: te avisamos cuando llegás.",
           ["Funciona con la pantalla apagada", "Alarma en pantalla completa", "GPS que ahorra batería"]),
    "en": ("Location Alarm", "Sleep on the bus. We'll wake you on arrival.",
           ["Works with the screen off", "Full-screen alarm on arrival", "Battery-saving GPS"]),
    "pt": ("Alarme de Chegada", "Durma tranquilo: avisamos quando chegar.",
           ["Funciona com a tela apagada", "Alarme em tela cheia", "GPS que economiza bateria"]),
}


def gradient():
    top, bot = (9, 20, 38), (14, 44, 66)
    g = Image.new("RGB", (W, H))
    d = ImageDraw.Draw(g)
    for y in range(H):
        t = y / (H - 1)
        d.line([(0, y), (W, y)], fill=tuple(int(a + (b - a) * t) for a, b in zip(top, bot)))
    return g.convert("RGBA")


def art(im):
    """Right side: route line to a destination pin with radar rings."""
    cx, cy = 820, 230
    glow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    ImageDraw.Draw(glow).ellipse([cx - 170, cy - 150, cx + 170, cy + 190], fill=TEAL + (70,))
    im = Image.alpha_composite(im, glow.filter(ImageFilter.GaussianBlur(60)))
    d = ImageDraw.Draw(im)
    # radar rings around the arrival point
    for r, a in ((150, 60), (110, 100), (70, 150)):
        d.ellipse([cx - r, cy + 70 - r * 0.35, cx + r, cy + 70 + r * 0.35], outline=TEAL + (a,), width=3)
    # dotted route from the bottom-left to the pin
    pts = [(600, 470), (640, 420), (700, 400), (760, 360), (800, 320)]
    for (x0, y0), (x1, y1) in zip(pts, pts[1:]):
        for k in range(0, 10, 2):
            t0, t1 = k / 10, (k + 1) / 10
            d.line([(x0 + (x1 - x0) * t0, y0 + (y1 - y0) * t0), (x0 + (x1 - x0) * t1, y0 + (y1 - y0) * t1)],
                   fill=(220, 235, 245, 200), width=5)
    d.ellipse([588, 458, 612, 482], fill=(220, 235, 245, 255))
    # map pin: circle + triangle, with a white dot
    r = 62
    d.polygon([(cx - r * 0.82, cy - 20), (cx + r * 0.82, cy - 20), (cx, cy + 75)], fill=CORAL)
    d.ellipse([cx - r, cy - r - 40, cx + r, cy + r - 40], fill=CORAL)
    d.ellipse([cx - 24, cy - 64, cx + 24, cy - 16], fill=(255, 255, 255))
    # small alarm bell marks next to the pin
    for sx in (-1, 1):
        x = cx + sx * 95
        d.arc([x - 28, cy - 120, x + 28, cy - 64], start=200 if sx < 0 else 280, end=260 if sx < 0 else 340,
              fill=TEAL, width=6)
        d.arc([x - 44, cy - 136, x + 44, cy - 48], start=200 if sx < 0 else 280, end=260 if sx < 0 else 340,
              fill=TEAL, width=6)
    return im


def check(d, x, y):
    d.ellipse([x, y, x + 26, y + 26], fill=TEAL)
    d.line([(x + 6, y + 14), (x + 11, y + 19), (x + 20, y + 8)], fill=(9, 20, 38), width=4)


def make(lang):
    head, sub, bullets = TEXT[lang]
    im = art(gradient())
    d = ImageDraw.Draw(im)
    x = 56
    d.text((x, 44), "GPS Alert", font=ImageFont.truetype(BOLD, 76), fill=TEAL)
    d.text((x, 134), head, font=ImageFont.truetype(BOLD, 42), fill=(255, 255, 255))
    d.line([(x, 194), (x + 470, 194)], fill=TEAL, width=4)
    fs = ImageFont.truetype(REG, 24)
    d.text((x, 212), sub, font=fs, fill=(205, 220, 235))
    fb = ImageFont.truetype(REG, 27)
    for k, b in enumerate(bullets):
        y = 272 + k * 52
        check(d, x, y + 2)
        d.text((x + 40, y), b, font=fb, fill=(255, 255, 255))
    for txt, f, left in ((sub, fs, x), *[(b, fb, x + 40) for b in bullets], (head, ImageFont.truetype(BOLD, 42), x)):
        right = left + d.textlength(txt, font=f)
        assert right < 640, (lang, txt, right)
    im.convert("RGB").save(OUT.format(lang), optimize=True)
    print(OUT.format(lang))


for lang in sys.argv[1:] or ["es", "en", "pt"]:
    make(lang)
