package data

data class ResponseDB(
    val db: String,
    val command: Command,
    var exitCode: ExitCode,
    var key: String? = null,
    var value: String? = null
)
