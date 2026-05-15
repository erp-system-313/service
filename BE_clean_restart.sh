#!/bin/bash

echo "Stopping containers and removing volumes..."
docker-compose down -v

echo "Starting containers..."
docker-compose up -d

echo "Waiting for database to be ready..."
sleep 10

echo "Running Flyway repair..."
mvn flyway:repair

echo "Waiting for containers to stabilize..."
sleep 5

echo "Running Spring Boot application..."
mvn spring-boot:run