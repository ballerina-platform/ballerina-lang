# Ballerina Base Docker Image

This Dockerfile can be used to create a Ballerina base Docker distribution that should be used for subsequent Docker image build processes of Ballerina Services and Functions. 

```bash
# Copy the Ballerina distribution 
cp <BALLERINA_HOME>/modules/distribution/target/ballerina-0.8.0-SNAPSHOT.zip .

# bash build.sh -d <ballerina-dist>
bash build.sh -d ballerina-0.8.0-SNAPSHOT.zip
```