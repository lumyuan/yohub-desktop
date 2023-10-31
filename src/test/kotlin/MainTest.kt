import kotlinx.coroutines.*
import java.lang.RuntimeException

fun main() {
    GlobalScope.launch {
        println("Hello")
    }
}