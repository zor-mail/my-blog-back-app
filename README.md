
 # Программа "Мой блог" (серверная часть)
________________________________________________

Серверное приложение для блога «Мой блог» реализует бизнес-логику работы с постами, комментариями и изображениями, а также доступ к базе данных. В проекте предусмотрены Unit-тесты и интеграционные тесты.

Сервис предоставляет REST API для управления блогом: создание, чтение, обновление и удаление постов, работа с комментариями и изображениями.
Приложение реализует слой бизнес-логики и слой доступа к данным (DAL/Repository) поверх реляционной или документной СУБД.
В проекте настроены Unit-тесты для проверки бизнес-логики и интеграционные тесты для проверки работы API и взаимодействия с базой данных.

## Технологии
- Язык: Java
- Фреймворк: Spring Framework 7
- База данных: PostgreSQL, H2
- Система сборки проекта: Gradle 9.2+
- Тестирование: Unit-тесты (JUnit5), интеграционные тесты


### Предварительные требования
- Установленный язык и пакетный менеджер (Java 21)
- Установленная и настроенная база данных (PostgreSQL 12+)
- Установленный Tomcat 10, Docker

## Конфигурация

1. Структура рабочей БД Postgresql 18 находится в файле /src/main/resources/schema.sql,
    настройки доступа к БД в файле /src/main/resourcesapplication.yaml в параметрах:
```text
spring:
  application:
    name: my-blog-springboot
  datasource:
    url: ${DB_URL}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
  sql:
    init:
      mode: always
      schema-locations: classpath:schema.sql
      encoding: UTF-8
```
   <br>
   (Переименуйте файл application-example.yaml в application.yaml и добавьте 
   соответствующие глобальные переменные или конкретные данные)

2. Структура тестовой БД H2 находится в файле /src/test/resources/schema.sql,
   настройки доступа к БД в файле /src/test/resources/application.yaml в параметрах:
```text
spring:
  application:
    name: my-blog-springboot
  datasource:
    url: jdbc:h2:mem:myblogdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    username: sa
    password:
    driver-class-name: org.h2.Driver
  sql:
    init:
      mode: embedded
      platform: h2
      schema-locations: classpath:schema.sql
      encoding: UTF-8
```

3. Требуемые внешние сервисы:
    - PostgreSQL 18 (по умолчанию `localhost:5432`, база `myblogdb`)
    - Установленное через Docker-контейнер клиентское приложение (`localhost:80)

## Структура проекта

```text
my-blog-back-up
├── build.gradle
├── README.md
└── src
    ├── main
    │   └── java
    │       └── ru.yandex.practica
    │           ├── controllers/    # Контроллеры
    │           ├── services/       # Бизнес-логика
    │           ├── repositories/   # Доступ к БД
    │           ├── models/         # Модели домена
    │           └── config/         # Конфигурация
    └── test
        └── java
            └── ru.yandex.practica
                ├── integration/
                ├── module_tests/
                └── testconfig/
```

## Тестирование
Для запуска Unit‑тестов и интеграционных тестов:
```text
./gradlew test
```

## Установка и запуск в Tomcat
Запустить сборку
```text
./gradlew bootJar
```
Затем запустить на встроенном Tomcat
```text
 java -jar build/libs/api.jar
```