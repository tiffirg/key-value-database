package data

data class Arguments(
    val db: String,
    val command: Command,
    val key: String?,
    val value: String?
) {
    fun toResponseDB(exitCode: ExitCode, value: String? = null) =
        ResponseDB(db = db, command = command, exitCode, key = key, value)
}
