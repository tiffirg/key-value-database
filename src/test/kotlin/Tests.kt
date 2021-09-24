import data.Arguments
import data.Command.*
import services.ArgsParser
import kotlin.test.Test
import kotlin.test.assertEquals

val argsParser = ArgsParser()

internal class Tests {

    @Test
    fun testParserCreateAndDrop() {
        val argsCreate = arrayOf("test.db", "CREATE")
        val argumentsCreate = Arguments("test.db", CREATE, null, null, null)
        val argsDrop = arrayOf("test.db", "DROP")
        val argumentsDrop = Arguments("test.db", DROP, null, null, null)
        assertEquals(argumentsCreate, argsParser.parse(argsCreate))
        assertEquals(argumentsDrop, argsParser.parse(argsDrop))
    }

    @Test
    fun testParserAddAndUpdate() {
        val argsAdd = arrayOf("test.db", "ADD", "kotlin", "java")
        val argsUpdate = arrayOf("test.db", "UPDATE", "kotlin", "noJava")

        val argumentsAdd = Arguments("test.db", ADD, "kotlin", "java", null)
        val argumentsUpdate = Arguments("test.db", UPDATE, "kotlin", "noJava", null)
        assertEquals(argumentsAdd, argsParser.parse(argsAdd))
        assertEquals(argumentsUpdate, argsParser.parse(argsUpdate))
    }

    @Test
    fun testParserGetAndDelete() {
        val argsGet = arrayOf("test.db", "GET", "kotlin")
        val argsDelete = arrayOf("test.db", "DELETE", "kotlin")

        val argumentsGet = Arguments("test.db", GET, "kotlin", null, null)
        val argumentsDelete = Arguments("test.db", DELETE, "kotlin", null, null)
        assertEquals(argumentsGet, argsParser.parse(argsGet))
        assertEquals(argumentsDelete, argsParser.parse(argsDelete))
    }

    @Test
    fun testParserBatch() {
        val argsBatch = arrayOf("test.db", "BATCH", "batch.txt")
        val argumentsBatch = Arguments("test.db", GET, null, null, "batch.txt")
        assertEquals(argumentsBatch, argsParser.parse(argsBatch))
    }
}
