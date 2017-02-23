# Exception Handling

Ballerina supports throwing and catching exceptions using a simple exception handling model. This is supported by the `exception` type and the constructs `try-catch` and `throw`.

## Behavior

An exception may be thrown by a native Ballerina function or any Ballerina construct using the `throw` statement. When thrown, the runtime searches for the nearest enclosing block containing a `try-catch` statement. If none is found in the current stack frame then execution of the function (or resource or action or type mapper) stops and the frame is popped and the search continues until a `try-catch` statement is found. If none is found at the outermost level of the worker, then that worker thread dies in an abnormal state.

If the exception goes through the default worker of a `main` function without being caught then the entire program will stop executing. If the exception goes through a `resource` without being caught then that particular invocation of the service & resource will fail and the server connector will choose the appropriate behavior in that situation.

# The `exception` Type

Exceptions are instances of a built-in, opaque reference type named `exception`.

A collection of library functions can be used to set and get properties of exceptions, including stack traces. Note that unlike other languages, Ballerina does not allow developers to define subtypes of the exception type and custom exceptions must be thrown by using custom category strings. As such exception category strings starting with "Ballerina:" are reserved for system use only.

Variables of type `exception` are defined and initialized to a new exception as follows:
```
exception VariableName = {};
```
Library functions for accessing information from this type are in the package `ballerina.lang.exceptions`. These functions can be used to set the category of the exception, the descriptive messsage and any additional properties.

# The `try-catch` Statement

The syntax of a `try-catch` is as follows:
```
try {
    Statement;+
} catch (exception e) {
    Statement;+
}
```
If any exception occurs while execution the first block of statements then the exception will be handled by the block after the `catch`.

> NOTE: Ballerina currently does not have a `finally` concept but we will likely add it.

# The `throw` Statement

The syntax of a `throw` statement is as follows:
```
throw ExceptionVariableName;
```

The `throw` statement is used to throw an exception from the current location. An execution stack trace pointing to the current location will be automatically inserted into the exception before the runtime starts the exception handling process.

> NOTE: There will be more capabilities brought to exceptions in future.
