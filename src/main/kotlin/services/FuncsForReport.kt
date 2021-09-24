package services

fun printHelp() = println(
    """Usage:
    | <path db> <command> - CREATE and DROP
    | <path db> <command> <key> <value> - ADD and UPDATE
    | <path db> <command> <key> - GET and DELETE
    | <path db> <command> <path batch> - BATCH
""".trimMargin()
)

fun printNotExistFile(nameFile: String) = println("$nameFile: No such file or directory")

fun printInvalidKey(key: String) = println("$key: No such database key")