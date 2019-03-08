# Top Level definitions

* No indentation applied for the top level constructs.

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

* No spaces between org name , divider(`/`) and module name.

  ```ballerina
  import ballerina/http;
  ```
* Identifiers such as version and alias is fronted with single space.

  ```ballerina
  import wso2/twitter version 0.9.0 as twitter;

  import abc/foo.bar version 0.1 as foo;
  ```

## Function Definition
* No space between function name and the open parentheses `(` of function signature.

  ```ballerina
  function func1() {}
  ```
* Closing brace `}` of the function should indent and align with function start position.
  
  ```ballerina
  function getName() returns string {
      return "john";
  }
  ```
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
 
* If function is an object attached function there is no space around the `.`
  and should have single space between `function` keyword and the object name.

  ```ballerina
  function Person.getName() {}
  ```

* Should have a single space between closing parentheses and the `returns` keyword.

  ```ballerina
  function getName() returns string {
      return "john";
  }
  ``` 

## Service Definition

* Listeners should always front with a single space.
* No spaces applied before a comma `,` which used in listener list.
  
  ```ballerina
  service hello on ep1, ep2 {}
  ```

### Resource Function

* Resource functions should block-indent inside service body.
* Function definitions, which are defined in the Service definition, should block-indent.
* Resource functions and function definitions in service definition should follow [function formatting guidelines](#function-definition) for
  parameter, return type and function body formatting.

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

## Global Variable Definition

* When defining type definition there should be only single spaces around pipe operator `|`.

  ```ballerina
  type method "POST" | "GET" | "PUT";
  ```

## Object Definition

* Field definitions, each should block-indent on their own line.
* Function definitions, which are defined in the object, should block-indent.
* Function definitions in object definition should follow [function formatting guidelines](#function-definition) for 
  parameter, return type and function body formatting.

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
* For type reference formatting refer to [type reference](#referencing-record-or-abstract-object) section.
## Record Definition
* Field definitions (including Rest field), each should block-indent on their own line.

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
* For type reference formatting refer to [referencing](#referencing-record-or-abstract-object) section.

## Referencing Record or Abstract Object 
* No spaces between `*` and abstract object name or record name.
  
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