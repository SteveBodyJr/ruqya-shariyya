"""Third-source check of every verse the app uses against quran.com's Uthmani text.

    (download) https://api.quran.com/api/v4/quran/verses/uthmani -> tools/sources/qcom-uthmani.json
    py -I -X utf8 tools/verify_quran_online.py
"""
import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from build_content import skeleton  # noqa: E402

ROOT = Path(__file__).resolve().parent
app = json.loads((ROOT.parent / "app/src/main/assets/ruqya.json").read_text(encoding="utf-8"))
qcom = {v["verse_key"]: v["text_uthmani"] for v in
        json.loads((ROOT / "sources/qcom-uthmani.json").read_text(encoding="utf-8"))["verses"]}
bad = 0
for key, a in app["ayahs"].items():
    for label, text in (("Tanzil", a["t"]), ("KFGQPC", a["h"].rsplit("\u00a0", 1)[0])):
        if skeleton(text) != skeleton(qcom[key]):
            bad += 1
            print(f"{key} {label} differs from quran.com\n  {text}\n  {qcom[key]}")
print(f"{len(app['ayahs'])} verses checked against quran.com: {'all match' if not bad else f'{bad} differences'}")
sys.exit(1 if bad else 0)
