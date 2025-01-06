package com.github.saintedlittle.utils

fun kotlinToJavaLong(value: Long): java.lang.Long = java.lang.Long(value.toString())

fun javaToKotlinLong(value: java.lang.Long?): Long? = value?.toLong()