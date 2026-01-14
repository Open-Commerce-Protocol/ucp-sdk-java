package io.deeplumen.ucp.samples.java.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.deeplumen.ucp.models.schemas.shopping.CheckoutCreateRequest;
import io.deeplumen.ucp.models.schemas.shopping.CheckoutResponse;
import io.deeplumen.ucp.models.schemas.shopping.CheckoutUpdateRequest;
import io.deeplumen.ucp.samples.java.service.CheckoutService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class UcpController {

  private static final Pattern PROFILE_PATTERN =
      Pattern.compile("profile=\"([^\"]+)\"|profile=([^;]+)");

  private final CheckoutService checkoutService;
  private final ObjectMapper mapper;

  public UcpController(CheckoutService checkoutService, ObjectMapper mapper) {
    this.checkoutService = checkoutService;
    this.mapper = mapper;
  }

  @GetMapping("/.well-known/ucp")
  public ResponseEntity<JsonNode> discovery(HttpServletRequest request) throws IOException {
    JsonNode profile =
        mapper.readTree(new ClassPathResource("discovery_profile.json").getInputStream());
    if (profile.has("ucp")) {
      String base =
          request.getScheme()
              + "://"
              + request.getServerName()
              + (request.getServerPort() == 80 || request.getServerPort() == 443
                  ? ""
                  : ":" + request.getServerPort());
      ((com.fasterxml.jackson.databind.node.ObjectNode)
              profile.path("ucp").path("services").path("dev.ucp.shopping").path("rest"))
          .put("endpoint", base);
    }
    return ResponseEntity.ok(profile);
  }

  @GetMapping("/profiles/platform.json")
  public ResponseEntity<JsonNode> platformProfile() throws IOException {
    JsonNode profile =
        mapper.readTree(new ClassPathResource("platform_profile.json").getInputStream());
    return ResponseEntity.ok(profile);
  }

  @PostMapping("/ucp/negotiation")
  public ResponseEntity<JsonNode> negotiate(@RequestBody JsonNode platformProfile)
      throws IOException {
    JsonNode business =
        mapper.readTree(new ClassPathResource("discovery_profile.json").getInputStream());
    JsonNode businessCaps = business.path("ucp").path("capabilities");
    JsonNode platformCaps = platformProfile.path("ucp").path("capabilities");
    com.fasterxml.jackson.databind.node.ArrayNode intersection = mapper.createArrayNode();
    for (JsonNode b : businessCaps) {
      for (JsonNode p : platformCaps) {
        if (b.path("name").asText().equals(p.path("name").asText())) {
          intersection.add(b);
        }
      }
    }
    return ResponseEntity.ok(mapper.createObjectNode().set("capabilities", intersection));
  }

  @PostMapping("/checkout-sessions")
  public ResponseEntity<CheckoutResponse> createCheckout(
      @RequestHeader(value = "UCP-Agent", required = false) String ucpAgent,
      @RequestHeader(value = "Request-Signature", required = false) String requestSignature,
      @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
      @RequestHeader(value = "Request-Id", required = false) String requestId,
      @RequestHeader(value = "X-API-Key", required = false) String apiKey,
      @RequestBody(required = false) JsonNode body)
      throws IOException {
    JsonNode platformProfile = extractPlatformProfile(ucpAgent, body);
    CheckoutCreateRequest req =
        body != null ? mapper.convertValue(body, CheckoutCreateRequest.class) : null;
    CheckoutResponse response = checkoutService.createCheckout(platformProfile, req);
    return ResponseEntity.status(201).body(response);
  }

  @GetMapping("/checkout-sessions/{checkoutId}")
  public ResponseEntity<CheckoutResponse> getCheckout(
      @PathVariable("checkoutId") String checkoutId,
      @RequestHeader(value = "UCP-Agent", required = false) String ucpAgent)
      throws IOException {
    try {
      JsonNode platformProfile = extractPlatformProfile(ucpAgent, null);
      return ResponseEntity.ok(checkoutService.getCheckout(checkoutId, platformProfile));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @PutMapping("/checkout-sessions/{checkoutId}")
  public ResponseEntity<CheckoutResponse> updateCheckout(
      @PathVariable("checkoutId") String checkoutId,
      @RequestHeader(value = "UCP-Agent", required = false) String ucpAgent,
      @RequestHeader(value = "Request-Signature", required = false) String requestSignature,
      @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
      @RequestHeader(value = "Request-Id", required = false) String requestId,
      @RequestHeader(value = "X-API-Key", required = false) String apiKey,
      @RequestBody(required = false) JsonNode body)
      throws IOException {
    try {
      JsonNode platformProfile = extractPlatformProfile(ucpAgent, body);
      CheckoutUpdateRequest req =
          body != null ? mapper.convertValue(body, CheckoutUpdateRequest.class) : null;
      return ResponseEntity.ok(
          checkoutService.updateCheckout(checkoutId, platformProfile, req));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/checkout-sessions/{checkoutId}/complete")
  public ResponseEntity<CheckoutResponse> completeCheckout(
      @PathVariable("checkoutId") String checkoutId,
      @RequestHeader(value = "UCP-Agent", required = false) String ucpAgent,
      @RequestHeader(value = "Request-Signature", required = false) String requestSignature,
      @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
      @RequestHeader(value = "Request-Id", required = false) String requestId,
      @RequestBody(required = false) JsonNode body)
      throws IOException {
    try {
      CheckoutResponse response = checkoutService.completeCheckout(checkoutId, body);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/checkout-sessions/{checkoutId}/cancel")
  public ResponseEntity<CheckoutResponse> cancelCheckout(
      @PathVariable("checkoutId") String checkoutId,
      @RequestHeader(value = "UCP-Agent", required = false) String ucpAgent)
      throws IOException {
    try {
      JsonNode platformProfile = extractPlatformProfile(ucpAgent, null);
      return ResponseEntity.ok(checkoutService.cancelCheckout(checkoutId, platformProfile));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/checkout-sessions/{checkoutId}/mint-instrument")
  public ResponseEntity<CheckoutResponse> mintInstrument(
      @PathVariable("checkoutId") String checkoutId,
      @RequestHeader(value = "UCP-Agent", required = false) String ucpAgent,
      @RequestBody(required = false) JsonNode body)
      throws IOException {
    try {
      JsonNode platformProfile = extractPlatformProfile(ucpAgent, body);
      return ResponseEntity.ok(checkoutService.mintInstrument(checkoutId, platformProfile));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/webhooks/partners/{partnerId}/events/order")
  public ResponseEntity<Map<String, Object>> orderWebhook(
      @PathVariable("partnerId") String partnerId,
      @RequestHeader(value = "Request-Signature", required = false) String requestSignature,
      @RequestHeader(value = "X-API-Key", required = false) String apiKey,
      @RequestBody(required = false) Map<String, Object> body) {
    return ResponseEntity.ok(Map.of("received", true, "partner_id", partnerId));
  }

  // Compatibility aliases
  @PostMapping("/ucp/checkout")
  public ResponseEntity<CheckoutResponse> createCheckoutCompat(
      @RequestHeader(value = "UCP-Agent", required = false) String ucpAgent,
      @RequestBody(required = false) JsonNode body)
      throws IOException {
    return createCheckout(ucpAgent, null, null, null, null, body);
  }

  @PatchMapping("/ucp/checkout/{checkoutId}")
  public ResponseEntity<CheckoutResponse> updateCheckoutCompat(
      @PathVariable("checkoutId") String checkoutId,
      @RequestHeader(value = "UCP-Agent", required = false) String ucpAgent,
      @RequestBody(required = false) JsonNode body)
      throws IOException {
    return updateCheckout(checkoutId, ucpAgent, null, null, null, null, body);
  }

  @PostMapping("/ucp/checkout/{checkoutId}/mint_instrument")
  public ResponseEntity<CheckoutResponse> mintInstrumentCompat(
      @PathVariable("checkoutId") String checkoutId,
      @RequestHeader(value = "UCP-Agent", required = false) String ucpAgent,
      @RequestBody(required = false) JsonNode body)
      throws IOException {
    return mintInstrument(checkoutId, ucpAgent, body);
  }

  @PostMapping("/ucp/checkout/{checkoutId}/complete")
  public ResponseEntity<CheckoutResponse> completeCheckoutCompat(
      @PathVariable("checkoutId") String checkoutId,
      @RequestBody(required = false) JsonNode body)
      throws IOException {
    return completeCheckout(checkoutId, null, null, null, null, body);
  }

  @PostMapping("/webhooks/orders")
  public ResponseEntity<Map<String, Object>> webhookSimulate(
      @RequestBody(required = false) Map<String, Object> body) {
    return ResponseEntity.ok(Map.of("received", true, "body", body));
  }

  private JsonNode extractPlatformProfile(String ucpAgentHeader, JsonNode body)
      throws IOException {
    if (body != null && body.has("_platform_profile")) {
      return body.get("_platform_profile");
    }
    String url = extractProfileUrl(ucpAgentHeader);
    if (url != null) {
      try (var in = new java.net.URL(url).openStream()) {
        return mapper.readTree(in);
      } catch (IOException e) {
        return mapper.createObjectNode();
      }
    }
    return mapper.createObjectNode();
  }

  private String extractProfileUrl(String ucpAgentHeader) {
    if (ucpAgentHeader == null) {
      return null;
    }
    Matcher matcher = PROFILE_PATTERN.matcher(ucpAgentHeader);
    if (!matcher.find()) {
      return null;
    }
    String quoted = matcher.group(1);
    if (quoted != null && !quoted.isBlank()) {
      return quoted;
    }
    String unquoted = matcher.group(2);
    if (unquoted != null) {
      return unquoted.trim();
    }
    return null;
  }
}
