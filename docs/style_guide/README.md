# Ballerina style guide

Formatting is one of the most vital topics with respect to a programming language. You can follow your own formatting for Ballerina source code. However, this guide brings you formatting recommendations since all the Ballerina IDE plugins/extensions support only these by default. Also, this guide aims at forming a standard style among the Ballerina community.

### Indentation and length of the lines
* Use four spaces (not tabs) for each level of indentation.
* Do not indent the top level definitions. 

Example,
  ```ballerina
  // This is the top level definition of the .bal file.

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
* Keep the maximum length of a line as 120 characters. 

### Spacing

#### Inline spacing

Use only a single space to separate keywords, types, and identifiers. 

Example,
```ballerina
// public keyword, type keyword, identifier Employee, abstract keyword and object keyword
// all have to be separated with only a single space.
public type Employee abstract object {
    public int id;
    public string name;
};
```

#### Blank lines

Separate both statements and top level definitions by zero or one blank lines (i.e., one or two new lines).

  Example,
  ```ballerina
  import ballerina/http;
  import ballerina/io;
  
  const string CITY = "Colombo";
  const int CITY_NO = 1;
  
  function getName() returns string {
      string firstName = "john";
      string lastName = "doe";
      
      return firstName + lastName;
  }
  
  function getAge() returns int {}
  function setAge(int age) {}
  ```
### Blocks
* Add a single space before opening the curly braces. 

  Example,
  ```ballerina

  function func1() {
      if (true) {}
  }

   ```
* If a block is empty, do not keep spaces in between the opening `{` and closing `}` braces.
  
  Example,
  ```ballerina
  function func1() {}
  ``` 
* Indent all the statements inside a block to be at the same level.

### Parentheses
* To define empty parenthese, do not keep spaces between the opening and closing parentheses `()`.
* Do not have spaces before closing and after opening the parentheses.
  
  Example,
  ```ballerina
  function setValue(string value) {} 
  ```
  
### Line breaks

* Have only one statement in a line.
* Split a line at some point, in the direction from the start towards the end of the line.
* When splitting lines, which contain operator(s), split them right before an operator.
  
  Example,
  
  ```ballerina
  // Binary operations.
  string s = "added " + People.name
      + " in to database.";
  
  // Function invocation.
  string s = person
      .getName();
  
  // Binary operations in if condition
  if (isNameAvailable 
      && (i == 1)) {
  
  }

  ```

* Indent splitted lines with relation to the starting position of the statement or definition.
  
  Example,
  ```ballerina
  if (isNameAvailable 
      && (i == 1)) {
    
  }
  ```
* Avoid line breaks in constrained types and type casting.
  
  Example, 
  
  Correct method:
  
  ```ballerina
  map<int | string> // map reference type
  
  // or
  
  <string>
  ```
  
  Incorrect method:

  ```ballerina

  map<
      int
      |
      string
  > 

  // or

  <
      string
  >
  ```
  
  >**Info:** However, if you cannot add the type casting expression or statement with the constrained type in a single line 
  due to it exceeding the max line length, 
    - move the casting type with the operators to a new line.
  
      Example,
      ```ballerina
      string name =
          <string>json.name;
      ```
  
    - keep the constrained type on the same line by splitting the statement at some point, in the direction from the start towards the end of the line.
      
      Example,
      ```ballerina
      map<int | string> registry = {};
      
      table<Employee> employee = table {
          {key id, name, address}
      };
      ```

### [Top Level Definitions](definitions.md)
### [Statements](statements.md)
### [Expressions](expressions.md)
### [Operators, Keywords and Types](operators_keywords_and_types.md)

### Annotations
* Do not have spaces around the `@` symbol.
* Indent annotations to align them with the starting position of the owner (statement or definition).
  
  Example,
  ```ballerina
  // Service annotations indented to align with the starting position of the service.
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
* Indent each annotation attribute (i.e., key value pairs) as a block in their own lines.
  
  Example,
  ```ballerina
  @http:ServiceConfig {
      basePath: "greet",
      methods: ["GET"]
  }
  ```
* If an annotation is empty, place it in a single line and 
  do not have spoaces between both braces.
  
  Example,
  ```ballerina
  @http:ServiceConfig {}
  ```

### Comments
* Use `//` to write both single-line and multi-line comments.
  
  Example,

  ```ballerina
  // This is a single-line comment.
  ```
  and 
  
  ```ballerina
  // Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  //
  // WSO2 Inc. licenses this file to you under the Apache License,
  // Version 2.0 (the "License"); you may not use this file except
  // in compliance with the License.
  // You may obtain a copy of the License at
  //
  // http://www.apache.org/licenses/LICENSE-2.0
  //
  // Unless required by applicable law or agreed to in writing,
  // software distributed under the License is distributed on an
  // "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  // KIND, either express or implied.  See the License for the
  // specific language governing permissions and limitations
  // under the License.
  ```
  
* Add a single space between the `//` and the content.
* If the comment is in its own line, then indent it considering the 
  context its in (i.e., top level or in a block).
  
  Example,
  ```ballerina
  // This is a top level comment.

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

* If the comment is in line with the code, add a space before it.

  Example,
  ```ballerina

  type People record {}; // Inline comment

  function func1() {
      int a = 0; // Inline comment
  }

  ```


### Documentation
* Always, indent them to align with the starting position of the owner.
* Add a space before the`#` symnbol.
* Add an empty line after the description.

Example,
  ```ballerina
    # Get Value.
    #
    # + value - value input parameter 
    # + return - return a integer value
    function getValue(int value) returns int {
        return value;
    }
  ```
* Addonly one space after `+`, `-` and `return`.
* Begin the param identifier and description with a single space.

  Example,
  ```ballerina
  # Get Value.
  #
  # + value - value input parameter 
  # + return - return a integer value
  function getValue(int value) returns int {
      return value;
  }

  @http:ServiceConfig {
      basePath: "greet"
  }
  service greet on new http:Listener(8080) {
      # Say hello.
      #
      # + caller - caller endpoint that calling this resource 
      # + request - request sent by the caller
      @http:ResourceConfig {
          methods: [],
          path: "sayHello"
      }
      resource function sayHello(http:Caller caller, http:Request request) {
          http:Response res = new;
          res.setPayload("hello");
          _ = caller->respond(res);
      }
  }
  ```
  For more information on how to document Ballerina code, go to [How to document Ballerina code](https://ballerina.io/learn/how-to-document-ballerina-code/).