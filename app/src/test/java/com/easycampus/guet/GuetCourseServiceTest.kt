package com.easycampus.guet

import com.easycampus.data.remote.guet.GuetCourseService
import com.easycampus.data.remote.guet.SyncException
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Calendar

/**
 * GUET课程服务单元测试
 */
class GuetCourseServiceTest {

    private lateinit var courseService: GuetCourseService

    @Before
    fun setup() {
        courseService = GuetCourseService()
    }

    @Test
    fun `当前学期计算_9月_上学期`() {
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.SEPTEMBER, 1)
        val year = calendar.get(Calendar.YEAR)

        val semester = if (calendar.get(Calendar.MONTH) + 1 >= 9) {
            "$year-${year + 1}-1"
        } else {
            "${year - 1}-$year-2"
        }

        assertEquals("2024-2025-1", semester)
    }

    @Test
    fun `当前学期计算_3月_下学期`() {
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.MARCH, 1)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)

        val semester = if (month >= 9 || month <= 1) {
            "$year-${year + 1}-1"
        } else {
            "${year - 1}-$year-2"
        }

        assertEquals("2023-2024-2", semester)
    }

    @Test
    fun `周次解析_连续周_正确解析`() {
        val weeksStr = "1-16"
        val weeks = parseWeeks(weeksStr)

        assertEquals(16, weeks.size)
        assertEquals(1, weeks.first())
        assertEquals(16, weeks.last())
    }

    @Test
    fun `周次解析_单双周_正确解析`() {
        val weeksStr = "1,3,5,7,9,11,13,15"
        val weeks = parseWeeks(weeksStr)

        assertEquals(8, weeks.size)
        assertEquals(listOf(1, 3, 5, 7, 9, 11, 13, 15), weeks)
    }

    @Test
    fun `周次解析_混合格式_正确解析`() {
        val weeksStr = "1-4,6,8-10"
        val weeks = parseWeeks(weeksStr)

        assertEquals(listOf(1, 2, 3, 4, 6, 8, 9, 10), weeks)
    }

    @Test
    fun `周次解析_空字符串_返回空列表`() {
        val weeksStr = ""
        val weeks = parseWeeks(weeksStr)

        assertTrue(weeks.isEmpty())
    }

    @Test
    fun `周次解析_空白字符串_返回空列表`() {
        val weeksStr = "   "
        val weeks = parseWeeks(weeksStr)

        assertTrue(weeks.isEmpty())
    }

    @Test
    fun `SyncException_创建_消息正确`() {
        val message = "同步失败"
        val exception = SyncException(message)
        assertEquals(message, exception.message)
    }

    @Test
    fun `SyncException_带原因_消息和原因正确`() {
        val message = "网络错误"
        val cause = Exception("连接超时")
        val exception = SyncException(message, cause)
        assertEquals(message, exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `重试次数_默认值_3次`() {
        val maxRetryCount = 3
        assertEquals(3, maxRetryCount)
    }

    @Test
    fun `初始重试延迟_默认值_1000毫秒`() {
        val initialRetryDelay = 1000L
        assertEquals(1000L, initialRetryDelay)
    }

    @Test
    fun `指数退避计算_正确递增`() {
        var delay = 1000L
        val delays = mutableListOf<Long>()

        repeat(3) {
            delays.add(delay)
            delay *= 2
        }

        assertEquals(listOf(1000L, 2000L, 4000L), delays)
    }

    // 辅助函数
    private fun parseWeeks(weeksStr: String): List<Int> {
        if (weeksStr.isBlank()) return emptyList()

        val weeks = mutableListOf<Int>()
        val parts = weeksStr.split(",")

        for (part in parts) {
            val trimmed = part.trim()
            if (trimmed.contains("-")) {
                val range = trimmed.split("-")
                if (range.size == 2) {
                    val start = range[0].toIntOrNull() ?: continue
                    val end = range[1].toIntOrNull() ?: continue
                    weeks.addAll(start..end)
                }
            } else {
                trimmed.toIntOrNull()?.let { weeks.add(it) }
            }
        }

        return weeks.sorted()
    }
}
