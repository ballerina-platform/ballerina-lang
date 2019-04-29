# Annotations, Documentation and Comments

### Annotations
* Do not have spaces around the `@` symbol.
* Indent annotations to align them with the starting position of the owner (statement or definition).
  
  Example,
  ```ballerina
  // Service annotations indented to align with the starting position of the service.
  @http:ServiceConfig {
      ...
  }
  service greetingService on new http:Listener(8080) {
      // Resource annotation indented to align with start position of the resource.
      @http:ResourceConfig {
          ...
      }
      resource function hello(http:Caller caller, http:Request req) returns error? {
          ...
      }
  }
  ```
* Each annotation attribute (i.e., key-value pairs) should block indent on its own line..
  
  Example,
  ```ballerina
  @http:ServiceConfig {
      basePath: "greet",
      methods: ["GET"]
  }
  ```
* If an annotation is empty, place it in a single line and 
  do not have spaces between both braces.
  
  Example,
  ```ballerina
  @http:ServiceConfig {}
  ```
* If you are annotating a parameter or a return type, the annotation should be added inline to the parameter or the return type.
  
  Example, 
  
  ```ballerina
  // Parameter annotation.
  public function secureFunction1(@sensitive string secureInName, @sensitive int secureInId, string insecureIn) {
      ...
  }
  
  public function secureFunction2(@sensitive string secureInName,
      @sensitive int secureInId, string insecureIn) {
      ...
  }
  
  // Return type annotation.
  public function taintedReturn1() returns @tainted string {
      ...
  }
    
  public function taintedReturn2() returns 
      @tainted string {
      ...
  }
  
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
* Add a space after the `#` symbol.
* Add an empty line after the description.

  Example,
    ```ballerina
    # Description.
    #
    # + value - value input parameter 
    # + return - return a integer value
    function getValue(int value) returns int {
        return value;
    }
    ```
* Add only one space after parameter marker (`+`), divider (`-`) and `return`.
* Begin the param identifier and description with a single space.

  Example,
  ```ballerina
  # Description.
  #
  # + value - Parameter description
  # + return - Return value description
  function getValue(int value) returns int {...}

  # Description.
  @http:ServiceConfig {...}
  service greet on new http:Listener(8080) {
      # Description.
      #
      # + caller - Parameter description.
      # + request - Parameter description.
      @http:ResourceConfig {...}
      resource function sayHello(http:Caller caller, http:Request request) {...}
  }
  ```
