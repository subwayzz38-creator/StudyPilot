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
@Composable fun Screen(){val c= LocalContext.current;val prefs=remember{c.getSharedPreferences(PREFS,Context.MODE_PRIVATE)};var done by remember{mutableStateOf(prefs.getStringSet("done",emptySet())?:emptySet())};var notify by remember{mutableStateOf(prefs.getBoolean("notify",true))};val today=LocalDate.now(ZoneId.of(ZONE)).toString();val todayList=sessions.filter{it.day==today};Scaffold(topBar={TopAppBar(title={Text("StudyPilot",fontWeight=FontWeight.Bold)})}){pad->LazyColumn(Modifier.fillMaxSize().padding(pad).padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
 item{Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text("Your exam preparation",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);Text("25 Nov – 10 Dec 2026 • Kenya time");LinearProgressIndicator(progress={done.size.toFloat()/sessions.size},modifier=Modifier.fillMaxWidth());Text("${done.size} of ${sessions.size} sessions completed")}}}
 item{Card(Modifier.fillMaxWidth()){Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("Notifications",fontWeight=FontWeight.Bold);Text("Study starts, breaks, session ends and exams",style=MaterialTheme.typography.bodySmall)};Switch(checked=notify,onCheckedChange = { v -> notify=v;prefs.edit().putBoolean("notify",v).apply();if(v)Scheduler.schedule(c) else Scheduler.cancel(c) })}}}
 item{Text("TODAY",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)}
 if(todayList.isEmpty())item{Text("No scheduled sessions today. See the full timetable below.")}
 items(todayList){s->SessionRow(s,done.contains(s.id)){val x=done.toMutableSet();if(x.contains(s.id))x.remove(s.id)else x.add(s.id);done=x;prefs.edit().putStringSet("done",x).apply()}}
 item{Text("FULL TIMETABLE",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)}
 items(sessions){s->SessionRow(s,done.contains(s.id)){val x=done.toMutableSet();if(x.contains(s.id))x.remove(s.id)else x.add(s.id);done=x;prefs.edit().putStringSet("done",x).apply()}}
 item{Text("Allow notifications when asked. Battery-saving settings may delay reminders. Sunday study is scheduled for evening only.",style=MaterialTheme.typography.bodySmall);Spacer(Modifier.height(24.dp))}
}}}
@Composable fun SessionRow(s:Session,checked:Boolean,onCheck:()->Unit){Card(Modifier.fillMaxWidth()){Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("${s.day} • ${s.start}–${s.end}",style=MaterialTheme.typography.labelMedium);Text(s.subject+(if(s.exam)" — EXAM" else ""),fontWeight=FontWeight.Bold);Text(s.topic,style=MaterialTheme.typography.bodyMedium)};Checkbox(checked=checked,onCheckedChange = { onCheck() })}}}
