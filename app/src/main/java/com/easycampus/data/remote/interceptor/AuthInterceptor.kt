package com.easycampus.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * 认证拦截器
 * 自动添加Token到请求头
 */
class AuthInterceptor(
    private val tokenProvider: () -> String?
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = tokenProvider()
        
        return if (token != null) {
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("Content-Type", "application/json")
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}
