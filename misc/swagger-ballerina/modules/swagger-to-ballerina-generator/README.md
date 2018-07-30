# Swagger to Ballerina Code Generator
[Ballerina](https://ballerina.io/) is a programming language designed for integration. The Ballerina Composer allows you to define your integrations by creating sequence diagrams in the Design View, by writing Ballerina code in the Source View, or by creating Swagger definitions in the Swagger View. If you already have Swagger definition files that define functionality you want to use, the Swagger to Ballerina Code Generator can take existing Swagger definition files and generate Ballerina endpoints and services from them.

# Prerequisites
Download and install Ballerina by visiting https://ballerina.io/downloads/. 

# Generating a client endpoint

If you want to create a client endpoint from a given swagger file by entering the following command. You need to execute the following command within a Ballerina project, in order to successfully create the resulting artifacts.

```
ballerina swagger endpoint <swaggerFile> 
```

# Generating a mock service

If you want to generate the service skeletons and also add code into each resource body to return sample responses using the data type as defined by the Swagger resource definition, you can generate mock services by entering the following command:

```
ballerina swagger mock <swaggerFile>
```

This will generate the service definition with the stub implementations with sample responses.