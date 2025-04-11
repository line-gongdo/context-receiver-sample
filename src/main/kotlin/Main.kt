package org.example

fun main() {
    val foo = Foo("gongdo")
    val myLogger = ConsoleLogger()

    // will be replaced with `context` function
    with(myLogger) {
        foo.log()
    }

//    val sample = AcceptOrderUseCaseSample()
//    sample.run()
}

data class Foo(val who: String)

context(Logger)
fun Foo.log() = info("Hello $who")

interface Logger {
    fun info(message: String)
    fun error(message: String)
}

class ConsoleLogger : Logger {
    override fun info(message: String) {
        println("INFO: $message")
    }

    override fun error(message: String) {
        println("ERROR: $message")
    }
}

