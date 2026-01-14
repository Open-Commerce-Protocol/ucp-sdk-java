package io.deeplumen.ucp.helpers;

/**
 * Well-known capability identifiers from the UCP specification.
 *
 * <p>These are optional helpers; users may always provide arbitrary capability names as strings.
 * This class intentionally does not validate capability names or restrict extensions.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * // Example: building a capability reference as a Map for open-ended schema fields.
 * Map<String, Object> checkout =
 *     Map.of("name", UcpCapabilities.SHOPPING_CHECKOUT, "version", "2026-01-11");
 * }</pre>
 */
public final class UcpCapabilities {
  private UcpCapabilities() {}

  public static final String SHOPPING_CHECKOUT = "dev.ucp.shopping.checkout";
  public static final String SHOPPING_FULFILLMENT = "dev.ucp.shopping.fulfillment";
  public static final String SHOPPING_DISCOUNT = "dev.ucp.shopping.discount";
  public static final String SHOPPING_ORDER = "dev.ucp.shopping.order";
}
