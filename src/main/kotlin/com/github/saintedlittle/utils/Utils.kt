package com.github.saintedlittle.utils

fun Long.toJavaLong(): java.lang.Long = java.lang.Long(this.toString())

fun java.lang.Long.toKotlinLong(): Long = this.toLong()