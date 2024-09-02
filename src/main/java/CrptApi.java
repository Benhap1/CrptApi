import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CrptApi {

    private static final String API_URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    private final Semaphore semaphore;
    private final DocumentSerializer documentSerializer;
    private final ApiRequester apiRequester;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {

        HttpClient httpClient = HttpClient.newHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();
        this.documentSerializer = new DefaultDocumentSerializer(objectMapper);
        this.apiRequester = new DefaultApiRequester(httpClient);
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
                .uri(URI.create(API_URL))
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

    @AllArgsConstructor
    static class DefaultDocumentSerializer implements DocumentSerializer {
        private final ObjectMapper objectMapper;

        @Override
        public String serialize(Document document, String signature) {
            try {
                ObjectNode rootNode = objectMapper.createObjectNode();
                rootNode.set("description", objectMapper.valueToTree(document.getDescription()));
                rootNode.put("doc_id", document.getDoc_id());
                rootNode.put("doc_status", document.getDoc_status());
                rootNode.put("doc_type", document.getDoc_type());
                rootNode.put("importRequest", document.isImportRequest());
                rootNode.put("owner_inn", document.getOwner_inn());
                rootNode.put("participant_inn", document.getParticipant_inn());
                rootNode.put("producer_inn", document.getProducer_inn());
                rootNode.put("production_date", document.getProduction_date());
                rootNode.put("production_type", document.getProduction_type());
                rootNode.set("products", objectMapper.valueToTree(document.getProducts()));
                rootNode.put("reg_date", document.getReg_date());
                rootNode.put("reg_number", document.getReg_number());
                rootNode.put("signature", signature);
                return objectMapper.writeValueAsString(rootNode);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }


    interface ApiRequester {
        boolean sendRequest(HttpRequest request) throws InterruptedException;
    }

    @AllArgsConstructor
    static class DefaultApiRequester implements ApiRequester {
        private final HttpClient httpClient;

        @Override
        public boolean sendRequest(HttpRequest request) throws InterruptedException {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("Response: " + response.body());
                return response.statusCode() == 200;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}




