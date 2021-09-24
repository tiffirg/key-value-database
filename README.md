# Курс основ программирования на МКН СПбГУ

## Проект 2: key-value база данных

[Постановка задачи](./TASK.md)

# Commands

| Команда    | Описание                |
| ---------- | ---------------------   |
|` CREATE`   | Создание базы данных    |
| `DROP`     | Удаление базы данных    |
| `ADD`      | Вставка данных          |
| `GET`      | Получение данных        |
| `UPDATE`   | Изменение данных        |
| `DELETE`   | Удаление данных         |
| `BATCH`    | Выполнить команды батча |


# Run & Test Application

## Run

### Операции `CREATE` и `DROP`
```
main.kt <path db> <command>
```

### Операции `ADD` и `UPDATE`
```
main.kt <path db> <command> <key> <value>
```

### Операции `GET` и `DELETE`
```
main.kt <path db> <command> <key>
```

### Операция `BATCH`
```
main.kt <path db> <command> <path batch>
```

## Test

Запустить скрипт `Tests.kt`

### Exit codes

| Название        | Exit code | Описание                   |
| ----------      | --------- | -------------------------- |
| SUCCESS         | 0         | :)                         |
| HELP            | 1         | Неправильный ввод          |
| INVALID_DB      | 2         | Неправильный путь к БД     |
| INVALID_BATCH   | 3         | Неправильный путь к батчу  |
| INVALID_KEY     | 4         | Ключ отсутствует           |
