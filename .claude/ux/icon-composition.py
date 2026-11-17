import math, sys
from PIL import Image, ImageDraw, ImageFilter
LEO = r"C:\Users\rbald\Documents\Sync2PC\.claude\images\icon.png"
LEO_SCALE=0.98; LEO_DY=0.10; EAR_Y=0.62
def composition(S, diam_frac, bg=True):
    """Leo + Sync-Ring; Ring-Aussendurchmesser = diam_frac * S."""
    K = 2  # Supersampling
    W = S*K
    img = Image.new("RGBA", (W, W), (0,0,0,0))
    if bg:
        # vertikaler Verlauf #4A86C8 -> #2D5A94
        g = Image.new("RGBA", (1, W))
        for y in range(W):
            t = y/(W-1)
            c = tuple(int(a+(b-a)*t) for a,b in zip((0x4A,0x86,0xC8),(0x2B,0x57,0x92)))
            g.putpixel((0,y), c+(255,))
        img = g.resize((W, W))
    cx = cy = W/2
    R_out = diam_frac*W/2
    th = R_out*0.17            # Ringstärke
    R = R_out - th/2 - R_out*0.07   # Mittelradius (Platz für Pfeilspitzen)
    head = th*1.0              # Pfeilspitzen-Halbbreite
    ring = Image.new("RGBA", (W, W), (0,0,0,0))
    d = ImageDraw.Draw(ring)
    def arc(a0, a1, col):
        # Bogen als Polygon (Ring-Segment) + Dreieck-Spitze am Ende a1 (Uhrzeigersinn, Winkel in Grad, y nach unten)
        n = 90
        outer = []; inner = []
        for i in range(n+1):
            a = math.radians(a0+(a1-a0)*i/n)
            outer.append((cx+(R+th/2)*math.cos(a), cy+(R+th/2)*math.sin(a)))
            inner.append((cx+(R-th/2)*math.cos(a), cy+(R-th/2)*math.sin(a)))
        d.polygon(outer+inner[::-1], fill=col)
        # Rundung am Start
        a = math.radians(a0)
        d.ellipse([cx+R*math.cos(a)-th/2, cy+R*math.sin(a)-th/2, cx+R*math.cos(a)+th/2, cy+R*math.sin(a)+th/2], fill=col)
        # Pfeilspitze
        a = math.radians(a1)
        tip_a = math.radians(a1) + (head*1.9)/R
        base = [(cx+(R+head)*math.cos(a), cy+(R+head)*math.sin(a)),
                (cx+(R-head)*math.cos(a), cy+(R-head)*math.sin(a)),
                (cx+R*math.cos(tip_a), cy+R*math.sin(tip_a))]
        d.polygon(base, fill=col)
    arc(-25, 140, (255,255,255,255))       # weiss
    arc(155, 320, (255,184,64,255))       # warmes Orange (Leos Zunge/Halsband-Akzent)
    top = Image.new("L", (W, W), 0); ImageDraw.Draw(top).rectangle([0, 0, W, cy], fill=255)
    bot = Image.new("L", (W, W), 0); ImageDraw.Draw(bot).rectangle([0, cy, W, W], fill=255)
    def put(layer, mask):
        r = layer.copy(); a = r.split()[3]
        from PIL import ImageChops
        r.putalpha(ImageChops.multiply(a, mask)); return r
    def shadowed(layer):
        sh = Image.new("RGBA", (W, W), (0,0,0,0))
        sh.paste((10,30,60,110), mask=layer.split()[3].filter(ImageFilter.GaussianBlur(W*0.006)))
        o = Image.new("RGBA", (W, W), (0,0,0,0)); o.alpha_composite(sh, (0, int(W*0.006))); o.alpha_composite(layer); return o
    img.alpha_composite(shadowed(ring))
    leo = Image.open(LEO).convert("RGBA")
    leo = leo.crop(leo.getbbox())
    size = R*2*LEO_SCALE
    sc = size/max(leo.size)
    leo = leo.resize((int(leo.width*sc), int(leo.height*sc)), Image.LANCZOS)
    pos = (int(cx-leo.width/2), int(cy+R*LEO_DY-leo.height/2))
    lay = Image.new("RGBA", (W, W), (0,0,0,0)); lay.alpha_composite(leo, pos)
    clip = Image.new("L", (W, W), 0); cd = ImageDraw.Draw(clip)
    cd.rectangle([0, 0, W, cy+R*EAR_Y], fill=255); Ri=R-th/2; cd.ellipse([cx-Ri, cy-Ri, cx+Ri, cy+Ri], fill=255)
    lay = put(lay, clip)
    lsh = Image.new("RGBA", (W, W), (0,0,0,0))
    lsh.paste((10,30,60,120), mask=lay.split()[3].filter(ImageFilter.GaussianBlur(W*0.008)))
    img.alpha_composite(lsh); img.alpha_composite(lay)
    
    return img.resize((S, S), Image.LANCZOS)

out = sys.argv[1]
import os
os.makedirs(out, exist_ok=True)
composition(512, 0.84).convert("RGBA").save(os.path.join(out, "playstore_icon_512.png"))
composition(432, 0.62, bg=False).save(os.path.join(out, "ic_launcher_foreground_preview_432.png"))
# Vorschau mit Masken
prev = Image.new("RGBA", (1000, 340), (240,240,240,255))
base = composition(512, 0.84).resize((300,300), Image.LANCZOS)
m = Image.new("L", (300,300), 0); ImageDraw.Draw(m).ellipse([0,0,299,299], fill=255)
sq = Image.new("L", (300,300), 0); ImageDraw.Draw(sq).rounded_rectangle([0,0,299,299], radius=70, fill=255)
prev.paste(base, (20,20), sq); prev.paste(base, (340,20), m)
small = base.resize((96,96), Image.LANCZOS); ms = Image.new("L",(96,96),0); ImageDraw.Draw(ms).ellipse([0,0,95,95], fill=255)
prev.paste(small, (680,20), ms); prev.paste(base.resize((48,48), Image.LANCZOS), (800,20), Image.new("L",(48,48),255))
prev.save(os.path.join(out, "preview_masken.png"))