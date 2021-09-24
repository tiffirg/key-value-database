package data

enum class ExitCode(val exitCode: Int) {
    SUCCESS(0),
    HELP(1),
    INVALID_DB(2),
    INVALID_BATCH(3),
    INVALID_KEY(4),
    KEY_SIZE_EXCEEDED(5),
    VALUE_SIZE_EXCEEDED(6)
}