package com.r1n1os.jetpackcomposetemplateopensource.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val barcode: String,
    val name: String,
    val category: String,
    val isFeatured: Boolean = false
)
