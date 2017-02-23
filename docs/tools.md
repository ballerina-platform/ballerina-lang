# Tools

Ballerina provides several tools to help you create, document, and test your code.

## Flexible composer

The Ballerina Composer provides three ways to create your integrations:

* Draw sequence diagrams in the Design view
* Write Ballerina code in the Source view
* Create services by writing [Swagger](http://swagger.io) definitions in the Swagger view

As you work in one view, the diagrams and code are updated in the other views, allowing you to switch between them seamlessly as needed. Note that the Swagger view is only for defining services, so if you are creating an executable program with a `main()` function instead of a service, you cannot use the Swagger view. 

## IDE plug-ins

You can use plug-ins to write Ballerina code in your favorite IDE. The following plug-ins are available in Github:

* [Atom](https://github.com/ballerinalang/plugin-atom)
* [IntelliJ IDEA](https://github.com/ballerinalang/plugin-intellij)
* [Sublime Text 3](https://github.com/ballerinalang/plugin-sublimetext3)
* [Vim](https://github.com/ballerinalang/plugin-vim)
* [Visual Studio Code (VS Code)](https://github.com/ballerinalang/plugin-vscode)

## API documentation generator

As you develop new connectors, actions, and functions that you want to share with others, it's important to add API documentation that describes each entity and how it's used. Ballerina provides a framework called **Docerina** that generates API documentation from your annotations in your Ballerina files. You can check it out at https://github.com/ballerinalang/docerina. 

## Test framework

When you write your code in Ballerina Composer, the syntax is checked for you as you write it, and you can use the Debug button to step through your program. You can also manually test a Ballerina file using the following command:

```
ballerina test <ballerina_file>
```
Ballerina provides a testing framework called **Testerina** that you can use for your programs. You can check it out at https://github.com/ballerinalang/testerina. 
