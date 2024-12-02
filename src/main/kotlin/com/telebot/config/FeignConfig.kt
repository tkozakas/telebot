package com.telebot.config

import feign.RequestInterceptor
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
    }

    @Bean
    fun gptRequestInterceptor(): RequestInterceptor = RequestInterceptor { template ->
        template.header("Authorization", "Bearer $gptToken")
    }
}
