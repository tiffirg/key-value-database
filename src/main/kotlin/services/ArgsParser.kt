package services

import data.Arguments
import data.Command
import data.Command.*

const val REQUIRED_ARGS = 2
const val MAX_ARGS = 4

class ArgsParser {  // use kotlinx-cli...?
    fun parse(args: Array<String>): Arguments? {
        var key: String? = null
        var value: String? = null
        var batch: String? = null
        if (args.size !in REQUIRED_ARGS..MAX_ARGS)
            return null
        val command = defineCommand(args[1])
        when (command) {
            ADD, UPDATE -> {
                key = args[2]
                value = args[3]
            }
            GET, DELETE -> {
                key = args[2]
            }
            BATCH -> {
                batch = args[2]
            }
            else -> {
            }  // kotlin ругается
        }
        return Arguments(args[0], command, key, value, batch)
    }

    private fun defineCommand(parsedCommand: String) =
        Command.valueOf(parsedCommand.uppercase())  // Разрешаю нижний регистр
}