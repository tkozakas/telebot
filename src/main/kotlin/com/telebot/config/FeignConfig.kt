package com.telebot.config

import feign.RequestInterceptor
import feign.RequestTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FeignConfig {
    @Value("\${gpt.token}")
    private lateinit var gptToken: String

    @Bean
    fun requestInterceptor(): RequestInterceptor = RequestInterceptor { template ->
        template.header("Content-Type", "application/json")
        template.header("Accept", "application/json")
        template.header("User-Agent", "Telebot")
    }

    @Bean
    fun dynamicRequestInterceptor(): RequestInterceptor {
        return RequestInterceptor { requestTemplate: RequestTemplate ->
            val token = "Bearer $gptToken"
            requestTemplate.header("Authorization", token)
        }
    }
}
