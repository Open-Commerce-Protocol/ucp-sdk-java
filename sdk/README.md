# UCP SDK (Java)

Project homepage: https://deeplumen.io

Java UCP SDK models generated from the official UCP schemas at `../ucp/spec` (UCP docs: https://ucp.dev).

API docs: https://deeplumen.io/ucp-sdk/apidocs/

## Optional helpers (non-opinionated)

This artifact intentionally does **not** include any HTTP/MCP/Webhook/agent-flow client implementation.
It provides:

- Generated models in `io.deeplumen.ucp.models.*`
- Optional, non-binding helpers:
  - Constants in `io.deeplumen.ucp.helpers.*`
  - Typed views for open-ended schema fields in `io.deeplumen.ucp.typed.*`

## Quick start
```bash
cd sdk
mvn -DskipTests generate-sources
```

Generated models are written to `target/generated-sources/ucp` under the
package `io.deeplumen.ucp.models`.

## Notes

- Data sources and constraints follow the official UCP schemas.
- Use `mvn -DskipTests install` to publish into your local Maven cache for the
  sample app.
