#!/bin/bash

echo "Cloning ballerina-lang"
git clone https://github.com/ballerina-platform/ballerina-lang.git
echo "Download complete"
cd ballerina-lang/

echo $PWD
git submodule init
git submodule update
./gradlew clean build -x test -x check
./gradlew publishToMavenLocal
cd ..

echo $PWD
./gradlew clean build
echo "complete"
