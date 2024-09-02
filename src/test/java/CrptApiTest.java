import lombok.Data;
import lombok.NoArgsConstructor;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


public class CrptApiTest {

    private final Semaphore semaphore;
    private final DocumentSerializer documentSerializer;
    private final ApiRequester apiRequester;

    public CrptApiTest(TimeUnit timeUnit, int requestLimit) {

        // Создаем фиктивные реализации для тестирования
        this.documentSerializer = new MockDocumentSerializer();
        this.apiRequester = new MockApiRequester();
        this.semaphore = new Semaphore(requestLimit);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        long intervalMillis = timeUnit.toMillis(1);
        scheduler.scheduleAtFixedRate(() -> semaphore.release(requestLimit), 0, intervalMillis, TimeUnit.MILLISECONDS);
    }

    public boolean createDocument(Document document, String signature) {
        String jsonBody = documentSerializer.serialize(document, signature);

        if (jsonBody == null) {
            return false;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://fake.api/endpoint")) // Используем фиктивный URL
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        try {
            if (semaphore.tryAcquire(1, 1, TimeUnit.SECONDS)) {
                return apiRequester.sendRequest(request);
            } else {
                System.out.println("Request limit exceeded. Try again later.");
                return false;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
            return false;
        }
    }

    static class MockDocumentSerializer implements DocumentSerializer {
        @Override
        public String serialize(Document document, String signature) {
            // Простая имитация сериализации
            return "{\"mocked\": \"data\"}";
        }
    }

    static class MockApiRequester implements ApiRequester {
        @Override
        public boolean sendRequest(HttpRequest request) {
            // Имитируем успешный ответ API
            System.out.println("Mock request sent. Request body: " + request.bodyPublisher().orElseThrow());
            return true; // Имитируем успешный ответ (например, статус 200)
        }
    }

    @Data
    @NoArgsConstructor
    public static class Document {
        private Description description;
        private String doc_id;
        private String doc_status;
        private String doc_type;
        private boolean importRequest;
        private String owner_inn;
        private String participant_inn;
        private String producer_inn;
        private String production_date;
        private String production_type;
        private Product[] products;
        private String reg_date;
        private String reg_number;
    }

    @Data
    @NoArgsConstructor
    public static class Description {
        private String participantInn;
    }

    @Data
    @NoArgsConstructor
    public static class Product {
        private String certificate_document;
        private String certificate_document_date;
        private String certificate_document_number;
        private String owner_inn;
        private String producer_inn;
        private String production_date;
        private String tnved_code;
        private String uit_code;
        private String uitu_code;
    }

    interface DocumentSerializer {
        String serialize(Document document, String signature);
    }

    interface ApiRequester {
        boolean sendRequest(HttpRequest request) throws InterruptedException;
    }

    public static void main(String[] args) {
        CrptApiTest crptApi = new CrptApiTest(TimeUnit.SECONDS, 10);

        Document document = new Document();
        document.setDoc_id("123456");
        document.setDoc_status("NEW");
        document.setDoc_type("PRODUCTION");
        document.setOwner_inn("1234567890");
        document.setParticipant_inn("0987654321");

        String signature = "test_signature";

        boolean success = crptApi.createDocument(document, signature);
        System.out.println("Document creation " + (success ? "succeeded" : "failed"));
    }
}
