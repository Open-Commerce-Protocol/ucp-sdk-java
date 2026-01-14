package io.deeplumen.ucp.samples.java.data;

import java.net.URI;

public record Product(
    String id, String title, int priceCents, String currency, URI imageUrl, String category) {}
