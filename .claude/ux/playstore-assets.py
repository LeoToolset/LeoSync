# Play-Store-Assets LeoSync (UX, SYNC-20261114-327): rahmt vorhandene Emulator-Screenshots mit Schlagzeile.
# Aufruf: python -I playstore-assets.py   (Ausgabe: .claude/AppScreenshots/playstore/phone_de|phone_en|feature)
import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter
ROOT = r"C:\Users\rbald\Documents\Sync2PC\.claude\AppScreenshots"
SRC = ROOT + r"\website"
OUT = ROOT + r"\playstore"
ICON = OUT + r"\playstore_icon_512.png"
BOLD = r"C:\Windows\Fonts\segoeuib.ttf"
REG = r"C:\Windows\Fonts\segoeui.ttf"
# (nr, Quelle DE, Quelle EN, Schlagzeile DE, Schlagzeile EN)
MOTIVE = [
 (1, "syncing_bright_ger", "syncing_bright_en", "Fotos und Dateien sicher auf deinen PC", "Photos and files safely on your PC"),
 (2, "all_synced_bright_ger", "all_synced_bright_en", "Alles gesichert. Leo wacht darüber.", "All backed up. Leo keeps watch."),
 (3, "new_profile_bright_ger", "new_profile_bright_en", "In wenigen Schritten eingerichtet", "Set up in just a few steps"),
 (4, "profiles_tools_bright_ger", "profiles_tools_bright_en", "Jeder Ordner mit eigenem Profil", "One profile for every folder"),
 (5, "schutzbremse_bright_de", "schutzbremse_bright_en", "Schutz vor versehentlichem Löschen", "Safeguard against accidental deletion"),
 (6, "papierkorb_wiederherstellung_bright_de", "papierkorb_wiederherstellung_bright_en", "Gelöschtes jederzeit zurückholen", "Restore deleted files anytime"),
 (7, "verschluesselung_bright_ger", "verschluesselung_bright_en", "Verschlüsselt in deinem Heimnetz", "Encrypted on your home network"),
 (8, "verbindungsfehler_bright_de", "verbindungsfehler_bright_en", "Klare Hilfe, wenn etwas hakt", "Clear help when something goes wrong"),
]
def gradient(w, h):
    g = Image.new("RGB", (1, h))
    for y in range(h):
        t = y/(h-1)
        g.putpixel((0, y), tuple(int(a+(b-a)*t) for a, b in zip((0x4A,0x86,0xC8),(0x2B,0x57,0x92))))
    return g.resize((w, h)).convert("RGBA")
def wrap(draw, text, font, maxw):
    lines, cur = [], ""
    for wd in text.split():
        t = (cur+" "+wd).strip()
        if draw.textlength(t, font=font) <= maxw: cur = t
        else: lines.append(cur); cur = wd
    lines.append(cur); return lines
def frame(src, headline, hero, path):
    W, H = 1080, 1920
    img = gradient(W, H); d = ImageDraw.Draw(img)
    y = 80
    if hero:
        ic = Image.open(ICON).convert("RGBA").resize((200, 200), Image.LANCZOS)
        m = Image.new("L", (200,200), 0); ImageDraw.Draw(m).rounded_rectangle([0,0,199,199], radius=46, fill=255)
        img.paste(ic, ((W-200)//2, y), m); y += 240
    font = ImageFont.truetype(BOLD, 80 if hero else 74)
    for ln in wrap(d, headline, font, 940):
        w = d.textlength(ln, font=font)
        d.text(((W-w)/2, y), ln, font=font, fill="white"); y += 96
    y += 50
    shot = Image.open(os.path.join(SRC, src + ".jpg")).convert("RGBA")
    dw = 860; dh = int(shot.height*dw/shot.width)
    shot = shot.resize((dw, dh), Image.LANCZOS)
    bez = 14; r = 70
    dev = Image.new("RGBA", (dw+2*bez, dh+2*bez), (0,0,0,0))
    ImageDraw.Draw(dev).rounded_rectangle([0,0,dev.width-1,dev.height-1], radius=r+bez, fill=(18,32,56,255))
    mk = Image.new("L", shot.size, 0); ImageDraw.Draw(mk).rounded_rectangle([0,0,dw-1,dh-1], radius=r, fill=255)
    dev.paste(shot, (bez, bez), mk)
    x = (W-dev.width)//2
    sh = Image.new("RGBA", (W, H), (0,0,0,0))
    sh.paste((5,15,40,140), box=(x, y+14), mask=dev.split()[3]); sh = sh.filter(ImageFilter.GaussianBlur(22))
    img.alpha_composite(sh); img.alpha_composite(dev, (x, y))
    img.convert("RGB").save(path, "PNG", optimize=True)
def feature(lang, headline, sub, path):
    W, H = 1024, 500
    img = gradient(W, H); d = ImageDraw.Draw(img)
    ic = Image.open(ICON).convert("RGBA").resize((300, 300), Image.LANCZOS)
    m = Image.new("L", (300,300), 0); ImageDraw.Draw(m).rounded_rectangle([0,0,299,299], radius=68, fill=255)
    img.paste(ic, (70, 100), m)
    f1 = ImageFont.truetype(BOLD, 84); f2 = ImageFont.truetype(REG, 38)
    d.text((410, 150), "LeoSync", font=f1, fill="white")
    y = 262
    for ln in wrap(d, sub, f2, 570):
        d.text((414, y), ln, font=f2, fill=(225,236,250)); y += 50
    img.convert("RGB").save(path, "PNG", optimize=True)
for lang, idx in (("de", 1), ("en", 2)):
    od = os.path.join(OUT, "phone_"+lang); os.makedirs(od, exist_ok=True)
    for m in MOTIVE:
        frame(m[idx], m[idx+2], m[0] == 1, os.path.join(od, "%02d.png" % m[0]))
fd = os.path.join(OUT, "feature"); os.makedirs(fd, exist_ok=True)
feature("de", "LeoSync", "Fotos und Dateien sicher auf deinen eigenen PC", os.path.join(fd, "feature_1024x500_de.png"))
feature("en", "LeoSync", "Back up photos and files to your own PC", os.path.join(fd, "feature_1024x500_en.png"))
print("fertig")