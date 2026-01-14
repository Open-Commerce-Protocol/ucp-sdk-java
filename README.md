# Unofficial SDKs for Universal Commerce Protocol (UCP)

> Generated from official JSON Schema

This repository hosts multi-language SDK implementations (currently Java, with others like Golang planned) for the Universal Commerce Protocol.

Project homepage: https://deeplumen.io

## Compliance & Status

The upstream UCP repository is included as a git submodule at `ucp/` (source: https://github.com/Universal-Commerce-Protocol/ucp).
SDK models are generated from `ucp/spec` so you can update the spec at any time by updating the submodule.

We currently support the official `dev.ucp` interface specifications. We ensure that:
*   All data interfaces are **100% aligned** with the official specification.
*   Interactions between the Server and Client implementations **pass 100%** of the official examples.
*   Seamless verification of Client-Server interactions.

### Version Mapping

| SDK Version | Upstream Spec Commit |
|-------------|----------------------|
| 0.0.1       | (latest)             |

We are committed to following the official protocol updates and subsequent extensions without breaking existing users.

---

## Overview

The Universal Commerce Protocol (UCP) addresses a fragmented commerce landscape by providing a standardized common language and functional primitives. It enables platforms (like AI agents and apps), businesses, Payment Service Providers (PSPs), and Credential Providers (CPs) to communicate effectively, ensuring secure and consistent commerce experiences across the web.

With UCP, businesses can:

*   Declare supported capabilities to enable autonomous discovery by platforms.
*   Facilitate secure checkout sessions, with or without human intervention.
*   Offer personalized shopping experiences through standardized data exchange.

### Why UCP?

As commerce becomes increasingly agentic and distributed, the ability for different systems to interoperate without custom, one-off integrations is vital. UCP aims to:

*   **Standardize Interaction:** Provide a uniform way for platforms to interact with businesses, regardless of the underlying backend.
*   **Modularize Commerce:** Breakdown commerce into distinct Capabilities (e.g., Checkout, Order) and Extensions (e.g., Discounts, Fulfillment), allowing for flexible implementation.
*   **Enable Agentic Commerce:** Designed from the ground up to support AI agents acting on behalf of users to discover products, fill carts, and complete purchases securely.
*   **Enhance Security:** Support for advanced security patterns like AP2 mandates and verifiable credentials.

## Future Roadmap

### Planned Artifacts (Maven Central)

The following artifacts are planned for future release under the `io.deeplumen` group:

*   `io.deeplumen:ucp-codegen` (Generator/Plugin)
*   `io.deeplumen:ucp-sdk` (Generated models + Java client)
*   `io.deeplumen:ucp-spring-boot-starter`

## Java SDK

Maven coordinates: `io.deeplumen:ucp-sdk`  
Models package: `io.deeplumen.ucp.models.*`

Optional helpers (no transport/flow implementation):
- Constants: `io.deeplumen.ucp.helpers.*`
- Typed views: `io.deeplumen.ucp.typed.*`

## API Docs

- Javadoc (GitHub Pages): `https://deeplumen.io/ucp-sdk/apidocs/`

## Update spec (submodule)

```bash
git submodule update --init --recursive
git submodule update --remote --merge
```

Then regenerate and build the SDK:

```bash
cd sdk
mvn -DskipTests clean install
```

### Upcoming Features

*   **Polyglot Support:** Expansion to other languages such as Golang.
*   **Custom Telemetry:** Support for custom "buried points" (analytics/tracking).
*   **Enhanced Observability:** Richer logging and configurable storage types.

## Contact
For specialized questions, please reach out to: ucp@deeplumen.io