package org.example.devmarketbackend.dto.request

import com.fasterxml.jackson.annotation.JsonProperty

data class ItemCreateRequest(
    val itemname: String,
    val price: Int,
    val brand: String,
    @JsonProperty("isNew")
    val isNew: Boolean,
    val categoryIds: List<Long>
)

