#!/bin/bash

# ============================================
# PAYAL BOT - Automated Setup Script
# ============================================

echo "🚀 Starting Payal Bot Setup..."

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Check Java
echo -e "${YELLOW}📌 Checking Java...${NC}"
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | sed 's/^1\.//' | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -ge 11 ]; then
        echo -e "${GREEN}✅ Java $JAVA_VERSION installed${NC}"
    else
        echo -e "${RED}❌ Java 11+ required. Found Java $JAVA_VERSION${NC}"
        echo "Installing Java 11..."
        sudo apt-get update && sudo apt-get install -y default-jdk
    fi
else
    echo -e "${RED}❌ Java not found${NC}"
    echo "Installing Java 11..."
    sudo apt-get update && sudo apt-get install -y default-jdk
fi

# Check Maven
echo -e "${YELLOW}📌 Checking Maven...${NC}"
if command -v mvn &> /dev/null; then
    echo -e "${GREEN}✅ Maven installed${NC}"
else
    echo -e "${RED}❌ Maven not found${NC}"
    echo "Installing Maven..."
    sudo apt-get install -y maven
fi

# Check Git
echo -e "${YELLOW}📌 Checking Git...${NC}"
if command -v git &> /dev/null; then
    echo -e "${GREEN}✅ Git installed${NC}"
else
    echo -e "${RED}❌ Git not found${NC}"
    echo "Installing Git..."
    sudo apt-get install -y git
fi

# Check Docker
echo -e "${YELLOW}📌 Checking Docker...${NC}"
if command -v docker &> /dev/null; then
    echo -e "${GREEN}✅ Docker installed${NC}"
else
    echo -e "${RED}❌ Docker not found${NC}"
    echo "Installing Docker..."
    sudo apt-get install -y docker.io
    sudo systemctl start docker
    sudo systemctl enable docker
fi

# Environment variables
echo -e "${YELLOW}📌 Setting up environment...${NC}"
read -p "Enter BOT_TOKEN: " BOT_TOKEN
read -p "Enter GEMINI_API_KEY: " GEMINI_API_KEY
read -p "Enter CHANNEL_USERNAME (optional): " CHANNEL_USERNAME
read -p "Enter GROUP_USERNAME (optional): " GROUP_USERNAME
read -p "Enter OWNER_USERNAME (optional): " OWNER_USERNAME

# Create .env file
cat > .env << EOL
BOT_TOKEN=$BOT_TOKEN
GEMINI_API_KEY=$GEMINI_API_KEY
CHANNEL_USERNAME=${CHANNEL_USERNAME:-@your_channel}
GROUP_USERNAME=${GROUP_USERNAME:-@your_group}
OWNER_USERNAME=${OWNER_USERNAME:-@your_username}
EOL

echo -e "${GREEN}✅ .env file created${NC}"

# Build project
echo -e "${YELLOW}📌 Building project...${NC}"
mvn clean package

# Check build
if [ -f "target/PayalBot-1.0.0.jar" ]; then
    echo -e "${GREEN}✅ Build successful!${NC}"
else
    echo -e "${RED}❌ Build failed${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Setup complete!${NC}"
echo "Run: java -jar target/PayalBot-1.0.0.jar"