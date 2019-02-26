# Operators, Keywords and Boundaries

## Single line formatting
It is recommend to follow the following rules when applying white spaces on a line. 
Following will list down where a single whitespace can apply.
### Keywords
* Keywords should always followed by a single space. Exceptions are as below.
    - For types like map which followed by `<>`
    - This will be ignore when defining rest parameter (`string...`).
    - If record type has a rest field no spaces between type and rest operator(`...`) 
    ```ballerina
    type RecordName2 record {
        string name;
        string...;
    }
    ```
    - When types are used in a array definition which followed by `[ ]`
### Operators
* No space between `!` and `...` in sealed type.
* There should be no spaces between unary operator and the expression.
```ballerina
int a = 0;
a = -a;
``` 

* Any `binary` and `ternary` operator should start and end with a Single space.
* `:` will be start with empty space and followed by single space if it is a key value pair except
    - In package invocation it is start with and followed by a empty space.
    - Also this rule is ignored when it is a ternary operation.

### Blocks
* Before opening curly brace there should be a space. 

```ballerina

function name() {
   if (true) {}
}

```

Exceptions to this rule as below.

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
* If block is empty, no space between opening `{` and closing `}` brace.
  ```ballerina
  function func1() {}
  ``` 
### Parentheses
* No space between opening `(` and closing `)` parentheses if it is empty.
* No space in front of closing `)` parentheses.

## Line breaks
* Line should break after `;`.
* Only one statement on a line. 
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