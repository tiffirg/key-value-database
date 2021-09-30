package data

enum class ExitCode(val exitCode: Int) {
    SUCCESS(0),
    HELP(1),
    INVALID_DB(2),
    INVALID_KEY(3),
    KEY_SIZE_EXCEEDED(4),
    INVALID_STRING(5),
    DB_ALREADY_EXISTS(6),
    KEY_ALREADY_EXIST(7)
}