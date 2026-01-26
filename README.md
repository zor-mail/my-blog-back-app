
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
- Система сборки проекта: Apache Maven 3.9+
- Тестирование: Unit-тесты (JUnit5), интеграционные тесты


### Предварительные требования
- Установленный язык и пакетный менеджер (Java 21)
- Установленная и настроенная база данных (PostgreSQL 12+)
- Установленный Tomcat 10, Docker

## Конфигурация

1. Структура рабочей БД Postgresql 18 находится в файле /src/main/resources/schema.sql,
    настройки доступа к БД в файле /src/main/resources/application.yaml в параметрах:
   - spring.datasource.url
   - spring.datasource.username
   - spring.datasource.password
   - spring.datasource.driver-class-name
   <br>
   (Переименуйте файл application.example.yaml в application.yaml и добавьте 
   соответствующие глобальные переменные или конкретные данные)

2. Структура тестовой БД H2 находится в файле /src/test/resources/schema.sql,
   настройки доступа к БД в файле /src/test/resources/application.yaml в параметрах:
    - spring.datasource.url
    - spring.datasource.username
    - spring.datasource.password
    - spring.datasource.driver-class-name

3. Требуемые внешние сервисы:
    - PostgreSQL 18 (по умолчанию `localhost:5432`, база `myblogdb`)
    - Установленное через Docker-контейнер клиентское приложение (`localhost:80)

## Структура проекта

```text
my-blog-back-up
├── pom.xml
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
mvn test
```

## Установка и запуск в Tomcat
Добавьте в файл [Путь к Tomcat]/conf/tomcat-users.xml
роли и пользователя для деплоя файла war в директорию webapps.

```text
<role rolename="manager-gui"/>
<role rolename="manager-script"/>
<user username="robot" password="robot" roles="manager-gui,manager-script"/>`
```
Если будут использованы другие username и password, 
измените их также в файле pom.xml 
в соответствующих xml-node плагина tomcat7-maven-plugin

Деплой в Tomcat происходит на фазе install жизненного цикла Maven 
```text
mvn clean install
```