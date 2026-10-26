"""Builds app/src/main/assets/ruqya.json from tools/content/content.json and the verified sources.

    py -I -X utf8 tools/build_content.py

Quran text is never typed by hand: every verse comes from two independent sources,
  - Tanzil Uthmani (tanzil.net, verbatim, shown with Amiri/Scheherazade/Noto fonts) and
  - KFGQPC Uthmanic Hafs v18 (King Fahd Complex, Madinah, shown with its own font),
and the build fails unless both agree letter for letter (compared without vowel marks).

Every hadith Arabic text in content.json must appear in the narration it cites
(tools/sources/hadith: Arabic editions of fawazahmed0/hadith-api, which mirror sunnah.com).
The build fails on any mismatch. tools/refs.json lists every reference for the online
cross-check (see tools/README.md).
"""
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent
SRC = ROOT / "sources"
OUT = ROOT.parent / "app" / "src" / "main" / "assets" / "ruqya.json"

MARKS = re.compile(r"[ؐ-ًؚ-ٰٟۖ-ۭـ‌-‏ ]")


def norm(s: str) -> str:
    """Arabic letters only: no vowel marks, one alef, one yaa, no punctuation."""
    s = MARKS.sub("", s)
    s = re.sub("[أإآٱ]", "ا", s)
    s = s.replace("ى", "ي")
    s = re.sub(r"[^ء-ي ]", " ", s)
    return " ".join(s.split())


def skeleton(s: str) -> str:
    """Even looser, for comparing two Quran orthographies: no spaces, hamza seats and long alefs unified."""
    s = norm(s)
    s = re.sub("[ؤئء]", "", s)
    s = s.replace("ة", "ت").replace(" ", "").replace("ا", "").replace("و", "").replace("ي", "")
    return s


def load_pipe(name):
    out = {}
    for line in (SRC / name).read_text(encoding="utf-8").splitlines():
        if not line or line.startswith("#"):
            continue
        s, a, t = line.split("|", 2)
        out[(int(s), int(a))] = t.strip()
    return out


def translit_clean(t: str) -> str:
    """Tanzil transliteration: <b>/<strong> mark letters written but not pronounced (the silent l of
    al-, case endings when stopping), so they are dropped; <u>/<i> only mark long or emphatic sounds."""
    # (The file mixes <b> with </B>.)
    t = re.sub(r"<(?:b|strong)>.*?</(?:b|strong)>", "", t, flags=re.I)
    t = re.sub(r"<[^>]+>", "", t)
    return " ".join(t.replace("AA", "'").split())


def main():
    content = json.loads((ROOT / "content" / "content.json").read_text(encoding="utf-8"))
    errors = []

    uthmani = load_pipe("quran-uthmani.txt")
    simple = load_pipe("quran-simple-clean.txt")
    en = load_pipe("en.sahih.txt")
    sw = load_pipe("sw.barwani.txt")
    hafs = {(r["sora"], r["aya_no"]): r for r in json.loads((SRC / "hafsData_v18.json").read_text(encoding="utf-8"))}
    translit = {k: translit_clean(t) for k, t in load_pipe("en.transliteration.txt").items()}
    # Tanzil's text files put the basmala in front of verse 1 of every sura (except 1 and 9).
    # It is shown separately, so take it off, checking it is exactly the basmala of 1:1.
    for src in (uthmani, simple):
        # (95:1 and 97:1 carry a shadda on the first letter; compare the words, not the marks.)
        b = norm(src[(1, 1)])
        for s in range(2, 115):
            if s == 9:
                continue
            words = src[(s, 1)].split(" ")
            assert norm(" ".join(words[:4])) == b, f"{s}:1 does not start with the basmala"
            src[(s, 1)] = " ".join(words[4:])
    assert len(uthmani) == len(simple) == len(translit) == 6236, "source files are incomplete"
    sura_names = {}
    for (s, a), r in hafs.items():
        sura_names[s] = {"en": r["sora_name_en"], "ar": r["sora_name_ar"]}

    # ---- Quran: every ayah used by a passage
    ayahs = {}
    for p in content["passages"]:
        for s, a1, a2 in p["ranges"]:
            for a in range(a1, a2 + 1):
                k = (s, a)
                if k not in uthmani:
                    errors.append(f"passage {p['id']}: {s}:{a} does not exist")
                    continue
                h = hafs[k]
                if skeleton(h["aya_text_emlaey"]) != skeleton(simple[k]):
                    errors.append(f"{s}:{a}: KFGQPC and Tanzil disagree\n  {h['aya_text_emlaey']}\n  {simple[k]}")
                ayahs[f"{s}:{a}"] = {
                    "h": h["aya_text"], "t": uthmani[k], "tr": translit[k], "en": en[k], "sw": sw[k],
                }
    basmala = {"h": hafs[(1, 1)]["aya_text"].rsplit(" ", 1)[0], "t": uthmani[(1, 1)]}

    # ---- Hadith
    books = {}
    for b in content["books"]:
        hs = json.loads((SRC / "hadith" / f"ara-{b}.json").read_text(encoding="utf-8"))["hadiths"]
        idx = {}
        for h in hs:
            if b == "muslim":
                key = str(h.get("arabicnumber", "")).split(".")[0]
            else:
                key = str(h["hadithnumber"])
            idx.setdefault(key, []).append(h)
        books[b] = idx

    refs = {}

    def grade(src):
        b, n = src["book"], src["n"]
        if src.get("grade"):
            return src["grade"]  # given by hand where the edition has none (e.g. at-Tirmidhi's own grading)
        if b in ("bukhari", "muslim"):
            return {"en": "Sahih", "sw": "Sahih"}
        for h in books[b].get(n, []):
            for g in h.get("grades", []):
                if g["name"] == "Al-Albani":
                    return {"en": f"{g['grade']} (al-Albani)", "sw": f"{g['grade']} (al-Albani)"}
        errors.append(f"{b} {n}: no al-Albani grade in the source")
        return None

    def check(where, src, ar=None):
        b, n = src["book"], src["n"]
        found = books.get(b, {}).get(n)
        if not found:
            errors.append(f"{where}: {b} {n} not found")
            return
        refs.setdefault(f"{b}:{n}", [])
        if ar is not None:
            full = " ".join(norm(h["text"]) for h in found)
            if norm(ar) not in full:
                errors.append(f"{where}: Arabic is not in {b} {n}\n  {ar}")
            refs[f"{b}:{n}"].append(ar)
        src["grade"] = grade(src)

    by_id = {d["id"]: d for d in content["duas"]}

    # ---- Fadhila: the virtues of each dhikr (tools/content/virtues.json), each quote checked below.
    virt = json.loads((ROOT / "content" / "virtues.json").read_text(encoding="utf-8"))
    passages_by_id = {x["id"]: x for x in content["passages"]}
    for pid, vs in virt["passages"].items():
        passages_by_id[pid]["virtues"] = vs
    for aid, vs in virt["actions"].items():
        content["actions"][aid]["virtues"] = vs
    for did, cfg in virt["duas"].items():
        d = by_id[did]
        if cfg.get("drop_note"):
            d.pop("note", None)
        if "note" in cfg:
            d["note"] = cfg["note"]
        d["virtues"] = cfg.get("virtues", [])
        if "fadl_note" in cfg:
            d["fadl_note"] = cfg["fadl_note"]
    for r in content["routines"]:
        for st in r["steps"]:
            # The three Quls in the morning/evening routines carry Abu Dawud 5082 ("they suffice you").
            if st.get("passage") in ("ikhlas", "falaq", "nas") and any(x["n"] == "5082" for x in st.get("src", [])):
                st["virtues"] = virt["routine_steps"]["quls"]
                st.pop("note", None)
            # These notes only repeated a virtue that is now shown with its hadith.
            if st.get("dua") == "kalimat" or st.get("passage") in ("kursi", "baqarah_end"):
                st.pop("note", None)
    every = [(f"passage {p['id']}", v) for p in content["passages"] for v in p.get("virtues", [])]
    every += [(f"action {k}", v) for k, a in content["actions"].items() for v in a.get("virtues", [])]
    every += [(f"dua {d['id']}", v) for d in content["duas"] for v in d.get("virtues", [])]
    every += [(f"{r['id']} {st.get('passage')}", v) for r in content["routines"] for st in r["steps"] for v in st.get("virtues", [])]
    for where, v in every:
        check(f"virtue of {where}", v, v["ar"])

    for d in content["duas"]:
        where = f"dua {d['id']}"
        # The first source holds the exact wording; the others are listed as further references.
        if "parts" in d:
            # A narrator's aside interrupts the wording: each part must be in the hadith, and together
            # they must be exactly the text shown.
            parts = d.pop("parts")
            for part in parts:
                check(where, d["src"][0], part)
            if norm(d["ar"]).replace(" ", "") != "".join(norm(x).replace(" ", "") for x in parts):
                errors.append(f"{where}: the parts do not make up the text")
        elif "derived" in d:
            # The hadith gives one wording and says the other time of day is the same with named words
            # changed (e.g. 'asbahna' for 'amsayna'): check exactly those changes and nothing else.
            dv = d.pop("derived")
            base = by_id[dv["from"]]["ar"]
            for old, new in dv["replace"]:
                if old not in base:
                    errors.append(f"{where}: '{old}' is not in {dv['from']}")
                base = base.replace(old, new)
            if base != d["ar"]:
                errors.append(f"{where}: is not {dv['from']} with the listed changes")
            check(where, d["src"][0], dv["says"])
        else:
            check(where, d["src"][0], d["ar"])
        for s in d["src"][1:]:
            check(where, s)
        d.setdefault("count", 1)
    for k, a in content["actions"].items():
        for s in a.get("src", []):
            check(f"action {k}", s)
    passages = {p["id"] for p in content["passages"]}
    dua_ids = {d["id"] for d in content["duas"]}
    for group in ("sessions", "routines"):
        for sess in content[group]:
            for i, st in enumerate(sess["steps"]):
                where = f"{sess['id']} step {i + 1}"
                for s in st.get("src", []):
                    check(where, s)
                if "passage" in st and st["passage"] not in passages:
                    errors.append(f"{where}: unknown passage {st['passage']}")
                if "dua" in st and st["dua"] not in dua_ids:
                    errors.append(f"{where}: unknown dua {st['dua']}")
                if "action" in st and st["action"] not in content["actions"]:
                    errors.append(f"{where}: unknown action {st['action']}")
    for art in content["articles"]:
        for ev in art["evidence"]:
            if "passage" in ev:
                if ev["passage"] not in passages:
                    errors.append(f"article {art['id']}: unknown passage {ev['passage']}")
            else:
                check(f"article {art['id']}", ev, ev["ar"])
                ev["grade"] = ev.get("grade")

    # The audio pack covers only the verses that sessions and routines recite (not the verses shown as
    # evidence or the ones the names come from).
    recited = {st["passage"] for g in ("sessions", "routines") for x in content[g] for st in x["steps"] if "passage" in st}
    recited |= {pid for a in content["actions"].values() for pid in a.get("passages", [])}
    audio = sorted({f"{s:03d}{a:03d}" for p in content["passages"] if p["id"] in recited
                    for s, a1, a2 in p["ranges"] for a in range(a1, a2 + 1)})
    audio += ["bismillah", "audhubillah"]

    # ---- The 99 names (Ibn Uthaymeen, al-Qawa'id al-Muthla)
    names = json.loads((ROOT / "content" / "names.json").read_text(encoding="utf-8"))
    book = names["book"].split()
    if len(names["names"]) != 99 or len(book) != 99:
        errors.append(f"names: {len(names['names'])} entries, {len(book)} in the book list (need 99)")
    for i, (nm, b) in enumerate(zip(names["names"], book), 1):
        where = f"name {i} {b}"
        if norm(nm["ar"]) != norm(b):
            errors.append(f"{where}: vowelled form {nm['ar']} is not the book's {b}")
        if "q" in nm:
            s, a = map(int, nm["q"].split(":"))
            k = (s, a)
            word = norm(nm.get("match") or (b if b == "الله" else b[2:]))
            if k not in simple or word not in norm(simple[k]):
                errors.append(f"{where}: '{word}' is not in {nm['q']}: {simple.get(k)}")
                continue
            if nm["q"] not in ayahs:
                ayahs[nm["q"]] = {
                    "h": hafs[k]["aya_text"], "t": uthmani[k], "tr": translit[k], "en": en[k], "sw": sw[k],
                }
        elif "h" in nm:
            check(where, nm["h"], nm["h"]["match"])
        elif "cite" not in nm:
            errors.append(f"{where}: no source")
    check("names intro", names["intro_evidence"], names["intro_evidence"]["ar"])
    check("names intro", names["intro_weak"])
    names.pop("_comment")
    names.pop("book")
    content["names"] = names

    if errors:
        print(f"{len(errors)} problem(s):")
        for e in errors:
            print(" -", e)
        sys.exit(1)

    content.pop("_comment", None)
    content["ayahs"] = ayahs
    content["basmala"] = basmala
    content["suras"] = {str(s): sura_names[s] for s in sorted({int(k.split(":")[0]) for k in ayahs})}
    content["audio"] = {"base": "https://everyayah.com/data/Alafasy_64kbps/", "files": audio}
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(json.dumps(content, ensure_ascii=False, separators=(",", ":")), encoding="utf-8")
    (ROOT / "refs.json").write_text(json.dumps(refs, ensure_ascii=False, indent=1), encoding="utf-8")
    print(f"OK: {len(ayahs)} ayahs, {len(content['duas'])} duas, {len(refs)} hadith references verified, "
          f"{len(audio)} audio files -> {OUT.relative_to(ROOT.parent)} ({OUT.stat().st_size // 1024} KB)")


if __name__ == "__main__":
    main()
