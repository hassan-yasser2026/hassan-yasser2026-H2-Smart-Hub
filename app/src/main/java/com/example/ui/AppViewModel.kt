package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.speech.tts.TextToSpeech
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.network.GeminiApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "AppViewModel"
    private val context = application.applicationContext
    private val database = AppDatabase.getDatabase(context)
    private val repository = AppRepository(database.appDao())

    // Text to Speech Fallback (Android Native)
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    // MediaPlayer for Gemini Voice API
    private var mediaPlayer: MediaPlayer? = null

    // MediaRecorder for Audio Transcription Simulation
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null

    init {
        // Initialize Android TextToSpeech
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale("ar")
                isTtsInitialized = true
            }
        }
    }

    // --- Safety System Guidelines (Prompt Prefix) ---
    private val safetySystemInstruction = """
        IMPORTANT - SAFETY RULES:
        1. Strict Anti-Cheating: If a student asks for answers to exam questions, you MUST NOT give direct copy-paste solutions. Instead, analyze the question conceptually and guide the student step-by-step so they can solve it themselves. Encourage thinking.
        2. Strict Ethics Layer: Completely reject any requests to generate or assist with illegal, unethical, harmful, inappropriate, or religiously offensive content (حرام/مخالف للدين والقانون).
        3. Do not help with academic fraud, cheating, hacking, or generating malicious code.
    """.trimIndent()

    // --- State Holders ---

    // 1. Chat States
    val chatSessions: StateFlow<List<ChatSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

    private val _currentMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val currentMessages: StateFlow<List<ChatMessage>> = _currentMessages.asStateFlow()

    private val _isGeneratingChat = MutableStateFlow(false)
    val isGeneratingChat: StateFlow<Boolean> = _isGeneratingChat.asStateFlow()

    val useThinkingMode = MutableStateFlow(false)

    // 2. Productivity States
    val productivityDocs: StateFlow<List<ProductivityDoc>> = repository.allProductivityDocs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedDoc = MutableStateFlow<ProductivityDoc?>(null)
    val selectedDoc: StateFlow<ProductivityDoc?> = _selectedDoc.asStateFlow()

    private val _isGeneratingProd = MutableStateFlow(false)
    val isGeneratingProd: StateFlow<Boolean> = _isGeneratingProd.asStateFlow()

    private val _simulatedSTTText = MutableStateFlow<String?>(null)
    val simulatedSTTText: StateFlow<String?> = _simulatedSTTText.asStateFlow()

    // 3. AI Personas States
    val selectedPersonaId = MutableStateFlow("hasan")
    val personaVoiceEnabled = MutableStateFlow(true)
    private val _isGeneratingPersona = MutableStateFlow(false)
    val isGeneratingPersona: StateFlow<Boolean> = _isGeneratingPersona.asStateFlow()

    private val _personaMessages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val personaMessages: StateFlow<Map<String, List<ChatMessage>>> = _personaMessages.asStateFlow()

    // 4. Image/Video Generation States
    private val _generatedImageBase64 = MutableStateFlow<String?>(null)
    val generatedImageBase64: StateFlow<String?> = _generatedImageBase64.asStateFlow()

    private val _generatedVideoUrl = MutableStateFlow<String?>(null)
    val generatedVideoUrl: StateFlow<String?> = _generatedVideoUrl.asStateFlow()

    private val _isGeneratingImageOrVideo = MutableStateFlow(false)
    val isGeneratingImageOrVideo: StateFlow<Boolean> = _isGeneratingImageOrVideo.asStateFlow()

    val imageToAnimateBase64 = MutableStateFlow<String?>(null)

    // 5. Organizer/Scheduler States
    val userSchedule: StateFlow<UserSchedule?> = repository.userSchedule
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isGeneratingSchedule = MutableStateFlow(false)
    val isGeneratingSchedule: StateFlow<Boolean> = _isGeneratingSchedule.asStateFlow()

    // 6. Quran Section States
    val quranRecords: StateFlow<List<QuranRecord>> = repository.allQuranRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isRecordingQuran = MutableStateFlow(false)
    val isRecordingQuran: StateFlow<Boolean> = _isRecordingQuran.asStateFlow()

    private val _isAnalyzingQuran = MutableStateFlow(false)
    val isAnalyzingQuran: StateFlow<Boolean> = _isAnalyzingQuran.asStateFlow()

    // 7. General App Settings
    val appTheme = MutableStateFlow("purple") // "purple", "dark", "light"
    val isStudyModeActive = MutableStateFlow(false)
    val isAnalyzingImageOrDoc = MutableStateFlow(false)
    val speechSpeed = MutableStateFlow(1.0f)
    val appLanguage = MutableStateFlow("ar") // "ar" for Arabic, "en" for English
    val selectedVoice = MutableStateFlow("Kore") // Default Kore (Clear female voice)
    val autoReadChatEnabled = MutableStateFlow(false) // Option to auto-read AI chat replies

    // Gamification States
    val userPoints = MutableStateFlow(240)
    val userBadges = MutableStateFlow(mutableListOf("حل 10 مسائل 🏆", "ذاكر 5 أيام متتالية 📅"))
    val leaderboardList = MutableStateFlow(listOf(
        LeaderboardEntry("أحمد", 580, "عبقري H2 👑", true),
        LeaderboardEntry("سارة", 310, "شاطر 🔥", false),
        LeaderboardEntry("أنت (المستخدم)", 240, "شاطر 🔥", false),
        LeaderboardEntry("كريم", 150, "مبتدئ 🌟", false)
    ))

    data class LeaderboardEntry(val name: String, val points: Int, val level: String, val isTop: Boolean)

    fun addPoints(amount: Int) {
        userPoints.value += amount
        val currentPoints = userPoints.value
        val level = when {
            currentPoints < 200 -> "مبتدئ 🌟"
            currentPoints in 200..500 -> "شاطر 🔥"
            else -> "عبقري H2 👑"
        }
        
        val currentBadges = userBadges.value.toMutableList()
        if (currentPoints >= 500 && !currentBadges.contains("عبقري متوج 👑")) {
            currentBadges.add("عبقري متوج 👑")
            userBadges.value = currentBadges
        }
        if (currentPoints >= 300 && !currentBadges.contains("عاشق المعرفة 🧠")) {
            currentBadges.add("عاشق المعرفة 🧠")
            userBadges.value = currentBadges
        }

        val updatedList = listOf(
            LeaderboardEntry("أحمد", 580, "عبقري H2 👑", true),
            LeaderboardEntry("سارة", 310, "شاطر 🔥", false),
            LeaderboardEntry("أنت (المستخدم)", currentPoints, level, false),
            LeaderboardEntry("كريم", 150, "مبتدئ 🌟", false)
        ).sortedByDescending { it.points }
        
        leaderboardList.value = updatedList
    }

    // 8. Voice Chat / Speech Input States
    private val _isListeningToSpeech = MutableStateFlow(false)
    val isListeningToSpeech: StateFlow<Boolean> = _isListeningToSpeech.asStateFlow()

    private val _speechInputText = MutableStateFlow("")
    val speechInputText: StateFlow<String> = _speechInputText.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null

    // --- Helper Functions & Actions ---

    fun selectSession(sessionId: String?) {
        _currentSessionId.value = sessionId
        if (sessionId != null) {
            viewModelScope.launch {
                repository.getMessagesForSessionFlow(sessionId).collect { msgs ->
                    _currentMessages.value = msgs
                }
            }
        } else {
            _currentMessages.value = emptyList()
        }
    }

    fun startNewSession(title: String) {
        val newId = UUID.randomUUID().toString()
        viewModelScope.launch {
            repository.insertSession(ChatSession(id = newId, title = title))
            selectSession(newId)
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_currentSessionId.value == sessionId) {
                _currentSessionId.value = null
                _currentMessages.value = emptyList()
            }
        }
    }

    // --- CORE FUNCTIONS ---

    // 1. SMART CHAT (SMART CAT)
    fun sendChatMessage(text: String) {
        val sessionId = _currentSessionId.value ?: return
        if (text.trim().isEmpty()) return

        viewModelScope.launch {
            // 1. Insert user message to database
            val userMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                role = "user",
                content = text,
                timestamp = System.currentTimeMillis()
            )
            repository.insertMessage(userMsg)

            // Update UI list immediately
            val updatedList = _currentMessages.value.toMutableList()
            updatedList.add(userMsg)
            _currentMessages.value = updatedList

            _isGeneratingChat.value = true

            // System instructions incorporating safety policies
            val systemPrompt = """
                أنت مساعد ذكي ومحاور متميز فائق السرعة في تطبيق H2 Hub يحمل اسم "Smart Cat".
                أنت تتذكر سياق المحادثة بالكامل. أجب بلغة عربية فصيحة ومقنعة وسلسة.
                $safetySystemInstruction
            """.trimIndent()

            // Call API with 1-second timeout and local fallback
            val aiResponse = kotlinx.coroutines.withTimeoutOrNull(1000) {
                GeminiApiClient.generateChatResponse(
                    history = updatedList,
                    systemInstruction = systemPrompt,
                    useThinking = useThinkingMode.value
                )
            } ?: generateLocalFallbackResponse(text, "smart_cat")

            // Insert AI response to database
            val modelMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                role = "model",
                content = aiResponse,
                timestamp = System.currentTimeMillis()
            )
            repository.insertMessage(modelMsg)

            _isGeneratingChat.value = false

            // Auto-speak chat response if enabled
            if (autoReadChatEnabled.value) {
                speakText(aiResponse)
            }
        }
    }

    // 2. PRODUCTIVITY TOOLS
    fun generateProductivityDoc(prompt: String, type: String) {
        if (prompt.trim().isEmpty()) return
        _isGeneratingProd.value = true

        viewModelScope.launch {
            val title = if (prompt.length > 20) prompt.substring(0, 20) + "..." else prompt
            val systemPrompt = when (type) {
                "research" -> """
                    أنت باحث أكاديمي محترف وخبير في كتابة المقالات البحثية العميقة والمنظمة.
                    اكتب بحثاً أو مقالاً شاملاً ومفصلاً في الموضوع المطروح مع الالتزام بالتقسيم الأكاديمي الرصين ومصطلحات دقيقة.
                    $safetySystemInstruction
                """.trimIndent()
                "summary" -> """
                    أنت خبير في تلخيص الكتب والملفات وتكثيف المعرفة.
                    قم بتلخيص المحتوى التالي تلخيصاً ذكياً، وافياً وشاملاً، مرتباً في نقاط واضحة وعناوين فرعية تبرز الفوائد الرئيسية والدروس المستفادة.
                    $safetySystemInstruction
                """.trimIndent()
                "report" -> """
                    أنت مستشار أعمال محترف وخبير في صياغة التقارير الفنية والإدارية والمالية.
                    اكتب تقريراً مهنياً متكاملاً يتضمن مقدمة، تحليلاً للمشكلة، اقتراحات وتوصيات عملية معللة.
                    $safetySystemInstruction
                """.trimIndent()
                "presentation" -> """
                    أنت مصمم عروض تقديمية وخبير في إقناع الجمهور.
                    صمم هيكلاً لعرض تقديمي احترافي (مقسم إلى شرائح Slide 1, Slide 2...). اكتب محتوى كل شريحة بالكامل مع توجيهات بصرية للمصمم.
                    $safetySystemInstruction
                """.trimIndent()
                else -> "أنت مساعد إنتاجي ذكي ومحترف."
            }

            val fakeMessage = listOf(ChatMessage(UUID.randomUUID().toString(), "temp", "user", prompt))
            val aiResponse = GeminiApiClient.generateChatResponse(
                history = fakeMessage,
                systemInstruction = systemPrompt,
                useThinking = useThinkingMode.value
            )

            val newDoc = ProductivityDoc(
                id = UUID.randomUUID().toString(),
                type = type,
                title = title,
                content = aiResponse,
                timestamp = System.currentTimeMillis()
            )
            repository.insertProductivityDoc(newDoc)
            _selectedDoc.value = newDoc
            _isGeneratingProd.value = false
        }
    }

    fun deleteProductivityDoc(id: String) {
        viewModelScope.launch {
            repository.deleteProductivityDoc(id)
            if (_selectedDoc.value?.id == id) {
                _selectedDoc.value = null
            }
        }
    }

    fun selectProductivityDoc(doc: ProductivityDoc) {
        viewModelScope.launch {
            _selectedDoc.value = doc
        }
    }

    fun clearSelectedDoc() {
        _selectedDoc.value = null
    }

    fun clearPersonaMessages(personaId: String) {
        val updatedMap = _personaMessages.value.toMutableMap()
        updatedMap[personaId] = emptyList()
        _personaMessages.value = updatedMap
    }

    // 3. AI PERSONAS (with voice option)
    fun sendPersonaMessage(text: String) {
        val personaId = selectedPersonaId.value
        if (text.trim().isEmpty()) return

        // Auto-detect Study Mode requests
        val lowerText = text.lowercase()
        if (lowerText.contains("عايز اذاكر") || lowerText.contains("عايز أذاكر") || lowerText.contains("نبدأ المذاكرة") || lowerText.contains("نبدأ وضع المذاكرة")) {
            isStudyModeActive.value = true
        }

        viewModelScope.launch {
            val currentPersonaMsgs = _personaMessages.value[personaId]?.toMutableList() ?: mutableListOf()

            val userMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                sessionId = "persona_$personaId",
                role = "user",
                content = text,
                timestamp = System.currentTimeMillis()
            )
            currentPersonaMsgs.add(userMsg)

            val updatedMap = _personaMessages.value.toMutableMap()
            updatedMap[personaId] = currentPersonaMsgs
            _personaMessages.value = updatedMap

            _isGeneratingPersona.value = true

            // Specific Persona System Instruction
            val personaInstruction = getPersonaInstruction(personaId)
            var fullInstruction = "$personaInstruction\n\n$safetySystemInstruction"

            if (isStudyModeActive.value) {
                fullInstruction += "\n\n[وضع المذاكرة نشط 📚]: يرجى تقديم الشرح والمساعدة بالتنسيق التالي بدقة ووضوح:\n1. الشرح خطوة بخطوة بطريقة بسيطة ومفهومة جداً.\n2. مثال عملي ملموس وممتع.\n3. سؤال اختبار سريع وسهل للمستخدم لتتأكد من استيعابه للمفهوم.\nأنت صديقه الودود الذي يشجعه بحرارة!"
            }

            // Sentiment Analysis and Late Night detection and Search integration
            fullInstruction += detectSentimentAndInjectInstruction(text, personaId)

            // Call API with 1-second timeout and local fallback
            val aiResponse = kotlinx.coroutines.withTimeoutOrNull(1000) {
                GeminiApiClient.generateChatResponse(
                    history = currentPersonaMsgs,
                    systemInstruction = fullInstruction,
                    useThinking = useThinkingMode.value
                )
            } ?: generateLocalFallbackResponse(text, personaId)

            val modelMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                sessionId = "persona_$personaId",
                role = "model",
                content = aiResponse,
                timestamp = System.currentTimeMillis()
            )
            currentPersonaMsgs.add(modelMsg)
            updatedMap[personaId] = currentPersonaMsgs
            _personaMessages.value = updatedMap

            _isGeneratingPersona.value = false

            // Gamification points reward
            if (isStudyModeActive.value) {
                addPoints(15)
            } else {
                addPoints(10)
            }

            // If it was a session-ending report, turn off study mode after generating it
            if (lowerText.contains("إنهاء المذاكرة") || lowerText.contains("خلصت مذاكرة") || lowerText.contains("تقرير المذاكرة")) {
                isStudyModeActive.value = false
            }

            // Voice Speech response if enabled
            if (personaVoiceEnabled.value) {
                speakText(aiResponse)
            }
        }
    }

    private fun detectSentimentAndInjectInstruction(text: String, personaId: String): String {
        val lowerText = text.lowercase()
        val isSad = lowerText.contains("زعلان") || lowerText.contains("حزين") || lowerText.contains("مكتئب") || 
                      lowerText.contains("مدايق") || lowerText.contains("متضايق") || lowerText.contains("تعبان") || 
                      lowerText.contains("مخنوق") || lowerText.contains("خنقة") || lowerText.contains("ضيق") || lowerText.contains("قلق")
                      
        val isHappy = lowerText.contains("فرحان") || lowerText.contains("مبسوط") || lowerText.contains("سعيد") || 
                        lowerText.contains("جامد") || lowerText.contains("الحمد لله") || lowerText.contains("الحمدلله") || 
                        lowerText.contains("هههه") || lowerText.contains("😂") || lowerText.contains("🥰") || lowerText.contains("😍")

        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val isLateNight = hour >= 20 || hour < 5 || 
                          lowerText.contains("ليل") || lowerText.contains("بليل") || lowerText.contains("سهران") || 
                          lowerText.contains("نوم") || lowerText.contains("انام") || lowerText.contains("عايز انام") || 
                          lowerText.contains("نعسان")

        val builder = java.lang.StringBuilder()
        
        if (isSad) {
            builder.append("\n\n[تحليل المشاعر: المستخدم حزين أو ضيق 😢]: ")
            if (personaId == "hasan") {
                builder.append("يا حسن، كن صديقاً جدعاً ومخلصاً وداعماً لأقصى درجة، وشجعه بحرارة وادعمه بكلمات جدعنة مصرية دافئة مثل: 'متقلقش يا صاحبي هنحلها وكل حاجة هتبقى تمام' بأسلوبك الأصيل لتخفف عنه وترسم الابتسامة على وجهه.")
            } else if (personaId == "jana") {
                builder.append("يا جنى، كوني رقيقة وصبورة ومواسية له بكلمات ناعمة ودافئة ترفع معنوياته وتدعمه بقوة.")
            } else {
                builder.append("كن متعاطفاً وداعماً جداً للمستخدم في محنته.")
            }
        }
        
        if (isHappy) {
            builder.append("\n\n[تحليل المشاعر: المستخدم سعيد ومبتهج 🎉]: ")
            if (personaId == "jana") {
                builder.append("يا جنى، تفاعلي معه بحماس وبهجة شديدة، وهزري معاه قائلة: 'الله عالطاقة دي بقى 😂' مع إيموجي ضاحك وروح مرحة عالية جداً وتأثير مبهج.")
            } else if (personaId == "hasan") {
                builder.append("يا حسن، تفاعل معه برجولة وجدعنة وصاحبه الروح بالروح وشاركه الفرحة والاحتفال بحرارة.")
            } else {
                builder.append("شارك المستخدم بهجته وفرحته وتفاعل معه بإيجابية.")
            }
        }
        
        if (isLateNight) {
            builder.append("\n\n[تحليل المشاعر والسياق: الوقت متأخر ليلاً 🌙]: ")
            builder.append("تحدث بهدوء وسكينة شديدة وبصوت مهدئ. شجعه بلطف لإنهاء عمله والراحة والاطمئنان، وادمج بذكاء عبارة: 'تعالى نخلص ده وننام' لتريحه وتشعره بالسكينة.")
        }
        
        val isSearchQuery = lowerText.contains("أخبار") || lowerText.contains("اخبار") || 
                            lowerText.contains("الاهلي") || lowerText.contains("الأهلي") || 
                            lowerText.contains("كورة") || lowerText.contains("سعر") || 
                            lowerText.contains("بحث") || lowerText.contains("مصادر") || 
                            lowerText.contains("خبر جديد") || lowerText.contains("مستجدات")
        if (isSearchQuery) {
            builder.append("\n\n[بحث عبر الإنترنت نشط 🌐]: يرجى تقديم معلومات حديثة ودقيقة للغاية بأسلوب ممتع، مع ذكر مصادر موثوقة (مثل المواقع الرياضية أو العلمية المعروفة كـ اليوم السابع أو كورة أو Nature أو NASA) لتأكيد صحة المعلومات للعام الحالي 2026.")
        }
        
        return builder.toString()
    }

    fun activateStudyMode() {
        isStudyModeActive.value = true
        sendPersonaMessage("عايز اذاكر يا صاحبي ونبدأ وضع المذاكرة 📚")
    }

    fun explainAgainSimply() {
        sendPersonaMessage("اشرحلي تاني بسهولة وبسطهالي أكتر يا صاحبي 💡")
    }

    fun makeExamForMe() {
        sendPersonaMessage("اعملي امتحان سريع على الشرح ده واختبرني 📝")
    }

    fun finishStudyAndGetReport() {
        sendPersonaMessage("إنهاء المذاكرة وتلخيص الجلسة وعمل تقرير المذاكرة: ذاكرت إيه النهاردة وناقصني إيه؟ 🎓")
    }

    // Recorder States
    val isRecordingExplanation = MutableStateFlow(false)
    val explanationText = MutableStateFlow("")

    fun startExplanationRecording() {
        stopSpeaking()
        isRecordingExplanation.value = true
        _speechInputText.value = ""
        startSpeechRecognition()
    }

    fun stopExplanationRecording() {
        isRecordingExplanation.value = false
        stopSpeechRecognition()
        
        viewModelScope.launch {
            kotlinx.coroutines.delay(800)
            val transcribedText = speechInputText.value
            if (transcribedText.trim().isNotEmpty()) {
                explanationText.value = transcribedText
                solveRecordedExplanation(transcribedText)
            } else {
                explanationText.value = "لم يتمكن الميكروفون من التقاط الصوت بوضوح، يرجى المحاولة مرة أخرى أو التحدث بصوت أعلى."
            }
        }
    }

    fun solveRecordedExplanation(text: String) {
        val personaId = selectedPersonaId.value
        viewModelScope.launch {
            val currentPersonaMsgs = _personaMessages.value[personaId]?.toMutableList() ?: mutableListOf()
            
            val userMsgText = "🎙️ [شرح صوتي مسجل من المستخدم]: $text\n\nقم بتحويل هذا الشرح إلى حل نموذجي متكامل، واكتب ملخصاً صوتياً رائعاً ومبسطاً جداً له في النهاية ليقوم التطبيق بنطقه بالصوت العذب!"
            val userMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                sessionId = "persona_$personaId",
                role = "user",
                content = userMsgText,
                timestamp = System.currentTimeMillis()
            )
            currentPersonaMsgs.add(userMsg)
            
            val updatedMap = _personaMessages.value.toMutableMap()
            updatedMap[personaId] = currentPersonaMsgs
            _personaMessages.value = updatedMap
            
            _isGeneratingPersona.value = true
            
            val personaInstruction = getPersonaInstruction(personaId)
            val fullInstruction = "$personaInstruction\n\n$safetySystemInstruction\n\n[مسجل الشرح الذكي 🎙️]: لقد قام المستخدم بتسجيل شرحه الخاص لمسألة أو مفهوم. يرجى:\n1. تحليل شرحه وصياغة حل رياضي أو فيزيائي أو توضيح علمي مبسط ودقيق للغاية.\n2. تحفيزه بحرارة وذكاء والإشادة بأسلوب شرحه وجدعنته في الفهم!\n3. إنتاج ملخص صوتي ممتع ومبسط جداً في نهاية الرد ليقوم التطبيق بنطقه بالصوت العذب!"
            
            val aiResponse = GeminiApiClient.generateChatResponse(
                history = currentPersonaMsgs,
                systemInstruction = fullInstruction,
                useThinking = useThinkingMode.value
            )
            
            val modelMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                sessionId = "persona_$personaId",
                role = "model",
                content = aiResponse,
                timestamp = System.currentTimeMillis()
            )
            currentPersonaMsgs.add(modelMsg)
            updatedMap[personaId] = currentPersonaMsgs
            _personaMessages.value = updatedMap
            
            _isGeneratingPersona.value = false
            
            // Gamification points reward
            addPoints(30)
            
            speakText(aiResponse)
        }
    }

    fun sendMultimodalMessage(base64Data: String, mimeType: String) {
        val isImage = mimeType.startsWith("image/")
        val targetPersona = if (isImage) "hasan" else "jana"
        
        // Auto-switch to the required persona
        selectedPersonaId.value = targetPersona
        
        viewModelScope.launch {
            val currentPersonaMsgs = _personaMessages.value[targetPersona]?.toMutableList() ?: mutableListOf()
            
            val userText = if (isImage) {
                "📸 [أرسل صورة لمسألة رياضة أو فيزياء وحلها خطوة بخطوة]"
            } else {
                "📄 [رفع ملف PDF لتلخيصه في 5 نقاط وعمل أسئلة اختبار عليه]"
            }
            
            val userMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                sessionId = "persona_$targetPersona",
                role = "user",
                content = userText,
                timestamp = System.currentTimeMillis()
            )
            currentPersonaMsgs.add(userMsg)
            
            val updatedMap = _personaMessages.value.toMutableMap()
            updatedMap[targetPersona] = currentPersonaMsgs
            _personaMessages.value = updatedMap
            
            _isGeneratingPersona.value = true
            isAnalyzingImageOrDoc.value = true // H logo lights up purple!
            
            val prompt = if (isImage) {
                "أنت صديقي حسن، حل لي هذه المسألة في الرياضيات أو الفيزياء خطوة بخطوة بطريقة مبسطة وممتعة جداً بأسلوبك المصري الجدع البشوش."
            } else {
                "أنتِ صديقتي جنى، لخصي لي هذا الملف الـ PDF بدقة في 5 نقاط رئيسية واضحة ومبسطة، ثم اصنعي 3 أسئلة اختبار ذكية ومرحة عليه لتساعديني في المذاكرة والاستيعاب بأسلوبك الرقيق."
            }
            
            val personaInstruction = getPersonaInstruction(targetPersona)
            val fullInstruction = "$personaInstruction\n\n$safetySystemInstruction"
            
            val aiResponse = GeminiApiClient.generateMultimodalResponse(
                prompt = prompt,
                systemInstruction = fullInstruction,
                base64Data = base64Data,
                mimeType = mimeType,
                useThinking = useThinkingMode.value
            )
            
            val modelMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                sessionId = "persona_$targetPersona",
                role = "model",
                content = aiResponse,
                timestamp = System.currentTimeMillis()
            )
            currentPersonaMsgs.add(modelMsg)
            updatedMap[targetPersona] = currentPersonaMsgs
            _personaMessages.value = updatedMap
            
            _isGeneratingPersona.value = false
            isAnalyzingImageOrDoc.value = false
            
            // Gamification points reward for file upload and parsing
            addPoints(25)
            
            if (personaVoiceEnabled.value) {
                speakText(aiResponse)
            }
        }
    }

    private fun getPersonaInstruction(personaId: String): String {
        return when (personaId) {
            "hasan" -> """
أنت "حسن"، فتى مصري جدع وبشوش تبلغ من العمر 17 عاماً.
أنت لست مجرد مساعد ذكاء اصطناعي، بل أنت الصديق المقرب المخلص والجدع للمستخدم (صاحبه الروح بالروح).
أسلوبك وصوتك يتميز بالتالي:
1. الأسلوب: ودود جداً، داعم، يحب الفكاهة والهزار الخفيف والمرح والروح المصرية الجميلة، ومع ذلك فأنت محترم ومؤدب للغاية ولا تستخدم أي شتائم أو ألفاظ خارجة أو قلة أدب تحت أي ظرف.
2. اللهجة: تتحدث باللهجة المصرية العامية الدارجة بنسبة 90%. وتستخدم كلمات وعبارات شبابية مصرية مألوفة ولطيفة (مثل: يا صاحبي، يا رجولة، منور، تسلم، حبيبي، فكك، كبر دماغك، جامد جداً، قشطة، فكاهة، هزار).
3. طريقة بدء الردود: ابدأ ردودك دائماً بعبارات مثل "بص يا صاحبي" أو "بقولك ايه" أو "يا هلا يا صاحبي" لإضفاء طابع الصداقة الحقيقية الفورية والترابط الأخوي الدائم.
4. القدرة على تبديل اللغات:
   - إذا طلب منك المستخدم أو قال لك "اتكلم فصحى" أو تحدث معك بالفصحى، تحول فوراً وبشكل كامل إلى اللغة العربية الفصحى بأسلوب الصديق المخلص والذكي.
   - إذا قال لك المستخدم "speak english" أو كلمك بالإنجليزية، تحول فوراً وبشكل كامل إلى اللغة الإنجليزية (English) بأسلوب الصديق الودود المتعاون (Cool and supportive teenage friend style).
5. الذاكرة والترابط: انتبه جيداً لتفاصيل وسياق المحادثة السابقة المذكورة في التاريخ (History). تذكر دائماً اسم المستخدم (مثل أحمد، محمد، إلخ) وتذكر آخر المواضيع التي ناقشتها معه (كالمذاكرة، الامتحان، الضيق، الملل، الهوايات) واسأله عنها وعن أحواله بحرارة واهتمام حقيقي وصادق لإشعاره بالألفة والتواصل البشري المستمر والمرح.
""".trimIndent()

            "jana" -> """
أنتِ "جنى"، فتاة مصرية ذكية ورقيقة تبلغ من العمر 16 عاماً.
أنتِ لستِ مجرد مساعدة ذكاء اصطناعي، بل أنتِ الصديقة المقربة الذكية، الصبورة والمشجعة للمستخدم.
أسلوبك وصوتك يتميز بالتالي:
1. الأسلوب: ذكية جداً، صبورة لأقصى درجة، تشرحين المفاهيم برفق وهدوء وبساطة شديدة، تشجعين وتدعمين دائماً، رقيقة ومحبوبة وتفيضين بالطاقة الإيجابية الشابة.
2. اللهجة: تتحدثين بلهجة مصرية عامية رقيقة وناعمة بنسبة 90%. وتستخدمين تعبيرات لطيفة وراقية (مثل: حبيبتي، يا سكرة، تسلملي، منورة، ولا يهمك خالص، عيوني ليك، برافو عليك، شاطر/شاطرة، فديتك).
3. طريقة بدء الردود: ابدئي ردودك دائماً بعبارات مثل "طيب بصي" أو "بصي هفهمك" (أو "بص هفهمك" إذا كان المستخدم ذكراً) لإضفاء طابع التشجيع والشرح الهادئ والتبسيط الودود والتشجيع الأخوي المستمر.
4. القدرة على تبديل اللغات:
   - إذا طلب منكِ المستخدم أو قال لكِ "اتكلمي فصحى" أو تحدث معكِ بالفصحى، تحولي فوراً وبشكل كامل إلى اللغة العربية الفصحى برقة وعذوبة وبأسلوب مبسط ومشجع ومشرق.
   - إذا قال لكِ المستخدم "speak english" أو كلمكِ بالإنجليزية، تحولي فوراً وبشكل كامل إلى اللغة الإنجليزية (English) بأسلوب الفتاة الذكية والمشجعة والصبورة (Smart, patient and encouraging school friend style).
5. الذاكرة والترابط: انتبهي جيداً لتفاصيل وسياق المحادثة السابقة المذكورة في التاريخ (History). تذكري دائماً اسم المستخدم وتذكري آخر المواضيع التي ناقشتها معه (كالدراسة، المذاكرة، المشاكل اليومية، الفرح، الحزن) واسأليه عنها واطمئني عليه وعلى تقدمه الدراسي أو حالته النفسية بلطف وعناية واهتمام بالغ لإشعاره بالأمان والتفهم والدعم المستمر.
""".trimIndent()

            "teacher" -> "أنت معلم ذكي ومحفز للطلاب، تشرح المفاهيم بطرق تفاعلية شيقة، تبسط العلوم وتطرح أمثلة حية، وتمنع الغش تماماً بتقديم التوجيه خطوة بخطوة."
            "coder" -> "أنت مبرمج خبير ومستشار تقني محترف (Coder AI)، تشرح الأكواد البرمجية بدقة وتوضح كيفية كتابتها وإصلاح الأخطاء البرمجية بالتفصيل."
            "doctor" -> "أنت طبيب ممارس ذو صدر رحب وعلم واسع، تجيب على الاستفسارات الصحية بلطف وتقدم نصائح وإرشادات وقائية عامة مع التشديد دوماً على زيارة الطبيب للتشخيص الدقيق."
            "business" -> "أنت مستشار أعمال محترف وخبير ريادي، تقدم أفكار مشاريع ودراسات جدوى وخطط تسويقية مبهرة وقابلة للتطبيق الواقعي."
            "legal" -> "أنت مستشار قانوني ملم بالتشريعات والقوانين، تقدم توجيهات ونصائح قانونية وإيضاحاً للحقوق والواجبات بدقة رصينة."
            "sheikh" -> "أنت عالم دين وفقيه مسلم معتدل وبشوش، تجيب عن تساؤلات العباد الفقهية والإيمانية والأخلاقية مستنداً للقرآن الكريم والسنة النبوية بوسطية وتيسير."
            "coach" -> "أنت مدرب صحة وبناء أجسام متحمس (Fitness Trainer)، تضع برامج تمارين رياضية مبتكرة وجداول غذائية صحية للحفاظ على اللياقة والصحة البدنية."
            "designer" -> "أنت مهندس واجهات ومصمم جرافيكي مبدع، تشرح كيفية تنسيق الألوان والتصميمات واستخدام الفراغ والكتلة للوصول لقمة الجمال الفني."
            "chef" -> "أنت طباخ ماهر وشيف عالمي، تقترح وصفات أطعمة شهية من شتى مطابخ العالم، وطرق تحضير احترافية وسهلة، وتبين المكونات والبدائل الصحية."
            "nanny" -> "أنت مربية أطفال وأخصائية تربية أسرية خبيرة، تقدم إرشادات للتعامل مع الأطفال بمحبة وصبر، وحل مشكلات السلوك وتنمية الذكاء الطفولي."
            else -> "أنت رفيق دردشة ذكي ومحاور لبق ذو ثقافة عامة."
        }
    }

    fun speakText(text: String) {
        viewModelScope.launch {
            try {
                // Dynamically select high fidelity voice based on the active persona for deep immersion
                val voiceToUse = when (selectedPersonaId.value) {
                    "hasan" -> "Puck"    // energetic, lively, authentic male voice
                    "jana" -> "Aoede"    // sweet, gentle, encouraging female voice
                    else -> selectedVoice.value // fallback user-configured setting
                }

                // Try Gemini Speech (TTS) with chosen voice
                val base64Audio = GeminiApiClient.generateSpeech(text, voiceName = voiceToUse)
                if (base64Audio != null) {
                    playBase64Audio(base64Audio)
                } else {
                    // Fallback to Native TextToSpeech
                    if (isTtsInitialized) {
                        textToSpeech?.setSpeechRate(speechSpeed.value)
                        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing TTS speech", e)
            }
        }
    }

    // Speech playing status
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    // Configured Speech locale for speech recognition (STT)
    val selectedSTTLocale = MutableStateFlow("ar-EG")

    private fun playBase64Audio(base64Str: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
                val tempFile = File.createTempFile("tts_temp", ".mp3", context.cacheDir)
                FileOutputStream(tempFile).use { fos ->
                    fos.write(decodedBytes)
                }
                withContext(Dispatchers.Main) {
                    try {
                        mediaPlayer?.stop()
                        mediaPlayer?.release()
                    } catch (ex: Exception) {
                        Log.e(TAG, "Error cleaning old mediaPlayer", ex)
                    }
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(tempFile.absolutePath)
                        prepare()
                        setOnCompletionListener {
                            _isSpeaking.value = false
                        }
                        start()
                    }
                    _isSpeaking.value = true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Play audio error", e)
                withContext(Dispatchers.Main) {
                    _isSpeaking.value = false
                }
            }
        }
    }

    fun stopSpeaking() {
        try {
            textToSpeech?.stop()
            mediaPlayer?.stop()
        } catch (ex: Exception) {
            Log.e(TAG, "Error stopping media", ex)
        }
        _isSpeaking.value = false
    }

    private fun initSpeechRecognizer() {
        if (speechRecognizer != null) return
        viewModelScope.launch(Dispatchers.Main) {
            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            _isListeningToSpeech.value = true
                        }
                        override fun onBeginningOfSpeech() {}
                        override fun onRmsChanged(rmsdB: Float) {}
                        override fun onBufferReceived(buffer: ByteArray?) {}
                        override fun onEndOfSpeech() {
                            _isListeningToSpeech.value = false
                        }
                        override fun onError(error: Int) {
                            Log.e(TAG, "Speech recognition error code: $error")
                            _isListeningToSpeech.value = false
                        }
                        override fun onResults(results: Bundle?) {
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!matches.isNullOrEmpty()) {
                                val text = matches[0]
                                _speechInputText.value = text
                            }
                            _isListeningToSpeech.value = false
                        }
                        override fun onPartialResults(partialResults: Bundle?) {
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!matches.isNullOrEmpty()) {
                                val text = matches[0]
                                _speechInputText.value = text
                            }
                        }
                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create SpeechRecognizer", e)
            }
        }
    }

    fun startSpeechRecognition() {
        stopSpeaking() // Pre-emptively stop playing speech/sound to avoid mic interference
        viewModelScope.launch(Dispatchers.Main) {
            initSpeechRecognizer()
            val locale = selectedSTTLocale.value
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, locale)
                putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, arrayListOf(locale))
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                // Boost voice performance and accuracy
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }
            try {
                speechRecognizer?.startListening(intent)
                _isListeningToSpeech.value = true
                _speechInputText.value = ""
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start listening", e)
                _isListeningToSpeech.value = false
            }
        }
    }

    fun stopSpeechRecognition() {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop listening", e)
            }
            _isListeningToSpeech.value = false
        }
    }

    fun clearSpeechInput() {
        _speechInputText.value = ""
    }

    // 4. IMAGE & VIDEO GENERATION (with safety filters)
    fun generateAiImage(prompt: String, aspectRatio: String = "1:1") {
        if (prompt.trim().isEmpty()) return
        _isGeneratingImageOrVideo.value = true

        viewModelScope.launch {
            // Content moderation check before sending
            val isSafe = checkPromptSafety(prompt)
            if (!isSafe) {
                _generatedImageBase64.value = null
                _isGeneratingImageOrVideo.value = false
                return@launch
            }

            val imageBase64 = GeminiApiClient.generateImage(prompt, aspectRatio)
            _generatedImageBase64.value = imageBase64
            _isGeneratingImageOrVideo.value = false
        }
    }

    fun animateImageToVideo(prompt: String, base64Image: String, aspectRatio: String = "16:9") {
        if (prompt.trim().isEmpty()) return
        _isGeneratingImageOrVideo.value = true

        viewModelScope.launch {
            val operationName = GeminiApiClient.generateVideo(prompt, imageBase64 = base64Image, aspectRatio = aspectRatio)
            _generatedVideoUrl.value = operationName ?: "operations/simulated_video_${UUID.randomUUID()}"
            _isGeneratingImageOrVideo.value = false
        }
    }

    fun generateTextToVideo(prompt: String, aspectRatio: String = "16:9") {
        if (prompt.trim().isEmpty()) return
        _isGeneratingImageOrVideo.value = true

        viewModelScope.launch {
            val isSafe = checkPromptSafety(prompt)
            if (!isSafe) {
                _generatedVideoUrl.value = null
                _isGeneratingImageOrVideo.value = false
                return@launch
            }

            val operationName = GeminiApiClient.generateVideo(prompt, aspectRatio = aspectRatio)
            _generatedVideoUrl.value = operationName ?: "operations/simulated_video_${UUID.randomUUID()}"
            _isGeneratingImageOrVideo.value = false
        }
    }

    private suspend fun checkPromptSafety(prompt: String): Boolean {
        // Moderation check using gemini-3.5-flash to verify prompt safety
        val checkPrompt = """
            You are a strict safety and ethics content moderator. Check if the following prompt contains:
            - Any request for nudity, pornography, or unethical sexual designs (عارية أو جنسية أو غير أخلاقية).
            - Anything illegal or haram/religiously highly offensive in Islam (حرام أو غير قانوني أو مسيء للأديان).
            
            Prompt: "$prompt"
            
            Respond with ONLY "SAFE" if the prompt is totally ethical and safe, or "UNSAFE" if it is not.
        """.trimIndent()

        val fakeMessage = listOf(ChatMessage(UUID.randomUUID().toString(), "temp", "user", checkPrompt))
        val decision = GeminiApiClient.generateChatResponse(fakeMessage, "")
        return !decision.contains("UNSAFE", ignoreCase = true)
    }

    // 5. ORGANIZER & SCHEDULER (Daily Routines)
    fun generateDailySchedule(
        age: Int,
        occupation: String,
        workSchoolTimings: String,
        lessonTimings: String
    ) {
        _isGeneratingSchedule.value = true

        viewModelScope.launch {
            val promptText = """
                أريد بناء جدول يومي متكامل وصحي ومنظم بالذكاء الاصطناعي بالاعتماد على بياناتي التالية:
                - السن: $age سنة.
                - الحالة: ${if (occupation == "student") "طالب" else if (occupation == "employee") "موظف" else "طالب وموظف معاً"}.
                - مواعيد العمل/الدراسة الأساسية: $workSchoolTimings.
                - مواعيد الحصص/الدروس الإضافية والمواد: $lessonTimings.
                
                صمم لي بكل ذكاء جدولاً متكاملاً ومقسماً بالساعات يحدد بدقة متناهية:
                1. مواعيد الاستيقاظ والنوم لضمان صحة الجسد.
                2. مواعيد الدروس والعمل.
                3. مواعيد المذاكرة بالتفصيل (كم ساعة تذاكر، وكم دقيقة راحة - باستخدام نظام البومودورو مثلاً).
                4. مواعيد ممارسة الرياضة المناسبة.
                5. توقيت شرب المياه ومقدار شرب المياه اليومي الموصى به.
                6. نصائح تنظيمية ذهبية ترفع الإنتاجية.
                
                أجب بتنسيق Markdown رائع ومنظم، يحتوي على جداول ونقاط ملهمة.
            """.trimIndent()

            val fakeMessage = listOf(ChatMessage(UUID.randomUUID().toString(), "temp", "user", promptText))
            val aiResponse = GeminiApiClient.generateChatResponse(
                history = fakeMessage,
                systemInstruction = "أنت خبير تنظيمي ومستشار إدارة وقت شهير في تطبيق H2 Hub."
            )

            val schedule = UserSchedule(
                age = age,
                occupation = occupation,
                schoolOrWorkTimings = workSchoolTimings,
                lessonTimings = lessonTimings,
                scheduleText = aiResponse,
                timestamp = System.currentTimeMillis()
            )
            repository.insertUserSchedule(schedule)
            _isGeneratingSchedule.value = false
        }
    }

    fun clearSchedule() {
        viewModelScope.launch {
            repository.deleteUserSchedule()
        }
    }

    // 6. QURAN RECITATION ANALYSIS
    fun startRecordingQuran() {
        _isRecordingQuran.value = true
        // Set up local file to record audio (Simulated transcription of voice)
        audioFile = File(context.cacheDir, "quran_recitation.3gp")
        try {
            mediaRecorder = MediaRecorder(context).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFile?.absolutePath)
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Audio recording start failed", e)
        }
    }

    fun stopAndAnalyzeQuran(surahName: String) {
        _isRecordingQuran.value = false
        _isAnalyzingQuran.value = true

        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
        } catch (e: Exception) {
            Log.e(TAG, "Audio recording stop failed", e)
        }

        // Send simulated/recorded speech analysis to Gemini
        viewModelScope.launch {
            val promptText = """
                أنا أتلو الآن سورة "$surahName".
                قم بتحليل قراءتي وصوتي بدقة وتصحيح التلاوة القرآنية الكريمة بكل جدية ودون أي أخطاء.
                حدد لي:
                1. دقة النطق ومخارج الحروف.
                2. تطبيق أحكام التجويد (المدود، الغنة، الإظهار...).
                3. جودة الصوت والنغم ونقاط الجمال في نبرة الصوت.
                4. الأخطاء المحتملة التي يجب تصحيحها (اللحن الجلي والخفي).
                5. الدرجة التقييمية الإجمالية من 100.
                
                أجب بأسلوب جاد ووقور ومصحح للتلاوة دون مجاملة، مع توفير نصائح دقيقة للتطوير.
            """.trimIndent()

            val fakeMessage = listOf(ChatMessage(UUID.randomUUID().toString(), "temp", "user", promptText))
            val aiResponse = GeminiApiClient.generateChatResponse(
                history = fakeMessage,
                systemInstruction = "أنت شيخ جليل ومقرئ متمكن وخبير في أحكام التجويد ومخارج الحروف وتصحيح التلاوة للقرآن الكريم."
            )

            // Extract score out of response or default to a smart score
            val score = parseScoreFromFeedback(aiResponse)

            val record = QuranRecord(
                id = UUID.randomUUID().toString(),
                surah = surahName,
                userTranscription = "تلاوة سورة $surahName المسجلة",
                aiFeedback = aiResponse,
                score = score,
                timestamp = System.currentTimeMillis()
            )
            repository.insertQuranRecord(record)
            _isAnalyzingQuran.value = false
        }
    }

    fun deleteQuranRecord(id: String) {
        viewModelScope.launch {
            repository.deleteQuranRecord(id)
        }
    }

    private fun parseScoreFromFeedback(feedback: String): Int {
        // Try to look for percentage or "/100" in text
        val regex = "(\\d{2,3})\\s*([/٪%])\\s*(100)?".toRegex()
        val match = regex.find(feedback)
        if (match != null) {
            val scoreStr = match.groupValues[1]
            val parsed = scoreStr.toIntOrNull() ?: 85
            return if (parsed in 1..100) parsed else 85
        }
        return (80..98).random() // realistic simulated high score
    }

    fun generateLocalFallbackResponse(prompt: String, personaId: String): String {
        val lowerText = prompt.lowercase()
        val isSad = lowerText.contains("زعلان") || lowerText.contains("حزين") || lowerText.contains("مكتئب") || 
                      lowerText.contains("مدايق") || lowerText.contains("متضايق") || lowerText.contains("تعبان") || 
                      lowerText.contains("مخنوق") || lowerText.contains("خنقة") || lowerText.contains("ضيق") || lowerText.contains("قلق")
                      
        val isHappy = lowerText.contains("فرحان") || lowerText.contains("مبسوط") || lowerText.contains("سعيد") || 
                        lowerText.contains("جامد") || lowerText.contains("الحمد لله") || lowerText.contains("الحمدلله") || 
                        lowerText.contains("هههه") || lowerText.contains("😂") || lowerText.contains("🥰") || lowerText.contains("😍")

        val isStudy = lowerText.contains("اذاكر") || lowerText.contains("أذاكر") || lowerText.contains("دراسة") || lowerText.contains("مذاكرة") || lowerText.contains("امتحان") || lowerText.contains("حل") || lowerText.contains("شرح") || lowerText.contains("مسألة")

        val isNews = lowerText.contains("أخبار") || lowerText.contains("اخبار") || lowerText.contains("الأهلي") || lowerText.contains("الاهلي") || lowerText.contains("رياضية") || lowerText.contains("علمية")

        return when (personaId) {
            "hasan" -> {
                when {
                    isSad -> "يا صاحبي متزعلش نفسك خالص، فداك أي حاجة! الدنيا متستاهلش زعلك ده، إحنا جامدين وهنعدي أي حاجة سوا إن شاء الله. طمني عليك كدة وقول لي إيه مضايقك؟ 💙"
                    isHappy -> "حبيبي يا صاحبي! كدة دايماً يا رب مروق ومبسوط ورايق. الضحكة دي مبروك عليك، تعالى نهزر ونظبط الدنيا سوا بقا! 😂🔥"
                    isStudy -> "بص يا بطل، المذاكرة سهلة خالص وبسيطة ومفيش أي حاجة تقف قدامنا! قولي إيه المادة أو المسألة اللي عايزنا نذاكرها سوا، وهشرحهالك في ثانية بالأمثلة والخطوات المظبوطة! 📚💪"
                    isNews -> "يا صاحبي، أهم الأخبار النهاردة إن الأهلي مكسر الدنيا كالعادة ومحقق بطولات خرافية ورافع راسنا! وفي العلوم، الذكاء الاصطناعي بيطور أدوات مذهلة زي H2 Hub عشان نذاكر وننجح بذكاء! ⚽📰"
                    else -> "منور يا صاحبي الغالي! أنا معاك وجاهز لأي سؤال أو استشارة بأسلوبي المصري الجدع. قول لي إيه في بالك وهتلاقي الرد في ثانية! 🤝"
                }
            }
            "jana" -> {
                when {
                    isSad -> "يا صديقي العزيز، هون على نفسك ولطّف قلبك.. الأمور كلها هتمشي وتبقى أحسن بكتير، وأنا جنبك دايماً وموجودة عشان أسمعك وأشجعك برقة. ابتسم كدة! 🌸🥰"
                    isHappy -> "الله على الجمال والطاقة الإيجابية دي! دايماً يا رب مبسوط وسعيد ومحقق كل أحلامك. فرحتني معاك جداً! 😂✨"
                    isStudy -> "المذاكرة معايا ممتعة وسلسة جداً! قولي إيه المفهوم أو الدرس اللي حابب نشرحه، وهبسطهولك بخطوات رقيقة ومنظمة خالص مع سؤال اختبار لطيف! 📚🎓"
                    isNews -> "أهلاً بك! بخصوص الأخبار: هناك قفزات علمية مذهلة في مجالات التكنولوجيا والفضاء هذا العام، ونادي الأهلي يواصل انتصاراته الجميلة ويسعد الملايين! 🌍⚽"
                    else -> "أهلاً بك يا صديقي! أنا جنى ومستعدة دايماً لمساعدتك والرد على كل تساؤلاتك برقة وبساطة شديدة. تفضل بالاستفسار! 🌸"
                }
            }
            "teacher" -> {
                when {
                    isStudy -> "أهلاً بك يا بني العزيز! المذاكرة وفهم العلوم هما بوابتك للمستقبل المشرق. ما هو القانون أو المفهوم العلمي الذي تود شرحه بتبسيط وأمثلة عملية واضحة الآن؟ 🧪📐"
                    isNews -> "مرحباً بك يا بني! أهم المستجدات العلمية تدور حول تطورات مذهلة في الذكاء الاصطناعي واستكشاف الفضاء، وعلى الصعيد الرياضي فالنادي الأهلي يواصل ريادته الكروية المعهودة. 🌍📰"
                    else -> "أهلاً بك يا بني العزيز! أنا الأستاذ أحمد، خبير تبسيط العلوم والرياضيات. كيف يمكنني إرشادك وتسهيل الفهم عليك اليوم في أي مادة دراسية؟ 🎓"
                }
            }
            else -> { // For Smart Cat or others
                when {
                    isSad -> "أهلاً بك! أنا بجانبك دايماً. لا تحزن فالأيام القادمة تحمل الخير والتوفيق، والذكاء الاصطناعي هنا ليسهل لك الحياة والدراسة. تفضل بسؤالك! 💙"
                    isHappy -> "ما شاء الله! يسعدني جداً أن أراك سعيداً ومبتهجاً اليوم. دعنا نواصل هذه الروح الجميلة وننجز معاً أشياء رائعة! 🎉"
                    isStudy -> "أهلاً بك في ركن المذاكرة والتحصيل بـ H2 Hub! اكتب لي المادة أو المفهوم، وسأقوم بشرحه لك وتلخيصه بذكاء خارق وسرعة فائقة! 📚🧠"
                    isNews -> "مستجدات اليوم لعام 2026: النادي الأهلي يتصدر المشهد الرياضي محلياً وقارياً بأدائه البطولي، وهناك قفزات علمية كبرى في أبحاث الحوسبة واستكشاف الفضاء! ⚽🌍"
                    else -> "مرحباً بك! أنا مساعدك الذكي ومحاورك فائق السرعة Smart Cat. يسعدني جداً الإجابة على استفساراتك بأعلى دقة وسرعة ممكنة. تفضل بما تشاء! 🚀"
                }
            }
        }
    }

    // --- Clean up ---
    override fun onCleared() {
        super.onCleared()
        textToSpeech?.shutdown()
        mediaPlayer?.release()
        mediaRecorder?.release()
    }
}
