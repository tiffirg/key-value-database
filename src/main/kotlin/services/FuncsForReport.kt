package services

fun printHelp() = println(
    """Usage:
    | <path db> <command> - CREATE and DROP
    | <path db> <command> <key> <value> - ADD and UPDATE
    | <path db> <command> <key> - GET and DELETE
""".trimMargin()
)

fun printNotExistFile(nameFile: String) = println("$nameFile: No such file or directory")

fun printInvalidKey(db: String, key: String?) = println("$db: No such database key $key")

fun printKeySizeExceeded(key: String?) = println("$key: Key size exceeded")

fun printInvalidString(str: String?) = println("$str: Incorrect entry")

fun printDBAlreadyExists(db: String) = println("$db already exists")

fun printKeyAlreadyExists(key: String?) = println("$key already exists")

fun printGetResponseDB(db: String, key: String?, value: String?) = println("$db: $key - $value")