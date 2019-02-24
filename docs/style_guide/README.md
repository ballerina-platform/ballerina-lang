# Ballerina Style Guide

Formatting is one of the most argued topic when it comes to a programming language.
To avoid these arguments we decide to come up with a opinionated guide for Ballerina source code formatting. 
Users can follow their own formatting but we recommend to use this formatting guide as all the IDE tools 
will support only this format. Also following this guide will form a standard style across all developers.

## Formatting Conventions

### Indentation and line length
* Use four spaces, not tabs
* No indentation top level (definitions) 
* Maximum line length is 120 characters 

### [Operators, Keywords and Boundaries](operators_keywords_and_boundaries.md)
### [Definitions](definitions.md)
### [Statements](statements.md)
### [Expressions](expressions.md)

### Comments (//)

* If Comment is taking an entire line it should indent accordingly.

```ballerina
// This Comment is in the top level

function name() {
    // This comment is in a block. 
}

function name1() {
    if (true) {
        if (true) {
            // This comment is in a nested block.
        }
    }
}
```

* If comment is inline with code there should be a space before it.

```ballerina

type People record {}; // Inline comment with a defintion

function name() {
    int a = 0; // Inline comment with a statement
}

```


### Documentation(#)

* Always should be on top of the parent definition.
* Always should be indented as to the parent definition.
* `#` should followed by a space.
* `+`, `-` , `return`, param identifier and description should start with a space.

```ballerina
# Description
#
# + a - a Parameter Description 
# + return - Return Value Description
function name( int a) returns int {
    return 0;
}

service serviceName on new http:Listener(8080) {
    # Description
    #
    # + caller - caller Parameter Description 
    # + request - request Parameter Description
    resource function newResource(http:Caller caller, http:Request request) {
        
    }
}
```