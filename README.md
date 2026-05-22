# PrepMaster — Exam Preparation App

Secure Android exam prep app with whitelist-based access, PDF study materials, MCQ exams, daily challenges, and Firebase Auth.

## Setup

1. Open in Android Studio — let it sync dependencies
2. Create `.env` from `.env.example`:
   ```
   GEMINI_API_KEY=your_gemini_key
   FIREBASE_API_KEY=your_firebase_web_api_key
   ```
3. Remove `signingConfig = signingConfigs.getByName("debugConfig")` from `app/build.gradle.kts`
4. Run on emulator/device

## Auth

**3-tier login chain:** Firebase REST API → JSON SHA-256 hash → per-device SharedPreferences

### Default accounts (seeded)

| Email | Password | Role |
|-------|----------|------|
| spam.iamshivanshcoder@gmail.com | admin | admin |
| exammanager@gmail.com | manager | admin |
| student@school.edu | student | user |
| testuser@gmail.com | 12345678 | user |

### Firebase Auth (optional)

1. Create project at [Firebase Console](https://console.firebase.google.com/)
2. Enable Email/Password sign-in in Authentication
3. Copy **Web API Key** from Project Settings → General
4. Set `FIREBASE_API_KEY=your_key` in `.env`
5. Rebuild — no google-services.json needed

### JSON hash fallback

Without Firebase, passwords verified via SHA-256 hashes from `prep_database.json`:
```bash
echo -n "yourpassword" | sha256sum
```

## Remote Sync

1. Host `prep_database.json` on a GitHub Gist
2. Paste raw Gist URL in app Settings → Sync Database & Whitelist

## Google Drive PDF Hosting

Convert `https://drive.google.com/file/d/FILE_ID/view?usp=sharing` to
`https://docs.google.com/uc?export=download&id=FILE_ID`
