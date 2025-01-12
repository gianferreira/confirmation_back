package br.pucpr.authserver.files

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class FileConfig {
    @Bean("fileStorage")
    @Profile("!fs")
    fun s3Storage(): S3Storage = S3Storage()

    @Bean("fileStorage")
    @Profile("fs")
    fun localStorage(): FileSystemStorage = FileSystemStorage()
}