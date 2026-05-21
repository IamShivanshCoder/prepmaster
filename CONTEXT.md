# PrepMaster — Project Context

## Overview
PrepMaster is a secure Android exam preparation application with Google login whitelist, PDF study materials, offline syllabus cards, dynamic sync via a remote JSON index (GitHub Gist), and Gemini AI API integration.

## Tech Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose (no Jetpack Navigation — uses custom string-based screen routing via `MutableStateFlow`)
- **Database**: Room (SQLite)
- **Networking**: Retrofit + Moshi
- **PDF Viewing**: WebView with Google Docs Viewer (`https://docs.google.com/viewer?url=...&embedded=true`)
- **Auth**: Google Sign-In with custom whitelist validation
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
│   │   └── UpdateService.kt             # Retrofit service for remote config fetch
│   └── repo/
│       ├── AuthRepository.kt            # Authentication + whitelist logic
│       └── PdfRepository.kt             # PDF data repo, sync, seeding, bookmarks, progress
├── ui/
│   ├── screens/
│   │   ├── AnalyticsScreen.kt           # Score charts, attempt history
│   │   ├── DailyChallengeScreen.kt      # Daily MCQ quiz
│   │   ├── DashboardScreen.kt           # Home screen with stats, streak, recent papers
│   │   ├── ExamAttemptScreen.kt         # MCQ exam with timer, flagging, question grid
│   │   ├── LoginScreen.kt               # Google sign-in with whitelist check
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
| `"login"` | `LoginScreen` | Google sign-in with whitelist validation |
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
- **`RemoteConfig`** — whitelist, pdfs, dailyChallenges

### ViewModel Models (`PrepViewModel.kt`)
- **`ExamQuestion`** — id, text, options, correctAnswerIndex, explanation
- **`SyncState`** — sealed interface: Idle, Syncing, Success(message), Error(error)

## Key Navigation Methods in PrepViewModel
- `navigateTo(screen: String)` — pushes screen, clears stack for login/dashboard
- `navigateBack()` — pops back stack
- `selectPdf(pdfId: String)` — sets selected PDF, navigates to `"pdf_viewer"`
- `openPaper(pdfId, title, subject)` — **ROUTING**: checks `examType` → `"mcq"` → `startExamAttempt()`, else → `selectPdf()`
- `startExamAttempt(pdfId, examName, subject)` — generates subject-based MCQs, starts 30-min timer, navigates to `"attempt"`
- `selectSubject(subject)` — navigates to `"library"` pre-filtered

## Paper Click Routing (IMPORTANT — Recent Fix)
**"Start Paper" buttons call `viewModel.openPaper()` which routes by `examType`:**
- `examType == "pdf"` → `PdfViewScreen` (WebView with Google Docs Viewer)
- `examType == "mcq"` → `ExamAttemptScreen` (MCQ exam with generated questions)
- `examType == "Daily Challenge"` → `ExamAttemptScreen` (MCQ)

The `examType` field defaults to `"pdf"` if missing from JSON.

## Remote Sync Flow
1. Admin enters raw Gist URL in Settings
2. `syncDatabase()` → `PdfRepository.syncRemoteConfig()` → Retrofit fetches JSON
3. Parses `RemoteConfig` → clears and seeds Room DB with new PDFs
4. Updates whitelist in SharedPreferences
5. Syncs daily challenges into `DailyChallengeQuestionEntity` table
6. Default config URL: `https://raw.githubusercontent.com/aistudio-templates/mock-data/main/exam_prep_config.json`

## Database Seeding
`PdfRepository.seedDatabaseIfEmpty()` inserts 9 sample PDF items on first launch (all with `examType = "pdf"` + 1 MCQ entry). All use dummy PDF URLs from w3.org.

## Auth Flow
1. User enters email + password on LoginScreen
2. Checks whitelist (SharedPreferences) OR hardcoded admin emails
3. If first login for email, stores password in SharedPreferences
4. Subsequent logins verify password against stored value
5. Creates `UserSessionEntity` in Room
6. Updates streak stats on login

## Theme Colors
- `PrimaryAccentAmber` — `#f59e0b` (CTAs, highlights)
- `SecondaryViolet` — `#8b5cf6` (secondary accents)
- `BackgroundDeepNavy` — `#0a0a1a` (main background)
- `SurfaceNavy` — `#111128` (card surfaces)
- `CorrectGreen` — `#22c55e` (success states)
- `TextMuted` — `#8888aa` (secondary text)

## Setup Instructions
1. Open in Android Studio → let it auto-fix imports
2. Create `.env` file with `GEMINI_API_KEY=your_key`
3. Remove `signingConfig = signingConfigs.getByName("debugConfig")` from `app/build.gradle.kts`
4. Run on emulator/device
5. Set up GitHub Gist with `prep_database.json` (see `prep_database.json` template in project root)
6. Paste raw Gist URL in app Settings → Verify & Dynamic Cache Sync

## Google Drive PDF Hosting
Convert share link `https://drive.google.com/file/d/FILE_ID/view?usp=sharing`
to direct download: `https://docs.google.com/uc?export=download&id=FILE_ID`

## JSON Database Schema
```json
{
  "whitelist": ["email1@gmail.com", "email2@gmail.com"],
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
- Timer runs in `viewModelScope` with `Dispatchers.Default`, auto-submits at 0
- Streak tracking uses date keys (`yyyy-MM-dd`) — increments if yesterday was active, resets otherwise
- Bookmark and progress are stored locally in Room — works 100% offline after sync
- The app uses `AnimatedContent` for screen transitions in `MainActivity.kt`
- Bottom navigation bar is hidden on login, attempt, and pdf_viewer screens
