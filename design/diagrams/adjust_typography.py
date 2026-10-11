"""Apply the StarVocab slightly larger type treatment after Archify delivery."""

import re
from pathlib import Path


ROOT = Path(__file__).resolve().parent
MARKER = '<meta name="starvocab-type-scale" content="1.06">'
STYLE = re.compile(r"(<style>)(.*?)(</style>)", re.S)
DECLARATION = re.compile(r"(?<![\w-])font(?:-size)?\s*:\s*([^;{}]+)")
UNIT = re.compile(r"(\d+(?:\.\d+)?)(rem|px)")
SVG_TEXT = re.compile(r'(<text\b[^>]*?\bfont-size=")(\d+(?:\.\d+)?)(")')


def enlarge_unit(match: re.Match[str]) -> str:
    size = float(match.group(1))
    unit = match.group(2)
    larger = max(size + 1, round(size * 1.06)) if unit == "px" else max(size + .0625, round(size * 1.06, 4))
    return f"{larger:g}{unit}"


def enlarge_style(match: re.Match[str]) -> str:
    def change_declaration(declaration: re.Match[str]) -> str:
        value = UNIT.sub(enlarge_unit, declaration.group(1))
        return declaration.group(0).replace(declaration.group(1), value, 1)

    return match.group(1) + DECLARATION.sub(change_declaration, match.group(2)) + match.group(3)


def enlarge_svg_text(match: re.Match[str]) -> str:
    size = float(match.group(2))
    return match.group(1) + f"{max(size + 1, round(size * 1.06, 1)):g}" + match.group(3)


for name in ("01-architecture", "02-api-journey", "03-learning-dataflow"):
    path = ROOT / f"{name}.html"
    source = path.read_bytes().decode("utf-8")
    if MARKER in source:
        print(f"{name}: already adjusted")
        continue
    adjusted, style_count = STYLE.subn(enlarge_style, source, count=1)
    if style_count != 1:
        raise RuntimeError(f"Expected one style block in {path}")
    adjusted = SVG_TEXT.sub(enlarge_svg_text, adjusted)
    adjusted = adjusted.replace('<meta charset="UTF-8">', '<meta charset="UTF-8">\n  ' + MARKER, 1)
    path.write_bytes(adjusted.encode("utf-8"))
    print(f"{name}: adjusted")
