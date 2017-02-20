# Ballerina Base Docker Image

This Dockerfile can be used to create a Ballerina base Docker distribution that can be used to package Ballerina archives as distributable Docker images.

## Usage
```bash
# (Optionally) Copy the Ballerina distribution 
cp <BALLERINA_HOME>/modules/distribution/target/ballerina-0.8.0.zip .

# bash build.sh -d <ballerina-dist> to build ballerina-pkg:latest
bash build.sh -d ballerina-0.8.0.zip
# or
bash build.sh -d ~/Downloads/ballerina-0.8.0.zip 

# Show usage
bash build.sh -h
```