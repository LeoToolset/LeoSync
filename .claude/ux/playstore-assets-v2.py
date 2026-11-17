# Play-Store-Phone-Bilder im Stil des neuen Feature-Bildes (UX, SYNC-20261114-331)
# Hintergrund background2.png, Teil der Bilder im 3D-Smartphone (smartphone_3d.png, Bildschirm = Magenta-Flaeche)
import os, sys, numpy as np
from PIL import Image, ImageDraw, ImageFont, ImageFilter, ImageChops
ROOT = r"C:\Users\rbald\Documents\Sync2PC\.claude\AppScreenshots"
SRC = ROOT + r"\website"; OUT = ROOT + r"\playstore"; TMP = os.environ.get("LS_OUT", OUT)
BG = Image.open(OUT + r"\background2.png").convert("RGB").resize((1080, 1920), Image.LANCZOS)
PHONE = Image.open(OUT + r"\smartphone_3d.png").convert("RGB")
ICON = Image.open(OUT + r"\playstore_icon_512.png").convert("RGBA")
BOLD = r"C:\Windows\Fonts\segoeuib.ttf"
# (Nr, 3D?, Quelle DE, Quelle EN, Schlagzeile DE, Schlagzeile EN)
MOTIVE = [
 (1, True,  "syncing_bright_ger", "syncing_bright_en", "Fotos und Dateien sicher auf deinen PC", "Photos and files safely on your PC"),
 (2, False, "all_synced_bright_ger", "all_synced_bright_en", "Alles gesichert. Leo wacht darüber.", "All backed up. Leo keeps watch."),
 (3, True,  "new_profile_bright_ger", "new_profile_bright_en", "In wenigen Schritten eingerichtet", "Set up in just a few steps"),
 (4, False, "profiles_tools_bright_ger", "profiles_tools_bright_en", "Jeder Ordner mit eigenem Profil", "One profile for every folder"),
 (5, True,  "schutzbremse_bright_de", "schutzbremse_bright_en", "Schutz vor versehentlichem Löschen", "Safeguard against accidental deletion"),
 (6, False, "papierkorb_wiederherstellung_bright_de", "papierkorb_wiederherstellung_bright_en", "Gelöschtes jederzeit zurückholen", "Restore deleted files anytime"),
 (7, True,  "verschluesselung_bright_ger", "verschluesselung_bright_en", "Verschlüsselt in deinem Heimnetz", "Encrypted on your home network"),
 (8, False, "verbindungsfehler_bright_de", "verbindungsfehler_bright_en", "Klare Hilfe, wenn etwas hakt", "Clear help when something goes wrong"),
]
# ---- 3D-Telefon vorbereiten ----
_arr = np.array(PHONE).astype(int)
_mag = (_arr[...,0] > 200) & (_arr[...,1] < 80) & (_arr[...,2] > 200)
_ys, _xs = np.where(_mag); SX0, SX1, SY0, SY1 = _xs.min(), _xs.max()+1, _ys.min(), _ys.max()+1
_screen_mask = Image.fromarray((_mag*255).astype("uint8")).filter(ImageFilter.MaxFilter(5))
def phone_with(shot_path):
    shot = Image.open(shot_path).convert("RGB")
    w, h = SX1-SX0, SY1-SY0
    sc = max(w/shot.width, h/shot.height)
    shot = shot.resize((int(shot.width*sc)+1, int(shot.height*sc)+1), Image.LANCZOS)
    shot = shot.crop((0, 0, w, h))
    base = PHONE.copy(); layer = Image.new("RGB", PHONE.size, (0,0,0)); layer.paste(shot, (SX0, SY0))
    base.paste(layer, (0,0), _screen_mask)
    # Silhouette: abgerundetes Rechteck + Tasten
    S = 4; m = Image.new("L", (PHONE.width*S, PHONE.height*S), 0); d = ImageDraw.Draw(m)
    d.rounded_rectangle([177*S, 93*S, 821*S, 1469*S], radius=118*S, fill=255)
    for (y0, y1) in ((352, 546), (600, 690)):
        d.rounded_rectangle([812*S, y0*S, 842*S, y1*S], radius=6*S, fill=255)
    m = m.resize(PHONE.size, Image.LANCZOS)
    rgba = base.convert("RGBA"); rgba.putalpha(m)
    return rgba.crop((170, 86, 850, 1476))
def shadow_paste(canvas, img, xy, blur=28, off=(0, 26), alpha=150):
    sh = Image.new("RGBA", canvas.size, (0,0,0,0))
    sh.paste((0, 20, 70, alpha), box=(xy[0]+off[0], xy[1]+off[1]), mask=img.split()[3])
    canvas.alpha_composite(sh.filter(ImageFilter.GaussianBlur(blur)))
    canvas.alpha_composite(img, xy)
def wrap(d, text, font, maxw):
    lines, cur = [], ""
    for wd in text.split():
        t = (cur+" "+wd).strip()
        if d.textlength(t, font=font) <= maxw: cur = t
        else: lines.append(cur); cur = wd
    lines.append(cur); return lines
def headline(canvas, text, y, size):
    font = ImageFont.truetype(BOLD, size); d0 = ImageDraw.Draw(canvas)
    lines = wrap(d0, text, font, 960)
    sh = Image.new("RGBA", canvas.size, (0,0,0,0)); sd = ImageDraw.Draw(sh)
    for i, ln in enumerate(lines):
        w = sd.textlength(ln, font=font); sd.text(((1080-w)/2, y+i*int(size*1.2)+6), ln, font=font, fill=(0,30,100,230))
    canvas.alpha_composite(sh.filter(ImageFilter.GaussianBlur(8)))
    d = ImageDraw.Draw(canvas)
    for i, ln in enumerate(lines):
        w = d.textlength(ln, font=font); d.text(((1080-w)/2, y+i*int(size*1.2)), ln, font=font, fill="white")
    yy = y + len(lines)*int(size*1.2) + 4
    # gelber Schwung (wie im Feature-Bild)
    sw = Image.new("RGBA", (1080, 40), (0,0,0,0)); sd = ImageDraw.Draw(sw)
    pts = [(300, 30), (540, 2), (780, 30), (540, 24)]
    sd.polygon(pts, fill=(255, 205, 20, 255))
    canvas.alpha_composite(sw.filter(ImageFilter.GaussianBlur(0.8)), (0, yy))
    return yy + 40
def make(nr, is3d, src, text, hero, path):
    c = BG.convert("RGBA"); y = 70
    if hero:
        ic = ICON.resize((190, 190), Image.LANCZOS); m = Image.new("L", (190,190), 0); ImageDraw.Draw(m).rounded_rectangle([0,0,189,189], radius=44, fill=255)
        glow = Image.new("RGBA", c.size, (0,0,0,0)); glow.paste((255,255,255,200), box=((1080-190)//2, y), mask=m)
        c.alpha_composite(glow.filter(ImageFilter.GaussianBlur(22)))
        c.paste(ic, ((1080-190)//2, y), m); y += 215
    y = headline(c, text, y, 76) + 20
    shot = os.path.join(SRC, src + ".jpg")
    if is3d:
        ph = phone_with(shot); avail = 1920 - y - 30
        sc = avail/ph.height; ph = ph.resize((int(ph.width*sc), avail), Image.LANCZOS)
        shadow_paste(c, ph, ((1080-ph.width)//2, y))
    else:
        im = Image.open(shot).convert("RGBA"); dw = 840; dh = int(im.height*dw/im.width); im = im.resize((dw, dh), Image.LANCZOS)
        bez, r = 14, 70
        dev = Image.new("RGBA", (dw+2*bez, dh+2*bez), (0,0,0,0))
        ImageDraw.Draw(dev).rounded_rectangle([0,0,dev.width-1,dev.height-1], radius=r+bez, fill=(18,32,56,255))
        mk = Image.new("L", im.size, 0); ImageDraw.Draw(mk).rounded_rectangle([0,0,dw-1,dh-1], radius=r, fill=255)
        dev.paste(im, (bez, bez), mk)
        shadow_paste(c, dev, ((1080-dev.width)//2, y))
    c.convert("RGB").save(path, "PNG", optimize=True)
if __name__ == "__main__":
    only = [int(a) for a in sys.argv[1:]] or None
    for lang, i in (("de", 2), ("en", 3)):
        od = os.path.join(TMP, "phone_"+lang); os.makedirs(od, exist_ok=True)
        for m in MOTIVE:
            if only and m[0] not in only: continue
            make(m[0], m[1], m[i], m[i+2], m[0] == 1, os.path.join(od, "%02d.png" % m[0]))
    print("fertig")