package io.deeplumen.ucp.samples.java.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class DataLoader {
  private final Map<String, Product> products = new HashMap<>();

  public DataLoader() throws IOException {
    loadProducts();
  }

  public Map<String, Product> getProducts() {
    return Collections.unmodifiableMap(products);
  }

  private void loadProducts() throws IOException {
    ClassPathResource resource =
        new ClassPathResource("test_data/flower_shop/products.csv");
    try (InputStream is = resource.getInputStream();
         BufferedReader reader =
             new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
      String header = reader.readLine();
      if (header == null) {
        return;
      }
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.isBlank()) continue;
        String[] parts = line.split(",", -1);
        if (parts.length < 7) continue;
        String id = parts[0].trim();
        String title = parts[1].trim();
        int price = Integer.parseInt(parts[2].trim());
        String currency = parts[3].trim();
        String imageRaw = parts[4].trim();
        URI image = imageRaw.isBlank() ? null : URI.create(imageRaw);
        String category = parts[5].trim();
        products.put(id, new Product(id, title, price, currency, image, category));
      }
    }
  }
}
