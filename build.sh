#!/bin/bash

echo "🚀 Building Payal Bot for Render..."

# Install Maven if not present
if ! command -v mvn &> /dev/null; then
    echo "📦 Installing Maven..."
    apt-get update && apt-get install -y maven
fi

# Build the project
echo "📦 Building with Maven..."
mvn clean package -DskipTests

echo "✅ Build complete!"