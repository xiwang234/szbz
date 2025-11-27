#!/bin/bash

# Java 和 Maven 环境设置脚本
# 使用方法：source setup-env.sh 或 . setup-env.sh

export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"
export JAVA_HOME="/opt/homebrew/opt/openjdk@17"

echo "✓ Java 环境已设置"
echo "  JAVA_HOME: $JAVA_HOME"
echo "  Java版本: $(java -version 2>&1 | head -n 1)"
echo "  Maven版本: $(mvn -version 2>&1 | head -n 1)"
