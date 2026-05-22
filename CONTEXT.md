# PrepMaster — Project Context

## Overview
Secure Android exam prep app: Firebase Auth, remote JSON sync (GitHub Gist), offline Room DB, PDF viewer (Google Docs WebView), Gemini AI, MCQ exams with generated questions.

## Tech Stack
Kotlin · Jetpack Compose (custom `MutableStateFlow` routing, no Nav Compose) · Room · Retrofit + Moshi · Firebase REST Auth · Gemini API (`.env`)

## Project Structure
```
app/src/main/java/com/example/
├── MainActivity.kt              # Entry + bottom nav + AnimatedContent router
├── data/
│   ├── db/
│   │   ├── AppDatabase.kt       # Room DB v4, fallbackToDestructiveMigration
│   │   ├── Dao.kt               # PdfDao, SessionDao, StatsDao
│   │   └── Entities.kt          # 8 entities: PdfItem, Bookmark, Progress, UserSession, AttemptHistory, DailyChallenge, StreakStats, DailyChallengeQuestion
│   ├── model/RemoteConfig.kt    # RemotePdfItem, RemoteDailyChallenge, RemoteConfig (Moshi)
│   ├── net/
│   │   ├── UpdateService.kt     # Retrofit GET for remote JSON
│   │   └── FirebaseAuthService.kt # Retrofit POST to identitytoolkit.googleapis.com
│   └── repo/
│       ├── AuthRepository.kt    # Firebase verify + role from admin list
│       └── PdfRepository.kt     # Sync, seed, bookmarks, progress, prefs
├── ui/
│   ├── screens/
│   │   ├── LoginScreen.kt       # Email/password → Firebase → role
│   │   ├── DashboardScreen.kt   # Stats, streak, challenge, recent papers
│   │   ├── PapersLibraryScreen.kt # Grid with JEE/NEET/Boards/SAT filters
│   │   ├── ExamAttemptScreen.kt # MCQ with timer, flagging, question grid
│   │   ├── PdfViewScreen.kt     # WebView via Google Docs Viewer
│   │   ├── DailyChallengeScreen.kt
│   │   ├── AnalyticsScreen.kt   # Charts from attempt data
│   │   └── SettingsScreen.kt    # Admin: sync URL, whitelist CRUD
│   ├── theme/ (Color.kt, Theme.kt, Type.kt)
│   └── viewmodel/PrepViewModel.kt # Shared ViewModel for all screens
└── res/
```

## Screen Navigation (StateFlow)
`"login"` → `"dashboard"` → `"library"` | `"attempt"` | `"pdf_viewer"` | `"analytics"` | `"challenge"` | `"settings"`
- `navigateTo(screen)` — clears stack for login/dashboard, pushes others
- `navigateBack()` — pops stack

## Paper Click Routing (`openPaper`)
- `examType == "mcq"` → `startExamAttempt()` (generates subject MCQs, 30-min timer)
- `examType == "pdf"` (default) → `selectPdf()` → `PdfViewScreen`

## Sync Flow
1. On **startup**: `syncRemoteConfig()` fetches from default URL
2. Default URL: `https://raw.githubusercontent.com/IamShivanshCoder/prepmaster/refs/heads/main/prep_database.json`
3. Parses `RemoteConfig` → clears & seeds Room PDFs, updates whitelist/admins in SharedPrefs, inserts daily challenges
4. Refresh button (🔄) next to paper count in Papers Library manually re-syncs
4. Admin can override URL in Settings → "Sync Database & Whitelist"

## Auth Flow (Firebase-only)
- Firebase validates credentials — single source of truth
- JSON `admins` list determines role (no passwords in JSON)
- Success + in `admins` → admin; success + not in `admins` → user; not in Firebase → rejected
- Creates `UserSessionEntity` in Room on success

## Room Entities
- **PdfItemEntity**: id, title, subject, category, examType, year, url, size
- **BookmarkEntity**: pdfId (FK), bookmarkedAt
- **ProgressEntity**: pdfId (FK), lastPageRead, totalPages, lastStudiedAt
- **UserSessionEntity**: email (PK), displayName, loginTime, allowedByRemoteWhitelist, role
- **AttemptHistoryEntity**: id (auto), pdfId, examName, subject, score, totalQuestions, timeSpentSeconds, completedAt
- **DailyChallengeEntity**: dateKey (PK), completed, correct, completedAt, timeTakenSeconds, selectedOptionIndex
- **StreakStatsEntity**: id (PK=1), currentStreak, lastActiveDate, mon-sun booleans
- **DailyChallengeQuestionEntity**: id (PK), dateKey, subject, topic, question, optionsList (||joined), correctIndex, explanation, avgTimeMinutes

## Key ViewModel StateFlows
- `currentScreen`, `screenBackStack` — nav
- `allPdfs`, `bookmarkedPdfs`, `activePdfDetail` — PDFs
- `allAttempts`, `dailyStreakStats` — stats
- `syncState`, `configUrlInput`, `whitelistedEmails` — admin
- `dailyChallengeQuestion/State/SelectedOption/Answered` — challenge
- `examQuestions`, `currentQuestionIndex`, `examSelectedOptions`, `examFlaggedQuestions`, `examTimeRemaining` — exam
- `activeSession` — logged-in user

## Theme Colors
| Name | Hex | Usage |
|------|-----|-------|
| `PrimaryAccentAmber` | `#F59E0B` | CTAs, highlights, selected |
| `SecondaryViolet` | `#6C63FF` | Secondary accents |
| `BackgroundDeepNavy` | `#0A0A1A` | Main bg |
| `SurfaceNavy` | `#111128` | Card surfaces |
| `CorrectGreen` | `#16A34A` | Success |
| `TextMuted` | `#8888AA` | Secondary text |
| `ErrorRed` | `#DC2626` | Errors |

## Important Notes
- `fallbackToDestructiveMigration()` — schema changes wipe DB
- Exam questions generated locally via `generateQuestionsForSubject()` (Physics/Chemistry/Maths/CS keyword match)
- Timer runs on Main dispatcher in `viewModelScope`
- Streak: date keys (`yyyy-MM-dd`), inc if yesterday active, reset otherwise
- Bottom bar hidden on login/attempt/pdf_viewer
- Sync state auto-clears after 4s in SettingsScreen
- `.env` needs `GEMINI_API_KEY` and `FIREBASE_API_KEY` (real key is in `.env` already)
- Default accounts must exist in Firebase Console; passwords NOT in JSON
