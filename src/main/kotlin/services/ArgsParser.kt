package services

import data.Arguments
import data.Command
import data.Command.CREATE
import data.Command.DROP
import data.Command.ADD
import data.Command.UPDATE
import data.Command.GET
import data.Command.DELETE

const val REQUIRED_ARGS = 2
const val MEAN_ARGS = 3
const val MAX_ARGS = 4

class ArgsParser {  // use kotlinx-cli...?
    fun parse(args: Array<String>): Arguments? {
        var key: String? = null
        var value: String? = null
        if (args.size !in REQUIRED_ARGS..MAX_ARGS) {
            return null
        }
        val command = defineCommand(args[1]) ?: return null

        when {
            checkCreateGetAndDelete(command, args.size) -> {
                key = args[2]
            }
            checkAddAndUpdate(command, args.size) -> {
                key = args[2]
                value = args[3]
            }
            !checkCreateAndDrop(command, args.size) -> return null
        }
        return Arguments(args[0], command, key, value)
    }

    private fun checkCreateAndDrop(command: Command, argsSize: Int) =
        (command == CREATE || command == DROP) && argsSize == REQUIRED_ARGS

    private fun checkAddAndUpdate(command: Command, argsSize: Int) =
        (command == ADD || command == UPDATE) && argsSize == MAX_ARGS

    private fun checkCreateGetAndDelete(command: Command, argsSize: Int) =
        (command == GET || command == DELETE) && argsSize == MEAN_ARGS


    private fun defineCommand(parsedCommand: String): Command? {
        for (command in Command.values()) {
            if (command.toString() == parsedCommand.uppercase()) {
                return command
            }
        }
        return null
    }
}