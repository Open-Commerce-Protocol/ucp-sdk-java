package io.deeplumen.ucp.helpers;

/**
 * Well-known capability identifiers from the UCP specification.
 *
 * <p>These are optional helpers; users may always provide arbitrary capability names as strings.
 */
public final class UcpCapabilities {
  private UcpCapabilities() {}

  public static final String SHOPPING_CHECKOUT = "dev.ucp.shopping.checkout";
  public static final String SHOPPING_FULFILLMENT = "dev.ucp.shopping.fulfillment";
  public static final String SHOPPING_DISCOUNT = "dev.ucp.shopping.discount";
  public static final String SHOPPING_ORDER = "dev.ucp.shopping.order";
}

