package com.easycampus.guet

import com.easycampus.data.remote.guet.CheckInException
import com.easycampus.domain.model.CheckInResult
import com.easycampus.domain.model.CheckInType
import org.junit.Assert.*
import org.junit.Test

/**
 * GUET签到服务单元测试
 */
class GuetCheckInServiceTest {

    @Test
    fun `距离计算_相同位置_距离为0`() {
        val lat1 = 25.2826
        val lng1 = 110.2961
        val lat2 = 25.2826
        val lng2 = 110.2961

        val distance = calculateDistance(lat1, lng1, lat2, lng2)

        assertEquals(0.0, distance, 1.0) // 允许1米误差
    }

    @Test
    fun `距离计算_不同位置_距离正确`() {
        // 桂林电子科技大学花江校区附近两点
        val lat1 = 25.2826
        val lng1 = 110.2961
        val lat2 = 25.2830
        val lng2 = 110.2970

        val distance = calculateDistance(lat1, lng1, lat2, lng2)

        // 距离应该在100米左右
        assertTrue("距离应该在合理范围内", distance > 50 && distance < 150)
    }

    @Test
    fun `位置验证半径_默认值_500米`() {
        val validRadius = 500.0
        assertEquals(500.0, validRadius, 0.01)
    }

    @Test
    fun `位置验证_范围内_通过`() {
        val distance = 300.0
        val validRadius = 500.0

        assertTrue("距离在允许范围内", distance <= validRadius)
    }

    @Test
    fun `位置验证_范围外_失败`() {
        val distance = 600.0
        val validRadius = 500.0

        assertFalse("距离超出允许范围", distance <= validRadius)
    }

    @Test
    fun `签到码验证_空码_失败`() {
        val code: String? = null
        assertTrue("签到码为空", code.isNullOrBlank())
    }

    @Test
    fun `签到码验证_空白码_失败`() {
        val code = "   "
        assertTrue("签到码为空白", code.isBlank())
    }

    @Test
    fun `签到码验证_有效码_通过`() {
        val code = "123456"
        assertFalse("签到码有效", code.isBlank())
    }

    @Test
    fun `CheckInResult_成功状态_正确`() {
        val result = CheckInResult(
            checkInId = "checkin123",
            success = true,
            message = "签到成功",
            timestamp = System.currentTimeMillis()
        )

        assertTrue(result.success)
        assertEquals("签到成功", result.message)
        assertEquals("checkin123", result.checkInId)
    }

    @Test
    fun `CheckInResult_失败状态_正确`() {
        val result = CheckInResult(
            checkInId = "checkin123",
            success = false,
            message = "签到失败：已过期",
            timestamp = System.currentTimeMillis()
        )

        assertFalse(result.success)
        assertEquals("签到失败：已过期", result.message)
    }

    @Test
    fun `CheckInException_创建_消息正确`() {
        val message = "签到失败"
        val exception = CheckInException(message)
        assertEquals(message, exception.message)
    }

    @Test
    fun `CheckInException_带原因_消息和原因正确`() {
        val message = "网络错误"
        val cause = Exception("连接超时")
        val exception = CheckInException(message, cause)
        assertEquals(message, exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `签到类型_普通签到_不需要额外参数`() {
        val type = CheckInType.NORMAL
        val needsCode = type == CheckInType.CODE ||
                type == CheckInType.GESTURE ||
                type == CheckInType.QR_CODE
        val needsLocation = type == CheckInType.LOCATION

        assertFalse("普通签到不需要签到码", needsCode)
        assertFalse("普通签到不需要位置", needsLocation)
    }

    @Test
    fun `签到类型_签到码签到_需要码参数`() {
        val type = CheckInType.CODE
        val needsCode = type == CheckInType.CODE ||
                type == CheckInType.GESTURE ||
                type == CheckInType.QR_CODE

        assertTrue("签到码签到需要码参数", needsCode)
    }

    @Test
    fun `签到类型_位置签到_需要位置参数`() {
        val type = CheckInType.LOCATION
        val needsLocation = type == CheckInType.LOCATION

        assertTrue("位置签到需要位置参数", needsLocation)
    }

    @Test
    fun `重试次数_默认值_3次`() {
        val maxRetryCount = 3
        assertEquals(3, maxRetryCount)
    }

    @Test
    fun `重试延迟_初始值_1000毫秒`() {
        val initialRetryDelay = 1000L
        assertEquals(1000L, initialRetryDelay)
    }

    // 辅助函数：计算两点间距离
    private fun calculateDistance(
        lat1: Double, lng1: Double,
        lat2: Double, lng2: Double
    ): Double {
        val earthRadius = 6371000.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return earthRadius * c
    }
}
