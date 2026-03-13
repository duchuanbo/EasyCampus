package com.easycampus.presentation.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.easycampus.domain.model.CheckIn
import com.easycampus.domain.model.CheckInStatus
import com.easycampus.domain.model.CheckInType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSchedule: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EasyCampus") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToLogin
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加账号")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Today's schedule summary
            TodayScheduleCard(
                onClick = onNavigateToSchedule,
                modifier = Modifier.padding(16.dp)
            )

            // Pending check-ins section
            Text(
                text = "待签到",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (uiState.pendingCheckIns.isEmpty()) {
                EmptyCheckInState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.pendingCheckIns) { checkIn ->
                        CheckInCard(
                            checkIn = checkIn.toUiModel(),
                            onCheckIn = { viewModel.performCheckIn(checkIn.id, checkIn.platformId) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodayScheduleCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "今日课程",
                    style = MaterialTheme.typography.titleMedium
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "3 节课",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "下一节: 高等数学 @ 10:00",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CheckInCard(
    checkIn: CheckInUiModel,
    onCheckIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = checkIn.courseName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${checkIn.platformName} · ${checkIn.type}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "截止时间: ${checkIn.deadline}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Button(onClick = onCheckIn) {
                Text("签到")
            }
        }
    }
}

@Composable
private fun EmptyCheckInState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "暂无待签到",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Text(
            text = "所有签到已完成",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// UI Model
data class CheckInUiModel(
    val id: String,
    val courseName: String,
    val platformName: String,
    val type: String,
    val deadline: String
)

// Extension function to convert CheckIn to CheckInUiModel
private fun CheckIn.toUiModel(): CheckInUiModel {
    return CheckInUiModel(
        id = id,
        courseName = courseName,
        platformName = platformId,
        type = when (type) {
            CheckInType.NORMAL -> "普通签到"
            CheckInType.GESTURE -> "手势签到"
            CheckInType.LOCATION -> "位置签到"
            CheckInType.QR_CODE -> "二维码签到"
            CheckInType.CODE -> "签到码"
            CheckInType.FACE -> "人脸识别"
        },
        deadline = endTime?.let { 
            java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(it))
        } ?: "无截止时间"
    )
}
