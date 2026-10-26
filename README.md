# Ruqya Shariyya

Android app for self-ruqya from the Quran and the authentic Sunnah, in English and Kiswahili.
Package `com.digitalbeavertz.ruqya`, Digital Beaver Apps.

## What is in it

- **Ruqya sessions**, step by step with a repetition counter and recitation: evil eye (kijicho, following the guide
  *Ruqyah sahihi kwa kujitibu na kijicho*), complete ruqya, magic, jinn and whispers, illness and pain, worry and
  distress, and reciting over a child or family member.
- **Daily protection**: morning, evening and bedtime routines with optional reminders.
- **Duas** from the Sunnah, each with its source and grading.
- **The 99 names of Allah** as compiled by Ibn Uthaymeen (al-Qawa'id al-Muthla), each with its verse or hadith.
- **Learn**: what ruqya is, how to do it, the evil eye, what to avoid, medicine, common mistakes, relying on Allah.
- Arabic fonts: Madinah Mushaf (KFGQPC Uthmanic Hafs, default), Amiri Quran, Scheherazade New, Noto Naskh.
- Recitation (Mishary Rashid Alafasy) downloaded once (~12 MB) and played offline, with a media notification.

## Content and verification

No Quran or hadith text is typed by hand. `tools/content/content.json` and `tools/content/names.json` hold the
hand-written parts (structure, notes, meanings) and refer to verses and hadith by number.

```
py -I -X utf8 tools/build_content.py         # writes app/src/main/assets/ruqya.json
py -I -X utf8 tools/verify_quran_online.py   # third-source check against quran.com
```

`build_content.py` fails the build unless:

- every verse is identical (letters, ignoring marks) in Tanzil Uthmani and the KFGQPC Hafs text;
- every dua and quoted hadith appears word for word in the narration it cites;
- every one of the 99 names matches Ibn Uthaymeen's list and occurs in its verse or hadith.

Sources in `tools/sources/` (see `tools/README.md` to download the hadith collections, which are not committed).
On 2026-10-26 all hadith references were also checked against the live sunnah.com pages, and the names list against
the book on shamela.ws (book 8874, pages 15-16).

## Build

Same toolchain as Until/Hush: AGP 8.7.3, Kotlin 2.1.20, Gradle 8.14, JDK 21.

```
JAVA_HOME=C:/Users/user/.jdks/jdk-21.0.12.1+1 ./gradlew.bat :app:testDebugUnitTest :app:assembleRelease
```

Release signing reads `C:/Users/user/.android/keys/ruqya-release.properties` (outside the repo; never commit it),
or `-PruqyaSigning=<path>`. Without it, release builds fall back to the debug key.

## Testing the audio download on the emulator

Avast's HTTPS scanning breaks TLS from the emulator. Serve the files locally instead:

```
py -I -m http.server 8765 --directory <folder with the 107 mp3 files>
adb reverse tcp:8765 tcp:8765
adb shell am start -n com.digitalbeavertz.ruqya/.MainActivity --es audioBase http://localhost:8765/
```

(debug builds only; `--es audioBase ""` goes back to everyayah.com).
