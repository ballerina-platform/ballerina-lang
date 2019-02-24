# Operators, Keywords and Boundaries

## Single line formatting
It is recommend to follow the following rules when applying white spaces on a line. 
Following will list down where a single whitespace can apply.
* Keywords such as `function`, `endpoint`, `service`, `type` , `if`, `while`, `catch`, `finally`, `try`,
`transaction` and `any type` should followed by a single space. Exceptions are as below.
    - For `service` keyword single space is added only if service definition doesn’t have a service type.
    - For types like map which followed by `<>`
    - When types are used in a array definition which followed by `[ ]`
* Any `binary` and `ternary` operator should start and end with a Single space. Exceptions are as below.
    - This will be ignore if the operator is rest parameter (`string...`).
    - Sealed types (`!...`)
    - Post and pre increment (`++`) and decrement (`--`).
* `:` will be start with empty space and followed by single space if it is a key value pair except
    - In package invocation it is start with and followed by a empty space.
    - Also this rule is ignored when it is a ternary operation.
* Before opening curly brace there should be a space. Exceptions to this as below.
    - In XML referencing a xml namespace as a attribute
    - Table column expression
    ```ballerina
        table {
            {key id, name, address}
        }
    ```
    - Table data expression
    ```ballerina
        table {
            {id, name, address},
            [{"1", "test", "No:123 hty RD"}]
        }
    ```
* Before opening bracket in a array literal there should be a space. Exception to this is as below.
    - Opening bracket of a table data list fronted by a new line and indented relative to parent.
    - Opening bracket of a array variable value access, rest variable value access and map variable reference.
    - Opening bracket of a xml attribute add `x1@[“foo”] = “bar”`

## Line breaks

* Line should break after `;` 
* Avoid line breaking in types and conversions.
e.g., prefer

```ballerina

map<
    int
    |
    string
> 

// or

<
    string
>
```

to

```ballerina
map<int | string> 

// or

<string>
```

* Line break can happen before `+` and if line break happens all the `+`s should be line broken.

```ballerina
string s = "sdd"
    + People.name
    + 0;
```

* Broken lines should be indented with four spaces.