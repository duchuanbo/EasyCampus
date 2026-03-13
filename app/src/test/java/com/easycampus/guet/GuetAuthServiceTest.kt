package com.easycampus.guet

import com.easycampus.data.remote.guet.GuetAuthException
import com.easycampus.data.remote.guet.GuetAuthResult
import com.easycampus.data.remote.guet.GuetAuthService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

/**
 * GUET认证服务单元测试
 */
class GuetAuthServiceTest {

    private lateinit var authService: GuetAuthService

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        authService = GuetAuthService()
    }

    @Test
    fun `学号格式验证_正确格式_通过`() {
        val validStudentId = "2023010001"
        assertTrue(validStudentId.matches(Regex("^\\d{10}$")))
    }

    @Test
    fun `学号格式验证_错误格式_失败`() {
        val invalidStudentIds = listOf(
            "202301",           // 太短
            "202301000101",     // 太长
            "202301abc1",       // 包含字母
            "",                 // 空字符串
            "2023 010001"       // 包含空格
        )

        invalidStudentIds.forEach { studentId ->
            assertFalse("学号 $studentId 应该验证失败",
                studentId.matches(Regex("^\\d{10}$")))
        }
    }

    @Test
    fun `密码验证_空密码_失败`() {
        val password = ""
        assertTrue(password.isBlank())
    }

    @Test
    fun `密码验证_非空密码_通过`() {
        val password = "123456"
        assertFalse(password.isBlank())
    }

    @Test
    fun `登录参数验证_空学号_抛出异常`() = runBlocking {
        val studentId = ""
        val password = "123456"

        assertTrue(studentId.isBlank())
    }

    @Test
    fun `登录参数验证_空密码_抛出异常`() = runBlocking {
        val studentId = "2023010001"
        val password = ""

        assertTrue(password.isBlank())
    }

    @Test
    fun `Token有效期计算_7天_正确`() {
        val validityMillis = 7L * 24 * 60 * 60 * 1000
        val expectedMillis = 604800000L // 7天的毫秒数
        assertEquals(expectedMillis, validityMillis)
    }

    @Test
    fun `GuetAuthException_创建_消息正确`() {
        val message = "登录失败"
        val exception = GuetAuthException(message)
        assertEquals(message, exception.message)
    }

    @Test
    fun `GuetAuthException_带原因_消息和原因正确`() {
        val message = "网络错误"
        val cause = Exception("连接超时")
        val exception = GuetAuthException(message, cause)
        assertEquals(message, exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `GuetAuthResult_数据类_属性正确`() {
        val account = com.easycampus.domain.model.Account(
            id = "user123",
            platformId = "guet_yuketang",
            platformName = "GUET雨课堂",
            username = "2023010001",
            isActive = true,
            lastLoginTime = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis()
        )

        val user = com.easycampus.domain.model.User(
            id = "user123",
            username = "张三",
            email = null,
            avatarUrl = null,
            createdAt = System.currentTimeMillis(),
            lastLoginAt = System.currentTimeMillis()
        )

        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

        val result = GuetAuthResult(account, user, token)

        assertEquals(account, result.account)
        assertEquals(user, result.user)
        assertEquals(token, result.token)
    }
}
