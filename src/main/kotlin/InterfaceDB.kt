import data.Arguments
import data.Command.CREATE
import data.Command.DROP
import data.Command.ADD
import data.Command.UPDATE
import data.Command.GET
import data.Command.DELETE
import data.ExitCode.SUCCESS
import data.ExitCode.INVALID_DB
import data.ExitCode.INVALID_KEY
import data.ExitCode.INVALID_STRING
import data.ExitCode.KEY_SIZE_EXCEEDED
import data.ExitCode.KEY_ALREADY_EXISTS
import data.ExitCode.DB_ALREADY_EXISTS
import data.ResponseBinarySearch
import data.ResponseDB
import java.io.File
import java.io.RandomAccessFile
import java.util.Queue
import java.util.LinkedList

class InterfaceDB {
    private val sizeFieldKey = 32
    private val sizeFieldLinkValue = 32
    private val sizeField = sizeFieldKey + sizeFieldLinkValue

    private val queueCommands: Queue<Arguments> = LinkedList()

    private val nameDBValues = "values.db"

    /**
     * Бинарный поиск по индексированному файлу
     * Каждое поле состоит из одного количества информации, тем самым
     * итерация идет по каждому такому полю, в случае нахождения искомого ключа -
     * вывод пары с номером байта, если же ключ не найден -
     * вывод номера байта для вставки нового ключа
     */

    internal fun binarySearch(fileDB: RandomAccessFile, keySearch: String): ResponseBinarySearch {
        var left = -1L
        var medium: Long
        var right = fileDB.length() / sizeField
        var key: String
        while (right - left > 1) {
            medium = (right + left) / 2
            key = getKeyByFirstByte(fileDB, medium * sizeField)
            if (key < keySearch) {
                left = medium
            } else if (key > keySearch) {
                right = medium
            } else {
                return ResponseBinarySearch(medium * sizeField, true)
            }
        }
        return ResponseBinarySearch(right * sizeField, false)
    }

    fun run(): List<ResponseDB> {
        val responsesDB: MutableList<ResponseDB> = mutableListOf()
        while (!queueCommands.isEmpty()) {
            responsesDB.add(processRequest(queueCommands.poll()))
        }
        return responsesDB
    }

    fun addRequest(args: Arguments) = queueCommands.add(args)

    private fun processRequest(args: Arguments): ResponseDB {
        val existDB = checkExistDB(args.db)
        if (args.command != CREATE && !existDB) {
            return args.toResponseDB(INVALID_DB)
        }

        if (!args.key.isNullOrBlank()) {
            if (checkSizeKey(args.key)) {
                return args.toResponseDB(KEY_SIZE_EXCEEDED)
            }

            if (checkNullByte(args.key) || (!args.value.isNullOrBlank() && checkNullByte(args.value))) {
                return args.toResponseDB(INVALID_STRING)
            }
        }

        if (existDB && args.command == CREATE) {
            return args.toResponseDB(DB_ALREADY_EXISTS)
        }

        return when (args.command) {
            CREATE -> create(args)
            DROP -> drop(args)
            ADD -> add(args)
            GET -> get(args)
            UPDATE -> update(args)
            DELETE -> delete(args)
        }
    }

    private fun create(args: Arguments): ResponseDB {
        File(args.db).createNewFile()
        return args.toResponseDB(SUCCESS)
    }

    private fun drop(args: Arguments): ResponseDB {
        File(args.db).delete()
        val valuesDB = File(nameDBValues)
        if (valuesDB.exists()) {
            valuesDB.delete()
        }
        return args.toResponseDB(SUCCESS)
    }

    internal fun add(args: Arguments): ResponseDB {
        val fileDB = RandomAccessFile(args.db, "rws")
        val (numberFirstByteFieldSearch, isSearched) = binarySearch(fileDB, args.key!!)
        if (isSearched) {
            return args.toResponseDB(KEY_ALREADY_EXISTS)
        }
        val linkValue = addValueToDB(args.value!!).toString()
        addFieldToDB(fileDB, numberFirstByteFieldSearch, args.key, linkValue)
        fileDB.close()
        return args.toResponseDB(SUCCESS)
    }

    internal fun get(args: Arguments): ResponseDB {
        val fileDB = RandomAccessFile(args.db, "r")
        val (numberFirstByteFieldSearch, isSearched) = binarySearch(fileDB, args.key!!)
        if (!isSearched) {
            return args.toResponseDB(INVALID_KEY)
        }
        val linkValue = getLinkValueByFirstByte(fileDB, numberFirstByteFieldSearch)
        fileDB.close()
        return args.toResponseDB(value = getValueByFirstByte(linkValue), exitCode = SUCCESS)
    }

    internal fun update(args: Arguments): ResponseDB {
        val fileDB = RandomAccessFile(args.db, "rws")
        val (numberFirstByteFieldSearch, isSearched) = binarySearch(fileDB, args.key!!)
        if (!isSearched) {
            return args.toResponseDB(INVALID_KEY)
        }
        val newLinkValue = addValueToDB(args.value!!).toString()
        updateLinkValue(fileDB, numberFirstByteFieldSearch, newLinkValue)
        fileDB.close()
        return args.toResponseDB(exitCode = SUCCESS)
    }

    internal fun delete(args: Arguments): ResponseDB {
        val fileDB = RandomAccessFile(args.db, "rws")
        val (numberFirstByteFieldSearch, isSearched) = binarySearch(fileDB, args.key!!)
        if (!isSearched) {
            return args.toResponseDB(INVALID_KEY)
        }
        deleteFieldToDB(fileDB, numberFirstByteFieldSearch)
        fileDB.close()
        return args.toResponseDB(SUCCESS)
    }

    private fun checkExistDB(db: String) = File(db).exists()

    private fun checkSizeKey(key: String) = key.toByteArray().size > sizeFieldKey

    private fun checkNullByte(str: String) = str.contains("0x00")

    private fun addFieldToDB(fileDB: RandomAccessFile, numberFirstByte: Long, key: String, linkValue: String) {
        val newField = transformByteArrayField(key, linkValue)
        fileDB.seek(numberFirstByte)
        val prevFields = ByteArray((fileDB.length() - numberFirstByte).toInt())
        fileDB.read(prevFields)
        fileDB.seek(numberFirstByte)
        fileDB.write(newField)
        fileDB.write(prevFields)
    }

    private fun deleteFieldToDB(fileDB: RandomAccessFile, numberFirstByte: Long) {
        val prevFields = ByteArray((fileDB.length() - (sizeFieldKey + numberFirstByte)).toInt())
        fileDB.seek(numberFirstByte + sizeField)
        fileDB.read(prevFields)
        fileDB.seek(numberFirstByte)
        fileDB.write(prevFields)
        fileDB.setLength(fileDB.length() - sizeField)
    }

    private fun updateLinkValue(fileDB: RandomAccessFile, numberFirstByte: Long, linkValue: String) {
        fileDB.seek(numberFirstByte + sizeFieldKey)
        fileDB.write(transformByteArrayLinkValue(linkValue))
    }

    private fun addValueToDB(value: String): Long {
        val fileDBValues = RandomAccessFile(nameDBValues, "rws")
        val sizeDB = fileDBValues.length()
        fileDBValues.seek(sizeDB)
        fileDBValues.writeBytes(value)
        fileDBValues.write(0x00)
        return sizeDB
    }

    private fun getValueByFirstByte(linkValue: Long): String {
        val fileDBValues = RandomAccessFile(nameDBValues, "r")
        fileDBValues.seek(linkValue)
        val valueList = mutableListOf<Byte>()
        var valuePart = fileDBValues.read()
        while (valuePart != -1 && valuePart != 0x00) {
            valueList.add(valuePart.toByte())
            valuePart = fileDBValues.read()
        }
        return valueList.toByteArray().toString(Charsets.UTF_8)
    }

    private fun getKeyByFirstByte(fileDB: RandomAccessFile, numberFirstByte: Long): String {
        var arrayKey = ByteArray(sizeFieldKey)
        fileDB.seek(numberFirstByte)
        fileDB.read(arrayKey)
        val index = arrayKey.indexOf(0.toByte())
        if (index != -1) {
            arrayKey = arrayKey.sliceArray(0 until index)
        }
        return arrayKey.toString(Charsets.UTF_8)
    }

    internal fun getLinkValueByFirstByte(fileDB: RandomAccessFile, numberFirstByte: Long): Long {
        var arrayLinkValue = ByteArray(sizeFieldLinkValue)
        fileDB.seek(numberFirstByte + sizeFieldKey)
        fileDB.read(arrayLinkValue)
        val index = arrayLinkValue.indexOf(0.toByte())
        if (index != -1) {
            arrayLinkValue = arrayLinkValue.sliceArray(0 until index)
        }
        return arrayLinkValue.toString(Charsets.UTF_8).toLong()
    }

    private fun transformByteArrayLinkValue(element: String): ByteArray {
        val resultArray = ByteArray(sizeFieldLinkValue) { 0x00 }
        for (i in element.indices) {
            resultArray[i] = element[i].code.toByte()
        }
        return resultArray
    }

    internal fun transformByteArrayField(key: String, linkValue: String): ByteArray {
        val resultArray = ByteArray(sizeFieldKey + sizeFieldLinkValue) { 0x00 }
        for (i in key.indices) {
            resultArray[i] = key[i].code.toByte()
        }
        for (i in linkValue.indices) {
            resultArray[i + sizeFieldKey] = linkValue[i].code.toByte()
        }
        return resultArray
    }
}