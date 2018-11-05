# Tools and IDEs for Ballerina

Ballerina provides several tools to help you create, document, and test your code. These includes editors, IDEs as well as CLI tools as described below.

These tools include the following.

- [IDE plug-ins](#ide-plug-ins)
- [Swagger to Ballerina code generator](#swagger-to-ballerina-code-generator)
- [API documentation generator](#api-documentation-generator)
- [Test framework](#test-framework)

## IDE plug-ins

You can use plug-ins to write Ballerina code in your favorite IDE. There are several plug-ins available in Github. Click one of the following links to learn how to use that IDE's plug-in. 

* [Visual Studio Code (VS Code)](https://marketplace.visualstudio.com/items?itemName=ballerina.ballerina)
* [IntelliJ IDEA](https://plugins.jetbrains.com/plugin/9520-ballerina)

## Swagger to Ballerina code generator

You can use existing Swagger files to generate connectors and services in Ballerina code. For details, see the [Swagger to Ballerina Code Generator](https://github.com/ballerinalang/tool-swagger-ballerina/tree/master/modules/swagger-ballerina-generator).

## API documentation generator

As you develop new connectors, actions, and functions that you want to share with others, it's important to add API documentation that describes each entity and how it's used. Ballerina provides a framework called **Docerina** that generates API documentation from your annotations in your Ballerina files. You can check it out at https://github.com/ballerinalang/docerina. 

## Test framework

When you write your code in aforementioned editors and IDEs, the syntax is checked for you as you write it, and you can use the Debug button to step through your program. You can also manually test a Ballerina file using the following command:

```
./ballerina test <file_name>
```

If you want to run all the tests within a module use the following command

```
./ballerina test <module_name>
```

Ballerina provides a testing framework called **Testerina** that you can use for your programs. You can check it out at https://github.com/ballerinalang/testerina. 
