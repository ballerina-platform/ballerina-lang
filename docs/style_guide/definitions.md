# Top Level definitions

This section will focus on styling the top level definitions available in the Ballerina.

* No indentation applied for the top level constructs.

  ```ballerina
  import ballerina/http;

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

## Function
* No indentation for function except when defined in an object definition.
* No space between function name and the open parentheses `(` of function signature.

  ```ballerina
  function func1()
  ```
* Closing brace `}` of the function should indent according to the function.
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

## Service

* No indentation for service.
* Single space between service name and `on` keyword.
* Listeners should always front with a single space.
* No spaces applied before a comma `,` which used in listener list.
  
  ```ballerina
  service hello on ep1, ep2 {}
  ```

### Resource

* Resource is intended accordingly related to parent service.
* As rest of the syntaxes for resource is similar to function please refer to [function](#Function) formatting guidelines for
parameter, return type and resource body formatting.  

  ```ballerina
  service hello on ep1, ep2 {
      resource function sayHello(http:Caller caller, http:Request req) returns error? {
          http:Response res = new;
          res.setPayload("hello");
          _ = caller->respond(res);
      }
  }
  ```
  
## Type Definition

* Should have single spaces around pipe operator `|`.

  ```ballerina
  type method "POST" | "GET" | "PUT";
  ```
* No indentation for type definition. 

### Object Definition

* Field definitions should indented relative to the parent object.
* Function definitions should indent relative to parent object.
* For rest of function syntax formatting please refer to [function](#Function) formatting guidelines.

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
* For type reference formatting refer to [type reference](#Referencing) section.
### Record Definition
* Field definitions should indent relative to the parent record.
* Rest field should indent relative to the parent record.

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
* For type reference formatting refer to [referencing](#Referencing) section.

### Referencing
* No spaces between `*` and abstract object name or record name.
  
  ```ballerina
  *Person;
  ```
* Should indent related to the parent.

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