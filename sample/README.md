# UCP Java Sample (REST)

Minimal Java sample implementing the core UCP flow using REST:
Profile, Discovery, Negotiation, Create/Update Checkout, Mint Instrument,
Complete Checkout, and Webhook simulation.

## Prerequisites

Install the Java SDK into your local Maven cache:

```bash
cd sdk
mvn -DskipTests install
```

## Run server

```bash
cd sample
mvn spring-boot:run
```

## Endpoints (official paths)

- `GET /.well-known/ucp`
- `POST /checkout-sessions`
- `GET /checkout-sessions/{id}`
- `PUT /checkout-sessions/{id}`
- `POST /checkout-sessions/{id}/complete`
- `POST /checkout-sessions/{id}/cancel`
- `POST /checkout-sessions/{id}/mint-instrument` (sample extension)
- `POST /webhooks/partners/{partner_id}/events/order`

Compatibility aliases are available under `/ucp/checkout*` and `/webhooks/orders`.

## Client

Run the Java client:

```bash
mvn -DskipTests compile exec:java
```

Environment variables:
- `UCP_SAMPLE_BASEURL` (default: http://localhost:8080)
- `UCP_PROFILE_URL` (default: http://localhost:8080/profiles/platform.json)
- `UCP_AGENT_VERSION` (default: 2026-01-11)
- `UCP_API_KEY` (default: test)
- `UCP_SAMPLE_ITEM_ID` (default: bouquet_roses)

## Interop

- Java client -> Python server:
  ```bash
  set UCP_SAMPLE_BASEURL=http://localhost:8182
  mvn -DskipTests compile exec:java
  ```
- Python client -> Java server:
  ```bash
  python samples/rest/python/client/flower_shop/simple_happy_path_client.py --server_url=http://localhost:8080
  ```

The sample data uses product IDs `bouquet_roses` and `pot_ceramic` to match the
Python happy path client expectations.
