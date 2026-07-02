#!/bin/sh
# Configuration

DIR="$(cd "$(dirname "$0")" && pwd)"
java -jar "$DIR/vertex-latest.jar" --help
