# Ballerina Style Guide

Formatting is one of the most argued topic when it comes to a programming language.
To avoid these arguments we decide to come up with a opinionated guide for Ballerina source code formatting. 
Users can follow their own formatting but we recommend to use this formatting guide as all the IDE tools 
will support only this format. Also following this guide will form a standard style across all developers.

## Formatting Conventions

### Indentation and Line Length
* Use four spaces, not tabs.
* No indentation for top level definitions. 
  ```ballerina
  // This is the top level of the .bal file.

  import ballerina/http;

  function func1() {
      int a = 0;
  }

  service hello on ep1 {
      resource function sayHello(http:Caller caller, http:Request req) {
          http:Response res = new();
          res.setPayload("Test");
          _ = caller->respond(res);
      }
  }

  ```
* Maximum line length is 120 characters. 

### [Operators, Keywords and Boundaries](operators_keywords_and_boundaries.md)
### [Top Level Definitions](definitions.md)
### [Statements](statements.md)
### [Expressions](expressions.md)

### Annotations (@)
* No spaces around `@`.
* Indented to align with the start position of the parent (statement, definition).
  e.g:
  ```ballerina
  // Service annotations indented to align with start position of the service.
  @http:ServiceConfig {
      basePath: "greet"
  }
  service greetingService on new http:Listener(8080) {
      // Resource annotation indented to align with start position of the resource.
      @http:ResourceConfig {
          methods: [],
          path: "sayHello"
      }
      resource function hello(http:Caller caller, http:Request req) returns error? {
          http:Response res = new;
          res.setPayload("hello");
          _ = caller->respond(res);
      }
  }
  ```
* Key value pairs in annotation body should indent relative to the annotation.
  ```ballerina
  @http:ServiceConfig {
      basePath: "greet"
  }
  ```  

### Comments (//)

* `//` should followed by a single space.
* If comment is on its own line then it should indent considering the 
  context(top level or in a block) its in.
  e.g: 

  ```ballerina
  // This is a top level comment

  function func1() {
      // This is a block level comment. 
  }

  function func2() {
      if (true) {
          if (true) {
              // This is a nested if block level comment.
          }
      }
  }
  ```

* If comment is inline with code there should be a space before it.

  ```ballerina

  type People record {}; // Inline comment

  function func1() {
      int a = 0; // Inline comment
  }

  ```


### Documentation(#)

* Always should be on top of the parent definition.
* Always should be indented as to the parent definition.
* `#` should followed by a space.
* `+`, `-` , `return`, param identifier and description should start with a space.

  ```ballerina
  # Get Value.
  #
  # + value - value input parameter 
  # + return - return a integer value
  function getValue(int value) returns int {
      return value;
  }

  service greet on new http:Listener(8080) {
      # Say hello.
      #
      # + caller - caller endpoint that calling this resource 
      # + request - request sent by the caller
      resource function sayHello(http:Caller caller, http:Request request) {
        
      }
  }
  ```