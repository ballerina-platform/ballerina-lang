# Operators, keywords and types

## Single-line formatting
### Keywords and types
* Keep single spaces between the type and a pipe operator when it is in an union type `string | int`.
  
  Example,
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
* When accessing a type(Record or Object) from a another module, do not keep spaces around the colon (e.g., `http:Response`).
* Do not keep spaces between the type and the optional operator `?`.
  
  Example,
  ```ballerina
  string? name;
  ```
### Operators
* Do not keep spaces before and after the `=` operator.
* Do not keep spaces around the semicolon `;`.
* Do not keep spaces between the unary operator and expression.

  Example,
  ```ballerina
  int a = 0;
  a = -a;
  ``` 

* Keep a single space before and after any `binary` or `ternary` operator.

  Example,
  ```ballerina
  var fullName = firstName + lastName;
  
  string | () name = isNameAvailable() ? getName() : "Unknown";
  
  var elvisOperator = name ?: "Unknown";
  ```
* Keep a single space before and after a compound operator such as `-=` and `+=`.

  Example,
  ```ballerina
  name += lastName;
  ```
* When accessing a function, object or record from another module, do not keep spaces around `:`.
  
  Example,
  ```ballerina
  io:println("john");
  http:Response res = new();
  ```
  