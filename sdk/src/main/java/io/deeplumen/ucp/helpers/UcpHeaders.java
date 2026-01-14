package io.deeplumen.ucp.helpers;

/**
 * UCP-related HTTP header names.
 *
 * <p>These constants are optional conveniences and do not implement any transport logic. They are
 * provided to reduce typos and keep header names consistent across integrations.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * requestBuilder
 *     .header(UcpHeaders.UCP_AGENT, "profile=\"...\"; version=\"2026-01-11\"")
 *     .header(UcpHeaders.REQUEST_ID, UUID.randomUUID().toString());
 * }</pre>
 */
public final class UcpHeaders {
  private UcpHeaders() {}

  public static final String UCP_AGENT = "UCP-Agent";
  public static final String REQUEST_SIGNATURE = "Request-Signature";
  public static final String IDEMPOTENCY_KEY = "Idempotency-Key";
  public static final String REQUEST_ID = "Request-Id";
  public static final String API_KEY = "X-API-Key";
}
