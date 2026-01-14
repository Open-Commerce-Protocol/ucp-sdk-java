package io.deeplumen.ucp.samples.java.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.deeplumen.ucp.models.schemas.shopping.CheckoutCreateRequest;
import io.deeplumen.ucp.models.schemas.shopping.CheckoutResponse;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

/**
 * Minimal client to demonstrate discovery -> create checkout -> complete checkout flow.
 */
public class SimpleClient {

  public static void main(String[] args) throws Exception {
    String baseUrl =
        System.getenv().getOrDefault("UCP_SAMPLE_BASEURL", "http://localhost:8080");
    String profileUrl =
        System.getenv()
            .getOrDefault("UCP_PROFILE_URL", baseUrl + "/profiles/platform.json");
    String version = System.getenv().getOrDefault("UCP_AGENT_VERSION", "2026-01-11");
    String apiKey = System.getenv().getOrDefault("UCP_API_KEY", "test");
    String itemId1 = System.getenv().getOrDefault("UCP_SAMPLE_ITEM_ID", "bouquet_roses");
    ObjectMapper mapper = new ObjectMapper();
    HttpClient httpClient = HttpClient.newHttpClient();

    // 1) Discovery
    HttpRequest discoveryReq =
        HttpRequest.newBuilder(URI.create(baseUrl + "/.well-known/ucp")).GET().build();
    HttpResponse<String> discoveryResp =
        httpClient.send(discoveryReq, HttpResponse.BodyHandlers.ofString());
    System.out.println("Discovery status: " + discoveryResp.statusCode());
    System.out.println("Discovery profile:\n" + discoveryResp.body());
    JsonNode discoveryJson = mapper.readTree(discoveryResp.body());
    ArrayNode handlers =
        discoveryJson.path("payment").path("handlers").isArray()
            ? (ArrayNode) discoveryJson.path("payment").path("handlers")
            : mapper.createArrayNode();
    String handlerId =
        handlers.size() > 0
            ? handlers.get(0).path("id").asText()
            : System.getenv().getOrDefault("UCP_HANDLER_ID", "mock_payment_handler");

    // 2) Create checkout
    ObjectNode createPayload = mapper.createObjectNode();
    createPayload.put("currency", "USD");
    ArrayNode lineItems = mapper.createArrayNode();
    ObjectNode lineItem1 = mapper.createObjectNode();
    lineItem1.set("item", mapper.createObjectNode().put("id", itemId1));
    lineItem1.put("quantity", 1);
    lineItems.add(lineItem1);
    createPayload.set("line_items", lineItems);
    ObjectNode payment = mapper.createObjectNode();
    payment.set("handlers", handlers);
    payment.set("instruments", mapper.createArrayNode());
    createPayload.set("payment", payment);
    createPayload.set(
        "buyer", mapper.createObjectNode().put("full_name", "Java Client").put("email", "java@example.com"));

    CheckoutCreateRequest createRequest =
        mapper.convertValue(createPayload, CheckoutCreateRequest.class);
    CheckoutResponse checkout =
        postJson(
            httpClient,
            mapper,
            URI.create(baseUrl + "/checkout-sessions"),
            createRequest,
            201,
            profileUrl,
            version,
            apiKey,
            CheckoutResponse.class);
    System.out.println("Checkout id: " + checkout.getId());
    System.out.println("Checkout response:\n" + mapper.writeValueAsString(checkout));

    // 3) Complete checkout (pay)
    ObjectNode instrument = mapper.createObjectNode();
    instrument.put("id", "instr_" + UUID.randomUUID().toString().substring(0, 8));
    instrument.put("handler_id", handlerId);
    instrument.put("type", "card");
    instrument.put("brand", "Visa");
    instrument.put("last_digits", "4242");
    instrument.set("credential", mapper.createObjectNode().put("type", "token").put("token", "success_token"));
    ObjectNode completePayload = mapper.createObjectNode();
    completePayload.set("payment_data", instrument);
    completePayload.set(
        "risk_signals",
        mapper
            .createObjectNode()
            .put("ip", "127.0.0.1")
            .put("client", "java")
            .put("timestamp", Instant.now().toString()));
    CheckoutResponse paid =
        postJson(
            httpClient,
            mapper,
            URI.create(baseUrl + "/checkout-sessions/" + checkout.getId() + "/complete"),
            completePayload,
            200,
            profileUrl,
            version,
            apiKey,
            CheckoutResponse.class);
    System.out.println("Payment success: " + (paid.getStatus() == CheckoutResponse.Status.COMPLETED));
    System.out.println("Payment response:\n" + mapper.writeValueAsString(paid));
  }

  private static <T> T postJson(
      HttpClient httpClient,
      ObjectMapper mapper,
      URI uri,
      Object payload,
      int expectedStatus,
      String profileUrl,
      String version,
      String apiKey,
      Class<T> responseType)
      throws Exception {
    String json = payload == null ? "{}" : mapper.writeValueAsString(payload);
    HttpRequest request =
        HttpRequest.newBuilder(uri)
            .header("Content-Type", "application/json")
            .header("UCP-Agent", "profile=\"" + profileUrl + "\"; version=\"" + version + "\"")
            .header("Request-Signature", "test")
            .header("Idempotency-Key", UUID.randomUUID().toString())
            .header("Request-Id", UUID.randomUUID().toString())
            .header("X-API-Key", apiKey)
            .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
            .build();
    HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    if (resp.statusCode() != expectedStatus) {
      throw new IllegalStateException("Unexpected status " + resp.statusCode() + ": " + resp.body());
    }
    return mapper.readValue(resp.body(), responseType);
  }
}
