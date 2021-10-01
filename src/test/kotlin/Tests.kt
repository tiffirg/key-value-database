import data.Arguments
import data.Command.CREATE
import data.Command.DROP
import data.Command.ADD
import data.Command.UPDATE
import data.Command.GET
import data.Command.DELETE
import data.ExitCode.SUCCESS
import services.ArgsParser
import java.io.File
import java.io.RandomAccessFile
import kotlin.test.Test
import kotlin.test.assertEquals

val app = App()
val interfaceDB = InterfaceDB()
val argsParser = ArgsParser()

val argsCreate = arrayOf("test.db", "CREATE")
val argsDrop = arrayOf("test.db", "DROP")

const val alphabet = "abcdefghijklmnopqrstuvwxyz0123456789"


internal class Tests {

    @Test
    fun testParserCreateAndDrop() {
        val argumentsCreate = Arguments("test.db", CREATE, null, null)
        val argumentsDrop = Arguments("test.db", DROP, null, null)
        assertEquals(argumentsCreate, argsParser.parse(argsCreate))
        assertEquals(argumentsDrop, argsParser.parse(argsDrop))
    }

    @Test
    fun testParserAddAndUpdate() {
        val argsAdd = arrayOf("test.db", "ADD", "kotlin", "java")
        val argsUpdate = arrayOf("test.db", "UPDATE", "kotlin", "noJava")

        val argumentsAdd = Arguments("test.db", ADD, "kotlin", "java")
        val argumentsUpdate = Arguments("test.db", UPDATE, "kotlin", "noJava")
        assertEquals(argumentsAdd, argsParser.parse(argsAdd))
        assertEquals(argumentsUpdate, argsParser.parse(argsUpdate))
    }

    @Test
    fun testParserGetAndDelete() {
        val argsGet = arrayOf("test.db", "GET", "kotlin")
        val argsDelete = arrayOf("test.db", "DELETE", "kotlin")

        val argumentsGet = Arguments("test.db", GET, "kotlin", null)
        val argumentsDelete = Arguments("test.db", DELETE, "kotlin", null)
        assertEquals(argumentsGet, argsParser.parse(argsGet))
        assertEquals(argumentsDelete, argsParser.parse(argsDelete))
    }

    @Test
    fun testFuncCreateAndDrop() {
        assertEquals(0, app.run(argsCreate))
        assertEquals(0, app.run(argsDrop))
    }

    @Test
    fun testFuncAdd() {
        val argsAdd = arrayOf("test.db", "ADD", "kotlin", "java")
        app.run(argsCreate)
        assertEquals(SUCCESS, interfaceDB.add(argsParser.parse(argsAdd)!!).exitCode)
        app.run(argsDrop)
    }

    @Test
    fun testFuncUpdate() {
        val argsAdd = arrayOf("test.db", "ADD", "kotlin", "java")
        val argsUpdate = arrayOf("test.db", "UPDATE", "kotlin", "harmonics")
        app.run(argsCreate)
        app.run(argsAdd)
        assertEquals(SUCCESS, interfaceDB.update(argsParser.parse(argsUpdate)!!).exitCode)
        app.run(argsDrop)
    }

    @Test
    fun testFuncGet() {
        val argsAdd = arrayOf("test.db", "ADD", "kotlin", "java")
        val argsGet = arrayOf("test.db", "GET", "kotlin")
        app.run(argsCreate)
        app.run(argsAdd)
        assertEquals(SUCCESS, interfaceDB.get(argsParser.parse(argsGet)!!).exitCode)
        app.run(argsDrop)
    }

    @Test
    fun testFuncDelete() {
        val argsAdd = arrayOf("test.db", "ADD", "kotlin", "java")
        val argsDelete = arrayOf("test.db", "DELETE", "kotlin")
        app.run(argsCreate)
        app.run(argsAdd)
        assertEquals(SUCCESS, interfaceDB.delete(argsParser.parse(argsDelete)!!).exitCode)
        app.run(argsDrop)
    }

    @Test
    fun testParserHelp() {
        val args1 = arrayOf("test.db", "REMOVE", "kotlin")
        val args2 = arrayOf("test.db", "PUT", "kotlin")
        assertEquals(null, argsParser.parse(args1))
        assertEquals(null, argsParser.parse(args2))
    }

    @Test
    fun testAppHelp() {
        val args1 = arrayOf("test.db", "REMOVE", "kotlin")
        val args2 = arrayOf("test.db", "PUT", "kotlin")
        assertEquals(1, app.run(args1))
        assertEquals(1, app.run(args2))
    }

    @Test
    fun testInvalidDB() {
        val allArgs = listOf(
            arrayOf("test.db", "DROP"),
            arrayOf("test.db", "GET", "kotlin"),
            arrayOf("test.db", "DELETE", "kotlin"),
            arrayOf("test.db", "ADD", "kotlin", "java"),
            arrayOf("test.db", "UPDATE", "kotlin", "java")
        )
        for (args in allArgs) {
            assertEquals(2, app.run(args))
        }
    }

    @Test
    fun testInvalidKey() {
        app.run(argsCreate)
        val args = arrayOf("test.db", "GET", "kotlin")
        assertEquals(3, app.run(args))
        app.run(argsDrop)
    }

    @Test
    fun testKeySizeExceeded() {
        app.run(argsCreate)
        val args = arrayOf(
            "test.db", "GET", """Славься, Отечество наше свободное,
Братских народов союз вековой,
Предками данная мудрость народная!
Славься, страна! Мы гордимся тобой!"""
        )
        assertEquals(4, app.run(args))
        app.run(argsDrop)
    }

    @Test
    fun testInvalidString() {
        app.run(argsCreate)
        val args1 = arrayOf("test.db", "ADD", "kotlin0x00", "java")
        val args2 = arrayOf("test.db", "ADD", "kotlin", "java0x00")
        assertEquals(5, app.run(args1))
        assertEquals(5, app.run(args2))
        app.run(argsDrop)
    }

    @Test
    fun testDBAlreadyExists() {
        app.run(argsCreate)
        assertEquals(6, app.run(argsCreate))
        app.run(argsDrop)
    }

    @Test
    fun testKeyAlreadyExists() {
        val argsAdd = arrayOf("test.db", "ADD", "kotlin", "java")
        app.run(argsCreate)
        app.run(argsAdd)
        assertEquals(7, app.run(argsAdd))
        app.run(argsDrop)
    }

    @Test
    fun testBinarySearch() {
        val file = RandomAccessFile("test.db", "rw")
        val keys = listOf("1", "2", "4", "5", "6", "7", "8", "9")
        val values = listOf("10", "20", "40", "50", "60", "70", "80", "90")
        for (i in keys.indices) {
            val byteString = interfaceDB.transformByteArrayField(keys[i], values[i])
            file.write(byteString)
        }
        for (i in 1..9) {
            file.seek(0)
            val (numberFirstByteFieldSearch, isSearched) = interfaceDB.binarySearch(file, i.toString())
            if (i == 3) {
                assertEquals(false, isSearched)
                val value = interfaceDB.getLinkValueByFirstByte(file, numberFirstByteFieldSearch)
                assertEquals(40L, value)
            } else {
                assertEquals(true, isSearched)
                val value = interfaceDB.getLinkValueByFirstByte(file, numberFirstByteFieldSearch)
                assertEquals(i * 10L, value)
            }
        }
        File("test.db").delete()
    }

    @Test
    fun testFuncsAddGet() {
        val argsAdd = arrayOf("test.db", "ADD", "kotlin", "java")
        val argsGet = arrayOf("test.db", "GET", "kotlin")
        app.run(argsCreate)
        app.run(argsAdd)
        assertEquals("java", interfaceDB.get(argsParser.parse(argsGet)!!).value)
        app.run(argsDrop)
    }

    @Test
    fun testFuncsAddGetUpdate() {
        val argsAdd = arrayOf("test.db", "ADD", "kotlin", "java")
        val argsUpdate = arrayOf("test.db", "UPDATE", "kotlin", "harmonics")
        val argsGet = arrayOf("test.db", "GET", "kotlin")
        app.run(argsCreate)
        app.run(argsAdd)
        app.run(argsUpdate)
        assertEquals("harmonics", interfaceDB.get(argsParser.parse(argsGet)!!).value)
        app.run(argsDrop)
    }

    @Test
    fun testFuncsAddDelete() {
        val argsAdd = arrayOf("test.db", "ADD", "kotlin", "java")
        val argsGet = arrayOf("test.db", "GET", "kotlin")
        val argsDelete = arrayOf("test.db", "DELETE", "kotlin")
        app.run(argsCreate)
        app.run(argsAdd)
        app.run(argsDelete)
        assertEquals(3, app.run(argsGet))
        app.run(argsDrop)
    }

    @Test
    fun testFuncsAddUpdateDelete() {
        val argsAdd = arrayOf("test.db", "ADD", "kotlin", "java")
        val argsUpdate = arrayOf("test.db", "UPDATE", "kotlin", "harmonics")
        val argsDelete = arrayOf("test.db", "DELETE", "kotlin")
        val argsGet = arrayOf("test.db", "GET", "kotlin")
        app.run(argsCreate)
        app.run(argsAdd)
        app.run(argsUpdate)
        app.run(argsDelete)
        assertEquals(3, app.run(argsGet))
        app.run(argsDrop)
    }

    @Test
    fun testAppAddUpdateGetSmall() {
        val map = mapOf(
            "kotlin" to "java",
            "a" to "b", "c" to "d", "e" to "f", "g" to "h"
        )
        val mapUpdate = mapOf("g" to "m", "e" to "y", "c" to "a")
        app.run(argsCreate)
        for (key in map.keys) {
            app.run(arrayOf("test.db", "ADD", key, map[key]!!))
        }
        for (key in mapUpdate.keys) {
            app.run(arrayOf("test.db", "UPDATE", key))
        }

        for (key in map.keys) {
            assertEquals(0, app.run(arrayOf("test.db", "GET", key)))
        }
        app.run(argsDrop)

    }

    @Test
    fun testAppAddUpdateGetBig() {

        val map: MutableMap<String, String> = mutableMapOf()
        app.run(argsCreate)
        for (i in 1..1000) {
            val amountSymbolsKey = (4..10).random()
            val amountSymbolsValue = (1..10).random()
            val key = List(amountSymbolsKey) { alphabet.random() }.joinToString("")
            val value = List(amountSymbolsValue) { alphabet.random() }.joinToString("")
            val exitCode = app.run(arrayOf("test.db", "ADD", key, value))
            if (exitCode == 0) {
                map[key] = value
            }
        }

        repeat(200) {
            val amountSymbolsValue = (1..10).random()
            val newValue = List(amountSymbolsValue) { alphabet.random() }.joinToString("")
            val key = map.keys.random()
            app.run(arrayOf("test.db", "UPDATE", key, newValue))
            map[key] = newValue
            assertEquals(map[key], interfaceDB.get(argsParser.parse(arrayOf("test.db", "GET", key))!!).value)
        }

        repeat(500) {
            val key = map.keys.random()
            assertEquals(map[key], interfaceDB.get(argsParser.parse(arrayOf("test.db", "GET", key))!!).value)
        }
        app.run(argsDrop)
    }

    @Test
    fun testAppAddUpdateDeleteGet() {
        val map: MutableMap<String, String> = mutableMapOf()
        app.run(argsCreate)
        for (i in 1..1000) {
            val amountSymbolsKey = (3..10).random()
            val amountSymbolsValue = (3..10).random()
            val key = List(amountSymbolsKey) { alphabet.random() }.joinToString("")
            val value = List(amountSymbolsValue) { alphabet.random() }.joinToString("")
            val exitCode = app.run(arrayOf("test.db", "ADD", key, value))
            if (exitCode == 0) {
                map[key] = value
            }
        }

        repeat(200) {
            val amountSymbolsValue = (1..10).random()
            val newValue = List(amountSymbolsValue) { alphabet.random() }.joinToString("")
            val key = map.keys.random()
            app.run(arrayOf("test.db", "UPDATE", key, newValue))
            map[key] = newValue
            assertEquals(map[key], interfaceDB.get(argsParser.parse(arrayOf("test.db", "GET", key))!!).value)
        }

        repeat(100) {
            val key = map.keys.random()
            map.remove(key)
            assertEquals(0, app.run(arrayOf("test.db", "DELETE", key)))
        }

        repeat(500) {
            val key = map.keys.random()
            assertEquals(map[key], interfaceDB.get(argsParser.parse(arrayOf("test.db", "GET", key))!!).value)
        }
        app.run(argsDrop)
    }

}
