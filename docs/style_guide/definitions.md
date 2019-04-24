# Top Level definitions

* Do not indent the top level definitions. 

  Example,
  
  Dos
    ```ballerina

    import ballerina/http;

    const int MIN_AGE = 20;
    int repititions = 0;
    
    service hello on ep1 {
        ...
    }

    ```
  Don'ts
  
    ```ballerina
        import ballerina/http;
    
        const int MIN_AGE = 20;
        int repititions = 0;
        
        service hello on ep1 {
            ...
        }
        
    ```
## Imports

* Do not keep spaces between the org name, divider(`/`), and module name.

  Example,
  ```ballerina
  import ballerina/http;
  ```
* Add a single space before identifiers such as version and alias.

  Example,
  ```ballerina
  import wso2/twitter version 0.9.0 as twitter;

  import abc/foo.bar version 0.1 as foo;
  ```

## Function definition
* Do not keep spaces between the function name and the open parentheses (`(`) of the function signature.

  Example,
  ```ballerina
  function func1() {}
  ```

* If the function body has at least one statement, indent the closing brace (`}`) on a new line to match the
  start position of the function.
  
  Example,
  ```ballerina
  function func1() {
      int a = 0;
  }
  ```
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
* If function needs to be split in to new lines, due to exceeding max line length
  - can break lines from the parameter list by moving a parameter value only to a 
    new line and indent it with four spaces from the function start position.
    
    Examples,
    ```ballerina
    function getAddress(int value,
        string name) returns (string | ()) {
        ...
    }
    ```
  - can break before the `returns` keyword and indent it with four spaces from the function start position.
    
    Examples,
    ```ballerina
    function getAddress(int value, string name)
        returns (string | ()) {
        ...
    }    
    ```
  - can break after the `returns` keyword and move return value to a new line
    and indent it with four spaces from the function start.
    
    Examples,
    ```ballerina
    function getAddress(int value, string name) returns
        (string | ()) {
        ...
    }          
    ```

## Service definition

* Always keep the Inline listener listener on the same line as the service signature.
  
  Example,
  ```ballerina
  service hello on new http:Listener(9090) {
      ...
  }
  ```
* When formatting resource functions, function definitions block indent each element and
  follow [function formatting guidelines.](#function-definition).
  
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

* When formatting field definition, block indent each on a own line.
 
## Object definition

* Block indent each field definition in their own line.
* Also, block indent \function definitions, which are defined in the object.
* For function definitions in the object definition, follow [function formatting guidelines](#function-definition).

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