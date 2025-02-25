package funkotlin.fp_in_kotlin_book

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FpInKotlinBookApplication {
	fun main(args: Array<String>) {
		runApplication<FpInKotlinBookApplication>(*args)
	}
}
