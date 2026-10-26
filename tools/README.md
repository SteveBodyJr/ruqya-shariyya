# Content tools

`build_content.py` turns `content/*.json` plus the sources below into `app/src/main/assets/ruqya.json`.

## Sources (`sources/`)

| File | From | Notes |
|---|---|---|
| `quran-uthmani.txt` | tanzil.net, Uthmani, text with aya numbers | Verbatim; licence at the end of the file |
| `quran-simple-clean.txt` | tanzil.net, Simple Clean | Only for comparing with KFGQPC |
| `hafsData_v18.json` | github.com/thetruetruth/quran-data-kfgqpc (KFGQPC Hafs v18) | Shown with the Madinah font |
| `en.sahih.txt`, `sw.barwani.txt` | tanzil.net/trans | Sahih International, Al-Barwani |
| `en.transliteration.txt` | tanzil.net/trans | `<b>` marks silent letters (dropped) |
| `qcom-uthmani.json` | api.quran.com/api/v4/quran/verses/uthmani | Third-source check only |
| `hadith/ara-*.json` | cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/ara-{bukhari,muslim,abudawud,tirmidhi,ibnmajah}.min.json | Not committed (large); download before building |

Download the hadith collections (PowerShell, works through Avast):

```
foreach ($c in "bukhari","muslim","abudawud","tirmidhi","ibnmajah") {
  Invoke-WebRequest "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/ara-$c.min.json" -OutFile "tools/sources/hadith/ara-$c.json" -UseBasicParsing }
```

Muslim is matched by its Abd al-Baqi number (`arabicnumber` 2186.01 -> 2186); the others by `hadithnumber`,
which follow sunnah.com.

## Online cross-check

`refs.json` (written by the build) lists every hadith reference with the Arabic that must appear in it.
sunnah.com blocks scripts (403), so the check runs in a real browser on a sunnah.com tab: fetch `/<book>:<n>`
for each reference (Muslim numbers with sub-letters, e.g. `muslim:2709a`), strip vowel marks, and confirm each
text appears. Last run 2026-10-26: all references found.
