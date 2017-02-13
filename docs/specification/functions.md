### Functions

A `function` is defined as follows:

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

All functions are private to the package unless explicitly declared to be public with the `public` keyword. Functions may be invoked within the same package from a resource or a function in the same package without importing. Functions marked `public` can be invoked from another package after importing the package.
