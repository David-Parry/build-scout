#!/bin/bash

# Build the project
mvn clean package

# Run the application
java -jar target/build-scout-1.0.0-SNAPSHOT.jar "$@"
