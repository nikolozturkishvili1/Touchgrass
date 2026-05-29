# Generates icon.html (512x512) and feature.html (1024x500) for the Play listing.
# Rendered to PNG via headless Edge. No third-party packages required.

import os
BASE = os.path.dirname(os.path.abspath(__file__))

MOSS_DARK  = "#2C4628"
MOSS       = "#3E5E3A"
MOSS_LIGHT = "#557A4D"
SAGE       = "#C9D5A4"
SAGE_BACK  = "#A9BC7E"
CREAM      = "#FAF8F3"


def blade(bx, by, tip_x, tip_y, base_w, bend, color):
    """A tapered grass blade: wide at (bx,by), meeting at a point at (tip_x,tip_y)."""
    cx = (bx + tip_x) / 2 + bend
    cy = (by + tip_y) / 2
    half = base_w / 2
    return (
        f'<path fill="{color}" d="'
        f'M {bx-half:.1f} {by:.1f} '
        f'Q {cx-half*0.25:.1f} {cy:.1f} {tip_x:.1f} {tip_y:.1f} '
        f'Q {cx+half*0.25:.1f} {cy:.1f} {bx+half:.1f} {by:.1f} Z"/>'
    )


def grass_cluster(cx, base_y, s=1.0):
    b = []
    # back layer (darker sage)
    b.append(blade(cx-8*s,  base_y,     cx-81*s,  base_y-197*s, 26*s, -40*s, SAGE_BACK))
    b.append(blade(cx+8*s,  base_y,     cx+84*s,  base_y-197*s, 26*s, +40*s, SAGE_BACK))
    b.append(blade(cx,      base_y,     cx+2*s,   base_y-262*s, 24*s,   0,   SAGE_BACK))
    # front layer (sage)
    b.append(blade(cx-18*s, base_y+2*s, cx-106*s, base_y-152*s, 24*s, -55*s, SAGE))
    b.append(blade(cx+18*s, base_y+2*s, cx+109*s, base_y-157*s, 24*s, +55*s, SAGE))
    b.append(blade(cx-8*s,  base_y+2*s, cx-51*s,  base_y-227*s, 22*s, -25*s, SAGE))
    b.append(blade(cx+10*s, base_y+2*s, cx+56*s,  base_y-227*s, 22*s, +25*s, SAGE))
    b.append(blade(cx,      base_y+2*s, cx,       base_y-277*s, 30*s,   0,   SAGE))
    return "\n".join(b)


HEAD = """<!doctype html><html><head><meta charset="utf-8"><style>
html,body{{margin:0;padding:0;width:{w}px;height:{h}px;overflow:hidden;background:#fff}}
svg{{display:block}}
</style></head><body>"""
TAIL = "</body></html>"


def write_html(path, w, h, svg):
    with open(path, "w", encoding="utf-8") as f:
        f.write(HEAD.format(w=w, h=h) + svg + TAIL)


# ---------- ICON 512x512 ----------
icon_svg = f"""<svg xmlns="http://www.w3.org/2000/svg" width="512" height="512" viewBox="0 0 512 512">
  <defs>
    <radialGradient id="moss" cx="50%" cy="36%" r="78%">
      <stop offset="0%" stop-color="{MOSS_LIGHT}"/>
      <stop offset="100%" stop-color="{MOSS_DARK}"/>
    </radialGradient>
    <radialGradient id="sheen" cx="50%" cy="20%" r="55%">
      <stop offset="0%" stop-color="#FFFFFF" stop-opacity="0.10"/>
      <stop offset="100%" stop-color="#FFFFFF" stop-opacity="0"/>
    </radialGradient>
  </defs>
  <rect width="512" height="512" fill="url(#moss)"/>
  <rect width="512" height="512" fill="url(#sheen)"/>
  <ellipse cx="256" cy="436" rx="98" ry="18" fill="{MOSS_DARK}" opacity="0.55"/>
  {grass_cluster(256, 432, 1.22)}
</svg>"""
write_html(os.path.join(BASE, "icon.html"), 512, 512, icon_svg)

# ---------- FEATURE GRAPHIC 1024x500 ----------
feat_svg = f"""<svg xmlns="http://www.w3.org/2000/svg" width="1024" height="500" viewBox="0 0 1024 500">
  <defs>
    <linearGradient id="bg" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="{MOSS_LIGHT}"/>
      <stop offset="55%" stop-color="{MOSS}"/>
      <stop offset="100%" stop-color="{MOSS_DARK}"/>
    </linearGradient>
  </defs>
  <rect width="1024" height="500" fill="url(#bg)"/>
  <!-- hero grass on the right -->
  <g transform="translate(60,0)">{grass_cluster(820, 560, 1.55)}</g>
  <!-- wordmark + taglines (kept in the safe left/centre area) -->
  <text x="70" y="205" font-family="'Segoe UI',Arial,sans-serif" font-weight="700" font-size="92" fill="{CREAM}">Touchgrass</text>
  <text x="74" y="270" font-family="'Segoe UI',Arial,sans-serif" font-weight="600" font-size="42" fill="{SAGE}">Block Reels, Shorts &amp; TikTok</text>
  <text x="74" y="324" font-family="'Segoe UI',Arial,sans-serif" font-weight="400" font-size="27" fill="{CREAM}" opacity="0.88">No ads &#183; No subscription &#183; Open source</text>
</svg>"""
write_html(os.path.join(BASE, "feature.html"), 1024, 500, feat_svg)

print("wrote icon.html and feature.html")
