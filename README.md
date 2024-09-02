# CrptApi

`CrptApi` — это класс, предназначенный для взаимодействия с API сервиса, который позволяет создавать документы, отправляя запросы в формате JSON.

## Основная структура

### CrptApi

`CrptApi` — основной класс, который предоставляет метод `createDocument` для создания документа и отправки его на сервер через API.

**Основные компоненты класса:**

- **Semaphore** — ограничивает количество одновременных запросов к API, чтобы избежать превышения лимита.
- **DocumentSerializer** — интерфейс для сериализации объектов `Document` в формат JSON.
- **ApiRequester** — интерфейс для отправки HTTP-запросов.

### Внутренние классы

#### Document

`Document` — это внутренний статический класс, представляющий документ, который будет отправлен через API.

**Поля:**

- `Description description`
- `String doc_id`
- `String doc_status`
- `String doc_type`
- `boolean importRequest`
- `String owner_inn`
- `String participant_inn`
- `String producer_inn`
- `String production_date`
- `String production_type`
- `Product[] products`
- `String reg_date`
- `String reg_number`

#### Description

`Description` — внутренний статический класс, который описывает поле `description` в документе.

**Поля:**

- `String participantInn`

#### Product

`Product` — внутренний статический класс, представляющий продукт, связанный с документом.

**Поля:**

- `String certificate_document`
- `String certificate_document_date`
- `String certificate_document_number`
- `String owner_inn`
- `String producer_inn`
- `String production_date`
- `String tnved_code`
- `String uit_code`
- `String uitu_code`

### Интерфейсы

#### DocumentSerializer

`DocumentSerializer` — интерфейс для сериализации объекта `Document` в JSON-строку.

**Метод:**

- `String serialize(Document document, String signature);`

#### ApiRequester

`ApiRequester` — интерфейс для отправки HTTP-запросов.

**Метод:**

- `boolean sendRequest(HttpRequest request) throws InterruptedException;`

### Реализации интерфейсов

#### DefaultDocumentSerializer

Реализация интерфейса `DocumentSerializer` на основе библиотеки Jackson, которая сериализует объект `Document` в JSON.

#### DefaultApiRequester

Реализация интерфейса `ApiRequester`, которая использует `HttpClient` для отправки HTTP-запросов и обработки ответа.

### Тестовый класс

### CrptApiTest

`CrptApiTest` — тестовый класс, который демонстрирует использование класса `CrptApi` с фиктивными реализациями интерфейсов. Этот класс нужен для проверки работоспособности логики без необходимости подключения к реальному API.

## Зачем нужны интерфейсы?

### Интерфейсы

Интерфейсы (`DocumentSerializer` и `ApiRequester`) обеспечивают гибкость и расширяемость кода. 


## Лицензия

Этот проект создан исключительно в образовательных целях.
