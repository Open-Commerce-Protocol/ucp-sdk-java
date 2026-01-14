package io.deeplumen.ucp.typed;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Optional typed view for capability references carried in some generated schemas as {@code Object}.
 *
 * <p>This type does not replace any generated field types. It exists purely to make it easier to
 * read/write these values without losing forward-compatibility.
 *
 * <p>Typical JSON shape:
 *
 * <pre>{@code
 * {"name":"dev.ucp.shopping.checkout","version":"2026-01-11"}
 * }</pre>
 *
 * <p>This view can:
 *
 * <ul>
 *   <li>Parse from {@code Map}, {@code JsonNode}, or an existing {@code CapabilityRef}
 *   <li>Convert back into a {@code Map<String, Object>} for assigning into {@code List<Object>}
 * </ul>
 */
public record CapabilityRef(String name, String version, String extendsName) {
  public CapabilityRef {
    Objects.requireNonNull(name, "name is required");
    Objects.requireNonNull(version, "version is required");
  }

  public static CapabilityRef of(String name, String version) {
    return new CapabilityRef(name, version, null);
  }

  public static Optional<CapabilityRef> tryFrom(Object value) {
    if (value == null) {
      return Optional.empty();
    }
    if (value instanceof CapabilityRef) {
      return Optional.of((CapabilityRef) value);
    }
    if (value instanceof Map<?, ?> map) {
      Object name = map.get("name");
      Object version = map.get("version");
      Object extendsValue = map.get("extends");
      if (name instanceof String && version instanceof String) {
        return Optional.of(
            new CapabilityRef((String) name, (String) version, extendsValue instanceof String ? (String) extendsValue : null));
      }
      return Optional.empty();
    }
    if (value instanceof JsonNode node) {
      JsonNode name = node.get("name");
      JsonNode version = node.get("version");
      if (name != null && name.isTextual() && version != null && version.isTextual()) {
        JsonNode ext = node.get("extends");
        return Optional.of(
            new CapabilityRef(
                name.asText(), version.asText(), ext != null && ext.isTextual() ? ext.asText() : null));
      }
      return Optional.empty();
    }
    return Optional.empty();
  }

  /**
   * Converts this capability reference into a {@link Map} suitable for assigning into generated
   * fields like {@code List&lt;Object&gt;}.
   */
  public Map<String, Object> toMap() {
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("name", name);
    result.put("version", version);
    if (extendsName != null && !extendsName.isBlank()) {
      result.put("extends", extendsName);
    }
    return result;
  }
}
