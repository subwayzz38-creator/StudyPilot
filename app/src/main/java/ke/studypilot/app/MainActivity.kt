package ke.studypilot.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import java.util.Locale
import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.time.*

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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen() {
    val c = LocalContext.current
    val prefs = remember {
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    var done by remember {
        mutableStateOf(prefs.getStringSet("done", emptySet()) ?: emptySet())
    }
    var notify by remember { mutableStateOf(prefs.getBoolean("notify", true)) }
    var dark by remember { mutableStateOf(prefs.getBoolean("dark", false)) }
    var tab by remember { mutableIntStateOf(0) }
    var selectedDay by remember {
        mutableStateOf(LocalDate.now(ZoneId.of(ZONE)).toString())
    }

    val units = remember {
        mutableStateListOf("CSC 212", "CSC 211", "SMA 215", "SMA 214", "SMA 206/211", "SMA 202", "SMA 213")
    }
    var newUnit by remember { mutableStateOf("") }

    MaterialTheme(
        colorScheme = if (dark) darkColorScheme() else lightColorScheme()
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("StudyPilot", fontWeight = FontWeight.Bold)
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    listOf("Home", "Timetable", "My Units", "Performance", "Settings")
                        .forEachIndexed { index, label ->
                            NavigationBarItem(
                                selected = tab == index,
                                onClick = { tab = index },
                                icon = {
                                    Text(
                                        listOf("⌂", "▦", "▤", "％", "⚙")[index]
                                    )
                                },
                                label = { Text(label) }
                            )
                        }
                }
            }
        ) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                when (tab) {
                    0 -> {
                        Text("Welcome back!", style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold)
                        Text("Your academic command centre")
                        Spacer(Modifier.height(16.dp))

                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Study progress", fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = {
                                        if (sessions.isEmpty()) 0f
                                        else done.size.toFloat() / sessions.size
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text("${done.size} of ${sessions.size} sessions completed")
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Text("Your next sessions", style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold)

                        val today = LocalDate.now(ZoneId.of(ZONE)).toString()
                        val upcoming = sessions.filter {
                            it.day >= today && !done.contains(it.id)
                        }.take(4)

                        if (upcoming.isEmpty()) {
                            Text("No upcoming sessions.")
                        } else {
                            upcoming.forEach { s ->
                                SessionRow(s, done.contains(s.id)) {
                                    val x = done.toMutableSet()
                                    if (!x.add(s.id)) x.remove(s.id)
                                    done = x
                                    prefs.edit().putStringSet("done", x).apply()
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { tab = 1 }, Modifier.fillMaxWidth()) {
                            Text("Open weekly timetable")
                        }
                    }

                    1 -> {
                        Text("Weekly timetable",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold)
                        Text("Select a day to see its study plan.")
                        Spacer(Modifier.height(12.dp))

                        val days = (0..6).map {
                            LocalDate.now(ZoneId.of(ZONE))
                                .with(java.time.DayOfWeek.MONDAY)
                                .plusDays(it.toLong())
                        }

                        Row(
                            Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            days.forEach { date ->
                                FilterChip(
                                    selected = selectedDay == date.toString(),
                                    onClick = { selectedDay = date.toString() },
                                    label = {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(date.dayOfWeek.name.take(3))
                                            Text(date.dayOfMonth.toString())
                                        }
                                    }
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Text(
                            LocalDate.parse(selectedDay).dayOfWeek.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        val daily = sessions.filter { it.day == selectedDay }

                        if (daily.isEmpty()) {
                            Text("No study sessions scheduled for this day.")
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(daily) { s ->
                                    SessionRow(s, done.contains(s.id)) {
                                        val x = done.toMutableSet()
                                        if (!x.add(s.id)) x.remove(s.id)
                                        done = x
                                        prefs.edit().putStringSet("done", x).apply()
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        Text("My Units",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold)
                        Text("Manage the units you are studying.")
                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = newUnit,
                            onValueChange = { newUnit = it },
                            label = { Text("Enter unit name or code") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                val value = newUnit.trim()
                                if (value.isNotEmpty() &&
                                    !units.contains(value)) {
                                    units.add(value)
                                    newUnit = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Add unit")
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(units) { unit ->
                                Card(Modifier.fillMaxWidth()) {
                                    Row(
                                        Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(unit, Modifier.weight(1f),
                                            fontWeight = FontWeight.Bold)
                                        TextButton(onClick = { units.remove(unit) }) {
                                            Text("Remove")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        PerformanceScreen()
                    }

                    4 -> {
                        Text("Settings",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))

                        Card(Modifier.fillMaxWidth()) {
                            Row(
                                Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text("Dark mode", fontWeight = FontWeight.Bold)
                                    Text("Use a darker appearance")
                                }
                                Switch(
                                    checked = dark,
                                    onCheckedChange = {
                                        dark = it
                                        prefs.edit().putBoolean("dark", it).apply()
                                    }
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Card(Modifier.fillMaxWidth()) {
                            Row(
                                Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text("Notifications", fontWeight = FontWeight.Bold)
                                    Text("Study, break and exam reminders")
                                }
                                Switch(
                                    checked = notify,
                                    onCheckedChange = {
                                        notify = it
                                        prefs.edit().putBoolean("notify", it).apply()
                                        if (it) Scheduler.schedule(c)
                                        else Scheduler.cancel(c)
                                    }
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Text("StudyPilot • Version 2.0")
                    }
                }
            }
        }
    }
}

@Composable
fun PerformanceScreen() {
    var cat1 by remember { mutableStateOf("9") }
    var max1 by remember { mutableStateOf("15") }
    var cat2 by remember { mutableStateOf("15") }
    var max2 by remember { mutableStateOf("20") }
    var target by remember { mutableStateOf("70") }

    val a = cat1.toDoubleOrNull()
    val b = max1.toDoubleOrNull()
    val d = cat2.toDoubleOrNull()
    val e = max2.toDoubleOrNull()
    val goal = target.toDoubleOrNull()

    val valid = a != null && b != null && d != null && e != null &&
        b > 0 && e > 0 && a >= 0 && d >= 0 &&
        a <= b && d <= e && goal != null && goal in 0.0..100.0

    val coursework = if (valid)
        ((a!! + d!!) / (b!! + e!!) * 30.0)
    else 0.0

    val needed = if (valid) goal!! - coursework else 0.0

    val grade = if (!valid) "-"
        else when {
            coursework >= 0 && goal!! >= 70 -> "A target"
            goal!! >= 60 -> "B target"
            goal!! >= 50 -> "C target"
            goal!! >= 40 -> "D target"
            else -> "Below D target"
        }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("CAT & Exam Calculator",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
            Text("Coursework contributes 30 marks. The final exam contributes 70.")
        }

        item {
            OutlinedTextField(
                value = cat1, onValueChange = { cat1 = it },
                label = { Text("CAT 1 score") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = max1, onValueChange = { max1 = it },
                label = { Text("CAT 1 maximum marks") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = cat2, onValueChange = { cat2 = it },
                label = { Text("CAT 2 score") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = max2, onValueChange = { max2 = it },
                label = { Text("CAT 2 maximum marks") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = target, onValueChange = { target = it },
                label = { Text("Desired final mark (e.g. 70)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            if (valid) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Your results", style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold)
                        Text("Coursework: ${String.format(Locale.US, "%.2f", coursework)} / 30")
                        Text("Exam maximum: 70")
                        HorizontalDivider()
                        Text("Exam marks needed: ${String.format(Locale.US, "%.2f", needed)} / 70",
                            fontWeight = FontWeight.Bold)
                        Text(grade)
                        when {
                            needed <= 0 -> Text("You have already reached this target through coursework.")
                            needed > 70 -> Text("This target is no longer reachable.")
                            else -> Text("You need at least ${kotlin.math.ceil(needed).toInt()} exam marks to reach your target.")
                        }
                    }
                }
            } else {
                Text("Enter valid CAT scores and maximum marks. Scores cannot exceed their maximum.")
            }
        }

        item {
            Text("Default grade targets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)
            Text("A: 70–100")
            Text("B: 60–69")
            Text("C: 50–59")
            Text("D: 40–49")
            Text("Below 40: F")
            Text("Grade boundaries can be adjusted if the official university regulations differ.",
                style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun SessionRow(s: Session, checked: Boolean, onCheck: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("${s.day} • ${s.start}–${s.end}",
                    style = MaterialTheme.typography.labelMedium)
                Text(
                    s.subject + if (s.exam) " — EXAM" else "",
                    fontWeight = FontWeight.Bold
                )
                Text(s.topic, style = MaterialTheme.typography.bodyMedium)
            }
            Checkbox(checked = checked, onCheckedChange = { onCheck() })
        }
    }
}

