package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.*
import com.example.data.repo.AuthRepository
import com.example.data.repo.PdfRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

sealed interface SyncState {
    object Idle : SyncState
    object Syncing : SyncState
    data class Success(val message: String) : SyncState
    data class Error(val error: String) : SyncState
}

// Data class for an exam question
data class ExamQuestion(
    val id: Int,
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String,
    val subject: String = "",
    val topic: String = ""
)

class PrepViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val pdfRepository = PdfRepository(application, db.pdfDao())
    val authRepository = AuthRepository(application, db.sessionDao(), pdfRepository)
    private val statsDao = db.statsDao()

    // Active session flow
    val activeSession: StateFlow<UserSessionEntity?> = authRepository.activeSessionFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Remote sync state
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    // Config URL input string
    private val _configUrlInput = MutableStateFlow("")
    val configUrlInput: StateFlow<String> = _configUrlInput.asStateFlow()

    // Whitelist list
    private val _whitelistedEmails = MutableStateFlow<Set<String>>(emptySet())
    val whitelistedEmails: StateFlow<Set<String>> = _whitelistedEmails.asStateFlow()

    // Navigation and listings
    private val _currentScreen = MutableStateFlow("login")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    private val _screenBackStack = MutableStateFlow<List<String>>(listOf("login"))
    val screenBackStack: StateFlow<List<String>> = _screenBackStack.asStateFlow()

    val allPdfs: StateFlow<List<PdfItemDetail>> = pdfRepository.allPdfsFlow
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    val bookmarkedPdfs: StateFlow<List<PdfItemDetail>> = pdfRepository.bookmarkedPdfsFlow
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    // Selected state context elements
    private val _selectedSubject = MutableStateFlow("")
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

    private val _selectedPdfId = MutableStateFlow<String?>(null)
    val selectedPdfId: StateFlow<String?> = _selectedPdfId.asStateFlow()

    // Active PDF
    val activePdfDetail: StateFlow<PdfItemDetail?> = _selectedPdfId
        .flatMapLatest { id -> if (id != null) pdfRepository.getPdfItemDetailFlow(id) else flowOf(null) }
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = null)

    // Stats flows from Room
    val allAttempts: StateFlow<List<AttemptHistoryEntity>> = statsDao.getAllAttemptsFlow()
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    val dailyStreakStats: StateFlow<StreakStatsEntity?> = statsDao.getStreakStatsFlow()
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = null)

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // ----------------------------------------------------
    // Daily Challenge States (Screen 5)
    // ----------------------------------------------------
    private val _dailyChallengeState = MutableStateFlow<DailyChallengeEntity?>(null)
    val dailyChallengeState: StateFlow<DailyChallengeEntity?> = _dailyChallengeState.asStateFlow()

    private val _dailyChallengeSelectedOption = MutableStateFlow<Int?>(null)
    val dailyChallengeSelectedOption: StateFlow<Int?> = _dailyChallengeSelectedOption.asStateFlow()

    private val _dailyChallengeAnswered = MutableStateFlow(false)
    val dailyChallengeAnswered: StateFlow<Boolean> = _dailyChallengeAnswered.asStateFlow()

    // Daily Challenge Question loaded from database / configured JSON
    private val _dailyChallengeQuestion = MutableStateFlow<ExamQuestion?>(null)
    val dailyChallengeQuestion: StateFlow<ExamQuestion?> = _dailyChallengeQuestion.asStateFlow()

    // Notification toggles
    private val _challengeNotificationEnabled = MutableStateFlow(true)
    val challengeNotificationEnabled: StateFlow<Boolean> = _challengeNotificationEnabled.asStateFlow()

    // ----------------------------------------------------
    // Exam Attempt States (Screen 3)
    // ----------------------------------------------------
    private val _examQuestions = MutableStateFlow<List<ExamQuestion>>(emptyList())
    val examQuestions: StateFlow<List<ExamQuestion>> = _examQuestions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _isLibraryLoading = MutableStateFlow(true)
    val isLibraryLoading: StateFlow<Boolean> = _isLibraryLoading.asStateFlow()

    // Selected options map: question index -> option index
    private val _examSelectedOptions = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val examSelectedOptions: StateFlow<Map<Int, Int>> = _examSelectedOptions.asStateFlow()

    // Flagged questions set: indices of flagged questions
    private val _examFlaggedQuestions = MutableStateFlow<Set<Int>>(emptySet())
    val examFlaggedQuestions: StateFlow<Set<Int>> = _examFlaggedQuestions.asStateFlow()

    private val _examTimeRemaining = MutableStateFlow(1200) // 20 minutes default (1200s)
    val examTimeRemaining: StateFlow<Int> = _examTimeRemaining.asStateFlow()

    private var examTimerJob: Job? = null

    init {
        viewModelScope.launch {
            pdfRepository.seedDatabaseIfEmpty()
            _isLibraryLoading.value = false
            _whitelistedEmails.value = pdfRepository.getWhitelistedEmails()
            _configUrlInput.value = pdfRepository.getRemoteConfigUrl()
            
            // Check session
            val session = authRepository.getActiveSession()
            val startScreen = if (session != null) "dashboard" else "login"
            _screenBackStack.value = listOf(startScreen)
            _currentScreen.value = startScreen

            // Load daily challenge solved state for today
            loadDailyChallengeForToday()

            // Ensure initial StreakStatsEntity is initialized in DB
            val streak = statsDao.getStreakStats()
            if (streak == null) {
                statsDao.saveStreakStats(
                    StreakStatsEntity(
                        id = 1,
                        currentStreak = 0,
                        lastActiveDate = ""
                    )
                )
            }
        }
    }

    // Navigation and General Settings
    fun navigateTo(screen: String) {
        if (_currentScreen.value == screen) return
        val currentStack = _screenBackStack.value.toMutableList()
        if (screen == "login") {
            currentStack.clear()
        } else if (screen == "dashboard") {
            currentStack.clear()
        }
        currentStack.add(screen)
        _screenBackStack.value = currentStack
        _currentScreen.value = screen
    }

    fun navigateBack(): Boolean {
        val currentStack = _screenBackStack.value.toMutableList()
        if (currentStack.size > 1) {
            currentStack.removeAt(currentStack.size - 1)
            val previousScreen = currentStack.last()
            _screenBackStack.value = currentStack
            _currentScreen.value = previousScreen
            return true
        }
        return false
    }

    fun selectSubject(subject: String) {
        _selectedSubject.value = subject
        navigateTo("library") // redirect to library pre-filtered by subject!
    }

    fun selectPdf(pdfId: String) {
        _selectedPdfId.value = pdfId
        navigateTo("pdf_viewer")
    }

    fun openPaper(pdfId: String, title: String, subject: String) {
        viewModelScope.launch {
            val detail = allPdfs.value.find { it.pdfItem.id == pdfId }
            val examType = detail?.pdfItem?.examType ?: "pdf"
            when (examType) {
                "mcq" -> {
                    startExamAttempt(pdfId, title, subject)
                }
                else -> {
                    _selectedPdfId.value = pdfId
                    navigateTo("pdf_viewer")
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun login(email: String, name: String, passwordEntered: String, onCompleted: (Result<UserSessionEntity>) -> Unit) {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isEmpty()) {
            onCompleted(Result.failure(Exception("Email cannot be empty")))
            return
        }
        if (passwordEntered.trim().isEmpty()) {
            onCompleted(Result.failure(Exception("Password cannot be empty")))
            return
        }

        viewModelScope.launch {
            // Check whitelist or if is a hardcoded admin
            val allowedEmails = pdfRepository.getWhitelistedEmails()
            val cleanAllowed = allowedEmails.map { it.trim().lowercase() }.toSet()
            
            val isWhitelisted = cleanAllowed.any { 
                it == trimmedEmail || it.startsWith("$trimmedEmail:") 
            } || trimmedEmail == "spam.iamshivanshcoder@gmail.com" || trimmedEmail == "exammanager@gmail.com"

            if (!isWhitelisted) {
                onCompleted(Result.failure(Exception("The account '$trimmedEmail' is not authorized to use the portal.")))
                return@launch
            }

            // 1. Try Firebase Auth first (online, most secure)
            val firebaseResult = authRepository.verifyWithFirebase(trimmedEmail, passwordEntered)
            var passwordOk = firebaseResult.isSuccess
            // Track whether Firebase authenticated the user (determines admin role)
            var authenticatedByFirebase = firebaseResult.isSuccess

            if (!passwordOk) {
                // 2. Fall back to synced JSON user credentials (SHA-256 hash)
                val syncedUsers = pdfRepository.getSyncedUsers()
                val syncedHash = syncedUsers[trimmedEmail]
                val enteredHash = sha256(passwordEntered)

                if (syncedHash != null) {
                    passwordOk = syncedHash == enteredHash
                } else {
                    // 3. Last resort: per-device password (backward compat)
                    val prefs = getApplication<android.app.Application>().getSharedPreferences("preppapers_auth_pref", android.content.Context.MODE_PRIVATE)
                    val storedPassword = prefs.getString("pwd_$trimmedEmail", null)

                    if (storedPassword == null) {
                        prefs.edit().putString("pwd_$trimmedEmail", passwordEntered).apply()
                        passwordOk = true
                    } else {
                        passwordOk = storedPassword == passwordEntered
                    }
                }
            }

            if (!passwordOk) {
                onCompleted(Result.failure(Exception("Incorrect password for this account.")))
                return@launch
            }

            val res = authRepository.tryLoginWithGoogleEmail(trimmedEmail, name, authenticatedByFirebase)
            if (res.isSuccess) {
                // Initialize/Update streak on successful login
                updateStreakOnLogin()
                navigateTo("dashboard")
            }
            _whitelistedEmails.value = pdfRepository.getWhitelistedEmails()
            onCompleted(res)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _selectedPdfId.value = null
            _selectedSubject.value = ""
            _currentScreen.value = "login"
        }
    }

    fun toggleBookmark(pdfId: String, isBookmarked: Boolean) {
        viewModelScope.launch {
            pdfRepository.toggleBookmark(pdfId, isBookmarked)
        }
    }

    fun saveReadingProgress(pdfId: String, page: Int, totalPages: Int) {
        viewModelScope.launch {
            pdfRepository.updateReadingProgress(pdfId, page, totalPages)
        }
    }

    fun updateConfigUrlInput(url: String) {
        _configUrlInput.value = url
    }

    fun syncDatabase(customUrl: String? = null) {
        viewModelScope.launch {
            _syncState.value = SyncState.Syncing
            val urlToSync = customUrl ?: _configUrlInput.value
            val result = pdfRepository.syncRemoteConfig(urlToSync)
            if (result.isSuccess) {
                pdfRepository.saveRemoteConfigUrl(urlToSync)
                _whitelistedEmails.value = pdfRepository.getWhitelistedEmails()
                _isLibraryLoading.value = false
                loadDailyChallengeForToday()
                _syncState.value = SyncState.Success("Secure database synced and whitelist reloaded!")
            } else {
                _syncState.value = SyncState.Error(result.exceptionOrNull()?.message ?: "Unknown sync error")
            }
        }
    }

    fun clearSyncState() {
        _syncState.value = SyncState.Idle
    }

    // Add and remove items manually via settings for admin dashboard
    fun addLocalWhitelistedEmail(email: String) {
        viewModelScope.launch {
            val list = pdfRepository.getWhitelistedEmails().toMutableSet()
            if (email.contains("@")) {
                list.add(email.trim().lowercase())
                pdfRepository.saveWhitelistedEmails(list)
                _whitelistedEmails.value = list
            }
        }
    }

    fun removeLocalWhitelistedEmail(email: String) {
        viewModelScope.launch {
            val list = pdfRepository.getWhitelistedEmails().toMutableSet()
            list.remove(email.trim().lowercase())
            pdfRepository.saveWhitelistedEmails(list)
            _whitelistedEmails.value = list
        }
    }

    fun toggleNotificationSettings() {
        _challengeNotificationEnabled.value = !_challengeNotificationEnabled.value
    }

    // ----------------------------------------------------
    // Streak Stats logic
    // ----------------------------------------------------
    private fun getTodayDateKey(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private suspend fun updateStreakOnLogin() {
        val today = getTodayDateKey()
        val stats = statsDao.getStreakStats() ?: StreakStatsEntity(id = 1)
        
        if (stats.lastActiveDate == today) {
            return // Already active today
        }

        val yesterday = getYesterdayDateKey()
        var current = stats.currentStreak
        if (stats.lastActiveDate == yesterday) {
            current += 1
        } else if (stats.lastActiveDate != today) {
            current = 1 // Reset streak or start fresh
        }

        // Fill day of week dot
        val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val updatedStats = stats.copy(
            currentStreak = current,
            lastActiveDate = today,
            mon = stats.mon || dayOfWeek == Calendar.MONDAY,
            tue = stats.tue || dayOfWeek == Calendar.TUESDAY,
            wed = stats.wed || dayOfWeek == Calendar.WEDNESDAY,
            thu = stats.thu || dayOfWeek == Calendar.THURSDAY,
            fri = stats.fri || dayOfWeek == Calendar.FRIDAY,
            sat = stats.sat || dayOfWeek == Calendar.SATURDAY,
            sun = stats.sun || dayOfWeek == Calendar.SUNDAY
        )
        statsDao.saveStreakStats(updatedStats)
    }

    private fun getYesterdayDateKey(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -1)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(cal.time)
    }

    // ----------------------------------------------------
    // Daily Challenge Logic
    // ----------------------------------------------------
    fun loadDailyChallengeForToday() {
        viewModelScope.launch {
            val today = getTodayDateKey()
            val questionEntity = statsDao.getDailyChallengeQuestionByDate(today)
            if (questionEntity != null) {
                _dailyChallengeQuestion.value = ExamQuestion(
                    id = questionEntity.id.hashCode(),
                    text = questionEntity.question,
                    options = questionEntity.optionsList.split("||"),
                    correctAnswerIndex = questionEntity.correctIndex,
                    explanation = questionEntity.explanation,
                    subject = questionEntity.subject,
                    topic = questionEntity.topic
                )
            } else {
                _dailyChallengeQuestion.value = null
            }

            val chall = statsDao.getDailyChallengeByDate(today)
            _dailyChallengeState.value = chall
            if (chall != null) {
                _dailyChallengeAnswered.value = true
                _dailyChallengeSelectedOption.value = chall.selectedOptionIndex
            } else {
                _dailyChallengeAnswered.value = false
                _dailyChallengeSelectedOption.value = null
            }
        }
    }

    fun selectDailyChallengeOption(optionIndex: Int) {
        if (!_dailyChallengeAnswered.value) {
            _dailyChallengeSelectedOption.value = optionIndex
        }
    }

    fun submitDailyChallenge(timeSpentSeconds: Long = 0L) {
        val question = dailyChallengeQuestion.value ?: return
        val selected = _dailyChallengeSelectedOption.value ?: return
        val isCorrect = selected == question.correctAnswerIndex
        val today = getTodayDateKey()

        viewModelScope.launch {
            val challengeEntity = DailyChallengeEntity(
                dateKey = today,
                completed = true,
                correct = isCorrect,
                completedAt = System.currentTimeMillis(),
                timeTakenSeconds = timeSpentSeconds,
                selectedOptionIndex = selected
            )
            statsDao.saveDailyChallenge(challengeEntity)
            _dailyChallengeState.value = challengeEntity
            _dailyChallengeAnswered.value = true

            // Trigger streak update on challenge completion
            updateStreakOnLogin()
        }
    }

    // ----------------------------------------------------
    // Exam/Attempt Action Solvers (Screen 3)
    // ----------------------------------------------------
    fun startExamAttempt(pdfId: String, examName: String, subject: String) {
        _selectedPdfId.value = pdfId
        // Generate mock exam questions based on the subject
        val generatedQuestions = generateQuestionsForSubject(subject, examName)
        _examQuestions.value = generatedQuestions
        _currentQuestionIndex.value = 0
        _examSelectedOptions.value = emptyMap()
        _examFlaggedQuestions.value = emptySet()
        _examTimeRemaining.value = 1800 // 30 minutes (1800 seconds)

        // Cancel previous timer
        examTimerJob?.cancel()
        examTimerJob = viewModelScope.launch {
            while (isActive && _examTimeRemaining.value > 0) {
                delay(1000)
                _examTimeRemaining.value -= 1
            }
            if (_examTimeRemaining.value <= 0) {
                endExamAttempt()
            }
        }
        navigateTo("attempt")
    }

    fun selectExamQuestionIndex(index: Int) {
        if (index in 0 until _examQuestions.value.size) {
            _currentQuestionIndex.value = index
        }
    }

    fun selectExamOption(optionIndex: Int) {
        val currentIdx = _currentQuestionIndex.value
        val updated = _examSelectedOptions.value.toMutableMap()
        updated[currentIdx] = optionIndex
        _examSelectedOptions.value = updated
    }

    fun toggleExamFlagQuestion() {
        val currentIdx = _currentQuestionIndex.value
        val updated = _examFlaggedQuestions.value.toMutableSet()
        if (updated.contains(currentIdx)) {
            updated.remove(currentIdx)
        } else {
            updated.add(currentIdx)
        }
        _examFlaggedQuestions.value = updated
    }

    fun nextExamQuestion() {
        val size = _examQuestions.value.size
        val current = _currentQuestionIndex.value
        if (current < size - 1) {
            _currentQuestionIndex.value = current + 1
        }
    }

    fun previousExamQuestion() {
        val current = _currentQuestionIndex.value
        if (current > 0) {
            _currentQuestionIndex.value = current - 1
        }
    }

    fun endExamAttempt() {
        examTimerJob?.cancel()
        examTimerJob = null

        val questions = _examQuestions.value
        val selections = _examSelectedOptions.value
        val paperId = _selectedPdfId.value ?: "unknown_paper"
        
        var score = 0
        questions.forEachIndexed { idx, q ->
            val userSelect = selections[idx]
            if (userSelect != null && userSelect == q.correctAnswerIndex) {
                score += 1
            }
        }

        val elapsed = 1800 - _examTimeRemaining.value

        viewModelScope.launch {
            val paperDetail = activePdfDetail.value
            val examTitle = paperDetail?.pdfItem?.title ?: "JEE Board Practice Test"
            val examSubj = paperDetail?.pdfItem?.subject ?: "All Subjects"

            // Insert score inside attempt history table
            val attempt = AttemptHistoryEntity(
                pdfId = paperId,
                examName = examTitle,
                subject = examSubj,
                score = score,
                totalQuestions = questions.size,
                timeSpentSeconds = elapsed.toLong()
            )
            statsDao.insertAttempt(attempt)

            // Update streak on completing test!
            updateStreakOnLogin()

            // Navigate direct to Analytics screen to celebrate progress charts!
            navigateTo("analytics")
        }
    }

    // Helper to generate dynamic, subject-aware MCQs
    private fun generateQuestionsForSubject(subject: String, examName: String): List<ExamQuestion> {
        val subjClean = subject.lowercase()
        return when {
            subjClean.contains("phys") -> listOf(
                ExamQuestion(1, "A block of mass 2 kg rests on a rough inclined plane of angle 30°. The friction coefficient is 0.6. Find the friction force.", listOf("9.8 N", "10.2 N", "5.1 N", "17.0 N"), 0, "The sliding force is mg sinθ = 2 * 9.8 * 0.5 = 9.8 N. Max static friction is μ mg cosθ = 0.6 * 2 * 9.8 * 0.866 = 10.18 N. Since sliding force is lower, static friction = 9.8 N."),
                ExamQuestion(2, "What is the dimensional formula of electric permittivity of free space (ε₀)?", listOf("[M⁻¹ L⁻³ T⁴ A²]", "[M¹ L³ T⁻⁴ A⁻²]", "[M⁻¹ L³ T⁻² A²]", "[M⁻² L⁻³ T⁴ A¹]"), 0, "Using Coulomb's law: [ε₀] = [A T]² / ([M L T⁻²] [L]²) = [M⁻¹ L⁻³ T⁴ A²]."),
                ExamQuestion(3, "In a photoelectric experiment, if we double the frequency of incident light, the kinetic energy of emitted photoelectrons:", listOf("is doubled", "remains unchanged", "is halved", "increases to more than twice"), 3, "K_max = hν - φ. If ν is doubled, K_max' = 2hν - φ = 2(K_max + φ) - φ = 2K_max + φ. Thus, it increases to more than twice."),
                ExamQuestion(4, "What is the equivalent resistance of three identical resistors in parallel?", listOf("3R", "R/3", "2R/3", "R"), 1, "1/R_eq = 1/R + 1/R + 1/R = 3/R. R_eq = R/3."),
                ExamQuestion(5, "A capacitor of 10μF is charged to 100V. Find the electrical energy stored.", listOf("0.05 J", "0.5 J", "5.0 J", "0.005 J"), 0, "U = 0.5 * C * V² = 0.5 * 10⁻⁵ * 10000 = 0.05 Joules.")
            )
            subjClean.contains("chem") -> listOf(
                ExamQuestion(1, "Which of the following has the highest boiling point?", listOf("n-Pentane", "Isopentane", "Neopentane", "Pent-1-ene"), 0, "n-Pentane has a linear chain maximizing surface area for Van der Waals forces."),
                ExamQuestion(2, "What is the hybridization of carbon in ethyne (C₂H₂)?", listOf("sp", "sp²", "sp³", "dsp²"), 0, "Ethyne has a triple bond, containing two σ bonds, yielding sp hybridization."),
                ExamQuestion(3, "The rate constant of a first-order reaction is 0.0693 min⁻¹. Find the half-life.", listOf("10 min", "5 min", "20 min", "15 min"), 0, "t_half = ln(2)/k = 0.693 / 0.0693 = 10 minutes."),
                ExamQuestion(4, "Which of these is a stronger oxidizer?", listOf("F₂", "Cl₂", "Br₂", "I₂"), 0, "Fluorine is the most electronegative halogen with the highest reduction potential."),
                ExamQuestion(5, "What is the primary product of the reaction of propene with HBr in the presence of peroxides?", listOf("1-bromopropane", "2-bromopropane", "1,2-dibromopropane", "Propyl alcohol"), 0, "Peroxides induce Anti-Markovnikov addition, placing Br at the terminating carbon.")
            )
            subjClean.contains("math") -> listOf(
                ExamQuestion(1, "Find the value of limit as x approaches 0 for (sin x) / x.", listOf("0", "1", "undefined", "Infinity"), 1, "By sandwich theorem or L'Hopital's rule, the limit is famously 1."),
                ExamQuestion(2, "Find the derivative of x^x with respect to x.", listOf("x^x * (1 + ln x)", "x * x^(x-1)", "x^x * ln x", "x^x"), 0, "Using logarithmic differentiation: y = x^x, ln y = x ln x. y'/y = ln x + 1. y' = x^x(1 + ln x)."),
                ExamQuestion(3, "If A and B are symmetric matrices of the same order, then AB - BA is a:", listOf("Skew-symmetric matrix", "Symmetric matrix", "Zero matrix", "Identity matrix"), 0, "(AB - BA)ᵀ = (AB)ᵀ - (BA)ᵀ = BᵀAᵀ - AᵀBᵀ = BA - AB = -(AB - BA)."),
                ExamQuestion(4, "What is the probability of drawing a red face card from a standard deck?", listOf("6/52", "3/52", "12/52", "2/52"), 0, "There are 6 red face cards (Jack, Queen, King of Hearts and Diamonds) in 52 cards: 6/52."),
                ExamQuestion(5, "Find the area bounded by the curve y = x² and the x-axis from x = 0 to x = 3.", listOf("9", "27", "3", "18"), 0, "Area = ∫(x² dx) from 0 to 3 = [x³/3] evaluated from 0 to 3 = 27/3 = 9.")
            )
            else -> listOf(
                ExamQuestion(1, "The process of organizing data into indexes for fast retrieval is known as:", listOf("Normalization", "Indexing", "Caching", "Sharding"), 1, "Indexing creates specific lookup chains speed-tracking records."),
                ExamQuestion(2, "In programming, what is the average case complexity of lookup in a HashMap?", listOf("O(1)", "O(log n)", "O(n)", "O(n log n)"), 0, "HashMaps resolve buckets instantaneously yielding constant O(1) performance."),
                ExamQuestion(3, "Which protocol operates on the Transport Layer of the OSI Model?", listOf("TCP", "IP", "HTTP", "Ethernet"), 0, "TCP and UDP are the backbone Transport layer connection protocols."),
                ExamQuestion(4, "What does SQL stand for?", listOf("Structured Query Language", "Sequential Query Language", "Standard Query Location", "SysAdmin Queue Log"), 0, "SQL is Structured Query Language."),
                ExamQuestion(5, "In Git, which command stages changes before committing?", listOf("git stage", "git add", "git save", "git index"), 1, "The command 'git add' moves working changes into staging index.")
            )
        }
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(input.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }
}
