package io.deeplumen.ucp.samples.java.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.deeplumen.ucp.models.discovery.PaymentHandlerResponse;
import io.deeplumen.ucp.models.schemas.shopping.CheckoutCreateRequest;
import io.deeplumen.ucp.models.schemas.shopping.CheckoutResponse;
import io.deeplumen.ucp.models.schemas.shopping.CheckoutUpdateRequest;
import io.deeplumen.ucp.models.schemas.shopping.ItemResponse;
import io.deeplumen.ucp.models.schemas.shopping.LineItemCreateRequest;
import io.deeplumen.ucp.models.schemas.shopping.LineItemResponse;
import io.deeplumen.ucp.models.schemas.shopping.PaymentResponse;
import io.deeplumen.ucp.models.schemas.shopping.TotalResponse;
import io.deeplumen.ucp.models.schemas.shopping.TotalResponse.Type;
import io.deeplumen.ucp.models.schemas.shopping.UCPCheckoutResponse;
import io.deeplumen.ucp.samples.java.data.DataLoader;
import io.deeplumen.ucp.samples.java.data.Product;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CheckoutService {
  private final Map<String, CheckoutSession> sessions = new HashMap<>();
  private final Map<String, Product> products;
  private final ObjectMapper mapper;

  private static final String PROTOCOL_VERSION = "2026-01-11";

  public CheckoutService(DataLoader loader, ObjectMapper mapper) {
    this.products = new HashMap<>(loader.getProducts());
    this.mapper = mapper;
  }

  public CheckoutResponse createCheckout(JsonNode platformProfile, CheckoutCreateRequest req) {
    String id = "chk_" + UUID.randomUUID().toString().substring(0, 8);
    List<LineItemResponse> lineItems =
        buildLineItems(req != null ? req.getLineItems() : null);
    PaymentResponse payment = buildPaymentSection();
    CheckoutSession session =
        new CheckoutSession(id, lineItems, payment, CheckoutResponse.Status.INCOMPLETE);
    sessions.put(id, session);
    return toResponse(session, platformProfile);
  }

  public CheckoutResponse getCheckout(String checkoutId, JsonNode platformProfile) {
    CheckoutSession session = sessions.get(checkoutId);
    if (session == null) {
      throw new IllegalArgumentException("checkout not found");
    }
    return toResponse(session, platformProfile);
  }

  public CheckoutResponse updateCheckout(
      String checkoutId, JsonNode platformProfile, CheckoutUpdateRequest req) {
    CheckoutSession session = sessions.get(checkoutId);
    if (session == null) {
      throw new IllegalArgumentException("checkout not found");
    }
    if (req != null && req.getLineItems() != null && !req.getLineItems().isEmpty()) {
      session.lineItems = buildLineItemsFromUpdate(req.getLineItems());
    }
    session.status = CheckoutResponse.Status.READY_FOR_COMPLETE;
    return toResponse(session, platformProfile);
  }

  public CheckoutResponse cancelCheckout(String checkoutId, JsonNode platformProfile) {
    CheckoutSession session = sessions.get(checkoutId);
    if (session == null) throw new IllegalArgumentException("checkout not found");
    session.status = CheckoutResponse.Status.CANCELED;
    return toResponse(session, platformProfile);
  }

  public CheckoutResponse mintInstrument(String checkoutId, JsonNode platformProfile) {
    CheckoutSession session = sessions.get(checkoutId);
    if (session == null) throw new IllegalArgumentException("checkout not found");
    Map<String, Object> instrument = new HashMap<>();
    instrument.put("id", "inst_" + checkoutId);
    instrument.put("handler_id", "mock_payment_handler");
    instrument.put("type", "card");
    instrument.put("brand", "visa");
    instrument.put("last_digits", "4242");
    instrument.put("credential", Map.of("type", "token", "token", "success_token"));
    if (session.payment.getInstruments() == null) {
      session.payment.setInstruments(new ArrayList<>());
    }
    session.payment.getInstruments().clear();
    session.payment.getInstruments().add(mapper.convertValue(instrument, Object.class));
    session.payment.setSelectedInstrumentId("inst_" + checkoutId);
    session.status = CheckoutResponse.Status.READY_FOR_COMPLETE;
    return toResponse(session, platformProfile);
  }

  public CheckoutResponse completeCheckout(String checkoutId, JsonNode paymentPayload) {
    CheckoutSession session = sessions.get(checkoutId);
    if (session == null) throw new IllegalArgumentException("checkout not found");
    session.status = CheckoutResponse.Status.COMPLETED;

    if (paymentPayload != null && paymentPayload.has("payment_data")) {
      JsonNode paymentData = paymentPayload.get("payment_data");
      if (paymentData.has("id")) {
        session.payment.setSelectedInstrumentId(paymentData.get("id").asText());
      }
      if (session.payment.getInstruments() == null) {
        session.payment.setInstruments(new ArrayList<>());
      }
      session.payment.getInstruments().clear();
      session.payment.getInstruments().add(mapper.convertValue(paymentData, Object.class));
    }

    CheckoutResponse resp = toResponse(session, mapper.createObjectNode());
    resp.setStatus(CheckoutResponse.Status.COMPLETED);
    var order = new io.deeplumen.ucp.models.schemas.shopping.OrderConfirmation();
    String orderId = "order_" + checkoutId;
    order.setId(orderId);
    order.setPermalinkUrl(URI.create("https://example.com/orders/" + orderId));
    resp.setOrder(order);
    return resp;
  }

  private CheckoutResponse toResponse(CheckoutSession session, JsonNode platformProfile) {
    CheckoutResponse resp = new CheckoutResponse();
    resp.setId(session.id);
    resp.setStatus(session.status);
    resp.setCurrency("USD");
    resp.setLineItems(session.lineItems);
    resp.setTotals(buildTotals(session.lineItems));
    resp.setPayment(session.payment);
    resp.setUcp(buildUcpMeta());
    resp.setContinueUrl(URI.create("https://example.com/continue/" + session.id));
    resp.setExpiresAt(Date.from(Instant.now().plusSeconds(3600)));
    return resp;
  }

  private UCPCheckoutResponse buildUcpMeta() {
    UCPCheckoutResponse ucpMeta = new UCPCheckoutResponse();
    ucpMeta.setVersion(PROTOCOL_VERSION);
    ucpMeta.setCapabilities(capabilitiesForResponse());
    return ucpMeta;
  }

  private List<Object> capabilitiesForResponse() {
    return List.of(
        Map.of("name", "dev.ucp.shopping.checkout", "version", PROTOCOL_VERSION),
        Map.of("name", "dev.ucp.shopping.fulfillment", "version", PROTOCOL_VERSION));
  }

  private PaymentResponse buildPaymentSection() {
    PaymentResponse payment = new PaymentResponse();
    PaymentHandlerResponse handler = new PaymentHandlerResponse();
    handler.setId("mock_payment_handler");
    handler.setName("dev.ucp.mock_payment");
    handler.setVersion(PROTOCOL_VERSION);
    handler.setSpec(URI.create("https://ucp.dev/specs/mock"));
    handler.setConfigSchema(URI.create("https://ucp.dev/schemas/mock.json"));
    handler.setInstrumentSchemas(
        List.of(URI.create("https://ucp.dev/schemas/shopping/types/card_payment_instrument.json")));
    var config = new io.deeplumen.ucp.models.discovery.Config();
    config.setAdditionalProperty("supported_tokens", List.of("success_token", "fail_token"));
    handler.setConfig(config);
    payment.setHandlers(List.of(handler));
    return payment;
  }

  private List<LineItemResponse> buildLineItems(List<LineItemCreateRequest> reqItems) {
    List<LineItemResponse> result = new ArrayList<>();
    if (reqItems == null || reqItems.isEmpty()) {
      Product p = products.values().stream().findFirst().orElseThrow();
      result.add(buildLineItem("item_1", p, 1));
      return result;
    }
    int idx = 1;
    for (LineItemCreateRequest req : reqItems) {
      String productId =
          req.getItem() != null
              ? req.getItem().getId()
              : products.keySet().stream().findFirst().orElse(null);
      Product p = resolveProduct(productId);
      int qty = req.getQuantity() != null ? req.getQuantity() : 1;
      result.add(buildLineItem("item_" + idx++, p, qty));
    }
    return result;
  }

  private List<LineItemResponse> buildLineItemsFromUpdate(
      List<io.deeplumen.ucp.models.schemas.shopping.LineItemUpdateRequest> reqItems) {
    List<LineItemResponse> result = new ArrayList<>();
    int idx = 1;
    for (var req : reqItems) {
      String productId =
          req.getItem() != null
              ? req.getItem().getId()
              : products.keySet().stream().findFirst().orElse(null);
      Product p = resolveProduct(productId);
      int qty = req.getQuantity() != null ? req.getQuantity() : 1;
      result.add(buildLineItem("item_" + idx++, p, qty));
    }
    return result.isEmpty() ? buildLineItems(null) : result;
  }

  private LineItemResponse buildLineItem(String id, Product p, int qty) {
    LineItemResponse li = new LineItemResponse();
    li.setId(id);
    ItemResponse item = new ItemResponse();
    item.setId(p.id());
    item.setTitle(p.title());
    item.setPrice(p.priceCents());
    if (p.imageUrl() != null) {
      item.setImageUrl(p.imageUrl());
    }
    li.setItem(item);
    li.setQuantity(qty);
    li.setTotals(buildLineTotals(p.priceCents() * qty));
    return li;
  }

  private List<TotalResponse> buildLineTotals(int amount) {
    TotalResponse subtotal = new TotalResponse();
    subtotal.setType(Type.SUBTOTAL);
    subtotal.setAmount(amount);
    TotalResponse total = new TotalResponse();
    total.setType(Type.TOTAL);
    total.setAmount(amount);
    return List.of(subtotal, total);
  }

  private List<TotalResponse> buildTotals(List<LineItemResponse> lineItems) {
    int subtotalAmount =
        lineItems.stream()
            .mapToInt(
                li ->
                    li.getTotals().stream()
                        .filter(t -> t.getType() == Type.TOTAL)
                        .mapToInt(TotalResponse::getAmount)
                        .findFirst()
                        .orElse(0))
            .sum();
    int shipping = 599;
    TotalResponse subtotal = new TotalResponse();
    subtotal.setType(Type.SUBTOTAL);
    subtotal.setAmount(subtotalAmount);
    TotalResponse fulfillment = new TotalResponse();
    fulfillment.setType(Type.FULFILLMENT);
    fulfillment.setDisplayText("Standard Shipping");
    fulfillment.setAmount(shipping);
    TotalResponse total = new TotalResponse();
    total.setType(Type.TOTAL);
    total.setAmount(subtotalAmount + shipping);
    return List.of(subtotal, fulfillment, total);
  }

  private Product resolveProduct(String productId) {
    if (productId == null || productId.isBlank()) {
      return products.values().stream().findFirst().orElseThrow();
    }
    Product product = products.get(productId);
    if (product != null) {
      return product;
    }
    Product seed = products.values().stream().findFirst().orElse(null);
    int price = seed != null ? seed.priceCents() : 1000;
    String currency = seed != null ? seed.currency() : "USD";
    Product fallback =
        new Product(productId, "Item " + productId, price, currency, null, "misc");
    products.put(productId, fallback);
    return fallback;
  }

  private static class CheckoutSession {
    final String id;
    List<LineItemResponse> lineItems;
    PaymentResponse payment;
    CheckoutResponse.Status status;

    CheckoutSession(
        String id,
        List<LineItemResponse> lineItems,
        PaymentResponse payment,
        CheckoutResponse.Status status) {
      this.id = id;
      this.lineItems = lineItems;
      this.payment = payment;
      this.status = status;
    }
  }
}
