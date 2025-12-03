package org.example.devmarketbackend.s3

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(S3Properties::class)
class S3Config(
    private val s3Properties: S3Properties
) {
    @Bean
    fun amazonS3(): AmazonS3 {
        val awsCreds = BasicAWSCredentials(
            s3Properties.accessKey,
            s3Properties.secretKey
        )
        return AmazonS3ClientBuilder.standard()
            .withRegion(s3Properties.region)
            .withCredentials(AWSStaticCredentialsProvider(awsCreds))
            .build()
    }
}

