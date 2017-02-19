# Functions

Functions are like functions in any other language and are a mechanism to create a reusable unit of functionality. Learning from Go, Ballerina functions can return many values at once instead of just one as is commonly done in most imperative languages.

A `function` is defined as follows:

```
[FunctionAnnotations]
[native] function FunctionName (((TypeName VariableName)[(, TypeName VariableName)*])?)
        ((TypeName [VariableName][(, TypeName [VariableName])*])?) [throws exception] {
    WorkerDeclaration;*
    Statement;+
}
```

The `native` keyword says that the function's implementation is not in Ballerina. In that case the body of the function is not given as it is opaque.

Functions may be invoked from another function (including itself) or from any resource or action. The function executes using the thread of execution given by the caller and the caller will block until the execution is completed. As suggested in the syntax, functions may define additional workers to execute parallel logic but that is limited to the duration of the function. That is, any worker that is created by a function is immediately terminated when the function completes execution.

As of version 0.8, all functions are globally visible via its package qualified name.

All functions are private to the package unless explicitly declared to be public with the `public` keyword. Functions may be invoked within the same package from a resource or a function in the same package without importing. Functions marked `public` can be invoked from another package after importing the package.

## The `main` Function

A function who's name is `main` and which takes an array of `string` as its only argument and optionally returns an `int` is a potential entry point for command line execution. Any package may have such a function and the execution instruction decides which `main` is executed.

## Example

A simple 'Hello World' function is as follows:

```
function main (string[] args) {
    system:println ("Hello, World!");
}
```
