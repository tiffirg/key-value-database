import data.Arguments
import data.Command.GET
import data.ExitCode
import data.ExitCode.*
import data.ResponseDB
import services.*

class App {
    private val db = InterfaceDB()
    private val parser = ArgsParser()

    fun run(args: Array<String>): Int {
        val parsedArgs = parser.parse(args)
        if (parsedArgs == null) {
            printHelp()
            return HELP.exitCode
        }
        sendRequestDB(parsedArgs)
        val resultExitCode = processResponses(db.run())  // Заготовка для batch режима
        return resultExitCode.exitCode  // exit codes: 0, 2, 3, 4, 5, 6
    }

    private fun processResponses(responsesDB: List<ResponseDB>): ExitCode {
        var resultProcess: ExitCode
        for (response in responsesDB) {
            resultProcess = processResponse(response)
            if (resultProcess != SUCCESS) {
                return resultProcess
            }
        }
        return SUCCESS
    }

    private fun processResponse(response: ResponseDB): ExitCode {
        when (response.exitCode) {
            INVALID_DB -> printNotExistFile(response.db)
            INVALID_KEY -> printInvalidKey(response.db, response.key)
            KEY_SIZE_EXCEEDED -> printKeySizeExceeded(response.key)
            INVALID_STRING -> printInvalidString(response.key)
            DB_ALREADY_EXISTS -> printDBAlreadyExists(response.db)
            KEY_ALREADY_EXIST -> printKeyAlreadyExists(response.key)
            else -> {
                if (response.command == GET) {
                    printGetResponseDB(response.db, response.key, response.value)
                }
            }
        }
        return response.exitCode
    }

    private fun sendRequestDB(args: Arguments) = db.addRequest(args)
}