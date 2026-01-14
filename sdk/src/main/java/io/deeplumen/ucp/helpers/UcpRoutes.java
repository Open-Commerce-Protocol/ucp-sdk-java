package io.deeplumen.ucp.helpers;

/**
 * Common UCP HTTP route paths.
 *
 * <p>These are provided as optional string constants. They do not enforce any routing behavior.
 */
public final class UcpRoutes {
  private UcpRoutes() {}

  public static final String WELL_KNOWN_UCP = "/.well-known/ucp";

  public static final String CHECKOUT_SESSIONS = "/checkout-sessions";

  public static String checkoutSessionById(String checkoutId) {
    return CHECKOUT_SESSIONS + "/" + checkoutId;
  }

  public static String checkoutSessionComplete(String checkoutId) {
    return checkoutSessionById(checkoutId) + "/complete";
  }

  public static String checkoutSessionCancel(String checkoutId) {
    return checkoutSessionById(checkoutId) + "/cancel";
  }
}

