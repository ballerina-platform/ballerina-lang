# Operators, Keywords and Types

## Single Line Formatting
### Keywords and Types
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
* When accessing a type(Record or Object) from a another module there cannot be any spaces around colon `http:Response`  

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
  