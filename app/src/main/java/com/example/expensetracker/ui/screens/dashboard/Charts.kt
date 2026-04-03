package com.example.expensetracker.ui.screens.dashboard

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

@Composable
fun PieSpendingChart(data: Map<String, Double>) {
    AndroidView(
        modifier = Modifier.fillMaxWidth().height(240.dp),
        factory = { context -> PieChart(context) },
        update = { chart ->
            val entries = data.map { PieEntry(it.value.toFloat(), it.key) }
            val set = PieDataSet(entries, "Category Split").apply {
                colors = listOf(
                    Color.parseColor("#EF5350"),
                    Color.parseColor("#5C6BC0"),
                    Color.parseColor("#66BB6A"),
                    Color.parseColor("#FFA726"),
                    Color.parseColor("#AB47BC")
                )
                valueTextColor = Color.WHITE
            }
            chart.data = PieData(set)
            chart.description.isEnabled = false
            chart.animateY(700)
            chart.invalidate()
        }
    )
}

@Composable
fun BarSpendingChart(data: List<Pair<String, Double>>) {
    AndroidView(
        modifier = Modifier.fillMaxWidth().height(240.dp),
        factory = { context -> BarChart(context) },
        update = { chart ->
            val entries = data.mapIndexed { idx, item -> BarEntry(idx.toFloat(), item.second.toFloat()) }
            val set = BarDataSet(entries, "By Day").apply { color = Color.parseColor("#29B6F6") }
            chart.data = BarData(set)
            chart.description.isEnabled = false
            chart.animateY(700)
            chart.invalidate()
        }
    )
}

@Composable
fun LineTrendChart(data: List<Pair<Float, Float>>) {
    AndroidView(
        modifier = Modifier.fillMaxWidth().height(240.dp),
        factory = { context -> LineChart(context) },
        update = { chart ->
            val entries = data.map { Entry(it.first, it.second) }
            val set = LineDataSet(entries, "Trend").apply {
                color = Color.parseColor("#66BB6A")
                setCircleColor(Color.parseColor("#2E7D32"))
                lineWidth = 2.5f
            }
            chart.data = LineData(set)
            chart.description.isEnabled = false
            chart.animateX(700)
            chart.invalidate()
        }
    )
}
