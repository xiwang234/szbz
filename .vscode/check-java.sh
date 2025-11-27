#!/bin/bash

echo "=== Java Environment Check ==="
echo ""
echo "Java Version:"
java -version
echo ""
echo "Java Home (should be set):"
echo "JAVA_HOME=${JAVA_HOME}"
echo ""
echo "Java Path:"
which java
echo ""
echo "Real Java Path:"
readlink -f $(which java)
echo ""
echo "Maven Version:"
mvn -version
echo ""
echo "=== VS Code Java Settings ==="
cat .vscode/settings.json
