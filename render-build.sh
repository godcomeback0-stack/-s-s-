#!/bin/bash

# Exit on error
set -e

echo "🔨 Render Build Script Started"

# Install dependencies
apt-get update
apt-get install -y maven curl

# Build the application
mvn clean package -DskipTests

echo "✅ Build successful!"