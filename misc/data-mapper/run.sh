#!/bin/bash

echo "Cloning ballerina-lang"
git clone https://github.com/Ayodhya94/ballerina-lang.git
echo "Download complete"
cd ballerina-lang/

echo $PWD
./gradlew clean build -x test -x check
./gradlew publishToMavenLocal
cd ..

echo $PWD
./gradlew clean build
echo "complete"
