package com.github.saintedlittle.annotations

import com.github.saintedlittle.messaging.KafkaTopic

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class KafkaEvent(val topic: KafkaTopic)