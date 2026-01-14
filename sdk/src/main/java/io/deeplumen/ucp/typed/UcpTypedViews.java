package io.deeplumen.ucp.typed;

import io.deeplumen.ucp.models.schemas.shopping.UCPCheckoutResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Optional typed helpers around generated UCP models.
 *
 * <p>These helpers do not modify or replace generated types. They only offer convenience views for
 * fields that are intentionally left open-ended by the schema.
 *
 * <p>Example:
 *
 * <pre>{@code
 * List<CapabilityRef> caps = UcpTypedViews.checkoutCapabilities(checkout.getUcp());
 * for (CapabilityRef cap : caps) {
 *   System.out.println(cap.name() + "@" + cap.version());
 * }
 * }</pre>
 */
public final class UcpTypedViews {
  private UcpTypedViews() {}

  public static List<CapabilityRef> capabilityRefs(List<Object> raw) {
    if (raw == null || raw.isEmpty()) {
      return List.of();
    }
    List<CapabilityRef> out = new ArrayList<>(raw.size());
    for (Object v : raw) {
      CapabilityRef.tryFrom(v).ifPresent(out::add);
    }
    return Collections.unmodifiableList(out);
  }

  public static List<Object> toRawCapabilities(List<CapabilityRef> refs) {
    if (refs == null || refs.isEmpty()) {
      return List.of();
    }
    List<Object> out = new ArrayList<>(refs.size());
    for (CapabilityRef ref : refs) {
      if (ref == null) {
        continue;
      }
      out.add(ref.toMap());
    }
    return out;
  }

  public static List<CapabilityRef> checkoutCapabilities(UCPCheckoutResponse ucpMeta) {
    Objects.requireNonNull(ucpMeta, "ucpMeta is required");
    return capabilityRefs(ucpMeta.getCapabilities());
  }
}
