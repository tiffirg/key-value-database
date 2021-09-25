package data

data class Arguments(
    val db: String,
    val command: Command,
    val key: String?,
    val value: String?,
    val batch: String?
) {
    fun toArgumentsForDB() = ArgumentsForDB(db, command, key, value)
}
