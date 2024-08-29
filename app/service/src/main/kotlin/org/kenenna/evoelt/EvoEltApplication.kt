package org.kenenna.evoelt

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(EvoEltConfig::class)
class EvoEltApplication

fun main(args: Array<String>) {
	runApplication<EvoEltApplication>(*args)
}
