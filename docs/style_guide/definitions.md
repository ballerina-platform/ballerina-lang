# Definitions (Top level definitions)

This section will focus on styling the top level definitions available in the Ballerina.

## Indentation
* No indentation applied for the top level constructs.

```ballerina
import ballerina/http;

function func1() {
    int a = 0;
}
```

## Imports

* `import` keyword followed by a single space.
* No spaces between org name , divider(`/`) and package name.

```ballerina
import ballerina/http;
```
* Identifiers such as version and alias is fronted with single space.

```ballerina
import wso2/twitter version 0.9.0 as twitter;

import abc/foo.bar version 0.1 as foo;
```

## Function
* No indentation for function except when defined in an object definition.
* No space between function name and the open parentheses `(` of function signature.
```ballerina
function func1()
```
* Closing brace `}` of the function should indent according to the function.
* If function body has at least on statement, closing brace `}` should fronted with a new line
and indent accordingly.
```ballerina
function func1() {
    int a = 0;
}
```
* First parameter should not fronted with a space. 
* `,` comma which separate the parameters followed by a single space.
* No spaces between last parameter and the closing parentheses.

```ballerina

function func1(int param1, string param2) {}

```
 
* If function is an object bound function there is no space in front and back of the `.`
and should have single space between `function` keyword and the object name.

```ballerina
function Person.getName() {}
```

* Should have a single space between closing parentheses and the `returns` keyword.

```ballerina
function getName() returns int {
    return 0;
}
``` 

## Service

* No indentation for service.
* Single space between service name and `on` keyword.
* Listeners should always front with a single space.
```ballerina
service hello on ep1, ep2 {}
```
* No spaces applied before comma `,` which separate listeners.

### Resource

* Resource is intended accordingly related to parent service.
* As rest of the syntaxes for  is similar to function please refer to [function](#Function) formatting guidelines for
parameter, return type and resource body formatting.  

```ballerina
service hello on ep1, ep2 {
    resource function sayHello(http:Caller caller, http:Request req) returns error? {
        http:Response res = new;
        res.setPayload("hello");
        _ = caller->respond(res);
    }
}
```

## type definition

### Object Definition

### Record Definition

## Global variable definition