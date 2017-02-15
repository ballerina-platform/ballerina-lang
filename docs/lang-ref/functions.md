# Functions

A function is a single operation that is intended to be a unit of reusable functionality. Ballerina includes a set of native functions you can call, such as the `ConvertToResponse()` function you used in the [first tutorial](../tutorials/first-program.md), and you can define additional functions within your Ballerina programs.

A function is defined as follows:
```
[FunctionAnnotations]
[public] function FunctionName (((TypeName VariableName)[(, TypeName VariableName)*])?)
        ((TypeName[(, TypeName)*])?) [throws exception] {
    ConnectorDeclaration;*
    VariableDeclaration;*
    WorkerDeclaration;*
    Statement;+
}
```
A function:

* Can have local [connectors](connectors.md) and [variables](types-variables.md)
* Can return any number of values
* Can have any number of [workers](workers.md)
* Can be a worker itself

All functions are private to the package unless explicitly declared to be public with the public keyword. Functions may be invoked from a [resource](resources.md) or a function within the same package without importing. Functions marked public can be invoked from another package after importing the package.
