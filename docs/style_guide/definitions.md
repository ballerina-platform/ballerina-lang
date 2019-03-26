# Top Level definitions

* Do not apply indentation for the top-level constructs.

  Example,
  ```ballerina
  import ballerina/http;

  // Global variable definitions.
  const int MIN_AGE = 20;
  int repititions = 0;

  function func1() {
      int a = 0;
  }
  ```

## Imports

* Do not keep spaces between the org name, divider(`/`), and module name.

  Example,
  ```ballerina
  import ballerina/http;
  ```
* Front the identifiers such as version and alias with a single space.

  Example,
  ```ballerina
  import wso2/twitter version 0.9.0 as twitter;

  import abc/foo.bar version 0.1 as foo;
  ```

## Function definition
* Do not keep spaces between the function name and the open parentheses `(` of the function signature.

  Example,
  ```ballerina
  function func1() {}
  ```
* Indent and align the closing brace `}` of the function with the starting position of the function.
  
  Example,
  ```ballerina
  function getName() returns string {
      return "john";
  }
  ```
* If the function body has at least one statement, front the closing brace `}` with a new line
  and indent it accordingly.
  
  Example,
  ```ballerina
  function func1() {
      int a = 0;
  }
  ```
* Do not front the first parameter with a space. 
* Add a single space before the `,` comma, which separates the parameters.
* Do not keep spaces between the last parameter and the closing parentheses.

  Example,
  ```ballerina

  function func1(int param1, string param2) {}

  ```
 
* If the function has an object attached to it, do not keep spaces around the `.`. Also, 
keep a single space between the `function` keyword and the name of the object.

  Example,
  ```ballerina
  function Person.getName() {}
  ```

* Keep a single space between the closing parentheses and the `returns` keyword.

  Example,
  ```ballerina
  function getName() returns string {
      return "john";
  }
  ``` 

## Service definition

* Front the listeners with a single space.
* Do not keep spaces before a comma `,`, which is used in a list of listeners.
  
  Example,
  ```ballerina
  service hello on ep1, ep2 {}
  ```

### Resource function

* Block indent resource functions inside the service body.
* Also, block indent the function definitions, which are defined in the service definition.
* For the parameter, return type, and function body formatting, of resource functions and function definitions in the service definition, follow [function formatting guidelines](#function-definition).

  Example,
  ```ballerina
  service hello on ep1, ep2 {
      resource function sayHello(http:Caller caller, http:Request req) returns error? {
          http:Response res = new;
          res.setPayload(self.getGreeting());
          _ = caller->respond(res);
      }
      
      function getGreeting() returns string {
          return "Hello";
      }
  }
  ```

## Global variable definition

* When defining type definition, keep only single spaces around pipe operator `|`.

  Example,
  ```ballerina
  type method "POST" | "GET" | "PUT";
  ```

## Object definition

* Block indent each field definition in their own line.
* Also, block indent \function definitions, which are defined in the object.
* For parameter, return type, and function body formatting, of function definitions in the object definition, follow [function formatting guidelines](#function-definition).

  Example,
  ```ballerina
  type Person object {
      // Object field definitions.
      public boolean isMarried = false;
      int age;
      string name;
  
      // Object init function.
      function __init(int age = 0, string name) {
          self.age = age;
          self.name = name;
      }
  
      // Object function definitions.
      function getName() returns string {
          return self.name;
      }
  
      function setIsMarried(boolean isMarried) {
          self.isMarried = isMarried;
      }
      
      function getIsMarried() returns boolean {
          return self.isMarried;
      }
  };
  ```
* For type reference formatting, see [referencing](#referencing-record-or-abstract-object).

## Record definition
* Block indent each of the field definitions (including the Rest field) in their own line.

  Example,
  ```ballerina
  type Person record {
      string name;
      int...;
  }

  // or

  type Person record {
      int id;
      string name;
      !...;
  }
  ```
* For type reference formatting, see [referencing](#referencing-record-or-abstract-object) section.

## Referencing record or abstract object 
* Do not keep spaces between the `*`, and abstract object name or record name.
  
  Example,
  ```ballerina
  *Person;
  ```
  and should block-indent.

  Example:

  ```ballerina
  type UserId record {
      string id = "";
  };
  
  type User record {
      *UserId; // Reference to UserId record.
      string name = "john";
      int age = 20;
  };

  // or
  type Person abstract object {
      string name;
  
      // Object function definitions.
      function getName() returns string;
  };

  type Employee object {
      *Person; // Reference to Person abstract object.

      function __init() {
          self.name = "asd";
      }

      function getName() returns string {
          return self.name;
      }
  };
  ```