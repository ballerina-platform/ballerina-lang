# Operators, Keywords and Boundaries

## Single Line Formatting
It is recommend to follow the following rules when applying white spaces on a line. 
Following will list down where a single whitespace can apply.
### Keywords
* Keywords should always followed by a single space. Exceptions are as below.
  - For types like map which followed by `<>`
  - This will be ignore when defining rest parameter (`string...`).
  - If record type has a rest field no spaces between type and rest operator(`...`) 
    ```ballerina
    type Person record {
        string name;
        string...;
    }
    ```
  - When types are used in a array definition which followed by `[ ]`
### Operators
* Should have single space before and after `=` operator.
* No spaces before semicolon `;`.
* No space between `!` and `...` in sealed type.
* There should be no spaces between unary operator and the expression.

  ```ballerina
  int a = 0;
  a = -a;
  ``` 

* Any `binary` and `ternary` operators should start and follow by a single space.

  ```ballerina
  var fullName = firstName + lastName;
  
  var name = isNameAvailable() ? getName() : "Unknown";
  ```
* colon `:` will be start with empty space and followed by single space if it is a key value pair. 
  
  ```ballerina
  Person person = {
      name: "john",
      age: 20
  };
  ```
  except in module invocation there are no spaces around colon.
  
  ```ballerina
  io:println("");
  ```

### Blocks
* Before opening curly brace there should be a space. 

  ```ballerina

  function func1() {
      if (true) {}
  }

   ```
* Exceptions for above rule as below.
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
* No space before closing parentheses `)` and after opening parentheses `(`.

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

* When splitting lines which contains operator/s split should happen 
  right before an operator.
  
  example:
  
  ```ballerina
  // Binary operations.
  string s = "added " + People.name
      + " in to database.";
  
  // Function invocation.
  string s = person
      .getName();
  
  // Binary operations in if condition
  if (isNameAvailable 
      && (i == 1)) {
  
  }

  ```

* Splitted lines should be indented with four spaces relative to the parent.