# Swagger to Ballerina-Connector Code Generator
Ballerina is a new programming language for integration built on a sequence diagram metaphor. The conceptual model of Ballerina is that of a sequence diagram. Each participant in the integration gets its own lifeline and Ballerina defines a complete syntax and semantics for how the sequence diagram works and execute the desired integration. This tool will generate ballerina connector skeleton from swagger definition. So you can use this project to convert your swagger definitions to ballerina connector quickly.

# Connector skeleton generation
First you need to build this project and have ballerina-connector code generation jar file.
```
/work/dev-code/swagger2BallerinaConnector# mvn clean install

```

Then run following command and execute jar file to generate connector.
```
>> java -jar target/swagger2ballerina-connector-1.0-SNAPSHOT.jar generate -i http://petstore.swagger.io/v2/swagger.json -l ballerina-connector -o samples/server/petstore/

```
when you type above command you can poit online swagger definition or local swagger file.
Once connector code generated you can see all generated files within you project as follows. If your service definition is having more than one tags then it will generate connectors per each tag defined with service definition 

```
.
├── pet.bal
├── README.md
├── store.bal
├── types.bal
└── user.bal
```
```

```
