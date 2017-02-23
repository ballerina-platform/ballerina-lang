# Concepts Under Consideration

This section is a set of ideas that are currently under consideration - feedback and suggestions are most welcome!

> NOTE: None of these are implemented in v0.8.0.

## Disabling Constructs from Execution

In most programming languages, developers use commenting as a technique to disable a block of code from executing. This is a second class way to achieve what the developer wants to achieve: make a statement or construct inactive. Further, in Ballerina, we currently do not allow comments arbitrarily as they are treated as statements.

Ballerina instead allows the developer (either visually or textually) to mark any statement, function, action, connector, resource, or service to be disabled by prefixing it with the `!` character. Disabling a construct does not prevent the language parser, type checker, and other validations; it simply stops that construct from being executed at runtime.

## Configuration/Lifecycle Management

In most practical development scenarios, programs have values that need to change when the program migrates through different lifecycle stages such as development, testing and production. In the case of Ballerina this may be credentials for connectors or others such as the URLs of network services.

In Ballerina v0.8.0, the only way to change such a value is by using environment variables (using `system:getenv(string)`) to determine the value instead of hard coding it in the code itself. This is now fairly common with the use of an external metadata registry such as etcd in Docker as it allows the values to be changed easily from outside without affecting the code itself.

We are considering whether this is sufficient or further capabilities are needed to make Ballerina programs easier to manage through the modern software development lifecycle.
