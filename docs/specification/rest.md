# Other Stuff TBD

## Disabling Constructs from Execution

In traditional programming languages, developers use commenting as a technique to disable a block of code from executing. In Ballerina, we do not allow comments arbitrarily - we only allow comments as statements.

Ballerina instead allows the developer (either visually or textually) to mark any statement or function, action, connector, resource, or service to be disabled by prefixing it with the `!` character. Disabling a construct does not prevent the language parser, type checker, and other validations; it simply stops that construct from being executed at runtime.

## Configuration Management

TODO!

Several Ballerina constructs such as connectors and resources have configurable parameters. Examples include the URI of an HTTP endpoint and timeout values. These values MAY be set explicitly within the program using annotations, but such values can be overridden from outside the program by applying appropriate property values. These values may be set via environment variables or other deployment management approaches.
