package com.skyba.vision.demo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import com.artems_apps.vision_pause.data.local.StatsViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skyba.vision.demo.data.local.TimeFormatter
import com.skyba.vision.demo.ui.components.ScrollColumnChart
import ir.ehsannarmani.compose_charts.models.DividerProperties
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.LineProperties
import kotlin.math.roundToInt
import com.skyba.vision.demo.R

@Composable
fun  statsScreen(viewModel: StatsViewModel){

    val uiState by viewModel.uiState.collectAsState()

    val scrollState = rememberScrollState()

    val context = LocalContext.current

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val chartContentWidth = remember(uiState.chartData.size) {
        val calculated = (uiState.chartData.size * 75).dp
        if (calculated > screenWidth - 60.dp) calculated else screenWidth - 60.dp
    }

    Column(modifier = Modifier
        .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally) {

        Spacer(modifier = Modifier.height(15.dp))

        Text(
            text = stringResource(R.string.stats_details_title),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 16.dp)
        )

        Spacer(modifier = Modifier.height(15.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 15.dp),
            shape = RoundedCornerShape(15.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Column(modifier = Modifier
                .fillMaxWidth(),
                verticalArrangement = Arrangement.Center
                ) {

                Spacer(modifier = Modifier.height(15.dp))

                when {
                    uiState.isLoading -> {
                        Box(modifier = Modifier.fillMaxSize())
                    }

                    uiState.chartData.isNotEmpty() -> {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                                .padding(horizontal = 16.dp)
                        ) {

                            ScrollColumnChart(
                                modifier = Modifier.fillMaxSize(),
                                data = uiState.chartData,
                                maxValue = uiState.yAxisMax,

                                scrollState = scrollState,
                                chartContentWidth = chartContentWidth,

                                labelHelperProperties = LabelHelperProperties(enabled = false),
                                indicatorProperties = HorizontalIndicatorProperties(
                                    enabled = true,
                                    textStyle = defaultChartStyle(),
                                    contentBuilder = { value: Double ->
                                        val totalSecs = (value * 60).roundToInt()
                                        TimeFormatter.formatSecondsRaw(
                                            context = context,
                                            totalSeconds = totalSecs,
                                            isPopup = false
                                        )
                                    }
                                ),
                                dividerProperties = DividerProperties(
                                    enabled = true,
                                    xAxisProperties = LineProperties(
                                        color = defaultGridBrush(),
                                        thickness = 1.dp
                                    ),
                                    yAxisProperties = LineProperties(
                                        color = defaultGridBrush(),
                                        thickness = 1.dp
                                    )
                                ),
                                gridProperties = GridProperties(
                                    enabled = true,
                                    xAxisProperties = GridProperties.AxisProperties(
                                        color = defaultGridBrush(),
                                        thickness = 1.dp
                                    ),
                                    yAxisProperties = GridProperties.AxisProperties(enabled = false)
                                ),
                                labelProperties = LabelProperties(
                                    enabled = true,
                                    padding = 8.dp,
                                    textStyle = defaultChartStyle()
                                ),
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LegendItem(
                            label = stringResource(R.string.status_bar_usage_time),
                            color = colorResource(R.color.screen_time)
                        )

                        LegendItem(
                            label = stringResource(R.string.status_bar_break_time),
                            color = colorResource(R.color.break_time)
                        )
                    }

                    else -> {

                        Column(modifier = Modifier
                            .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                modifier = Modifier
                                    .size(50.dp),
                                painter = painterResource(R.drawable.timeline_button_icon),
                                contentDescription = null
                            )

                            Text(
                                text = stringResource(R.string.stats_empty_title),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .padding(top = 15.dp)
                            )
                        }

                    }
                }

                Spacer(modifier = Modifier.height(15.dp))
            }
        }
    }

    LaunchedEffect(uiState.chartData, chartContentWidth, scrollState.maxValue) {
        if (uiState.chartData.isNotEmpty() && scrollState.maxValue > 0) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }
}



@Composable
fun StatRow(icon: Painter? = null,
            iconTint: Color = LocalContentColor.current,
            label: String,
            value: String) {

    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(painter = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = iconTint)
            Spacer(modifier = Modifier.width(8.dp))
        }
        else {
            Spacer(modifier = Modifier.width(28.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "$label: ", fontWeight = FontWeight.SemiBold)
        Text(text = value)
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .width(20.dp)
                .height(5.dp)
                .background(color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun defaultChartStyle() = TextStyle(
    color = MaterialTheme.colorScheme.primary,
    fontSize = 12.sp
)

@Composable
fun defaultGridBrush() = SolidColor(MaterialTheme.colorScheme.background)

