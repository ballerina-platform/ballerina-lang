#Swagger to Ballerina Code Generator
Ballerina is a new programming language for integration built on a sequence diagram metaphor. The conceptual model of
 Ballerina is that of a sequence diagram. Each participant in the integration gets its own lifeline and Ballerina
 defines a complete syntax and semantics for how the sequence diagram works and execute the desired integration. This
  tool can generate ballerina connector/service skeleton/mock service from swagger definition.

#prerequisites
Install Ballerina by visiting https://github.com/ballerinalang/ballerina

#Generating Connector
Navigate to bin folder of Ballerina distribution

Then run following command.
```
>>  ballerina swagger connector <swaggerFile> -p<package name> -d<output directory name>
```
when you type above command you can point online swagger definition or local swagger file.
Once connector code generated you can see all generated files within you project as follows. If your service definition is having more than one tags then it will generate connectors per each tag defined with service definition 

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

#Generating Service Skeleton
Navigate to bin folder of Ballerina distribution

Then run following command.
```
>>  ballerina swagger skeleton <swaggerFile> -p<package name> -d<output directory name>
```
when you type above command you can point online swagger definition or local swagger file.
Once connector code generated you can see all generated files within you project as follows.
```
.
└── server
    └── petstore
        └── wso2
            └── carbon
                └── test
                    └── ballerina
                        ├── store.bal

```

#Generating Mock Service
Navigate to bin folder of Ballerina distribution

Then run following command.
```
>>  ballerina swagger mock <swaggerFile> -p<package name> -d<output directory name>
```
when you type above command you can point online swagger definition or local swagger file.
Once connector code generated you can see all generated files within you project as follows.
In addition to skeleton this will include sample responses within the service.
```
.
└── server
    └── petstore
        └── wso2
            └── carbon
                └── test
                    └── ballerina
                        ├── store.bal

```