package com.easycampus.domain.usecase.guet

import com.easycampus.data.repository.GuetRepositoryImpl
import com.easycampus.domain.model.User
import javax.inject.Inject

/**
 * GUET登录用例
 */
class GuetLoginUseCase @Inject constructor(
    private val repository: GuetRepositoryImpl
) {
    /**
     * 执行登录
     * @param studentId 学号
     * @param password 密码
     * @return 登录结果
     */
    suspend operator fun invoke(studentId: String, password: String): Result<User> {
        // 参数验证
        if (studentId.isBlank()) {
            return Result.failure(IllegalArgumentException("学号不能为空"))
        }
        
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("密码不能为空"))
        }

        // 学号格式验证（GUET学号通常为10位数字）
        if (!studentId.matches(Regex("^\\d{10}$"))) {
            return Result.failure(IllegalArgumentException("学号格式不正确，应为10位数字"))
        }

        return repository.login(studentId, password)
    }
}
