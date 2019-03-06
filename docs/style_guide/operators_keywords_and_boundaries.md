# Operators, Keywords, types and Boundaries

## Single Line Formatting
### Keywords & Types
* If there is a keyword followed by a keyword they should be separated only by a single space.
  
  ```ballerina
  public remote function getUserName(http:Caller caller, http:Request req) {
  
  } 
  ```
* If there is a keyword or type followed by a identifier they should be separated only by a single space.
  
  ```ballerina
  string fullName = "john doe"; 
  
  function getFullName() returns string {
      return fullName;
  }
  ```
* There shouldn't be any spaces around a type when enclosed using angle brackets `<string>`. 
  
  ```ballerina
  map<string> names = {};
  ```
* There shouldn't be any spaces between type and opening bracket in array definition `string[]`.
  
  ```ballerina
  string[] names = [];
  
  ```
* There should be single spaces between type and a pipe operator when in an union type `string | int`.
  ```ballerina
  function getValue(string key) returns (string | error) {
      if (key == "") {
          error err = error("key '" + key + "' not found", {key: key});
          return err;
      } else {
          return "this is a value";
      }
  }
  
  function getName() returns string | error {
      (string | error) valueOrError = getValue("name");
      return valueOrError;
  }
  
  ```

### Operators
* There should be single space before and after `=` operator.
* There shouldn't be any spaces around semicolon `;`.
* There should be no spaces between unary operator and the expression.

  ```ballerina
  int a = 0;
  a = -a;
  ``` 

* There should be a single space before and after any `binary` or `ternary` operator.

  ```ballerina
  var fullName = firstName + lastName;
  
  var name = isNameAvailable() ? getName() : "Unknown";
  ```
* Colon `:` will be start with empty space and followed by single space if it is a key value pair. 
  
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
* If a block is empty, no space between opening `{` and closing `}` brace.
  ```ballerina
  function func1() {}
  ``` 
* All statements inside a block should block-indent.
### Parentheses
* If empty no spaces between opening and closing parentheses `()`.
* No space before closing parentheses and after opening parentheses.
  
  ```ballerina
  function setValue(string value) {} 
  ```

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