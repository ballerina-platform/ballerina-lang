# Operators, keywords, and types

## Single-line formatting
### Keywords and types
* Keep only a single space between the type and the pipe operator when it is in an union type (e.g., `string | int`).
  
  Example,
  ```ballerina
  type method "POST" | "GET" | "PUT";
    
  (int | string) variable = 0;
  
  function getValue(string key) returns (string | error) {
      ...
  }
  
  function getName() returns string | error {
      (string | error) valueOrError = getValue("name");
      ...
  }
  ```
* Do not keep spaces between the type and the optional operator `?`.
  
  Example,
  ```ballerina
  string? name;
  ```
* Avoid line breaks in constrained types.
  
  **Do's**
  ```ballerina
    map<int | string> // map reference type
  ```
  
  **Don'ts**
  ```ballerina
  map<
      int
      |
      string
  > 
  ```
### Operators
* Keep only a single space before and after the `=` operator.
  
  Example,
  ```ballerina
  int a = 0;
  ```
* Do not keep spaces around the semicolon `;`.
* Do not keep spaces between the unary operator and the expression.

  Example,
  ```ballerina
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
* When accessing a function, object, or record from another module, do not keep spaces around `:`.
  
  Example,
  ```ballerina
  io:println("john");
  http:Response res = new ();
  ```
  