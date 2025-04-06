
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.sarcopeniamonitor.PhysicalTestList
import com.example.sarcopeniamonitor.Prediction
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlin.random.Random

@Composable
fun StatusCard(title: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun Home(
    prediction: MutableState<Prediction>,
    physicalTestList: MutableState<PhysicalTestList>,
) {
    val gripData: List<Double?> = physicalTestList.value.getList().map { it.gripStrengthData }
    val gaitData: List<Double?> = physicalTestList.value.getList().map { it.gaitSpeedData }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = "Sarcopenia Risk Evaluation",
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                .background(color = MaterialTheme.colorScheme.background),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )

        LazyColumn {
            item {
                StatusCard("Sarcopenia predicted status:",
                    prediction.value.sarcopeniaStatus?.toString() ?: "Awaiting data..."
                )
                StatusCard("ASMI Prediction T-score: (DXA)",
                    prediction.value.asmiPredictionTScore?.toString() ?: "Awaiting data..."
                )
                StatusCard("ASMI Prediction Value: (DXA)",
                    prediction.value.asmiPrediction?.toString() ?: "Awaiting data..."
                )

                // Line charts in Cards
                ChartCard("Hand Grip Strength Over Time", gripData, List(10) { Random.nextDouble() * 50 })
                Spacer(modifier = Modifier.height(16.dp))
                ChartCard("Gait Speed Over Time", gaitData, List(10) { Random.nextDouble() * 2 })
            }
        }
    }
}

@Composable
fun ChartCard(title: String, dataPoints: List<Double?>, pseudoData: List<Double>) {
    val isPseudo = dataPoints.isEmpty()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            if (isPseudo) {
                Text("No Data Available! Present pseudo data for visualization",
                    color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Log.d("Pseudo", isPseudo.toString())
            if (isPseudo) {
                LineChartView(pseudoData)
            } else {
                LineChartView(dataPoints)
            }
        }
    }
}

@Composable
fun LineChartView(dataPoints: List<Double?>) {
    val entries = dataPoints.mapIndexed { index, value ->
        Entry(index.toFloat(), value!!.toFloat())
    }
    val dataSet = LineDataSet(entries, "Performance Data").apply {
        color = android.graphics.Color.BLUE
        valueTextColor = android.graphics.Color.BLACK
    }
    val lineData = LineData(dataSet)

    AndroidView(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        factory = { context ->
            LineChart(context).apply {
                data = lineData
//                invalidate()
            }
        }
    )
}
