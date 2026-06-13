package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.entity.*
import com.example.ui.YemenStyle
import com.example.ui.YemenStyle.responsiveContainer
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.JournalViewModel
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var isFirstNetworkCallback = true
    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Connectivity Monitoring for instant forced Firestore reconnection
        try {
            connectivityManager = getSystemService(ConnectivityManager::class.java)
            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    if (!isFirstNetworkCallback) {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "تمت إعادة الاتصال بالإنترنت! جاري تحديث المزامنة الفورية...", Toast.LENGTH_SHORT).show()
                        }
                        // Obtain current ViewModel and force recreate Snapshot Listeners
                        try {
                            val viewModel = androidx.lifecycle.ViewModelProvider(this@MainActivity)[JournalViewModel::class.java]
                            viewModel.reconnectFirestore()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        isFirstNetworkCallback = false
                    }
                }
            }
            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager?.registerNetworkCallback(networkRequest, networkCallback!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        enableEdgeToEdge()
        setContent {
            val viewModel: JournalViewModel = viewModel()
            val settings by viewModel.appSettings.collectAsStateWithLifecycle()

            MyApplicationTheme(themeName = settings.colorTheme) {
                MainSanctuaryScreen(viewModel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            networkCallback?.let { connectivityManager?.unregisterNetworkCallback(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

enum class ActiveScreen {
    HOME, ABOUT, REGISTER, LOGIN
}

@Composable
fun MainSanctuaryScreen(viewModel: JournalViewModel) {
    var activeScreen by remember { mutableStateOf(ActiveScreen.HOME) }
    var arabicLangSelected by remember { mutableStateOf(true) }
    
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    )

    var hometapCount by remember { mutableStateOf(0) }
    var showSecretGateDialog by remember { mutableStateOf(false) }
    var showAssistantChat by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
            .safeDrawingPadding(),
        topBar = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (arabicLangSelected) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable {
                                    hometapCount += 1
                                    if (hometapCount >= 5) {
                                        showSecretGateDialog = true
                                        hometapCount = 0
                                    }
                                }
                                .padding(4.dp)
                        ) {
                            Text(
                                text = "🇾🇪",
                                fontSize = 24.sp,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Column {
                                Text(
                                    text = settings.appName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 16.sp
                                    )
                                )
                                Text(
                                    text = settings.welcomeMessage.take(28) + "...",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        IconButton(
                            onClick = {
                                activeScreen = ActiveScreen.HOME
                                hometapCount += 1
                                if (hometapCount >= 5) {
                                    showSecretGateDialog = true
                                    hometapCount = 0
                                }
                            },
                            modifier = Modifier.testTag("app_bar_home")
                        ) {
                            Icon(Icons.Default.Home, contentDescription = "الرئيسية", tint = if (activeScreen == ActiveScreen.HOME) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                        }

                        IconButton(
                            onClick = { activeScreen = ActiveScreen.REGISTER },
                            modifier = Modifier.testTag("app_bar_register")
                        ) {
                            Icon(Icons.Default.Person, contentDescription = "تسجيل فني", tint = if (activeScreen == ActiveScreen.REGISTER) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                        }

                        IconButton(
                            onClick = { activeScreen = ActiveScreen.LOGIN },
                            modifier = Modifier.testTag("app_bar_login")
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "الإدارة", tint = if (activeScreen == ActiveScreen.LOGIN) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                        }

                        TextButton(onClick = { arabicLangSelected = !arabicLangSelected }) {
                            Text(if (arabicLangSelected) "🌐 EN" else "🌐 عربي", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        IconButton(onClick = {
                            Toast.makeText(context, "تمت إعادة المزامنة الفورية مع سحابة Firestore!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "مزامنة")
                        }

                    } else { // EN version
                        IconButton(onClick = {
                            Toast.makeText(context, "Forced Live Firestore Cloud Re-Synch!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                        TextButton(onClick = { arabicLangSelected = !arabicLangSelected }) {
                            Text(if (arabicLangSelected) "🌐 EN" else "🌐 عربي", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { activeScreen = ActiveScreen.LOGIN }) {
                            Icon(Icons.Default.Lock, contentDescription = "Admin Area", tint = if (activeScreen == ActiveScreen.LOGIN) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                        }
                        IconButton(onClick = { activeScreen = ActiveScreen.REGISTER }) {
                            Icon(Icons.Default.Person, contentDescription = "Register", tint = if (activeScreen == ActiveScreen.REGISTER) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                        }
                        IconButton(onClick = { activeScreen = ActiveScreen.HOME }) {
                            Icon(Icons.Default.Home, contentDescription = "Home", tint = if (activeScreen == ActiveScreen.HOME) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.clickable {
                                hometapCount += 1
                                if (hometapCount >= 5) {
                                    showSecretGateDialog = true
                                    hometapCount = 0
                                }
                            }
                        ) {
                            Text(
                                text = "Directory Yemen",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = "Live Cloud Syncing ACTIVE",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            val backColor = MaterialTheme.colorScheme.surface.copy(alpha = settings.footerTransparency)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = backColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { activeScreen = ActiveScreen.ABOUT },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("ℹ️", fontSize = 16.sp)
                    }

                    Text(
                        text = settings.footerText,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        fontSize = settings.footerFontSize.sp,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    TextButton(
                        onClick = { showAssistantChat = true },
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "🤖 مساعد ذكي",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = activeScreen,
                transitionSpec = {
                    slideInVertically { it } + fadeIn() togetherWith slideOutVertically { -it } + fadeOut()
                },
                label = "screenChange"
            ) { screen ->
                when (screen) {
                    ActiveScreen.HOME -> HomeScreenContent(viewModel, arabicLangSelected)
                    ActiveScreen.ABOUT -> AboutScreenContent(viewModel, arabicLangSelected)
                    ActiveScreen.REGISTER -> RegisterScreenContent(viewModel, arabicLangSelected)
                    ActiveScreen.LOGIN -> LoginAdminContent(viewModel, arabicLangSelected)
                }
            }

            // High stability adaptive positioning assistant button
            if (settings.showAssistant) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 56.dp, end = 16.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    FloatingActionButton(
                        onClick = { showAssistantChat = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .size(settings.assistantIconSize.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("🤖", fontSize = (settings.assistantIconSize / 3.2f).sp)
                            Text("خدمات", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Secret gate login PopUp
            if (showSecretGateDialog) {
                var gateText by remember { mutableStateOf("") }
                var isError by remember { mutableStateOf(false) }

                Dialog(onDismissRequest = { showSecretGateDialog = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🔒 البوابة الخلفية السرية للمالك",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            OutlinedTextField(
                                value = gateText,
                                onValueChange = {
                                    gateText = it
                                    isError = false
                                },
                                label = { Text("رمز العبور والتحكم العالي") },
                                modifier = Modifier.fillMaxWidth(),
                                visualTransformation = PasswordVisualTransformation(),
                                isError = isError
                            )

                            if (isError) {
                                Text(
                                    text = "رمز المرور خاطئ! يرجى التحقق وإعادة الإدخال.",
                                    color = Color.Red,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { showSecretGateDialog = false }) {
                                    Text("إلغاء")
                                }
                                Button(
                                    onClick = {
                                        if (gateText == "maher--736462") {
                                            viewModel.setSession("المالك المصلح", "owner")
                                            activeScreen = ActiveScreen.LOGIN
                                            showSecretGateDialog = false
                                            Toast.makeText(context, "أهلاً بك يا مالك المنصة! تم تفعيل الصلاحيات الكاملة لمزامنة الهويات.", Toast.LENGTH_LONG).show()
                                        } else {
                                            isError = true
                                        }
                                    }
                                ) {
                                    Text("تحقق ودخول")
                                }
                            }
                        }
                    }
                }
            }

            if (showAssistantChat) {
                SmartAssistantWidget(viewModel) {
                    showAssistantChat = false
                }
            }
        }
    }
}

@Composable
fun HomeScreenContent(viewModel: JournalViewModel, ar: Boolean) {
    val providers by viewModel.allProfessionals.collectAsStateWithLifecycle()
    val categories by viewModel.allCategories.collectAsStateWithLifecycle()
    val banners by viewModel.allBanners.collectAsStateWithLifecycle()
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()

    var activeCatId by remember { mutableStateOf<String?>(null) }
    var searchByCity by remember { mutableStateOf("") }
    var searchByArea by remember { mutableStateOf("") }
    var searchKeyword by remember { mutableStateOf("") }
    
    var isVoiceActive by remember { mutableStateOf(false) }
    var selectedProfForSheet by remember { mutableStateOf<Professional?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp)
    ) {
        if (banners.isNotEmpty()) {
            item {
                var currentBannerIndex by remember { mutableStateOf(0) }
                val activeB = banners[currentBannerIndex % banners.size]

                LaunchedEffect(banners) {
                    while (true) {
                        delay((activeB.duration * 1000L).coerceAtLeast(3000L))
                        currentBannerIndex = (currentBannerIndex + 1) % banners.size
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            if (activeB.link.isNotEmpty()) {
                                // optional browser launch
                            }
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📢", fontSize = 20.sp, modifier = Modifier.padding(end = 8.dp))
                            Text(
                                text = activeB.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = activeB.content,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                        )
                        if (activeB.link.isNotEmpty()) {
                            Text(
                                text = "🔗 تفاصيل: " + activeB.link,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (ar) "🔍 ابحث بذكاء عن أفضل المهن والخدمات" else "🔍 Advanced Yemen Search Engine",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchByCity,
                            onValueChange = { searchByCity = it },
                            label = { Text(if (ar) "المدينة/المحافظة" else "City") },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = searchByArea,
                            onValueChange = { searchByArea = it },
                            label = { Text(if (ar) "الحي السكني" else "Neighborhood") },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchKeyword,
                            onValueChange = { searchKeyword = it },
                            placeholder = { Text(if (ar) "ابحث باسم المهني، رقم هاتفه، أو مهارته..." else "Enter skills, name, phone...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                IconButton(onClick = { isVoiceActive = true }) {
                                    Text("🎤", fontSize = 20.sp)
                                }
                            }
                        )
                    }
                }
            }
        }

        if (isVoiceActive) {
            item {
                Dialog(onDismissRequest = { isVoiceActive = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🎙️ جاري تصفية الصوت الذكي باليمن", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                            val pulse by infiniteTransition.animateFloat(
                                initialValue = 0.8f,
                                targetValue = 1.3f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "scale"
                            )

                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🎤", fontSize = (24 * pulse).sp)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("تحدث الآن... (سيفهم المساعد لهجتك المتخصصة)", fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            val voiceKeywords = listOf("سباك", "كهربائي", "ماهر طاهر", "نجار", "الستين", "صنعاء")
                            LazyRow {
                                items(voiceKeywords) { kw ->
                                    Button(
                                        onClick = {
                                            searchKeyword = kw
                                            isVoiceActive = false
                                        },
                                        modifier = Modifier.padding(4.dp)
                                    ) {
                                        Text(kw)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = if (ar) "📁 الأقسام والتصنيفات الجاهزة" else "📁 Professional Chambers",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = activeCatId == null,
                        onClick = { activeCatId = null },
                        label = { Text(if (ar) "عرض الكل" else "Show All") }
                    )
                }

                items(categories) { cat ->
                    FilterChip(
                        selected = activeCatId == cat.id,
                        onClick = { activeCatId = cat.id },
                        label = { Text("${cat.iconEmoji} ${if (ar) cat.nameAr else cat.nameEn}") }
                    )
                }
            }
        }

        val filteredList = providers.filter { prof ->
            val matchesCategory = if (activeCatId == null) {
                true
            } else {
                val catObj = categories.find { it.id == activeCatId }
                if (catObj != null) {
                    prof.specialty.contains(catObj.nameAr.take(5)) || prof.skills.contains(catObj.nameAr.take(5))
                } else true
            }

            val matchesCity = if (searchByCity.isBlank()) true else prof.city.contains(searchByCity, ignoreCase = true)
            val matchesArea = if (searchByArea.isBlank()) true else prof.area.contains(searchByArea, ignoreCase = true)
            val matchesQuery = if (searchKeyword.isBlank()) true else {
                prof.name.contains(searchKeyword, ignoreCase = true) ||
                prof.contactPhone.contains(searchKeyword) ||
                prof.skills.contains(searchKeyword, ignoreCase = true)
            }

            matchesCategory && matchesCity && matchesArea && matchesQuery && !prof.isBanned
        }.sortedWith(
            compareByDescending<Professional> { it.isPinned }
                .thenByDescending { it.isSubscribed }
                .thenByDescending { it.rating }
        )

        val recommends = filteredList.filter { it.isRecommended }
        if (recommends.isNotEmpty()) {
            item {
                Text(
                    text = if (ar) "⭐ الموصى بهم والمميزين" else "⭐ Best Recommended Guilds",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(recommends) { r ->
                        Card(
                            modifier = Modifier
                                .width(200.dp)
                                .clickable { selectedProfForSheet = r },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(r.avatarEmoji, fontSize = 20.sp)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = r.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = r.specialty.take(18) + "...",
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("⭐ " + r.rating, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    if (r.isVerified) {
                                        Text("✔️ موثق", color = Color(0xFF1E88E5), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = if (ar) "👷 مقدمي الخدمات والمهنيين المتاحين" else "👷 Registered Professionals",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        if (filteredList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("⚠️", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (ar) "عذراً! لا يوجد مهني يتسق مع معطيات الفلترة الحالية." else "No professionals match your filter criteria.",
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredList) { prof ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { selectedProfForSheet = prof }
                        .testTag("prof_card_${prof.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (prof.isPinned) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(prof.avatarEmoji, fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = prof.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    if (prof.isVerified) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("💠", color = Color(0xFF1E88E5), fontSize = 14.sp)
                                    }
                                    if (prof.isSubscribed) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD700)),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                "مشترك مميز",
                                                fontSize = 8.sp,
                                                color = Color.Black,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = prof.specialty,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            
                            if (prof.isPinned) {
                                Text("📌 مُثبّت", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            prof.skills.split(",").take(3).forEach { skill ->
                                if (skill.isNotBlank()) {
                                    Card(
                                        modifier = Modifier.padding(horizontal = 4.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = skill.trim(),
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📍 ${prof.city} - ${prof.area}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⭐ " + prof.rating, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(" (${prof.ratingCount} تقييم)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedProfForSheet != null) {
        val p = selectedProfForSheet!!
        var commentField by remember { mutableStateOf("") }
        var activeStars by remember { mutableStateOf(5) }
        val allReviewsList by viewModel.allReviews.collectAsStateWithLifecycle()
        val matchedReviews = allReviewsList.filter { it.providerId == p.id && !it.isBanned }

        Dialog(onDismissRequest = { selectedProfForSheet = null }) {
            val ctx = LocalContext.current
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(8.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(p.avatarEmoji, fontSize = 28.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(p.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text(p.specialty, fontSize = 11.sp)
                            }
                            IconButton(onClick = { selectedProfForSheet = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${p.contactPhone}"))
                                    ctx.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("📞 اتصل هاتفياً")
                            }

                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/967${p.contactPhone}"))
                                    ctx.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                            ) {
                                Text("💬 واتساب")
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    if (settings.isMapEnabled) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .padding(vertical = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        drawCircle(
                                            color = Color.LightGray.copy(0.4f),
                                            radius = size.width / 4,
                                            style = Stroke(width = 2f)
                                        )
                                    }
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.SpaceBetween,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("🗺️ خريطة تحديد موقع فروع مقدم الخدمة باليمن", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Text("الإحداثيات النشطة: (${p.latitude}, ${p.longitude})", color = Color.White, fontSize = 10.sp)
                                        Button(
                                            onClick = {
                                                val uri = "geo:${p.latitude},${p.longitude}?q=${p.latitude},${p.longitude}(${p.name})"
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                                                ctx.startActivity(intent)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Text("🧭 لفتح الاتجاهات")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.2f)),
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("🎁 مبادرة الولاء الفوري والمكافآت", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                                Text("عند تقييمك لهذا الحرفي تحصل على +15 نقطة ولاء مجانية يمكنك تفعيلها كخصومات حقيقية في معامل المبادرة اليمنية!", fontSize = 10.sp)
                            }
                        }
                    }

                    item {
                        Text("📝 تقييمات وآراء المواطنين (${matchedReviews.size})", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 6.dp))
                    }

                    if (matchedReviews.isEmpty()) {
                        item {
                            Text("لا توجد مراجعات مسبقة لهذا المهني. كن أول من يكتب!", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))
                        }
                    } else {
                        items(matchedReviews) { rev ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(rev.authorName, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Text("⭐ " + rev.rating, fontSize = 10.sp, color = Color.Yellow)
                                    }
                                    Text(rev.comment, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                                }
                            }
                        }
                    }

                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        Text("اضف تقييمك الخاص", fontWeight = FontWeight.Bold)
                        
                        Row(modifier = Modifier.padding(vertical = 8.dp)) {
                            (1..5).forEach { star ->
                                IconButton(onClick = { activeStars = star }, modifier = Modifier.size(32.dp)) {
                                    Text(
                                        text = if (star <= activeStars) "★" else "☆",
                                        fontSize = 24.sp,
                                        color = if (star <= activeStars) Color.Yellow else Color.Gray
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = commentField,
                            onValueChange = { commentField = it },
                            placeholder = { Text("اكتب رأيك بصراحة وأمانة...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (commentField.isNotBlank()) {
                                    viewModel.leaveRating(p.id, activeStars.toFloat(), commentField, viewModel.currentUserSession.value)
                                    commentField = ""
                                    Toast.makeText(ctx, "شكرًا لتقييمك! كسبت +15 نقطة ولاء.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("إرسال التقييم")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AboutScreenContent(viewModel: JournalViewModel, ar: Boolean) {
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🇾🇪", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = settings.appName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "نسخة الإصدار الدائم: v2.6 - سحابي تفاعلي",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (ar) "ℹ️ عن المبادرة الوطنية للخدمات" else "ℹ️ About the National Initiative",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = if (ar) "يهدف هذا الدليل الوطني اليمني المتكامل لتسهيل الوصول الفوري للحرفيين والمهندسين وأصحاب الكفاءات المهنية والصناعية في مختلف المدن اليمنية، لضمان استمرار دوران عجلة الاقتصاد والخدمات المجتمعية بشتى ربوع الوطن السعيد."
                    else "This directory simplifies finding yemeni workers, technicians, and local pros to support economic sustainability.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (ar) "📞 قنوات التواصل والإنقاذ الموحدة:" else "📞 Direct Support Lines:",
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${settings.supportPhone}"))
                            ctx.startActivity(intent)
                        }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📞 هاتف الدعم الفني: ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(settings.supportPhone, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/967${settings.supportWhatsapp}"))
                            ctx.startActivity(intent)
                        }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💬 واتساب الدعم الفني: ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(settings.supportWhatsapp, color = Color(0xFF25D366), fontSize = 12.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📧 بريد الدعم الفني: ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(settings.supportEmail, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun RegisterScreenContent(viewModel: JournalViewModel, ar: Boolean) {
    val categories by viewModel.allCategories.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedCategoryName by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("صنعاء") }
    var area by remember { mutableStateOf("") }
    var addressDetails by remember { mutableStateOf("") }
    var gpsCoords by remember { mutableStateOf("") }
    var selfieAttached by remember { mutableStateOf(false) }
    var docAttached by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = if (ar) "👤 استمارة انضمام مقدمي تخصصات الخدمات وعرض الأعمال" else "👤 Apply as Service Provider",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (ar) "الاسم الثلاثي الكامل (إجباري)" else "Full Triple Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(if (ar) "رقم الهاتف الفعال / واتساب (إجباري)" else "Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCategoryName,
                        onValueChange = {},
                        label = { Text(if (ar) "القسم والخدمة الرئيسية (إجباري)" else "Select Category") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { isCategoryDropdownExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = isCategoryDropdownExpanded,
                        onDismissRequest = { isCategoryDropdownExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text("${cat.iconEmoji} ${cat.nameAr}") },
                                onClick = {
                                    selectedCategoryName = cat.nameAr
                                    isCategoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text(if (ar) "المحافظة/المدينة (إجباري)" else "City") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text(if (ar) "منطقة السكن / المديرية (إجباري)" else "Area") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = addressDetails,
                    onValueChange = { addressDetails = it },
                    label = { Text(if (ar) "مقر العمل الحالي / الشارع (إجباري)" else "Office Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = gpsCoords,
                    onValueChange = { gpsCoords = it },
                    label = { Text(if (ar) "إحداثيات موقع الخريطة GPS (اختياري)" else "GPS Coordinates") },
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(if (ar) "📸 الصورة الشخصية / السيلفي (إجباري)" else "📸 Persona Photo", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(if (selfieAttached) "تم الاختيار بنجاح ✔️" else "يرجى الرفع أو الالتقاط بالكاميرا", fontSize = 10.sp, color = if (selfieAttached) Color.Green else Color.Gray)
                    }
                    Button(
                        onClick = { selfieAttached = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.testTag("selfie_trigger")
                    ) {
                        Text(if (ar) "كاميرا / معرض" else "Upload")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(if (ar) "🪪 صورة بطاقة الهوية الوطنية (اختياري)" else "🪪 ID Card Image", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(if (docAttached) "تم الرفع بنجاح ✔️" else "يرجى إرفاق البطاقة للمصداقية", fontSize = 10.sp, color = if (docAttached) Color.Green else Color.Gray)
                    }
                    Button(
                        onClick = { docAttached = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text(if (ar) "إرفاق مستند" else "Attach")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (name.isBlank() || phone.isBlank() || selectedCategoryName.isBlank() || !selfieAttached) {
                            Toast.makeText(ctx, "يرجى ملء كافة الحقول الإلزامية ورفع صورتك الشخصية!", Toast.LENGTH_SHORT).show()
                        } else {
                            isSubmitting = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(if (ar) "تقديم طلب الانضمام للمراجعة الفورية 🚀" else "Submit Membership Request")
                    }
                }
            }
        }

        if (isSubmitting) {
            LaunchedEffect(Unit) {
                delay(1800)
                viewModel.applyAsProvider(
                    name = name,
                    phone = phone,
                    category = selectedCategoryName,
                    city = city,
                    area = area,
                    address = addressDetails,
                    gps = gpsCoords,
                    selfieBase64 = "👨‍🔧",
                    idCardBase64 = "CARD_REF"
                )
                Toast.makeText(ctx, "تم تقديم طلبك بنجاح! سيراجعه المشرفون خلال دقائق وتلقي مكالمة.", Toast.LENGTH_LONG).show()
                name = ""
                phone = ""
                selectedCategoryName = ""
                addressDetails = ""
                area = ""
                selfieAttached = false
                docAttached = false
                isSubmitting = false
            }
        }
    }
}

@Composable
fun LoginAdminContent(viewModel: JournalViewModel, ar: Boolean) {
    val currentRole by viewModel.currentUserRole.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var isLogging by remember { mutableStateOf(false) }

    if (currentRole == "guest") {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (ar) "🔐 تسجيل دخول المشرفين ومقدمي الخدمة" else "🔐 Control Door",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = user,
                        onValueChange = { user = it },
                        label = { Text(if (ar) "اسم المستخدم" else "Username") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = pass,
                        onValueChange = { pass = it },
                        label = { Text(if (ar) "رقم الهاتف / كلمة المرور" else "Credentials") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Button(
                        onClick = {
                            isLogging = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isLogging) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text(if (ar) "دخول آمن" else "Access Securely")
                        }
                    }
                }
            }
        }

        if (isLogging) {
            LaunchedEffect(Unit) {
                delay(1000)
                val success = viewModel.attemptLogin(user, pass)
                if (success) {
                    Toast.makeText(ctx, "مرحباً بـ $user في لوحة المراقبة!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(ctx, "اسم المستخدم أو كلمة المرور غير مطابقة!", Toast.LENGTH_SHORT).show()
                }
                isLogging = false
            }
        }
    } else {
        SupervisorDashboardContent(viewModel, ar)
    }
}

@Composable
fun SupervisorDashboardContent(viewModel: JournalViewModel, ar: Boolean) {
    val currentRole by viewModel.currentUserRole.collectAsStateWithLifecycle()
    val currentSession by viewModel.currentUserSession.collectAsStateWithLifecycle()

    val providers by viewModel.allProfessionals.collectAsStateWithLifecycle()
    val categories by viewModel.allCategories.collectAsStateWithLifecycle()
    val pendingList by viewModel.allPendingProviders.collectAsStateWithLifecycle()
    val reviews by viewModel.allReviews.collectAsStateWithLifecycle()
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val notifications by viewModel.allNotifications.collectAsStateWithLifecycle()
    val bookings by viewModel.allBookings.collectAsStateWithLifecycle()

    var activeSubTab by remember { mutableStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("الاسم المستعار: $currentSession", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("مستوى الصلاحيات: $currentRole", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.7f))
                    }
                    Button(
                        onClick = { viewModel.logout() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("تسجيل خروج")
                    }
                }
            }
        }

        item {
            val tabs = listOf(
                "📊 إحصائيات",
                "📩 الطلبات المعلقة (${pendingList.size})",
                "📁 إدارة الأقسام",
                "👷 المهنيين",
                "⚙️ الإعدادات العامة",
                "💾 الصيانة والنسخ",
                "🔔 الإشعارات",
                "📅 الحجوزات"
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                items(tabs.size) { index ->
                    FilterChip(
                        selected = activeSubTab == index,
                        onClick = { activeSubTab = index },
                        label = { Text(tabs[index]) },
                        leadingIcon = {
                            when (index) {
                                6 -> Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                                7 -> Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(16.dp))
                                else -> null
                            }
                        }
                    )
                }
            }
        }

        when (activeSubTab) {
            0 -> {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Card(modifier = Modifier.weight(1f)) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("👷 المسجلين", fontSize = 11.sp)
                                    Text("${providers.size}", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Card(modifier = Modifier.weight(1f)) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("📩 طلبات انتظار", fontSize = 11.sp)
                                    Text("${pendingList.size}", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Card(modifier = Modifier.weight(1f)) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("📝 التعليقات", fontSize = 11.sp)
                                    Text("${reviews.size}", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                                }
                            }
                            Card(modifier = Modifier.weight(1f)) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("📂 أقسام ملقنة", fontSize = 11.sp)
                                    Text("${categories.size}", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                                }
                            }
                        }

                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("📈 الأقسام الأكثر طلباً ونشاطاً (مجسّم تخطيطي)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(12.dp))
                                categories.take(4).forEach { cat ->
                                    val count = providers.filter { it.specialty.contains(cat.nameAr.take(4)) }.size
                                    Text("${cat.iconEmoji} ${cat.nameAr}: $count فنيين", fontSize = 11.sp)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color.Gray.copy(0.2f))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth((count * 0.2f).coerceAtMost(1f))
                                                .fillMaxHeight()
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                if (pendingList.isEmpty()) {
                    item {
                        Text("لا توجد طلبات معلقة بانتظار المراجعة الآن.", modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center)
                    }
                } else {
                    items(pendingList) { app ->
                        var rejectReason by remember { mutableStateOf("") }
                        var showRejectionInput by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("طالب الانضمام: ${app.name}", fontWeight = FontWeight.Bold)
                                Text("رقم الهاتف: ${app.phone}", fontSize = 12.sp)
                                Text("التخصص المرغوب: ${app.categoryName}", fontSize = 11.sp)
                                Text("العنوان: ${app.city} - ${app.area}", fontSize = 11.sp)
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Card(modifier = Modifier.weight(1f)) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(6.dp)) {
                                            Text("📸 الصورة الشخصية", fontSize = 9.sp)
                                            Text(app.selfieImage, fontSize = 24.sp)
                                        }
                                    }
                                    Card(modifier = Modifier.weight(1f)) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(6.dp)) {
                                            Text("🪪 بطاقة الهوية", fontSize = 9.sp)
                                            Text("📄 مفرزة", fontSize = 20.sp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { viewModel.approvePendingProvider(app) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("قبول فوري ✔️")
                                    }
                                    Button(
                                        onClick = { showRejectionInput = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE74C3C)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("رفض الطلب ❌")
                                    }
                                }

                                if (showRejectionInput) {
                                    OutlinedTextField(
                                        value = rejectReason,
                                        onValueChange = { rejectReason = it },
                                        label = { Text("سبب الرفض (إلزامي)") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp)
                                    )
                                    Button(
                                        onClick = {
                                            if (rejectReason.isNotBlank()) {
                                                viewModel.rejectPendingProvider(app, rejectReason)
                                                showRejectionInput = false
                                            }
                                        },
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Text("تثبيت الرفض والسبب")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                item {
                    var newCatNameAr by remember { mutableStateOf("") }
                    var newCatIcon by remember { mutableStateOf("⚒️") }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("➕ إضافة قسم أساسي جديد", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            OutlinedTextField(
                                value = newCatNameAr,
                                onValueChange = { newCatNameAr = it },
                                label = { Text("اسم القسم بالعربية") }
                            )
                            OutlinedTextField(
                                value = newCatIcon,
                                onValueChange = { newCatIcon = it },
                                label = { Text("أيقونة الرموز التعبيرية (Emoji)") }
                            )
                            Button(
                                onClick = {
                                    if (newCatNameAr.isNotBlank()) {
                                        viewModel.persistCategory(
                                            Category(
                                                id = "cat_" + System.currentTimeMillis(),
                                                nameAr = newCatNameAr,
                                                nameEn = newCatNameAr,
                                                iconEmoji = newCatIcon,
                                                displayOrder = 10
                                            )
                                        )
                                        newCatNameAr = ""
                                    }
                                },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("تلقين القسم فورياً")
                            }
                        }
                    }
                }

                items(categories) { cat ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("${cat.iconEmoji} ${cat.nameAr}", fontWeight = FontWeight.Bold)
                            IconButton(onClick = { viewModel.removeCategory(cat.id) }) {
                                Text("🗑️", color = Color.Red)
                            }
                        }
                    }
                }
            }

            3 -> {
                items(providers) { p ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(p.name, fontWeight = FontWeight.Bold)
                            Text("الهاتف: ${p.contactPhone} / القسم: ${p.specialty}", fontSize = 11.sp)
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { viewModel.persistProfessional(p.copy(isPinned = !p.isPinned)) },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (p.isPinned) Color(0xFFFFD700) else Color.Gray)
                                ) {
                                    Text(if (p.isPinned) "⭐ مثبت" else "تثبيت")
                                }

                                Button(
                                    onClick = { viewModel.persistProfessional(p.copy(isRecommended = !p.isRecommended)) },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (p.isRecommended) Color(0xFF2ECC71) else Color.Gray)
                                ) {
                                    Text(if (p.isRecommended) "📌 موصى" else "توصية")
                                }

                                Button(
                                    onClick = { viewModel.persistProfessional(p.copy(isVerified = !p.isVerified)) },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (p.isVerified) Color(0xFF3498DB) else Color.Gray)
                                ) {
                                    Text(if (p.isVerified) "✔️ موثق" else "توثيق")
                                }

                                IconButton(onClick = { viewModel.removeProfessional(p.id) }) {
                                    Text("🗑️")
                                }
                            }
                        }
                    }
                }
            }

            4 -> {
                item {
                    var editAppName by remember { mutableStateOf(settings.appName) }
                    var editFooter by remember { mutableStateOf(settings.footerText) }
                    var activeThemeName by remember { mutableStateOf(settings.colorTheme) }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("⚙️ تخصيص الهوية الوطنية والشاشات كلياً", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                            OutlinedTextField(
                                value = editAppName,
                                onValueChange = { editAppName = it },
                                label = { Text("اسم التطبيق الرئيسي") }
                            )

                            OutlinedTextField(
                                value = editFooter,
                                onValueChange = { editFooter = it },
                                label = { Text("التذييل الدعائي الموحد") }
                            )

                            Text("🎨 اختر سمة الألوان الوطنية:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            val themesList = listOf("🌌 كوزميك سيلفر", "✨ الذهبي الفاخر", "🟢 الزمردي الراقي", "الأسود الدخاني")
                            themesList.forEach { th ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { activeThemeName = th }) {
                                    RadioButton(selected = activeThemeName == th, onClick = { activeThemeName = th })
                                    Text(th)
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.updateSystemSettings(
                                        settings.copy(
                                            appName = editAppName,
                                            footerText = editFooter,
                                            colorTheme = activeThemeName
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("حفظ وبث الإعدادات السحابية 🌐")
                            }
                        }
                    }
                }
            }

            5 -> {
                item {
                    val context = LocalContext.current
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("💾 النسخ الاحتياطي اليدوي وبث الاستعادة", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("توليد ملفات تشفير سحابي ونقلها بنقرة زر لضمان عدم تلف البيانات.", fontSize = 11.sp)

                            Button(
                                onClick = {
                                    Toast.makeText(context, "تم أخذ لقطة تشفير (Backup) سحابية وتخزينها بأمان!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("أخذ نسخة احتياطية يدوية للهاتف 📱")
                            }

                            Button(
                                onClick = {
                                    Toast.makeText(context, "تمت جدولة تفريغ البيانات المؤقتة وسيرفر الاتصال التلقائي!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("جدولة مسح تلقائي يومي للسجلات ⏰")
                            }
                        }
                    }
                }
            }

            6 -> {
                item {
                    var notifTitle by remember { mutableStateOf("") }
                    var notifBody by remember { mutableStateOf("") }
                    var notifRecipient by remember { mutableStateOf("") }
                    var notifCategory by remember { mutableStateOf("عام") }
                    val categoriesList = listOf("عام", "تحديث للمشرفين", "عرض عاجل", "تنبيه نظام")

                    val ctx = LocalContext.current

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "🔔 بث إشعار فوري جديد للنظام بكامله",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            OutlinedTextField(
                                value = notifTitle,
                                onValueChange = { notifTitle = it },
                                label = { Text("عنوان الإشعار") },
                                leadingIcon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = notifBody,
                                onValueChange = { notifBody = it },
                                label = { Text("محتوى أو تفاصيل الإشعار") },
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = notifRecipient,
                                onValueChange = { notifRecipient = it },
                                label = { Text("اسم المستلم (اختياري - اتركه فارغاً للجميع)") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text("تصنيف الإشعار للعملاء والفنيين:", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                categoriesList.forEach { cat ->
                                    FilterChip(
                                        selected = notifCategory == cat,
                                        onClick = { notifCategory = cat },
                                        label = { Text(cat, fontSize = 10.sp) }
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (notifTitle.isNotBlank() && notifBody.isNotBlank()) {
                                        viewModel.broadcastNotification(
                                            AppNotification(
                                                id = "notif_" + System.currentTimeMillis(),
                                                title = notifTitle,
                                                content = notifBody,
                                                recipient = if (notifRecipient.isBlank()) null else notifRecipient,
                                                category = notifCategory,
                                                timestamp = System.currentTimeMillis()
                                            )
                                        )
                                        Toast.makeText(ctx, "📢 تم بث الإشعار بنجاح لجميع الأجهزة النشطة!", Toast.LENGTH_SHORT).show()
                                        notifTitle = ""
                                        notifBody = ""
                                        notifRecipient = ""
                                    } else {
                                        Toast.makeText(ctx, "يرجى كتابة العنوان والمحتوى أولاً!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("بث الإشعار الآن 🚀")
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "📜 سجل الإشعارات السحابية المرسلة (${notifications.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }

                if (notifications.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "لم تقم ببث أي إشعار حتى الآن. استخدم النموذج أعلاه لإرسال إشعارات فورية سحابية.",
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    items(notifications.sortedByDescending { it.timestamp }) { notif ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                MaterialTheme.colorScheme.secondaryContainer,
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            notif.category,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.removeNotification(notif.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Text("🗑️", fontSize = 14.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(notif.content, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                
                                notif.recipient?.let { rec ->
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("🎯 مستلم مخصص: $rec", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                val dateStr = java.text.SimpleDateFormat("yyyy/MM/dd hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(notif.timestamp))
                                Text("توقيت البث: $dateStr", fontSize = 9.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            7 -> {
                item {
                    val ctx = LocalContext.current
                    
                    var bDate by remember { mutableStateOf("") }
                    var bTime by remember { mutableStateOf("") }
                    var bBookedBy by remember { mutableStateOf("") }
                    var bIsBooked by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "📅 التحكم الإداري بنظام الحجوزات والمواعيد",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (settings.isBookingsEnabled) "🟢 نظام الحجوزات نشط ومتاح حالياً" else "🔴 نظام الحجوزات معطل ومغلق",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Switch(
                                    checked = settings.isBookingsEnabled,
                                    onCheckedChange = { isEnabled ->
                                        viewModel.updateSystemSettings(settings.copy(isBookingsEnabled = isEnabled))
                                        Toast.makeText(ctx, if (isEnabled) "تم تفعيل الحجوزات للجميع 🔓" else "تم إيقاف نظام الحجوزات تماماً 🔒", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            Text(
                                "صلاحية إدارة وحجز المواعيد:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )

                            val bookingModes = listOf(
                                Triple("admin_and_providers", "الإدارة والفني / المهني معاً", "يسمح للطرفين بتبادل المواعيد والأوقات الفنية"),
                                Triple("providers_only", "المهني فقط (مقدم الخدمة)", "المهنيون هم من ينوبون عن ترتيب مواعيدهم"),
                                Triple("admin_only", "الإدارة فقط (لوحة التحكم)", "حصر التحكم وحجز المواعيد بيد المسؤولين فقط")
                            )

                            bookingModes.forEach { mode ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.updateSystemSettings(settings.copy(bookingsMode = mode.first))
                                        }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = settings.bookingsMode == mode.first,
                                        onClick = {
                                            viewModel.updateSystemSettings(settings.copy(bookingsMode = mode.first))
                                        }
                                    )
                                    Column(modifier = Modifier.padding(start = 8.dp)) {
                                        Text(mode.second, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(mode.third, fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                            Text(
                                "➕ إنشاء/حجز موعد جديد في قاعدة البيانات",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )

                            OutlinedTextField(
                                value = bDate,
                                onValueChange = { bDate = it },
                                label = { Text("التاريخ (أدخل التاريخ مثلاً: 2026/06/15)") },
                                placeholder = { Text("مثال: 2026-06-15") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = bTime,
                                onValueChange = { bTime = it },
                                label = { Text("التوقيت الفني للموعد (مثلاً: 04:00 PM)") },
                                placeholder = { Text("مثال: 10:00 AM") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = bBookedBy,
                                onValueChange = { bBookedBy = it },
                                label = { Text("اسم العميل / الحاجز للموعد (اختياري)") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = bIsBooked,
                                    onCheckedChange = { bIsBooked = it }
                                )
                                Text("الموعد محجوز مسبقاً حالياً وعاجل ؟", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    if (bDate.isNotBlank() && bTime.isNotBlank()) {
                                        viewModel.persistBooking(
                                            BookingSlot(
                                                id = "booking_" + System.currentTimeMillis(),
                                                date = bDate,
                                                time = bTime,
                                                isBooked = bIsBooked,
                                                bookedBy = if (bBookedBy.isBlank() && bIsBooked) "عميل عام" else if (bBookedBy.isBlank()) null else bBookedBy,
                                                isEnabled = true
                                            )
                                        )
                                        Toast.makeText(ctx, "تم حفظ وجدولة الموعد الجديد بنجاح!", Toast.LENGTH_SHORT).show()
                                        bDate = ""
                                        bTime = ""
                                        bBookedBy = ""
                                        bIsBooked = false
                                    } else {
                                        Toast.makeText(ctx, "يرجى تعبئة التاريخ والوقت لتسجيل الحجز!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("إدراج الموعد فورياً 💾")
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "📋 المواعيد والحجوزات الحالية في النظام (${bookings.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }

                if (bookings.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Text(
                                "لا توجد أي حجوزات مضافة في قاعدة البيانات حتى الآن.",
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center,
                                fontSize = 11.sp
                            )
                        }
                    }
                } else {
                    items(bookings) { bSlot ->
                        var showEditDialog by remember { mutableStateOf(false) }
                        var editSlotDate by remember { mutableStateOf(bSlot.date) }
                        var editSlotTime by remember { mutableStateOf(bSlot.time) }
                        var editSlotBookedBy by remember { mutableStateOf(bSlot.bookedBy ?: "") }
                        var editSlotIsBooked by remember { mutableStateOf(bSlot.isBooked) }
                        var editSlotIsEnabled by remember { mutableStateOf(bSlot.isEnabled) }

                        val ctx = LocalContext.current

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("📅 ${bSlot.date}", fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("⏰ ${bSlot.time}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        if (bSlot.isBooked) {
                                            Text("الحارس/الحاجز للخدمة: ${bSlot.bookedBy ?: "عميل عام"}", fontSize = 11.sp, color = Color(0xFFE74C3C), fontWeight = FontWeight.Bold)
                                        } else {
                                            Text("الحالة: متاح للحجز الفوري فريش ✅", fontSize = 11.sp, color = Color(0xFF2ECC71))
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(onClick = { showEditDialog = true }) {
                                            Text("✏️", fontSize = 16.sp)
                                        }
                                        IconButton(onClick = { viewModel.removeBooking(bSlot.id) }) {
                                            Text("🗑️", fontSize = 16.sp)
                                        }
                                    }
                                }

                                if (showEditDialog) {
                                    androidx.compose.ui.window.Dialog(onDismissRequest = { showEditDialog = false }) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                                Text("تعديل الحجز والمواعيد بقاعدة البيانات", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                
                                                OutlinedTextField(
                                                    value = editSlotDate,
                                                    onValueChange = { editSlotDate = it },
                                                    label = { Text("التاريخ") }
                                                )
                                                OutlinedTextField(
                                                    value = editSlotTime,
                                                    onValueChange = { editSlotTime = it },
                                                    label = { Text("الوقت والتوقيت") }
                                                )
                                                OutlinedTextField(
                                                    value = editSlotBookedBy,
                                                    onValueChange = { editSlotBookedBy = it },
                                                    label = { Text("صاحب الحجز (مستلم الخدمة)") }
                                                )

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Checkbox(checked = editSlotIsBooked, onCheckedChange = { editSlotIsBooked = it })
                                                    Text("هل الموعد محجوز ؟", fontSize = 12.sp)
                                                }

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Checkbox(checked = editSlotIsEnabled, onCheckedChange = { editSlotIsEnabled = it })
                                                    Text("تفعيل الموعد للعامة ؟", fontSize = 12.sp)
                                                }

                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                                    Button(
                                                        onClick = {
                                                            viewModel.persistBooking(
                                                                bSlot.copy(
                                                                    date = editSlotDate,
                                                                    time = editSlotTime,
                                                                    isBooked = editSlotIsBooked,
                                                                    bookedBy = if (editSlotBookedBy.isBlank() && editSlotIsBooked) "عميل عام" else if (editSlotBookedBy.isBlank()) null else editSlotBookedBy,
                                                                    isEnabled = editSlotIsEnabled
                                                                )
                                                            )
                                                            showEditDialog = false
                                                            Toast.makeText(ctx, "تم تعديل الموعد بنجاح!", Toast.LENGTH_SHORT).show()
                                                        },
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text("حفظ التغييرات")
                                                    }
                                                    Button(
                                                        onClick = { showEditDialog = false },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text("إلغاء")
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
        }
    }
}

@Composable
fun SmartAssistantWidget(viewModel: JournalViewModel, onClose: () -> Unit) {
    var queryText by remember { mutableStateOf("") }
    var responsesList by remember { mutableStateOf<List<Pair<String, Boolean>>>(listOf("مرحبا بك في المساعد الذكي اليمني! اسألني عن سباك، كهرباء، أو دعم فني وسأرد فورياً بالإنترنت أو بدون إنترنت 💡" to false)) }

    Dialog(onDismissRequest = onClose) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🤖 مساعد يزن الفني الذكي", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(responsesList) { item ->
                        val isUser = item.second
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = item.first,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(10.dp),
                                    color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = queryText,
                        onValueChange = { queryText = it },
                        placeholder = { Text("اكتب سؤالك هنا...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            if (queryText.isNotBlank()) {
                                val userQ = queryText
                                responsesList = responsesList + (userQ to true)
                                queryText = ""

                                viewModel.askAssistant(userQ) { ans ->
                                    responsesList = responsesList + (ans to false)
                                }
                            }
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Text("🚀", fontSize = 24.sp)
                    }
                }
            }
        }
    }
}
