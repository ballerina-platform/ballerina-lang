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

### Spacing

#### Inline Spacing

When applying spaces to separate keywords, types and identifiers always use only a single space. 

```ballerina
// public keyword, type keyword, identifier Employee, abstract keyword and object keyword
// All have to be separated with only a single space.
public type Employee abstract object {
    public int id;
    public string name;
};
```

#### Blank Lines

Separate both statements and top level definitions by zero or one blank lines (one or two new lines).

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
* Before opening curly brace there should be a single space. 

  ```ballerina

  function func1() {
      if (true) {}
  }

   ```
* If a block is empty, no space between opening `{` and closing `}` brace.
  ```ballerina
  function func1() {}
  ``` 
* All statements inside a block should block-indent.
### Parentheses
* If empty no spaces between opening and closing parentheses `()`.
* No space before closing parentheses and after opening parentheses.
  
  ```ballerina
  function setValue(string value) {} 
  ```
  
### Line Breaks
* Line should break after `;`.
* Only one statement on a line. 
* Avoid line breaking in types and conversions.
  e.g., prefer

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

  to

  ```ballerina
  map<int | string> 

  // or

  <string>
  ```

* When splitting lines which contains operator/s split should happen 
  right before an operator.
  
  example:
  
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

* Splitted lines should be indented relative to the start position of the statement or definition.
  
  ```ballerina
  if (isNameAvailable 
      && (i == 1)) {
    
  }
  ```

### [Top Level Definitions](definitions.md)
  - [Imports](definitions.md#imports)
  - [Function](definitions.md#function)
  - [Service](definitions.md#service)
    - [Resource Function](definitions.md#resource-function)
  - [Object Definition](definitions.md#object-definition)
  - [Record Definition](definitions.md#record-definition)
  - [Referencing Record or Abstract Object](definitions.md#referencing-record-or-abstract-object)
### [Statements](statements.md)
  - [Variable Definition](statements.md#variable-definition)
  - [Compound Assignment](statements.md#compound-assignment)
  - [Flow Control](statements.md#flow-control)
    - [If Statement](statements.md#if-statement)
    - [Match Statement](statements.md#match-statement)
      - [Match Patterns](statements.md#match-patterns)
    - [Transaction Statement](statements.md#transaction-statement)
### [Expressions](expressions.md)
  - [Record Literals](expressions.md#record-literals)
  - [Map Literal](expressions.md#map-literal)
  - [Tuple](expressions.md#tuple)
  - [Function Invocation](expressions.md#function-invocation)
  - [Array Literals](expressions.md#array-literals)
  - [Type Conversion](expressions.md#type-conversion)
  - [Table literal](expressions.md#table-literal)
### [Operators, Keywords and Types](operators_keywords_and_types.md)
  - [Keywords and Types](operators_keywords_and_types.md#keywords-and-types)
  - [Operators](operators_keywords_and_types.md#Operators)

### Annotations
* No spaces around `@`.
* Annotation should indent to align with the start position of the owner (statement, definition).
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
* Annotation Attributes(Key value pairs), each should block-indent on their own line.
  
  ```ballerina
  @http:ServiceConfig {
      basePath: "greet",
      methods: ["GET"]
  }
  ```
* If annotation is empty, it should be placed on a single line and 
  between both braces there shouldn't be any spaces.
  
  ```ballerina
  @http:ServiceConfig {}
  ```

### Comments
* Ballerina uses `//` to write both single line and multiline comments.
  
  ```ballerina
  // This is a single line comment.
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
  
* There should be a single space between `//` and the content.
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


### Documentation
* Always should be indented to align with the start position of the owner.
* `#` should followed by a space.
* There should be an empty line after Description.
  ```ballerina
    # Get Value.
    #
    # + value - value input parameter 
    # + return - return a integer value
    function getValue(int value) returns int {
        return value;
    }
  ```
* There should be only a single space after `+`, `-` and `return`.
* param identifier and description

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
  Find more details about how to document Ballerina code [here](https://ballerina.io/learn/how-to-document-ballerina-code/).