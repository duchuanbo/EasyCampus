package com.easycampus.presentation.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Appearance section
            item {
                SettingsSection(title = "外观") {
                    SettingsSwitchItem(
                        title = "动态主题",
                        subtitle = "跟随系统壁纸颜色",
                        icon = Icons.Default.Palette,
                        checked = uiState.dynamicColorEnabled,
                        onCheckedChange = { viewModel.setDynamicColorEnabled(it) }
                    )

                    SettingsMenuItem(
                        title = "主题模式",
                        subtitle = uiState.themeMode.displayName,
                        icon = Icons.Default.DarkMode,
                        onClick = { /* Show theme selection dialog */ }
                    )
                }
            }

            // Notifications section
            item {
                SettingsSection(title = "通知") {
                    SettingsSwitchItem(
                        title = "启用通知",
                        subtitle = "接收签到和课程提醒",
                        icon = Icons.Default.Notifications,
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                    )

                    if (uiState.notificationsEnabled) {
                        SettingsMenuItem(
                            title = "签到提醒",
                            subtitle = "提前 ${uiState.checkInReminderMinutes} 分钟",
                            icon = Icons.Default.AccessTime,
                            onClick = { /* Show time picker */ }
                        )

                        SettingsMenuItem(
                            title = "课程提醒",
                            subtitle = if (uiState.courseReminderEnabled) "提前 ${uiState.courseReminderMinutes} 分钟" else "已关闭",
                            icon = Icons.Default.Schedule,
                            onClick = { /* Show time picker */ }
                        )
                    }
                }
            }

            // Check-in section
            item {
                SettingsSection(title = "签到") {
                    SettingsSwitchItem(
                        title = "自动签到",
                        subtitle = "检测到签到时自动执行",
                        icon = Icons.Default.AutoMode,
                        checked = uiState.autoCheckInEnabled,
                        onCheckedChange = { viewModel.setAutoCheckInEnabled(it) }
                    )

                    SettingsMenuItem(
                        title = "自动签到类型",
                        subtitle = "普通签到、手势签到...",
                        icon = Icons.Default.Checklist,
                        onClick = { /* Show check-in types dialog */ }
                    )
                }
            }

            // Accounts section
            item {
                SettingsSection(title = "账号") {
                    SettingsMenuItem(
                        title = "管理账号",
                        subtitle = "${uiState.accountCount} 个已登录账号",
                        icon = Icons.Default.AccountCircle,
                        onClick = { /* Navigate to account management */ }
                    )
                }
            }

            // About section
            item {
                SettingsSection(title = "关于") {
                    SettingsMenuItem(
                        title = "版本",
                        subtitle = uiState.appVersion,
                        icon = Icons.Default.Info,
                        onClick = { }
                    )

                    SettingsMenuItem(
                        title = "开源许可",
                        subtitle = "查看第三方库许可",
                        icon = Icons.Default.Description,
                        onClick = { /* Show licenses */ }
                    )
                }
            }

            // Danger zone
            item {
                SettingsSection(title = "危险区域") {
                    SettingsActionItem(
                        title = "清除所有数据",
                        subtitle = "删除所有本地存储的数据",
                        icon = Icons.Default.DeleteForever,
                        color = MaterialTheme.colorScheme.error,
                        onClick = { viewModel.clearAllData() }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
        Divider(modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
private fun SettingsMenuItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(icon, contentDescription = null)
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(icon, contentDescription = null)
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    )
}

@Composable
private fun SettingsActionItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(title, color = color)
        },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(icon, contentDescription = null, tint = color)
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

// Extension for theme mode display
private val com.easycampus.domain.repository.AppTheme.displayName: String
    get() = when (this) {
        com.easycampus.domain.repository.AppTheme.LIGHT -> "浅色"
        com.easycampus.domain.repository.AppTheme.DARK -> "深色"
        com.easycampus.domain.repository.AppTheme.SYSTEM -> "跟随系统"
    }
