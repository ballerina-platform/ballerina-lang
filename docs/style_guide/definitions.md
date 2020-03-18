# Top Level definitions

* Do not indent the top level definitions. 

  Example,
  
  **Do's**
    ```ballerina

    import ballerina/http;

    const int MIN_AGE = 20;
    int repititions = 0;
    
    service hello on ep1 {
        ...
    }

    ```
  **Don'ts**
  
    ```ballerina
    // This import is indented correctly.
    import ballerina/http; 
    
        const int MIN_AGE = 20; // Not indented correctly.
        int repititions = 0; // Not indented correctly.
        
       // Not indented correctly.
       service hello on ep1 {
           ...
       }
        
    ```
## Imports

* Do not keep spaces between the organization name, divider `/`, and module name.

  Example,
  ```ballerina
  import ballerina/http;
  ```
* Imports should be sorted alphabetically, first by the organization name and then by the module name.

## Function definition
* Do not keep spaces between the function name and the open parentheses `(` of the function signature.

  Example,
  ```ballerina
  function func1() {}
  ```
 
* If the function has an object attached to it, do not keep spaces around the Dot `.`. Also, keep a single space between the `function` keyword and the name of the object.

  Example,
  ```ballerina
  function Person.getName() {}
  ```
* If the function needs to be split in to new lines due to it exceeding the max line length,
  - can break lines from the parameter list by moving a parameter value only to a 
    new line and indenting it with four spaces from the starting position of the function.
    
    Example,
    ```ballerina
    function getAddress(int value,
        string name) returns (string|()) {
        ...
    }
    ```
  - can break before the `returns` keyword and indent it with four spaces from the starting position of the function.
    
    Example,
    ```ballerina
    function getAddress(int value, string name)
        returns (string|()) {
        ...
    }    
    ```
  - can break after the `returns` keyword by moving the return value to a new line
    and indenting it with four spaces from the starting position of the function.
    
    Example,
    ```ballerina
    function getAddress(int value, string name) returns
        (string|()) {
        ...
    }          
    ```

## Service definition

* Keep the listener inline with the service signature.
  
  Example,
  ```ballerina
  service hello on new http:Listener(9090) {
      ...
  }
  ```
* When formatting resource functions and function definitions, block indent each element and
  follow the [function formatting guidelines.](#function-definition).
  
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

* Block indent each function definition, resource definition, and field definition inside a service definition.
 
## Object definition

* Block indent each field definition and each function definition on their own line.
* Init function should be placed before all the other functions. 
* For function definitions in the object definition, follow the [function formatting guidelines](#function-definition).

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

## Record definition
Block indent each of the field definitions (including the Rest field) in their own line.

  Example,
  ```ballerina
  type Person record {
      string name;
      int...;
  }

  // or

  type Person record {|
      int id;
      string name;
  |}
  ```

## Referencing record or abstract object 
* Do not keep spaces between the `*`, the abstract object name, or the record name.
  
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