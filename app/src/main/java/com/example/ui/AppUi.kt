package com.example.ui

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

// Screen enum representing the main destinations
enum class AppScreen(val titleAr: String, val icon: ImageVector) {
    HOME("الرئيسية", Icons.Default.Dashboard),
    CHAT("Smart Cat", Icons.Default.ChatBubble),
    PRODUCTIVITY("الإنتاجية", Icons.Default.Analytics),
    PERSONAS("شخصيات AI", Icons.Default.People),
    CREATIVE("توليد الفن", Icons.Default.Brush),
    ORGANIZER("المنظم اليومي", Icons.Default.CalendarMonth),
    QURAN("مصحح التلاوة", Icons.Default.Mic)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUi(viewModel: AppViewModel) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val isAnalyzing by viewModel.isAnalyzingImageOrDoc.collectAsState()
    val isThinking by viewModel.isGeneratingPersona.collectAsState()
    
    val isAnimatingLogo = isSpeaking || isAnalyzing || isThinking

    val infiniteTransition = rememberInfiniteTransition(label = "logo_glow")
    val glowAlpha by if (isAnimatingLogo) {
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "logoGlowAlpha"
        )
    } else {
        remember { mutableStateOf(0.0f) }
    }
    val scale by if (isAnimatingLogo) {
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "logoScale"
        )
    } else {
        remember { mutableStateOf(1.0f) }
    }

    // Navigation back handling
    if (currentScreen != AppScreen.HOME) {
        BackHandler {
            currentScreen = AppScreen.HOME
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .scale(scale)
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            if (isAnalyzing) Color(0xFF8B5CF6) else MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                )
                                .then(
                                    if (isAnimatingLogo) {
                                        Modifier.border(
                                            width = 3.dp,
                                            color = if (isAnalyzing) Color(0xFF8B5CF6).copy(alpha = glowAlpha) else MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "H2",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (currentScreen == AppScreen.HOME) "H2 Hub" else currentScreen.titleAr,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                fontFamily = FontFamily.SansSerif
                            )
                            if (isThinking || isAnalyzing) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val dotsY by infiniteTransition.animateFloat(
                                        initialValue = 0f,
                                        targetValue = -6f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(450, easing = LinearEasing),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "titleDots"
                                    )
                                    (0..2).forEach { index ->
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .graphicsLayer {
                                                    this.translationY = if (index == 0) dotsY else if (index == 1) dotsY * 0.7f else dotsY * 0.4f
                                                }
                                                .clip(CircleShape)
                                                .background(if (isAnalyzing) Color(0xFF8B5CF6) else MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    if (currentScreen != AppScreen.HOME) {
                        IconButton(onClick = { currentScreen = AppScreen.HOME }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                actions = {
                    if (currentScreen == AppScreen.CHAT || currentScreen == AppScreen.PERSONAS) {
                        var showConfirmDelete by remember { mutableStateOf(false) }
                        val activePersona by viewModel.selectedPersonaId.collectAsState()
                        val activeSessionId by viewModel.currentSessionId.collectAsState()

                        IconButton(
                            onClick = { showConfirmDelete = true },
                            modifier = Modifier.testTag("delete_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Chat",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }

                        if (showConfirmDelete) {
                            AlertDialog(
                                onDismissRequest = { showConfirmDelete = false },
                                title = { Text("تأكيد الحذف ⚠️") },
                                text = { Text("عايز تمسح المحادثة دي؟") },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            if (currentScreen == AppScreen.CHAT) {
                                                val sessId = activeSessionId
                                                if (sessId != null) {
                                                    viewModel.deleteSession(sessId)
                                                }
                                            } else {
                                                viewModel.clearPersonaMessages(activePersona)
                                            }
                                            showConfirmDelete = false
                                        }
                                    ) {
                                        Text("مسح", color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showConfirmDelete = false }) {
                                        Text("إلغاء")
                                    }
                                }
                            )
                        }
                    }

                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                tonalElevation = 8.dp
            ) {
                AppScreen.values().forEach { screen ->
                    NavigationBarItem(
                        selected = currentScreen == screen,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.titleAr
                            )
                        },
                        label = {
                            Text(
                                text = screen.titleAr,
                                fontSize = 10.sp,
                                fontWeight = if (currentScreen == screen) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    slideInHorizontally { width -> if (targetState.ordinal > initialState.ordinal) width else -width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> if (targetState.ordinal > initialState.ordinal) -width else width } + fadeOut()
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    AppScreen.HOME -> HomeScreen(onNavigate = { currentScreen = it })
                    AppScreen.CHAT -> ChatScreen(viewModel)
                    AppScreen.PRODUCTIVITY -> ProductivityScreen(viewModel)
                    AppScreen.PERSONAS -> PersonasScreen(viewModel)
                    AppScreen.CREATIVE -> CreativeScreen(viewModel)
                    AppScreen.ORGANIZER -> OrganizerScreen(viewModel)
                    AppScreen.QURAN -> QuranScreen(viewModel)
                }
            }

            if (showSettingsDialog) {
                SettingsDialog(viewModel = viewModel, onDismiss = { showSettingsDialog = false })
            }
        }
    }
}

// ==================== HOME DASHBOARD ====================

@Composable
fun HomeScreen(onNavigate: (AppScreen) -> Unit) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Brand Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                )
                .padding(20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column {
                Text(
                    text = "مرحباً بك في H2 Hub",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "منصتك الذكية الشاملة والرفيق المثالي للإنتاجية والإبداع",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp
                )
            }
        }

        Text(
            text = "الخدمات المتاحة",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Services Grid / List of custom styled cards
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ServiceRow(
                title = "Smart Cat (الشات الذكي السريع)",
                desc = "دردشة فائقة الذكاء تتذكر سياق محادثتك بالكامل",
                icon = Icons.Default.ChatBubble,
                color = MaterialTheme.colorScheme.primary,
                onClick = { onNavigate(AppScreen.CHAT) }
            )
            ServiceRow(
                title = "أدوات الإنتاجية الشاملة",
                desc = "كتابة أبحاث ومقالات، تلخيص كتب وملفات، تحويل صوت وصور",
                icon = Icons.Default.Analytics,
                color = MaterialTheme.colorScheme.secondary,
                onClick = { onNavigate(AppScreen.PRODUCTIVITY) }
            )
            ServiceRow(
                title = "شخصيات AI المتخصصة",
                desc = "مدرس، مبرمج، طبيب، شيخ ديني، مستشار قانوني بصوت حقيقي",
                icon = Icons.Default.People,
                color = MaterialTheme.colorScheme.tertiary,
                onClick = { onNavigate(AppScreen.PERSONAS) }
            )
            ServiceRow(
                title = "قسم توليد الصور والفيديوهات (Veo)",
                desc = "توليد صور احترافية وتوليد فيديو من نص أو تحريك الصور مع حماية صارمة",
                icon = Icons.Default.Brush,
                color = Color(0xFFEC4899),
                onClick = { onNavigate(AppScreen.CREATIVE) }
            )
            ServiceRow(
                title = "المنظم والجدول اليومي الذكي",
                desc = "احصل على جدول يومي مخصص وفق عمرك ودراستك وصحتك",
                icon = Icons.Default.CalendarMonth,
                color = Color(0xFFF59E0B),
                onClick = { onNavigate(AppScreen.ORGANIZER) }
            )
            ServiceRow(
                title = "مصحح التلاوة القرآنية الذكي",
                desc = "سجل تلاوتك ودع الذكاء الاصطناعي يحلل نطقك وأحكام التجويد",
                icon = Icons.Default.Mic,
                color = Color(0xFF10B981),
                onClick = { onNavigate(AppScreen.QURAN) }
            )
        }
    }
}

@Composable
fun ServiceRow(
    title: String,
    desc: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = desc,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

// ==================== SMART CHAT (SMART CAT) SCREEN ====================

@Composable
fun ChatScreen(viewModel: AppViewModel) {
    val sessions by viewModel.chatSessions.collectAsState()
    val messages by viewModel.currentMessages.collectAsState()
    val isGenerating by viewModel.isGeneratingChat.collectAsState()
    val useThinking by viewModel.useThinkingMode.collectAsState()
    val isListeningToSpeech by viewModel.isListeningToSpeech.collectAsState()
    val speechInputText by viewModel.speechInputText.collectAsState()
    val autoReadChatEnabled by viewModel.autoReadChatEnabled.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showThreadSelector by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Sync speech input text to user text field
    LaunchedEffect(speechInputText) {
        if (speechInputText.isNotEmpty()) {
            inputText = speechInputText
        }
    }

    // Scroll to bottom when messages list size changes
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Chat Toolbar / Subheader
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { viewModel.startNewSession("محادثة جديدة " + (sessions.size + 1)) },
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Chat")
                Spacer(modifier = Modifier.width(4.dp))
                Text("محادثة جديدة", fontSize = 12.sp)
            }

            // High Thinking Mode switch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("التفكير العميق", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Switch(
                    checked = useThinking,
                    onCheckedChange = { viewModel.useThinkingMode.value = it },
                    modifier = Modifier.scale(0.8f)
                )
            }

            IconButton(onClick = { showThreadSelector = !showThreadSelector }) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = "Chat Logs",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Voice Chat Quick Controller Bar (High Quality)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "قارئ الردود التلقائي (صوت عالي الجودة)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Switch(
                checked = autoReadChatEnabled,
                onCheckedChange = { viewModel.autoReadChatEnabled.value = it },
                modifier = Modifier.scale(0.75f)
            )
        }

        if (showThreadSelector) {
            // Dropdown list of existing sessions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 180.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp)
            ) {
                Text(
                    "سجل المحادثات السابقة:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(8.dp)
                )
                sessions.forEach { sess ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.selectSession(sess.id)
                                showThreadSelector = false
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(sess.title, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = { viewModel.deleteSession(sess.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.Red,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Divider()
                }
            }
        }

        // Messages List
        Box(modifier = Modifier.weight(1f)) {
            if (viewModel.currentSessionId.collectAsState().value == null) {
                // Empty state or automatic first session start
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "ابدأ دردشة فائقة الذكاء مع Smart Cat",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "اسأل أي سؤال، ابحث عن أفكار، أو تعلّم مهارات جديدة فوراً مع ميزة حفظ الجلسات والمزامنة المحلية.",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { viewModel.startNewSession("محادثة رئيسية") }) {
                        Text("بدء دردشة الآن")
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(messages) { msg ->
                        ChatBubble(message = msg, onSpeakClick = { viewModel.speakText(msg.content) })
                    }

                    if (isGenerating) {
                        item {
                            TypingIndicator()
                        }
                    }
                }
            }
        }

        // Chat Input row
        if (viewModel.currentSessionId.collectAsState().value != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Microphone/Voice Chat button
                IconButton(
                    onClick = {
                        if (isListeningToSpeech) {
                            viewModel.stopSpeechRecognition()
                        } else {
                            viewModel.startSpeechRecognition()
                        }
                    },
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .background(
                            if (isListeningToSpeech) Color.Red.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer,
                            CircleShape
                        )
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isListeningToSpeech) Icons.Default.MicNone else Icons.Default.Mic,
                        contentDescription = "التحدث بالصوت",
                        tint = if (isListeningToSpeech) Color.Red else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { 
                        Text(
                            if (isListeningToSpeech) "جاري الاستماع لصوتك..." else "اكتب رسالتك لـ Smart Cat..."
                        ) 
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input"),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    trailingIcon = {
                        if (inputText.isNotEmpty()) {
                            IconButton(onClick = {
                                viewModel.sendChatMessage(inputText)
                                inputText = ""
                                viewModel.clearSpeechInput()
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, onSpeakClick: (() -> Unit)? = null) {
    val isUser = message.role == "user"
    val align = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val containerColor = if (isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (isUser) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = align) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 0.dp,
                bottomEnd = if (isUser) 0.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.content,
                    color = contentColor,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isUser && onSpeakClick != null) {
                        IconButton(
                            onClick = onSpeakClick,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "قراءة النص بصوت عالٍ",
                                tint = contentColor.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                    Text(
                        text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp)),
                        fontSize = 9.sp,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val animatedY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotsY"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("جاري التفكير والكتابة", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                (0..2).forEach { index ->
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .graphicsLayer {
                                this.translationY = if (index == 0) animatedY else animatedY * 0.5f
                            }
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

// ==================== PRODUCTIVITY HUBS SCREEN ====================

@Composable
fun ProductivityScreen(viewModel: AppViewModel) {
    val docs by viewModel.productivityDocs.collectAsState()
    val selectedDoc by viewModel.selectedDoc.collectAsState()
    val isGenerating by viewModel.isGeneratingProd.collectAsState()

    var activeTab by remember { mutableStateOf("write") } // "write", "summarize", "saved"
    var promptInput by remember { mutableStateOf("") }
    var prodType by remember { mutableStateOf("research") } // "research", "report", "presentation"

    var simulatedFileContent by remember { mutableStateOf("") }
    var fileTypeSelected by remember { mutableStateOf("PDF") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Sub Tabs
        TabRow(
            selectedTabIndex = when (activeTab) {
                "write" -> 0
                "summarize" -> 1
                "saved" -> 2
                else -> 0
            }
        ) {
            Tab(selected = activeTab == "write", onClick = { activeTab = "write" }) {
                Text("صناعة المحتوى", modifier = Modifier.padding(12.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = activeTab == "summarize", onClick = { activeTab = "summarize" }) {
                Text("التلخيص والأبحاث", modifier = Modifier.padding(12.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = activeTab == "saved", onClick = { activeTab = "saved" }) {
                Text("المستندات المحفوظة", modifier = Modifier.padding(12.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            when (activeTab) {
                "write" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("اكتب أبحاثاً، مقالات، تقارير أو خطط عروض تقديمي بالذكاء الاصطناعي:", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                        // Selector
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("research" to "بحث / مقال", "report" to "تقرير إداري", "presentation" to "عروض (Slides)").forEach { (type, name) ->
                                FilterChip(
                                    selected = prodType == type,
                                    onClick = { prodType = type },
                                    label = { Text(name) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = promptInput,
                            onValueChange = { promptInput = it },
                            placeholder = { Text("مثال: اكتب مقالاً وافياً عن مستقبل الحوسبة الكمية ودورها في الذكاء الاصطناعي...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            maxLines = 5
                        )

                        Button(
                            onClick = {
                                viewModel.generateProductivityDoc(promptInput, prodType)
                                activeTab = "saved"
                                promptInput = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isGenerating && promptInput.isNotEmpty()
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("توليد المستند الآن")
                            }
                        }
                    }
                }
                "summarize" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("تلخيص الكتب والملفات الذكي:", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                        // Simulation of File Upload
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("PDF", "Word", "Excel").forEach { format ->
                                InputChip(
                                    selected = fileTypeSelected == format,
                                    onClick = {
                                        fileTypeSelected = format
                                        simulatedFileContent = getSimulatedFileContent(format)
                                    },
                                    label = { Text(format) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = simulatedFileContent,
                            onValueChange = { simulatedFileContent = it },
                            placeholder = { Text("الصق هنا نصوص الكتاب أو الملف الذي تود تلخيصه...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            maxLines = 10
                        )

                        Button(
                            onClick = {
                                viewModel.generateProductivityDoc("قم بتلخيص هذا الملف ($fileTypeSelected):\n$simulatedFileContent", "summary")
                                activeTab = "saved"
                                simulatedFileContent = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isGenerating && simulatedFileContent.isNotEmpty()
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("تلخيص الملف بالكامل")
                            }
                        }
                    }
                }
                "saved" -> {
                    if (selectedDoc != null) {
                        // Display Single Document
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { viewModel.deleteProductivityDoc(selectedDoc!!.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                }
                                Text(
                                    selectedDoc!!.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.End
                                )
                                Button(onClick = { viewModel.generateProductivityDoc("تحويل النص إلى صوت: " + selectedDoc!!.content, "speech") }, modifier = Modifier.padding(start = 8.dp)) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = "Listen")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("استمع")
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                    .padding(16.dp)
                            ) {
                                Text(selectedDoc!!.content, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { viewModel.clearSelectedDoc() }, modifier = Modifier.fillMaxWidth()) {
                                Text("العودة للقائمة")
                            }
                        }
                    } else {
                        // Display Saved Documents list
                        if (docs.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("لا يوجد مستندات حالياً", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(docs) { doc ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.selectProductivityDoc(doc) },
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(doc.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text(
                                                    text = when (doc.type) {
                                                        "research" -> "بحث / مقال"
                                                        "summary" -> "تلخيص ملف"
                                                        "report" -> "تقرير أعمال"
                                                        "presentation" -> "عرض تقديمي"
                                                        else -> "مستند إنتاجي"
                                                    },
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            Icon(Icons.Default.ArrowForward, contentDescription = "Open")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getSimulatedFileContent(format: String): String {
    return when (format) {
        "PDF" -> "كتاب 'مقدمة ابن خلدون': الفصل الثاني في العمران البدوي والأمم الوحشية والقبائل، وفيه بيان أن أهل البدو أقرب إلى الخير من أهل الحضر، وأنهم أشجع من الحضر لبعدهم عن القوانين المفسدة للبأس والمضعفة للنفوس..."
        "Word" -> "تقرير مشروع تطوير تطبيقات الذكاء الاصطناعي للمؤسسات لعام 2026. الأهداف تشمل: تقليل تكاليف خدمة العملاء بنسبة 40%، تسريع وتيرة إنتاج المقالات، وتوفير أدوات مساعدة آمنة بالكامل للطلاب..."
        "Excel" -> "الجدول المالي للمبيعات والأرباح الربع سنوية: الربع الأول: المبيعات 120,000 ريال، الأرباح 45,000 ريال. الربع الثاني: المبيعات 145,000 ريال، الأرباح 55,000 ريال. الربع الثالث: المبيعات 170,000 ريال، الأرباح 68,000 ريال..."
        else -> ""
    }
}

// ==================== AI PERSONAS SCREEN ====================

data class Persona(
    val id: String,
    val name: String,
    val role: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun PersonasScreen(viewModel: AppViewModel) {
    val selectedPersonaId by viewModel.selectedPersonaId.collectAsState()
    val personaVoiceEnabled by viewModel.personaVoiceEnabled.collectAsState()
    val isGenerating by viewModel.isGeneratingPersona.collectAsState()
    val messagesMap by viewModel.personaMessages.collectAsState()
    val isListeningToSpeech by viewModel.isListeningToSpeech.collectAsState()
    val speechInputText by viewModel.speechInputText.collectAsState()

    // Gamification and Recorder States
    val userPoints by viewModel.userPoints.collectAsState()
    val userBadges by viewModel.userBadges.collectAsState()
    val leaderboardList by viewModel.leaderboardList.collectAsState()
    val isRecordingExplanation by viewModel.isRecordingExplanation.collectAsState()
    val explanationText by viewModel.explanationText.collectAsState()

    val personasList = listOf(
        Persona("hasan", "حسن (صوت ولد)", "صاحبك الجدع - صوت ولد تفاعلي", Icons.Default.RecordVoiceOver, Color(0xFF2563EB)),
        Persona("jana", "جنى (صوت بنت)", "صديقتك الذكية - صوت بنت رقيق", Icons.Default.Face, Color(0xFFEC4899)),
        Persona("teacher", "أ. أحمد", "معلم ومبسط العلوم", Icons.Default.School, Color(0xFF10B981)),
        Persona("coder", "Coder AI", "خبير البرمجة والأكواد", Icons.Default.Code, Color(0xFF14B8A6)),
        Persona("doctor", "د. خالد", "طبيب العائلة التوعوي", Icons.Default.LocalHospital, Color(0xFFEF4444)),
        Persona("business", "أ. سمير", "مستشار دراسات الجدوى", Icons.Default.BusinessCenter, Color(0xFFF59E0B)),
        Persona("legal", "المستشار عادل", "استشاري الشؤون القانونية", Icons.Default.Gavel, Color(0xFF8B5CF6)),
        Persona("sheikh", "الشيخ عبد الرحمن", "فقيه الشريعة المعتدل", Icons.Default.SelfImprovement, Color(0xFF06B6D4)),
        Persona("coach", "الكابتن فهد", "مدرب اللياقة والصحة", Icons.Default.FitnessCenter, Color(0xFFF97316)),
        Persona("designer", "المصمم فنان", "واجهات التصميم الجرافيكي", Icons.Default.Palette, Color(0xFFEC4899)),
        Persona("chef", "الشيف مراد", "وصفات الطهي والحلويات", Icons.Default.Restaurant, Color(0xFF06B6D4)),
        Persona("nanny", "المربية فاطمة", "تربية الأطفال والأسرة", Icons.Default.BabyChangingStation, Color(0xFF6366F1))
    )

    val context = LocalContext.current
    val contentResolver = context.contentResolver

    // Image selection launcher
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                if (bytes != null) {
                    val base64 = Base64.encodeToString(bytes, Base64.DEFAULT)
                    viewModel.sendMultimodalMessage(base64, "image/jpeg")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // PDF selection launcher
    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                if (bytes != null) {
                    val base64 = Base64.encodeToString(bytes, Base64.DEFAULT)
                    viewModel.sendMultimodalMessage(base64, "application/pdf")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    var textInput by remember { mutableStateOf("") }
    var showVoiceSetupDialog by remember { mutableStateOf(false) }
    val currentMessages = messagesMap[selectedPersonaId] ?: emptyList()
    val activePersona = personasList.first { it.id == selectedPersonaId }

    val listState = rememberLazyListState()

    // Sync speech input text to user text field
    LaunchedEffect(speechInputText) {
        if (speechInputText.isNotEmpty()) {
            textInput = speechInputText
        }
    }

    LaunchedEffect(currentMessages.size) {
        if (currentMessages.isNotEmpty()) {
            listState.animateScrollToItem(currentMessages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Personas Horizontal List Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            personasList.forEach { p ->
                val isSelected = p.id == selectedPersonaId
                Column(
                    modifier = Modifier
                        .clickable { viewModel.selectedPersonaId.value = p.id }
                        .padding(horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) p.color else p.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = p.icon,
                            contentDescription = p.name,
                            tint = if (isSelected) Color.White else p.color,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        p.name,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Divider()

        // Voice Feedback Toggle Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.RecordVoiceOver,
                    contentDescription = null,
                    tint = activePersona.color
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        "تفعيل الصوت البشري الذكي",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "ينطق الرد آلياً بصوت حقيقي مجسم",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            Switch(
                checked = personaVoiceEnabled,
                onCheckedChange = {
                    viewModel.personaVoiceEnabled.value = it
                    if (!it) {
                        viewModel.stopSpeaking()
                    }
                },
                colors = SwitchDefaults.colors(checkedThumbColor = activePersona.color)
            )
        }

        // Study Mode Active Banner
        val isStudyModeActive by viewModel.isStudyModeActive.collectAsState()
        if (isStudyModeActive) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "وضع المذاكرة",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "وضع المذاكرة نشط 📚",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "حسن وجنى هيشرحوا بالخطوات والأمثلة مع أسئلة اختبار",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Button(
                        onClick = { viewModel.finishStudyAndGetReport() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("إنهاء والتقرير 🎓", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Persona Chat History
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
        ) {
            if (currentMessages.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(activePersona.color.copy(alpha = 0.15f))
                            .border(2.dp, activePersona.color.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "H",
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Black,
                            color = activePersona.color
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(currentMessages) { msg ->
                        PersonaBubble(msg = msg, p = activePersona, onSpeakClick = { viewModel.speakText(msg.content) })
                    }
                    if (isGenerating) {
                        item {
                            TypingIndicator()
                        }
                    }
                }
            }
        }

        // Study Mode quick action buttons
        if (isStudyModeActive) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.explainAgainSimply() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("اشرحلي تاني بسهولة 💡", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { viewModel.makeExamForMe() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("اعملي امتحان على ده 📝", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Input Box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Camera Button
            IconButton(
                onClick = { imageLauncher.launch("image/*") },
                modifier = Modifier
                    .padding(end = 4.dp)
                    .background(activePersona.color.copy(alpha = 0.12f), CircleShape)
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "صورلي وحلهالي",
                    tint = activePersona.color,
                    modifier = Modifier.size(20.dp)
                )
            }

            // PDF Button
            IconButton(
                onClick = { pdfLauncher.launch("application/pdf") },
                modifier = Modifier
                    .padding(end = 4.dp)
                    .background(activePersona.color.copy(alpha = 0.12f), CircleShape)
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = "رفع ملف PDF",
                    tint = activePersona.color,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Microphone/Voice Chat button
            IconButton(
                onClick = {
                    if (isListeningToSpeech) {
                        viewModel.stopSpeechRecognition()
                    } else {
                        showVoiceSetupDialog = true
                    }
                },
                modifier = Modifier
                    .padding(end = 4.dp)
                    .background(
                        if (isListeningToSpeech) Color.Red.copy(alpha = 0.2f) else activePersona.color.copy(alpha = 0.15f),
                        CircleShape
                    )
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = if (isListeningToSpeech) Icons.Default.MicNone else Icons.Default.Mic,
                    contentDescription = "التحدث بالصوت",
                    tint = if (isListeningToSpeech) Color.Red else activePersona.color,
                    modifier = Modifier.size(20.dp)
                )
            }

            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { 
                    Text(
                        if (isListeningToSpeech) "جاري الاستماع لصوتك..." else "اطرح سؤالاً على ${activePersona.name}..."
                    ) 
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                maxLines = 3,
                trailingIcon = {
                    if (textInput.isNotEmpty()) {
                        IconButton(onClick = {
                            viewModel.sendPersonaMessage(textInput)
                            textInput = ""
                            viewModel.clearSpeechInput()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = activePersona.color
                            )
                        }
                    }
                }
            )
        }
    }

    if (showVoiceSetupDialog) {
        VoiceSetupDialog(
            viewModel = viewModel,
            onDismiss = { showVoiceSetupDialog = false },
            onStartVoice = {
                showVoiceSetupDialog = false
                viewModel.startSpeechRecognition()
            }
        )
    }
}

@Composable
fun PersonaBubble(msg: ChatMessage, p: Persona, onSpeakClick: (() -> Unit)? = null) {
    val isUser = msg.role == "user"
    val align = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val containerColor = if (isUser) p.color else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = align) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(msg.content, color = contentColor, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isUser && onSpeakClick != null) {
                        IconButton(
                            onClick = onSpeakClick,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "قراءة النص بصوت عالٍ",
                                tint = contentColor.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                    Text(
                        text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(msg.timestamp)),
                        fontSize = 8.sp,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

// ==================== ART & VIDEO GENERATOR SCREEN ====================

@Composable
fun CreativeScreen(viewModel: AppViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Elegant glowing "H" in the center
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2563EB).copy(alpha = 0.15f))
                    .border(2.dp, Color(0xFF2563EB).copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "H",
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF3B82F6)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "قسم توليد الصور والفيديو قريباً 🔒",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "هنفعل الذكاء الاصطناعي للصور بعد تفعيل السيرفر المدفوع",
                fontSize = 13.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "جاري التحديث حالياً ⚙️",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF3B82F6),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ==================== ORGANIZER & SCHEDULER SCREEN ====================

@Composable
fun OrganizerScreen(viewModel: AppViewModel) {
    val schedule by viewModel.userSchedule.collectAsState()
    val isGenerating by viewModel.isGeneratingSchedule.collectAsState()

    var ageInput by remember { mutableStateOf("") }
    var occupation by remember { mutableStateOf("student") } // "student", "employee", "both"
    var workSchoolTimings by remember { mutableStateOf("") }
    var lessonTimings by remember { mutableStateOf("") }

    var glassesOfWater by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (schedule == null) {
            // Schedule builder form
            Text("ابنِ جدولك اليومي المتكامل والصحي بالذكاء الاصطناعي:", fontWeight = FontWeight.Bold, fontSize = 16.sp)

            OutlinedTextField(
                value = ageInput,
                onValueChange = { ageInput = it },
                label = { Text("كم عمرك؟") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("ما هي طبيعة عملك أو دراستك؟", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("student" to "طالب", "employee" to "موظف", "both" to "طالب وموظف").forEach { (occ, label) ->
                    FilterChip(
                        selected = occupation == occ,
                        onClick = { occupation = occ },
                        label = { Text(label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            OutlinedTextField(
                value = workSchoolTimings,
                onValueChange = { workSchoolTimings = it },
                label = { Text("مواعيد العمل أو المدرسة والمحاضرات اليومية") },
                placeholder = { Text("مثال: من 8 صباحاً إلى 2 ظهراً") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = lessonTimings,
                onValueChange = { lessonTimings = it },
                label = { Text("مواعيد الدروس والمواد الإضافية بالملّي") },
                placeholder = { Text("مثال: فيزياء السبت والثلاثاء 4 عصراً") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val age = ageInput.toIntOrNull() ?: 20
                    viewModel.generateDailySchedule(age, occupation, workSchoolTimings, lessonTimings)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isGenerating && ageInput.isNotEmpty()
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("بناء جدول اليوم المتكامل ذكياً")
                }
            }
        } else {
            // Schedule View + health trackers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.clearSchedule() }) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear Schedule", tint = Color.Red)
                }
                Text("جدولك اليومي المولد بالكامل:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            // Health & Water Trackers
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("متابع شرب المياه اليومي 💧", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("أكواب اليوم: $glassesOfWater / 8 أكواب", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { if (glassesOfWater < 12) glassesOfWater++ },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("+ كوب ماء")
                    }
                }
            }

            // Generated Schedule details
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(schedule!!.scheduleText, fontSize = 13.sp)
            }
        }
    }
}

// ==================== QURAN RECITATION ANALYSIS SCREEN ====================

@Composable
fun QuranScreen(viewModel: AppViewModel) {
    val isRecording by viewModel.isRecordingQuran.collectAsState()
    val isAnalyzing by viewModel.isAnalyzingQuran.collectAsState()
    val records by viewModel.quranRecords.collectAsState()

    var selectedSurah by remember { mutableStateOf("الفاتحة") }
    val surahsList = listOf("الفاتحة", "البقرة", "يس", "الملك", "الرحمن", "جزء عم")

    var showHistory by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { showHistory = !showHistory }) {
                Text(if (showHistory) "عرض المسجل" else "سجل التلاوات")
            }
            Text("مصحح التلاوة والقرآن الكريم:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        if (showHistory) {
            // Show Recitation Records history
            if (records.isEmpty()) {
                Box(modifier = Modifier.height(150.dp), contentAlignment = Alignment.Center) {
                    Text("لا يوجد تلاوات مسجلة مسبقاً.")
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    records.forEach { rec ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("الدرجة: ${rec.score}/100 🏆", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text("سورة ${rec.surah}", fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(rec.aiFeedback, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                IconButton(
                                    onClick = { viewModel.deleteQuranRecord(rec.id) },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Recorder Main Section
            Text("اختر السورة الكريمة التي ستقوم بتلاوتها:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                surahsList.forEach { s ->
                    FilterChip(
                        selected = selectedSurah == s,
                        onClick = { selectedSurah = s },
                        label = { Text(s) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Microphone Breathing Animation Button
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = if (isRecording) 1.25f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "micScale"
            )

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(
                        if (isRecording) {
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFEF4444), Color(0xFFEF4444).copy(alpha = 0.2f))
                            )
                        } else {
                            Brush.radialGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            )
                        }
                    )
                    .clickable {
                        if (isRecording) {
                            viewModel.stopAndAnalyzeQuran(selectedSurah)
                        } else {
                            viewModel.startRecordingQuran()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.MicNone else Icons.Default.Mic,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isRecording) "اضغط مجدداً للإيقاف والتحليل فوراً" else "اضغط لتسجيل تلاوتك الآن بصوت نقي",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isRecording) Color.Red else MaterialTheme.colorScheme.onBackground
            )

            if (isAnalyzing) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("جاري تحليل التلاوة والتدقيق في أحكام التجويد ومخارج الحروف...", fontSize = 11.sp, textAlign = TextAlign.Center)
                }
            }

            // Latest Feedback Display
            if (records.isNotEmpty() && !isAnalyzing) {
                val lastRecord = records.first()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "النتيجة الأخيرة لتلاوتك لسورة ${lastRecord.surah}:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("درجة التلاوة: ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("${lastRecord.score}/100", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(lastRecord.aiFeedback, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// ==================== SETTINGS DIALOG ====================

@Composable
fun SettingsDialog(viewModel: AppViewModel, onDismiss: () -> Unit) {
    val language by viewModel.appLanguage.collectAsState()
    val speed by viewModel.speechSpeed.collectAsState()
    val selectedVoice by viewModel.selectedVoice.collectAsState()
    val autoReadChat by viewModel.autoReadChatEnabled.collectAsState()
    val appTheme by viewModel.appTheme.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("إعدادات تطبيق H2 Hub", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                // Theme selector
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("سمات التطبيق (Themes):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    val themes = listOf(
                        "purple" to "البنفسجي 💜",
                        "dark" to "داكن 🌙",
                        "light" to "فاتح ☀️"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        themes.forEach { (id, label) ->
                            val isSelected = appTheme == id
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                    .clickable { viewModel.appTheme.value = id }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    label,
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Audio speed setting
                Column {
                    Text("سرعة نطق الصوت: ${String.format(Locale.US, "%.1f", speed)}x", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Slider(
                        value = speed,
                        onValueChange = { viewModel.speechSpeed.value = it },
                        valueRange = 0.5f..2.0f
                    )
                }

                // Auto read toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("قراءة الردود تلقائياً 🔊", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("نطق ردود الذكاء الاصطناعي فور كتابتها بأعلى جودة ممكنة", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                    Switch(
                        checked = autoReadChat,
                        onCheckedChange = { viewModel.autoReadChatEnabled.value = it }
                    )
                }

                // Voice selector
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("صوت المعلق الذكي (Gemini High-Fi):", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    val voices = listOf(
                        "Kore" to "كور (نسائي نقي)",
                        "Aoede" to "أويدي (نسائي ناعم)",
                        "Charon" to "شارون (رجالي دافئ)",
                        "Puck" to "بوك (رجالي حيوي)",
                        "Fenrir" to "فينرير (رجالي معبر)"
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        voices.forEach { (id, label) ->
                            val isSelected = selectedVoice == id
                            FilterChip(
                                selected = isSelected,
                                onClick = { 
                                    viewModel.selectedVoice.value = id
                                    viewModel.speakText("تم اختيار صوت المعلق بنجاح")
                                },
                                label = { Text(label, fontSize = 10.sp) }
                            )
                        }
                    }
                }

                // Location simulator setting
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("مشاركة الموقع الآمن", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("مشاركة إحداثيات موقعك لحساب مواقيت الصلاة والمطاعم المجاورة", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                    Button(onClick = { /* Simulated safe share */ }) {
                        Text("تشير الموقع", fontSize = 11.sp)
                    }
                }

                // Update setting
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("التحديث والترقيات", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("الإصدار الحالي: 1.0.0 Global", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                    Button(onClick = { /* check for update */ }) {
                        Text("تحديث", fontSize = 11.sp)
                    }
                }

                // App Info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text("معلومات الاستخدام:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("التطبيق يعمل بنظام حماية البيانات والخصوصية الكامل للأفراد، ومزود بالذكاء الاصطناعي من Google Gemini وVeo.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("تم وحفظ")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceSetupDialog(
    viewModel: AppViewModel,
    onDismiss: () -> Unit,
    onStartVoice: () -> Unit
) {
    var selectedVoice by remember { mutableStateOf(viewModel.selectedPersonaId.value) }
    val currentLocale by viewModel.selectedSTTLocale.collectAsState()
    var selectedLang by remember { mutableStateOf(currentLocale) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    "المحادثة الصوتية الذكية 🎙️",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                Text(
                    "اضبط إعدادات المحادثة الصوتية الفورية مع الصديق الذكي لتبدأ الحوار الصوتي المباشر:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 1. Voice selector
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1. اختر معلقك المفضل:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Hasan Option
                        Card(
                            onClick = { selectedVoice = "hasan" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                width = if (selectedVoice == "hasan") 2.dp else 1.dp,
                                color = if (selectedVoice == "hasan") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedVoice == "hasan") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.RecordVoiceOver,
                                    contentDescription = null,
                                    tint = if (selectedVoice == "hasan") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.size(32.dp)
                                )
                                Text("حسن (صوت ولد)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("صوت ذكوري مصري عميق", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                            }
                        }

                        // Jana Option
                        Card(
                            onClick = { selectedVoice = "jana" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                width = if (selectedVoice == "jana") 2.dp else 1.dp,
                                color = if (selectedVoice == "jana") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedVoice == "jana") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Face,
                                    contentDescription = null,
                                    tint = if (selectedVoice == "jana") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.size(32.dp)
                                )
                                Text("جنى (صوت بنت)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("صوت أنثوي مصري رقيق", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }

                // 2. Language selector
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("2. لغة التحدث المفضلة:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val languages = listOf(
                            Triple("ar-EG", "مصري عامية", "🇪🇬"),
                            Triple("ar-SA", "عربي فصحى", "🇸🇦"),
                            Triple("en-US", "English", "🇺🇸")
                        )
                        languages.forEach { (locale, label, flag) ->
                            val isSelected = selectedLang == locale
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedLang = locale }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(flag, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        label,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.selectedPersonaId.value = selectedVoice
                    viewModel.selectedSTTLocale.value = selectedLang
                    onStartVoice()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("ابدأ التحدث بالصوت 🎙️", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("إلغاء", color = MaterialTheme.colorScheme.outline)
            }
        }
    )
}

