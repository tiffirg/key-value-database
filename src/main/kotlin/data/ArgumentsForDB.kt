package data

data class ArgumentsForDB(
    val db: String,
    val command: Command,
    val key: String?,
    val value: String?
)
