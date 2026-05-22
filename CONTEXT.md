# PrepMaster — Project Context

## Overview
PrepMaster is a secure Android exam preparation application with Google login whitelist, PDF study materials, offline syllabus cards, dynamic sync via a remote JSON index (GitHub Gist), and Gemini AI API integration.

## Tech Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose (no Jetpack Navigation — uses custom string-based screen routing via `MutableStateFlow`)
- **Database**: Room (SQLite)
- **Networking**: Retrofit + Moshi
- **PDF Viewing**: WebView with Google Docs Viewer (`https://docs.google.com/viewer?url=...&embedded=true`)
- **Auth**: Firebase REST API (single source of truth — no JSON passwords, no SharedPreferences fallback)
- **Build**: Gradle (Kotlin DSL), Android Studio
- **AI**: Gemini API (key via `.env` file)

## Project Structure
```
app/src/main/java/com/example/
├── MainActivity.kt                      # Entry point + MainAppContainer composable
├── data/
│   ├── db/
│   │   ├── AppDatabase.kt               # Room database (version 4, fallbackToDestructiveMigration)
│   │   ├── Dao.kt                       # All DAO interfaces (PdfDao, SessionDao, StatsDao)
│   │   └── Entities.kt                  # Room entities
│   ├── model/
│   │   └── RemoteConfig.kt              # Moshi JSON models (RemotePdfItem, RemoteDailyChallenge, RemoteConfig)
│   ├── net/
│   │   ├── UpdateService.kt             # Retrofit service for remote config fetch
│   │   └── FirebaseAuthService.kt       # Retrofit service for Firebase REST Auth API
│   └── repo/
│       ├── AuthRepository.kt            # Authentication + whitelist logic + Firebase verify
│       └── PdfRepository.kt             # PDF data repo, sync, seeding, bookmarks, progress, user credentials
├── ui/
│   ├── screens/
│   │   ├── AnalyticsScreen.kt           # Score charts, attempt history (real data from DB)
│   │   ├── DailyChallengeScreen.kt      # Daily MCQ quiz
│   │   ├── DashboardScreen.kt           # Home screen with stats, streak, recent papers
│   │   ├── ExamAttemptScreen.kt         # MCQ exam with timer, flagging, question grid
│   │   ├── LoginScreen.kt               # Email/password login with whitelist check
│   │   ├── PapersLibraryScreen.kt       # Full paper listing with filters (exam/subject/year)
│   │   ├── PdfViewScreen.kt             # WebView PDF viewer via Google Docs
│   │   └── SettingsScreen.kt            # Admin panel: sync URL, whitelist management
│   ├── theme/
│   │   ├── Color.kt                     # App colors (PrimaryAccentAmber, SecondaryViolet, etc.)
│   │   ├── Theme.kt                     # Theme setup
│   │   └── Type.kt                      # Typography
│   └── viewmodel/
│       └── PrepViewModel.kt             # Shared ViewModel for ALL screens
└── res/                                 # Android resources
```

## Screen Identifiers (Navigation)
Navigation is handled via `MutableStateFlow<String>` in `PrepViewModel`, NOT Jetpack Navigation Compose.

| Screen ID | Composable | Description |
|-----------|-----------|-------------|
| `"login"` | `LoginScreen` | Email/password login with whitelist validation |
| `"dashboard"` | `DashboardScreen` | Home with stats, streak, challenge, recent papers |
| `"library"` | `PapersLibraryScreen` | Full paper grid with JEE/NEET/Boards/SAT filters |
| `"attempt"` | `ExamAttemptScreen` | MCQ exam with 30-min timer, flagging, question grid |
| `"pdf_viewer"` | `PdfViewScreen` | WebView rendering PDF via Google Docs Viewer |
| `"analytics"` | `AnalyticsScreen` | Score charts, attempt history |
| `"challenge"` | `DailyChallengeScreen` | Daily MCQ quiz |
| `"settings"` | `SettingsScreen` | Admin: sync URL, whitelist CRUD |

## Data Models

### Room Entities (`Entities.kt`)
- **`PdfItemEntity`** — id, title, subject, category, examType, year, url, size
- **`BookmarkEntity`** — pdfId (FK), bookmarkedAt
- **`ProgressEntity`** — pdfId (FK), lastPageRead, totalPages, lastStudiedAt
- **`UserSessionEntity`** — email (PK), displayName, loginTime, allowedByRemoteWhitelist, role
- **`AttemptHistoryEntity`** — id (auto), pdfId, examName, subject, score, totalQuestions, timeSpentSeconds, completedAt
- **`DailyChallengeEntity`** — dateKey (PK), completed, correct, completedAt, timeTakenSeconds, selectedOptionIndex
- **`StreakStatsEntity`** — id (PK=1), currentStreak, lastActiveDate, mon/tue/wed/thu/fri/sat/sun booleans
- **`DailyChallengeQuestionEntity`** — id (PK), dateKey, subject, topic, question, optionsList (|| joined), correctIndex, explanation, avgTimeMinutes

### Remote JSON Models (`RemoteConfig.kt`)
- **`RemotePdfItem`** — id, title, subject, category, examType (default "pdf"), year, url, size
- **`RemoteDailyChallenge`** — id, date, subject, topic, question, options, correctIndex, explanation, avgTimeMinutes
- **`RemoteConfig`** — whitelist, users (backward compat email→any map), admins (email list), pdfs, dailyChallenges

### ViewModel Models (`PrepViewModel.kt`)
- **`ExamQuestion`** — id, text, options, correctAnswerIndex, explanation, subject, topic
- **`SyncState`** — sealed interface: Idle, Syncing, Success(message), Error(error)

### ViewModel State Flows
- `currentScreen`, `screenBackStack` — navigation
- `allPdfs`, `bookmarkedPdfs`, `activePdfDetail` — PDF data
- `allAttempts`, `dailyStreakStats` — stats
- `selectedSubject`, `selectedPdfId`, `searchQuery` — UI state
- `_isLibraryLoading` — loading spinner for PapersLibraryScreen
- `syncState`, `configUrlInput`, `whitelistedEmails` — admin settings
- `dailyChallengeQuestion`, `dailyChallengeState`, `dailyChallengeSelectedOption`, `dailyChallengeAnswered` — daily challenge
- `examQuestions`, `currentQuestionIndex`, `examSelectedOptions`, `examFlaggedQuestions`, `examTimeRemaining` — exam attempt
- `challengeNotificationEnabled` — preference toggle
- `activeSession` — logged-in user

## Key Navigation Methods in PrepViewModel
- `navigateTo(screen: String)` — pushes screen, clears stack for login/dashboard
- `navigateBack()` — pops back stack
- `selectPdf(pdfId: String)` — sets selected PDF, navigates to `"pdf_viewer"`
- `openPaper(pdfId, title, subject)` — **ROUTING**: checks `examType` → `"mcq"` → `startExamAttempt()`, else → `selectPdf()`
- `startExamAttempt(pdfId, examName, subject)` — generates subject-based MCQs, starts 30-min timer, navigates to `"attempt"`
- `selectSubject(subject)` — navigates to `"library"` pre-filtered

## Paper Click Routing
**"Start Paper" buttons call `viewModel.openPaper()` which routes by `examType`:**
- `examType == "pdf"` → `PdfViewScreen` (WebView with Google Docs Viewer)
- `examType == "mcq"` → `ExamAttemptScreen` (MCQ exam with generated questions)
- `examType == "Daily Challenge"` → `ExamAttemptScreen` (MCQ)

The `examType` field defaults to `"pdf"` if missing from JSON.

## Remote Sync Flow
1. Admin enters raw Gist URL in Settings
2. `syncDatabase()` → `PdfRepository.syncRemoteConfig()` → Retrofit fetches JSON
3. Parses `RemoteConfig` → clears and seeds Room DB with new PDFs
4. Updates whitelist from `whitelist` array in SharedPreferences
5. Updates admin emails from `admins` list (or backward compat `users` map keys) in SharedPreferences
6. Syncs daily challenges into `DailyChallengeQuestionEntity` table
7. Default config URL: `https://raw.githubusercontent.com/aistudio-templates/mock-data/main/exam_prep_config.json`

## Database Seeding
`PdfRepository.seedDatabaseIfEmpty()` inserts 9 sample PDF items on first launch (all with `examType = "pdf"` + 1 MCQ entry). All use dummy PDF URLs from w3.org. Also seeds default admin emails in SharedPreferences so login works without remote sync.

## Auth Flow (Firebase-only)
- **Firebase is the single source of truth** for authentication. The JSON file stores ONLY an admin list — no passwords, no hashes, no credentials of any kind.
- When user logs in: Firebase validates credentials → if rejected, login fails → if accepted, check admin list → `"admin"` or `"user"` role.
- Three possible states:
  - Not in Firebase → **rejected**, period
  - In Firebase, not in JSON admin list → **regular user**
  - In Firebase, and in JSON admin list → **admin**
- After successful auth, creates `UserSessionEntity` in Room and updates streak stats.

## Recent Bug Fixes (Session 2026-05-22)

### Fixed:
1. **Back stack after login** — `login()` now calls `navigateTo("dashboard")` instead of directly setting `_currentScreen`, fixing broken navigation stack
2. **DailyChallengeScreen subject tag** — Added `subject`/`topic` fields to `ExamQuestion`; DailyChallengeScreen now displays actual subject instead of question text
3. **Timer thread safety** — Exam countdown coroutine no longer uses `Dispatchers.Default` (runs on Main, safe for StateFlow mutation)
4. **PdfViewScreen back button** — Changed from `navigateTo("library")` to `navigateBack()` for proper back navigation
5. **Dashboard card routing** — Card body click calls `openPaper()` (routes by `examType`) instead of `selectPdf()` (always opens PDF viewer)
6. **Sync state auto-clear** — Added `LaunchedEffect` in SettingsScreen to clear sync state after 4 seconds
7. **Analytics charts** — Bar chart and line chart now compute from actual `attempts` data instead of hardcoded mock values
8. **Typo** — Fixed `"ACCURACY BY ACC REDUCTION"` → `"ACCURACY BY SUBJECT"`
9. **Loading state** — Added `isLibraryLoading` StateFlow; PapersLibraryScreen shows `CircularProgressIndicator` while PDFs load
10. **PDF viewer timer** — Timer reaching 0 now shows AlertDialog and navigates back on dismiss

### Added:
11. **Centralized user credentials** — `users` map in `prep_database.json` with SHA-256 hashed passwords; synced, seeded, verified at login
12. **Firebase Authentication** — Via REST API (no google-services.json needed). Optional: set `FIREBASE_API_KEY` in `.env` to enable. Falls back gracefully to JSON/SharedPreferences auth
13. **Build fix** — `FIREBASE_API_KEY` in `.env.example` uses a placeholder string (not empty) to prevent Secrets Gradle Plugin from generating invalid Java
14. **AuthRepository refactor** — Uses direct `BuildConfig.FIREBASE_API_KEY` with `.takeIf` filter (skips placeholder values starting with `YOUR_`) instead of fragile reflection

### Refactored (Session 2026-05-22):
15. **Firebase is single auth source** — Removed all JSON SHA-256 hash and SharedPreferences password fallbacks. Firebase REST API is the only authentication mechanism.
16. **JSON stores only admin list** — `users` map (email→hash) replaced with `admins` array (email list). No passwords, no hashes, no credentials in JSON.
17. **Role determination** — Firebase success + in `admins` list → admin; Firebase success + not in `admins` → user; not in Firebase → rejected.
18. **CI secrets injection** — GitHub Actions workflow injects `GEMINI_API_KEY` and `FIREBASE_API_KEY` from repo secrets into `.env` at build time.
19. **Moshi annotations** — Added `@JsonClass(generateAdapter = true)` to `FirebaseSignInResponse` and `FirebaseErrorBody` so Moshi KSP codegen can deserialize Firebase REST API responses.
20. **Real Firebase error messages** — Login screen now shows the actual Firebase error (e.g. "EMAIL_NOT_FOUND", "INVALID_PASSWORD", "Firebase not configured") instead of a generic message.
21. **Removed whitelist gate** — Deleted redundant whitelist authorization check in `tryLoginWithGoogleEmail()`; Firebase is the sole gatekeeper.

## Theme Colors
- `PrimaryAccentAmber` — `#f59e0b` (CTAs, highlights)
- `SecondaryViolet` — `#8b5cf6` (secondary accents) (Color.kt uses `#6C63FF`)
- `BackgroundDeepNavy` — `#0a0a1a` (main background)
- `SurfaceNavy` — `#111128` (card surfaces)
- `CorrectGreen` — `#22c55e` (success states) (Color.kt uses `#16A34A`)
- `TextMuted` — `#8888aa` (secondary text)

## Setup Instructions
1. Open in Android Studio → let it auto-fix imports
2. Create `.env` file from `.env.example` with:
   - `GEMINI_API_KEY=your_key` (required for Gemini AI)
   - `FIREBASE_API_KEY=your_key` (optional; enables Firebase Auth; MUST be non-empty placeholder to avoid build error)
3. Remove `signingConfig = signingConfigs.getByName("debugConfig")` from `app/build.gradle.kts`
4. Run on emulator/device
5. Set up GitHub Gist with `prep_database.json` (see `prep_database.json` template in project root)
6. Paste raw Gist URL in app Settings → Sync Database & Whitelist

## Default Accounts (Seeded + JSON template)

| Email | Password | Role |
|-------|----------|------|
| `spam.iamshivanshcoder@gmail.com` | `12345678` | admin |
| `exammanager@gmail.com` | `manager` | admin |
| `student@school.edu` | `student` | user |
| `testuser@gmail.com` | `12345678` | user |

**Note:** Passwords are NOT stored in JSON. Firebase handles all authentication. The JSON `admins` list is purely for role determination. Accounts must be created in the Firebase Console first.

## Firebase Setup (Optional)
1. Create project at [Firebase Console](https://console.firebase.google.com/)
2. Enable **Email/Password** sign-in in Authentication → Sign-in method
3. Add users in Authentication → Users tab
4. Copy **Web API Key** from Project Settings → General
5. Set `FIREBASE_API_KEY=your_key` in `.env`
6. Rebuild — no google-services.json needed

## Google Drive PDF Hosting
Convert share link `https://drive.google.com/file/d/FILE_ID/view?usp=sharing`
to direct download: `https://docs.google.com/uc?export=download&id=FILE_ID`

## JSON Database Schema
```json
{
  "whitelist": ["email1@gmail.com", "email2@gmail.com"],
  "admins": ["admin1@gmail.com"],
  "pdfs": [
    {
      "id": "unique_id",
      "title": "Display Name",
      "subject": "Physics|Chemistry|Mathematics|Computer Science",
      "category": "Past Papers|Notes|Formula Sheets|Syllabus|Daily Challenge",
      "exam_type": "pdf|mcq",
      "year": "2025",
      "url": "https://direct-pdf-link.com/file.pdf",
      "size": "1.2 MB"
    }
  ],
  "daily_challenges": [
    {
      "id": "challenge_id",
      "date": "2025-05-21",
      "subject": "Physics",
      "topic": "Mechanics",
      "question": "Question text?",
      "options": ["A", "B", "C", "D"],
      "correct_index": 0,
      "explanation": "Why A is correct",
      "avg_time_minutes": 1.5
    }
  ]
}
```

## Important Implementation Notes
- `fallbackToDestructiveMigration()` means DB schema changes wipe data — acceptable for this app
- Exam questions are **generated locally** by `generateQuestionsForSubject()` based on subject keyword matching (Physics/Chemistry/Maths/CS)
- Timer runs in `viewModelScope` (Main dispatcher), auto-submits at 0
- Streak tracking uses date keys (`yyyy-MM-dd`) — increments if yesterday was active, resets otherwise
- Bookmark and progress are stored locally in Room — works 100% offline after sync
- The app uses `AnimatedContent` for screen transitions in `MainActivity.kt`
- Bottom navigation bar is hidden on login, attempt, and pdf_viewer screens
- Sync state auto-clears after 4 seconds in SettingsScreen
- User credentials stored in SharedPreferences as serialized map (`key::value;;key::value`)
- Login auth chain: Firebase REST API → JSON admin list for role → session created
