package org.example.devmarketbackend.global.exception

import org.example.devmarketbackend.global.api.BaseCode

class CustomException(
    val errorCode: BaseCode
) : RuntimeException(errorCode.getReason().message)

