package ke.studypilot.app

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.time.*
import java.util.Locale

private const val CHANNEL = "study_reminders"
private const val PREFS = "study_pilot"
private const val ZONE = "Africa/Nairobi"
data class Session(val day:String,val start:String,val end:String,val subject:String,val topic:String,val exam:Boolean=false) {
    val id get() = "$day|$start|$subject|$topic"
}
private val sessions = listOf(
 Session("2026-11-25","06:30","08:00","SMA 215","First topics"),Session("2026-11-25","08:20","09:50","SMA 215","Next topics"),Session("2026-11-25","10:30","12:00","SMA 214","First topics"),Session("2026-11-25","14:00","15:30","CSC 212","First topics"),Session("2026-11-25","16:00","17:00","SMA 215","Practice questions"),
 Session("2026-11-26","06:30","08:00","SMA 215","Remaining topics"),Session("2026-11-26","08:20","09:50","SMA 214","Next topics"),Session("2026-11-26","10:30","12:00","CSC 212","Next topics"),Session("2026-11-26","14:00","15:30","CSC 211","First topics"),Session("2026-11-26","16:00","17:00","SMA 215","Practice questions"),
 Session("2026-11-27","06:30","08:00","SMA 215","Difficult topics"),Session("2026-11-27","08:20","09:50","SMA 214","Remaining topics"),Session("2026-11-27","10:30","12:00","CSC 212","Next topics"),Session("2026-11-27","14:00","15:30","SMA 202","First topics"),Session("2026-11-27","16:00","17:00","SMA 214","Practice questions"),
 Session("2026-11-28","06:30","08:00","SMA 215","Full revision"),Session("2026-11-28","08:20","09:50","SMA 214","Difficult topics"),Session("2026-11-28","10:30","12:00","CSC 212","Remaining topics"),Session("2026-11-28","14:00","15:30","SMA 213","First topics"),Session("2026-11-28","16:00","17:00","SMA 215","Past paper"),
 Session("2026-11-29","18:00","21:00","Revision","SMA 215, SMA 214 and CSC 212 recall"),
 Session("2026-11-30","06:30","08:00","SMA 215","Final revision"),Session("2026-11-30","08:20","09:50","SMA 215","Past paper"),Session("2026-11-30","10:30","12:00","SMA 214","Full revision"),Session("2026-11-30","14:00","15:30","CSC 212","Full revision"),Session("2026-11-30","16:00","17:00","Revision","Review weak areas"),
 Session("2026-12-01","08:30","10:30","SMA 215","EXAM",true),Session("2026-12-01","12:30","14:00","SMA 214","Weak areas"),Session("2026-12-01","14:20","15:50","SMA 214","Practice questions"),
 Session("2026-12-02","07:30","09:00","SMA 214","Recall"),Session("2026-12-02","09:20","10:30","SMA 214","Difficult questions"),Session("2026-12-02","14:30","16:30","SMA 214","EXAM",true),
 Session("2026-12-03","08:30","10:30","CSC 212","EXAM",true),Session("2026-12-03","13:30","15:00","SMA 206/211","Topics"),Session("2026-12-03","15:20","16:50","SMA 206/211","More topics"),
 Session("2026-12-04","07:30","09:00","SMA 206/211","Final recall"),Session("2026-12-04","11:30","13:30","SMA 206/211","EXAM",true),Session("2026-12-04","15:00","16:30","CSC 211","Topics"),
 Session("2026-12-05","06:30","08:00","CSC 211","Topics"),Session("2026-12-05","08:20","09:50","CSC 211","Topics"),Session("2026-12-05","10:30","12:00","SMA 202","Topics"),Session("2026-12-05","14:00","15:30","SMA 213","Topics"),Session("2026-12-05","16:00","17:00","CSC 211","Practice questions"),
 Session("2026-12-06","18:00","21:00","Revision","CSC 211, SMA 202 and SMA 213 recall"),
 Session("2026-12-07","06:30","08:00","CSC 211","Full revision"),Session("2026-12-07","08:20","09:50","CSC 211","Past paper"),Session("2026-12-07","10:30","12:00","SMA 202","Remaining topics"),Session("2026-12-07","14:00","15:30","SMA 213","Remaining topics"),Session("2026-12-07","16:00","17:00","CSC 211","Weak areas"),
 Session("2026-12-08","07:30","09:00","CSC 211","Final recall"),Session("2026-12-08","09:20","10:20","CSC 211","Practice questions"),Session("2026-12-08","11:30","13:30","CSC 211","EXAM",true),Session("2026-12-08","15:00","16:30","SMA 202","Revision"),
 Session("2026-12-09","07:30","09:00","SMA 202","Recall"),Session("2026-12-09","09:20","10:20","SMA 202","Practice questions"),Session("2026-12-09","11:30","13:30","SMA 202","EXAM",true),Session("2026-12-09","15:00","16:30","SMA 213","Revision"),Session("2026-12-09","17:00","17:45","SMA 213","Practice questions"),
 Session("2026-12-10","08:30","10:30","SMA 213","EXAM",true)
)

class MainActivity:ComponentActivity(){
 private val permission=registerForActivityResult(ActivityResultContracts.RequestPermission()){ if(it) Scheduler.schedule(this) }
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(CHANNEL,"Study and break reminders",NotificationManager.IMPORTANCE_HIGH));if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)permission.launch(Manifest.permission.POST_NOTIFICATIONS) else Scheduler.schedule(this);setContent{Screen()} }
}
object Scheduler {
 fun cancel(c:Context){ val am=c.getSystemService(Context.ALARM_SERVICE) as AlarmManager; for(id in 1000..1300){ val p=PendingIntent.getBroadcast(c,id,Intent(c,ReminderReceiver::class.java),PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE); if(p!=null) am.cancel(p) } }
 fun schedule(c:Context){ cancel(c); val am=c.getSystemService(Context.ALARM_SERVICE) as AlarmManager; var n=1000; val now=System.currentTimeMillis(); val zone=ZoneId.of(ZONE)
  fun add(date:String,time:String,title:String,body:String){val ms=LocalDateTime.of(LocalDate.parse(date),LocalTime.parse(time)).atZone(zone).toInstant().toEpochMilli();if(ms<=now)return;val i=Intent(c,ReminderReceiver::class.java).putExtra("title",title).putExtra("body",body);val p=PendingIntent.getBroadcast(c,n++,i,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,ms,p)}
  sessions.forEach { s->add(s.day,s.start,if(s.exam) "EXAM: ${s.subject}" else "Study time: ${s.subject}",s.topic);if(!s.exam)add(s.day,s.end,"Session finished","Take a break and check your next session.") }
  val dates=(25..30).filter{it!=29}.map{"2026-11-%02d".format(it)}+(5..7).filter{it!=6}.map{"2026-12-%02d".format(it)}
  dates.forEach{d->listOf("08:00" to "Break time","08:20" to "Resume studying","09:50" to "Break time","10:30" to "Resume studying","12:00" to "Lunch and rest","14:00" to "Afternoon study","15:30" to "Break time","16:00" to "Practice session").forEach{add(d,it.first,it.second,"StudyPilot timetable reminder")}}
 }
}
class ReminderReceiver:BroadcastReceiver(){override fun onReceive(c:Context,i:Intent){val n=NotificationCompat.Builder(c,CHANNEL).setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle(i.getStringExtra("title")?:"StudyPilot").setContentText(i.getStringExtra("body")?:"Check your timetable").setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true).build();try{NotificationManagerCompat.from(c).notify((System.currentTimeMillis()%100000).toInt(),n)}catch(_:SecurityException){}}}
class BootReceiver:BroadcastReceiver(){override fun onReceive(c:Context,i:Intent){Scheduler.schedule(c)}}

private val Ink = Color(0xFF070A18)
private val Panel = Color(0xFF11182D)
private val Panel2 = Color(0xFF171D38)
private val Violet = Color(0xFF8757FF)
private val Blue = Color(0xFF3478FF)
private val Cyan = Color(0xFF42D9F5)
private val Mint = Color(0xFF35D6B0)
private val Muted = Color(0xFF9AA7C5)

@Composable
fun Screen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    var tab by remember { mutableIntStateOf(0) }
    var welcome by remember { mutableStateOf(!prefs.getBoolean("welcome_done", false)) }
    var showSettings by remember { mutableStateOf(false) }
    var notify by remember { mutableStateOf(prefs.getBoolean("notify", true)) }
    var done by remember { mutableStateOf(prefs.getStringSet("done", emptySet()) ?: emptySet()) }
    val units = remember {
        mutableStateListOf<String>().apply {
            addAll(prefs.getStringSet("units", null)?.toList()?.sorted()
                ?: listOf("CSC 212", "CSC 211", "SMA 215", "SMA 214", "SMA 206/211", "SMA 202", "SMA 213"))
        }
    }
    var selectedDay by remember { mutableStateOf(LocalDate.now(ZoneId.of(ZONE)).toString()) }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Violet, secondary = Cyan, tertiary = Mint,
            background = Ink, surface = Panel, onBackground = Color.White,
            onSurface = Color.White
        )
    ) {
        if (welcome) {
            WelcomeScreen {
                welcome = false
                prefs.edit().putBoolean("welcome_done", true).apply()
            }
        } else {
            Scaffold(
                containerColor = Ink,
                topBar = {
                    Row(
                        Modifier.fillMaxWidth().background(Ink).padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("STUDYPILOT", color = Color.White, fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.titleLarge, letterSpacing = 2.sp)
                            Text("YOUR ACADEMIC COMMAND CENTRE", color = Muted,
                                style = MaterialTheme.typography.labelSmall, letterSpacing = 1.sp)
                        }
                        NeonAction("⚙", onClick = { showSettings = !showSettings })
                    }
                },
                bottomBar = {
                    NavigationBar(containerColor = Color(0xFF0D1224), contentColor = Color.White) {
                        val labels = listOf("Home", "Plan", "Units", "Grades", "AI")
                        val glyphs = listOf("⌂", "▦", "▤", "◈", "✧")
                        labels.forEachIndexed { index, label ->
                            NavigationBarItem(
                                selected = tab == index,
                                onClick = { tab = index; showSettings = false },
                                icon = { Text(glyphs[index], color = if (tab == index) Cyan else Muted) },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Cyan, selectedTextColor = Cyan,
                                    indicatorColor = Color(0xFF25234A),
                                    unselectedIconColor = Muted, unselectedTextColor = Muted
                                )
                            )
                        }
                    }
                }
            ) { padding ->
                Column(Modifier.fillMaxSize().padding(padding)) {
                    if (showSettings) {
                        SettingsScreen(notify, { enabled ->
                            notify = enabled
                            prefs.edit().putBoolean("notify", enabled).apply()
                            if (enabled) Scheduler.schedule(context) else Scheduler.cancel(context)
                        })
                    } else {
                        when (tab) {
                            0 -> HomeScreen(done, sessions, onQuickTab = { tab = it }) { s ->
                                val updated = done.toMutableSet()
                                if (!updated.add(s.id)) updated.remove(s.id)
                                done = updated
                                prefs.edit().putStringSet("done", updated).apply()
                            }
                            1 -> TimetableScreen(selectedDay, { selectedDay = it }, done) { s ->
                                val updated = done.toMutableSet()
                                if (!updated.add(s.id)) updated.remove(s.id)
                                done = updated
                                prefs.edit().putStringSet("done", updated).apply()
                            }
                            2 -> UnitsScreen(units) {
                                prefs.edit().putStringSet("units", units.toSet()).apply()
                            }
                            3 -> PerformanceScreen(prefs)
                            else -> AssistantScreen()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeScreen(onContinue: () -> Unit) {
    Box(Modifier.fillMaxSize().background(
        Brush.linearGradient(listOf(Color(0xFF090B1B), Color(0xFF17113B), Color(0xFF071D35)))
    ).padding(26.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(112.dp).background(
                    Brush.linearGradient(listOf(Violet, Blue, Cyan)), RoundedCornerShape(32.dp)
                ),
                contentAlignment = Alignment.Center
            ) {
                Text("✦", color = Color.White, fontSize = 58.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(28.dp))
            Text("StudyPilot", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
            Text("YOUR STUDY LIFE. UNDER CONTROL.", color = Cyan, letterSpacing = 2.sp,
                style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(18.dp))
            Text("Plan smarter. Learn deeper. Achieve more.",
                color = Color(0xFFB9C5E0), textAlign = TextAlign.Center)
            Spacer(Modifier.height(42.dp))
            GradientButton("GET STARTED", onContinue)
        }
    }
}

@Composable
private fun HomeScreen(done: Set<String>, allSessions: List<Session>, onQuickTab: (Int) -> Unit, onToggle: (Session) -> Unit) {
    val today = LocalDate.now(ZoneId.of(ZONE)).toString()
    val upcoming = allSessions.filter { it.day >= today && !done.contains(it.id) }.take(3)
    val progress = if (allSessions.isEmpty()) 0f else done.size.toFloat() / allSessions.size
    val nextExam = allSessions.firstOrNull { it.exam && it.day >= today }
    val daysToExam = nextExam?.let {
        runCatching { java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(ZoneId.of(ZONE)), LocalDate.parse(it.day)).toInt() }.getOrDefault(0)
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp)) {
        Text("Good to see you.", color = Muted, style = MaterialTheme.typography.bodyMedium)
        Text("Ready to make progress?", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NeonPanel(Modifier.weight(1.25f)) {
                Text("YOUR PROGRESS", color = Muted, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.sp)
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(progress = { progress }, modifier = Modifier.size(76.dp),
                            color = Cyan, trackColor = Color(0xFF293451), strokeWidth = 7.dp)
                        Text("${(progress * 100).toInt()}%", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("${done.size}/${allSessions.size}", color = Color.White,
                            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Sessions done", color = Muted, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            NeonPanel(Modifier.weight(0.85f), accent = Violet) {
                Text("EXAM COUNTDOWN", color = Muted, style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(10.dp))
                Text(daysToExam?.coerceAtLeast(0)?.toString() ?: "—", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
                Text("DAYS TO GO", color = Cyan, style = MaterialTheme.typography.labelSmall)
                Text(nextExam?.let { "${it.subject} • ${it.day}" } ?: "No upcoming exam", color = Muted, style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(Modifier.height(18.dp))
        SectionHeading("UP NEXT", "Your upcoming sessions")
        if (upcoming.isEmpty()) {
            NeonPanel { Text("No upcoming sessions. You're all caught up.", color = Muted) }
        } else upcoming.forEach { s -> SessionTile(s, done.contains(s.id)) { onToggle(s) } }
        Spacer(Modifier.height(14.dp))
        SectionHeading("QUICK ACCESS", "Keep your momentum")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickTile("▦", "Timetable", Modifier.weight(1f), Violet) { onQuickTab(1) }
            QuickTile("◈", "My grades", Modifier.weight(1f), Cyan) { onQuickTab(3) }
            QuickTile("✧", "AI tutor", Modifier.weight(1f), Mint) { onQuickTab(4) }
        }
    }
}

@Composable
private fun TimetableScreen(selectedDay: String, onDay: (String) -> Unit, done: Set<String>, onToggle: (Session) -> Unit) {
    var weekOffset by remember { mutableIntStateOf(0) }
    val monday = LocalDate.now(ZoneId.of(ZONE)).with(java.time.DayOfWeek.MONDAY).plusWeeks(weekOffset.toLong())
    val days = (0..6).map { monday.plusDays(it.toLong()) }
    val date = runCatching { LocalDate.parse(selectedDay) }.getOrDefault(monday)
    val daily = sessions.filter { it.day == date.toString() }
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        SectionHeading("YOUR PLAN", "Weekly timetable")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(monday.format(java.time.format.DateTimeFormatter.ofPattern("d MMM")) +
                " – " + monday.plusDays(6).format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy")),
                color = Cyan, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
            TextButton(onClick = {
                weekOffset--
                onDay(monday.minusWeeks(1).toString())
            }) { Text("‹", color = Cyan, fontSize = 22.sp) }
            TextButton(onClick = {
                weekOffset++
                onDay(monday.plusWeeks(1).toString())
            }) { Text("›", color = Cyan, fontSize = 22.sp) }
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            days.forEach { d ->
                val active = d == date
                Column(
                    Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                        .background(if (active) Brush.linearGradient(listOf(Violet, Blue)) else Brush.linearGradient(listOf(Panel, Panel)))
                        .clickable { onDay(d.toString()) }.padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(d.dayOfWeek.name.take(1), color = if (active) Color.White else Muted,
                        style = MaterialTheme.typography.labelSmall)
                    Text(d.dayOfMonth.toString(), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Text(date.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM")),
            color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        if (daily.isEmpty()) {
            NeonPanel { Text("No sessions scheduled for this day.", color = Muted) }
        } else {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                daily.forEach { s -> SessionTile(s, done.contains(s.id)) { onToggle(s) } }
            }
        }
    }
}

@Composable
private fun UnitsScreen(units: SnapshotStateList<String>, onSave: () -> Unit) {
    var entry by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        SectionHeading("YOUR COURSES", "My Units")
        Text("Manage your units and keep learning materials organised.", color = Muted)
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = entry, onValueChange = { entry = it },
                modifier = Modifier.weight(1f), singleLine = true,
                label = { Text("Unit code or name") },
                colors = neonFieldColors())
            Spacer(Modifier.width(8.dp))
            GradientButton("+", {
                val value = entry.trim()
                if (value.isNotEmpty() && value !in units) {
                    units.add(value)
                    entry = ""
                    onSave()
                }
            }, compact = true)
        }
        Spacer(Modifier.height(14.dp))
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(9.dp)) {
            units.forEachIndexed { index, unit ->
                NeonPanel(accent = if (index % 2 == 0) Violet else Blue) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(42.dp).background(Color(0xFF28234F), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center) {
                            Text("▤", color = Cyan, fontSize = 20.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(unit, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Topics and study materials", color = Muted, style = MaterialTheme.typography.labelSmall)
                        }
                        Text("›", color = Cyan, fontSize = 26.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PerformanceScreen(prefs: android.content.SharedPreferences) {
    var unit by remember { mutableStateOf(prefs.getString("cat_unit", "CSC 212") ?: "CSC 212") }
    val initialUnitKey = unit.trim().ifBlank { "default" }.replace(Regex("[^A-Za-z0-9_-]"), "_")
    var cat1 by remember { mutableStateOf(prefs.getString("cat_${initialUnitKey}_cat1", "9") ?: "9") }
    var max1 by remember { mutableStateOf(prefs.getString("cat_${initialUnitKey}_max1", "15") ?: "15") }
    var cat2 by remember { mutableStateOf(prefs.getString("cat_${initialUnitKey}_cat2", "15") ?: "15") }
    var max2 by remember { mutableStateOf(prefs.getString("cat_${initialUnitKey}_max2", "20") ?: "20") }
    var target by remember { mutableStateOf(prefs.getString("cat_${initialUnitKey}_target", "70") ?: "70") }

    val c1 = cat1.toDoubleOrNull()
    val m1 = max1.toDoubleOrNull()
    val c2 = cat2.toDoubleOrNull()
    val m2 = max2.toDoubleOrNull()
    val goal = target.toDoubleOrNull()
    val valid = c1 != null && m1 != null && c2 != null && m2 != null && goal != null &&
        m1 > 0 && m2 > 0 && c1 >= 0 && c2 >= 0 && c1 <= m1 && c2 <= m2 &&
        goal in 0.0..100.0
    val coursework = if (valid) ((c1!! + c2!!) / (m1!! + m2!!) * 30.0) else 0.0
    val examNeeded = if (valid) goal!! - coursework else 0.0
    val minExam = kotlin.math.ceil(examNeeded).toInt()

    fun save(key: String, value: String) {
        val safeUnit = unit.trim().ifBlank { "default" }.replace(Regex("[^A-Za-z0-9_-]"), "_")
        prefs.edit().putString("cat_${safeUnit}_$key", value).apply()
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp)) {
        SectionHeading("ACADEMIC TRACKER", "Performance")
        Text("Know where you stand. Plan the mark you need.", color = Muted)
        Spacer(Modifier.height(14.dp))
        NeonPanel {
            Text("COURSEWORK CALCULATOR", color = Cyan, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.sp)
            Text("CAT 1 + CAT 2  →  /30", color = Color.White, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(unit, {
                unit = it
                prefs.edit().putString("cat_unit", it).apply()
                val safeUnit = it.trim().ifBlank { "default" }.replace(Regex("[^A-Za-z0-9_-]"), "_")
                cat1 = prefs.getString("cat_${safeUnit}_cat1", "") ?: ""
                max1 = prefs.getString("cat_${safeUnit}_max1", "") ?: ""
                cat2 = prefs.getString("cat_${safeUnit}_cat2", "") ?: ""
                max2 = prefs.getString("cat_${safeUnit}_max2", "") ?: ""
                target = prefs.getString("cat_${safeUnit}_target", "70") ?: "70"
            }, label = { Text("Unit") },
                modifier = Modifier.fillMaxWidth(), singleLine = true, colors = neonFieldColors())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ScoreField("CAT 1 score", cat1, { cat1 = it; save("cat1", it) }, Modifier.weight(1f))
                ScoreField("Out of", max1, { max1 = it; save("max1", it) }, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ScoreField("CAT 2 score", cat2, { cat2 = it; save("cat2", it) }, Modifier.weight(1f))
                ScoreField("Out of", max2, { max2 = it; save("max2", it) }, Modifier.weight(1f))
            }
            ScoreField("Target total /100", target, { target = it; save("target", it) }, Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(12.dp))
        if (valid) {
            NeonPanel(accent = Cyan) {
                Text("YOUR CALCULATED RESULT", color = Muted, style = MaterialTheme.typography.labelSmall)
                Text(String.format(Locale.US, "%.2f", coursework), color = Cyan,
                    fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
                Text("COURSEWORK CONTRIBUTION OUT OF 30", color = Muted, style = MaterialTheme.typography.labelSmall)
                HorizontalDivider(color = Color(0xFF35405E), modifier = Modifier.padding(vertical = 12.dp))
                Text("Final exam target", color = Color.White, fontWeight = FontWeight.Bold)
                when {
                    examNeeded <= 0 -> Text("Your coursework already meets this target.", color = Mint)
                    examNeeded > 70 -> Text("Not reachable: you would need ${String.format(Locale.US, "%.2f", examNeeded)} / 70.", color = Color(0xFFFFA36C))
                    else -> {
                        Text("$minExam / 70 marks minimum", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                        Text("Exact requirement: ${String.format(Locale.US, "%.2f", examNeeded)} marks.", color = Muted)
                    }
                }
            }
        } else {
            NeonPanel(accent = Color(0xFFFFA36C)) {
                Text("Check your entries", color = Color(0xFFFFB27D), fontWeight = FontWeight.Bold)
                Text("Enter numeric scores. CAT maximums must be above zero, scores must be between zero and their maximum, and target must be 0–100.", color = Muted)
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("Formula: (CAT 1 + CAT 2) ÷ (CAT 1 maximum + CAT 2 maximum) × 30",
            color = Muted, style = MaterialTheme.typography.bodySmall)
        Text("Grade boundaries shown in this prototype are editable defaults, not verified official University of Embu regulations.",
            color = Muted, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun AssistantScreen() {
    var question by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        SectionHeading("YOUR PERSONAL TUTOR", "AI Study Assistant")
        Text("Ask questions, understand difficult topics and practise for exams.", color = Muted)
        Spacer(Modifier.height(18.dp))
        NeonPanel {
            Text("✧  STUDYPILOT AI", color = Cyan, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Hi! I can help explain concepts, create revision questions and summarise your notes once the AI service is connected.",
                color = Color.White)
        }
        Spacer(Modifier.height(12.dp))
        if (submitted) {
            NeonPanel(accent = Violet) {
                Text("Your prompt", color = Cyan, fontWeight = FontWeight.Bold)
                Text(question, color = Color.White)
                Text("AI connection is not configured yet. Your message is not being sent to an AI service.",
                    color = Muted, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.weight(1f))
        OutlinedTextField(value = question, onValueChange = { question = it },
            modifier = Modifier.fillMaxWidth(), minLines = 2,
            placeholder = { Text("Ask your study question…") }, colors = neonFieldColors())
        Spacer(Modifier.height(8.dp))
        GradientButton("SEND QUESTION", {
            if (question.isNotBlank()) submitted = true
        })
        Text("AI replies, file summaries and quiz generation require the AI service integration.",
            color = Muted, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun SettingsScreen(notify: Boolean, onNotify: (Boolean) -> Unit) {
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        SectionHeading("PREFERENCES", "Settings")
        NeonPanel {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Study reminders", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Study, break and exam notifications", color = Muted, style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = notify, onCheckedChange = onNotify)
            }
        }
        Spacer(Modifier.height(10.dp))
        NeonPanel {
            Text("STUDYPILOT", color = Cyan, fontWeight = FontWeight.Bold)
            Text("Dark Neon Edition • Android", color = Color.White)
            Text("Offline unit and CAT data are saved on this device. AI and cloud backup are not connected yet.",
                color = Muted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SessionTile(s: Session, checked: Boolean, onToggle: () -> Unit) {
    NeonPanel(accent = if (s.exam) Color(0xFFFFA36C) else Blue) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("${s.start} – ${s.end}  •  ${s.day}", color = Cyan, style = MaterialTheme.typography.labelSmall)
                Text(s.subject + if (s.exam) "  EXAM" else "", color = Color.White, fontWeight = FontWeight.Bold)
                Text(s.topic, color = Muted, style = MaterialTheme.typography.bodySmall)
            }
            Checkbox(checked = checked, onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = Mint, uncheckedColor = Muted))
        }
    }
}

@Composable
private fun NeonPanel(
    modifier: Modifier = Modifier,
    accent: Color = Violet,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
            .background(Brush.linearGradient(listOf(Panel2, Panel, Color(0xFF10172B))))
            .border(1.dp, Brush.linearGradient(listOf(accent.copy(alpha = .65f), Blue.copy(alpha = .25f))),
                RoundedCornerShape(22.dp))
            .padding(16.dp),
        content = content
    )
}

@Composable
private fun SectionHeading(kicker: String, title: String) {
    Text(kicker, color = Cyan, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.8.sp)
    Text(title, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun QuickTile(symbol: String, title: String, modifier: Modifier, tint: Color, onClick: () -> Unit) {
    NeonPanel(modifier.clickable(onClick = onClick), tint) {
        Text(symbol, color = tint, fontSize = 24.sp)
        Spacer(Modifier.height(6.dp))
        Text(title, color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun NeonAction(symbol: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) { Text(symbol, color = Cyan, fontSize = 23.sp) }
}

@Composable
private fun GradientButton(text: String, onClick: () -> Unit, compact: Boolean = false) {
    Box(
        Modifier.fillMaxWidth(if (compact) 0.16f else 1f)
            .clip(RoundedCornerShape(15.dp))
            .background(Brush.horizontalGradient(listOf(Violet, Blue, Color(0xFF247DCE))))
            .clickable(onClick = onClick).padding(horizontal = 18.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) { Text(text, color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 1.sp) }
}

@Composable
private fun ScoreField(label: String, value: String, onValue: (String) -> Unit, modifier: Modifier) {
    OutlinedTextField(value, onValue, modifier = modifier, label = { Text(label) },
        singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        colors = neonFieldColors())
}

@Composable
private fun neonFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
    focusedBorderColor = Cyan, unfocusedBorderColor = Color(0xFF46516E),
    focusedLabelColor = Cyan, unfocusedLabelColor = Muted,
    cursorColor = Cyan, focusedContainerColor = Color(0xFF0B1022),
    unfocusedContainerColor = Color(0xFF0B1022)
)
