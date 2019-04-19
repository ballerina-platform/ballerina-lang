# Operators, keywords and types

## Single-line formatting
### Keywords and types
* If there is a keyword followed by a keyword, separate them using a single space only.
  
  Example,
  ```ballerina
  public remote function getUserName(http:Caller caller, http:Request req) {
  
  } 
  ```
* If there is a keyword or type followed by an identifier, separate them using a single space only.
  
  Example,
  ```ballerina
  string fullName = "john doe"; 
  
  function getFullName() returns string {
      return fullName;
  }
  ```
* Do not keep spaces around a type when it is enclosed using angle brackets `<string>`. 
  
  Example,
  ```ballerina
  map<string> names = {};
  ```
* Do not keep spaces between the type and opening bracket in the array definition `string[]`.
  
  Example,
  ```ballerina
  string[] names = [];
  
  ```
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
  