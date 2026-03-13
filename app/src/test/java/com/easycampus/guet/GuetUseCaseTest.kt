package com.easycampus.guet

import com.easycampus.data.repository.GuetRepositoryImpl
import com.easycampus.domain.model.User
import com.easycampus.domain.usecase.guet.GuetCheckInUseCase
import com.easycampus.domain.usecase.guet.GuetLoginUseCase
import com.easycampus.domain.usecase.guet.SyncGuetCoursesUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

/**
 * GUET UseCase单元测试
 */
class GuetUseCaseTest {

    @Mock
    private lateinit var repository: GuetRepositoryImpl

    private lateinit var loginUseCase: GuetLoginUseCase
    private lateinit var syncCoursesUseCase: SyncGuetCoursesUseCase
    private lateinit var checkInUseCase: GuetCheckInUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        loginUseCase = GuetLoginUseCase(repository)
        syncCoursesUseCase = SyncGuetCoursesUseCase(repository)
        checkInUseCase = GuetCheckInUseCase(repository)
    }

    @Test
    fun `登录UseCase_空学号_返回参数错误`() = runBlocking {
        val result = loginUseCase.invoke("", "password123")

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is IllegalArgumentException)
        assertEquals("学号不能为空", exception?.message)
    }

    @Test
    fun `登录UseCase_空密码_返回参数错误`() = runBlocking {
        val result = loginUseCase.invoke("2023010001", "")

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is IllegalArgumentException)
        assertEquals("密码不能为空", exception?.message)
    }

    @Test
    fun `登录UseCase_错误学号格式_返回参数错误`() = runBlocking {
        val result = loginUseCase.invoke("202301", "password123")

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is IllegalArgumentException)
        assertEquals("学号格式不正确，应为10位数字", exception?.message)
    }

    @Test
    fun `登录UseCase_正确参数_调用Repository`() = runBlocking {
        val studentId = "2023010001"
        val password = "password123"
        val mockUser = User(
            id = "user123",
            username = "张三",
            email = null,
            avatarUrl = null,
            createdAt = System.currentTimeMillis(),
            lastLoginAt = System.currentTimeMillis()
        )

        `when`(repository.login(studentId, password)).thenReturn(Result.success(mockUser))

        val result = loginUseCase.invoke(studentId, password)

        assertTrue(result.isSuccess)
        assertEquals(mockUser, result.getOrNull())
        verify(repository).login(studentId, password)
    }

    @Test
    fun `签到UseCase_空签到ID_返回参数错误`() = runBlocking {
        val result = checkInUseCase.invoke(
            checkInId = "",
            type = com.easycampus.domain.model.CheckInType.NORMAL
        )

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is IllegalArgumentException)
        assertEquals("签到ID不能为空", exception?.message)
    }

    @Test
    fun `签到UseCase_签到码类型_空码_返回参数错误`() = runBlocking {
        val result = checkInUseCase.invoke(
            checkInId = "checkin123",
            type = com.easycampus.domain.model.CheckInType.CODE,
            code = null
        )

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is IllegalArgumentException)
        assertEquals("签到码不能为空", exception?.message)
    }

    @Test
    fun `签到UseCase_位置类型_空位置_返回参数错误`() = runBlocking {
        val result = checkInUseCase.invoke(
            checkInId = "checkin123",
            type = com.easycampus.domain.model.CheckInType.LOCATION,
            latitude = null,
            longitude = null
        )

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is IllegalArgumentException)
        assertEquals("位置签到需要获取当前位置信息", exception?.message)
    }

    @Test
    fun `签到UseCase_普通签到_正确参数_调用Repository`() = runBlocking {
        val checkInId = "checkin123"
        val mockResult = com.easycampus.domain.model.CheckInResult(
            checkInId = checkInId,
            success = true,
            message = "签到成功",
            timestamp = System.currentTimeMillis()
        )

        `when`(repository.performCheckIn(
            checkInId,
            com.easycampus.domain.model.CheckInType.NORMAL,
            emptyMap()
        )).thenReturn(Result.success(mockResult))

        val result = checkInUseCase.invoke(
            checkInId = checkInId,
            type = com.easycampus.domain.model.CheckInType.NORMAL
        )

        assertTrue(result.isSuccess)
        verify(repository).performCheckIn(
            checkInId,
            com.easycampus.domain.model.CheckInType.NORMAL,
            emptyMap()
        )
    }

    @Test
    fun `学号格式验证_10位数字_通过`() {
        val validIds = listOf(
            "2023010001",
            "2022011234",
            "2024019999"
        )

        validIds.forEach { id ->
            assertTrue("学号 $id 应该通过验证",
                id.matches(Regex("^\\d{10}$")))
        }
    }

    @Test
    fun `学号格式验证_非10位_失败`() {
        val invalidIds = listOf(
            "202301000",      // 9位
            "20230100012",    // 11位
            "202301000A",     // 含字母
            "2023 010001",    // 含空格
            "abcdefghij"      // 全字母
        )

        invalidIds.forEach { id ->
            assertFalse("学号 $id 应该验证失败",
                id.matches(Regex("^\\d{10}$")))
        }
    }
}
