package org.example.devmarketbackend.s3

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cloud.aws")
data class S3Properties(
    var accessKey: String = "",
    var secretKey: String = "",
    var bucket: String = "",
    var region: String = ""
)

