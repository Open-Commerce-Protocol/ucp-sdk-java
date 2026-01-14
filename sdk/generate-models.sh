#!/usr/bin/env bash
# Generate UCP Java models from ../ucp/spec
set -euo pipefail
cd "$(dirname "$0")"

echo "Generating Java models from ../ucp/spec ..."
mvn -DskipTests generate-sources
echo "Done. See target/generated-sources/ucp"
