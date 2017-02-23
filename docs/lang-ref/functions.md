# Functions

A function is a single operation that is intended to be a unit of reusable functionality. Ballerina includes a set of native library functions you can call, such as the `ConvertToResponse()` function you used in the [Writing your First Program tutorial](../tutorials/first-program.md), and you can define additional functions within your Ballerina programs. All library functions are found in `ballerina.*` packages under the `src` directory in your Ballerina distribution.  

## Using a function

To use a native Ballerina function, scroll down in the tool palette to the package that contains that function, and then drag the function's icon to the canvas. If the function is in another package and is marked public, you can import that package using the Import box in the upper left corner of the canvas, and then drag the Function Invocation icon to the canvas and call the function. (All functions are private to the package unless explicitly declared to be public with the `public` keyword.) You can invoke functions from a [resource](resources.md) or a function within the same package without importing.

## Defining a function

To define a new function, drag the Function icon to the canvas. 

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
