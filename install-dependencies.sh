#!/bin/bash

# ============================================
# INSTALL ALL DEPENDENCIES
# ============================================

echo "📦 Installing all dependencies..."

# Update package list
sudo apt-get update

# Install system dependencies
sudo apt-get install -y \
    default-jre-headless \
    default-jdk-headless \
    maven \
    git \
    curl \
    wget \
    docker.io \
    docker-compose

# Install Python dependencies (if needed)
pip3 install python-telegram-bot python-dotenv requests google-generativeai

# Install Node.js dependencies (if needed)
npm install -g node-telegram-bot-api dotenv axios @google/generative-ai

echo "✅ All dependencies installed!"