# Swagger to Ballerina Code Generator
[Ballerina](http://www.ballerinalang.org) is a programming language designed for integration. The Ballerina Composer allows you to define your integrations by creating sequence diagrams in the Design View, by writing Ballerina code in the Source View, or by creating Swagger definitions in the Swagger View. If you already have Swagger definition files that define functionality you want to use, the Swagger to Ballerina Code Generator can take existing Swagger definition files and generate Ballerina connectors and services from them.

# Prerequisites
Download and install Ballerina by visiting http://www.ballerinalang.org. 

# Generating a connector
To generate a connector from your Swagger definition file, navigate to the `bin` folder of your Ballerina distribution and run the following command:

```
ballerina swagger connector <swaggerFile> [-d <output directory name>] [-p <package name>] 
```

You can specify a local Swagger file by entering its file path, or specify an online file by entering its URL. The output directory name specifies where to save the generated files (default is the current directory), and the package name specifies the package name to use for this connector. If your Swagger definition has more than one tag, a separate Ballerina file will be generated for each tag, each definining a separate connector. Also, if any JSON Schema types are defined in the Swagger file, a JSON file will be generated defining those types.

For example, let's look at the sample Swagger file http://petstore.swagger.io/v2/swagger.json. It contains three tags: pet, store, and user, and it defines JSON Schema types. If you enter the following command:

```
ballerina swagger connector http://petstore.swagger.io/v2/swagger.json -d server/petstore/ -p wso2.carbon.test
```

The files will be generated as follows:  

```
.
└── server
    └── petstore
        └── wso2
            └── carbon
                └── test
                    └── ballerina
                        ├── pet.bal
                        ├── README.md
                        ├── store.bal
                        ├── types.json
                        └── user.bal

```

You can now connect to the pet, store, and user functionality in the Swagger file from your Ballerina services by using these connectors.

# Generating a service skeleton

If you want to package the functionality from the Swagger file as services, you can generate service skeletons instead of connectors by entering the following command:

```
ballerina swagger skeleton <swaggerFile> [-d <output directory name>] [-p <package name>] 
```

This will generate Ballerina files that define each tag as a service, and it generates a JSON file for any JSON Schema types defined in the Swagger file. 

# Generating a mock service

If you want to generate the service skeletons and also add code into each resource body to return sample responses using the data type as defined by the Swagger resource definition, you can generate mock services by entering the following command:

```
ballerina swagger mock <swaggerFile>  [-d <output directory name>] [-p <package name>] 
```

This will generate the service defintions and JSON Schema types file as it did with the skeleton command, but each service definition will include sample responses.
