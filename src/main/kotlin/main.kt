import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val app = App()
    val exitCode = app.run(args)
    exitProcess(exitCode)
}