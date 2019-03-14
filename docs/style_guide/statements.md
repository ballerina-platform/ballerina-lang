# Statements

## Flow Control

### If Statement

* Condition in If statement should have enclosing parentheses all the time.
* There shouldn't be any spaces between condition expression and parentheses.
* Statements in if block, each should block-indent on its own line.

  ```ballerina
  if (age >= 20 && age < 25) {
      inSallaryRange = true;
  } 
  ```
* `else` and `else if` keyword should be on the same line of previous `if` or `else if` block's
  closing brace `}`.
  
  - Single space between closing brace and `else` keyword.
  - Each statements should block-indented on its own line.
  
    ```ballerina
  
    if (sallary >= 2000 && sallary < 2500) {
        inSallaryRange = true;
    } else if (sallary >= 25 && sallary < 30) {
        inProperSallaryRange = true;
    } else {
        notInSalaryRange = false;
    }
  
    ```

* To have `if` or `else if` block without condition enclosing parentheses 
  condition should be a simple expression.
  
    ```ballerina
    if inProperSallaryRange {
        return 1;
    } else if inSallaryRange {
        return 2;
    }
    
    ```
#### Empty Block

* There shouldn't be any empty `if`, `else if` and `else` blocks.
* But if there are empty `if`, `else` and `else if` blocks
  - There should be a single space before opening brace.
  - There should be a empty line between opening and closing brace.
  - Closing brace should be indented and aligned
    - If brace is related to `if` block then the brace should indented to align with
      `if` keyword position.
    - If brace if related to `else` or `else if` block then the brace should indented to align with 
      previous block's closing brace.
      
  ```ballerina
  if (inProperSallaryRange) {
      
  } else if (inSallaryRange) {
      
  } else {
      
  }
  ```
  Above guidelines for empty blocks are valid even if only a `if` block is available.
  ```ballerina
  if (inProperSallaryRange) {
  
  }
  ```
## Match Statement
* Closing brace of a match statement should indent and align with the start position of the match statement.

  ```ballerina
  function foo(string | int | boolean a) returns string {
      match a {
          12 => return "Value is '12'";
          "Hello" => return "Value is 'Hello'";
          15 => return "Value is '15'";
          true => return "Value is 'true'";
          false => return "Value is 'false'";
          "HelloAgain" => return "Value is 'HelloAgain'";
      }
 
      return "Value is 'Default'";
  }
  ```

### Match Patterns Clause

* Each pattern clause should be block-indented on its own line.
* There should be a single space before and after `=>`.
* If a pattern clause only contains one statement it should be placed on the same line as the 
  pattern clause without any curly braces enclosing it.

  ```ballerina
  function foo(string | int | boolean a) returns string {
      match a {
          12 => return "Value is '12'";
      }
  
      return "Value is 'Default'";
  }
  ```
* If a pattern clause has more than one statement then
  - Before the opening brace there should be a single space and it should be on the same line as the pattern clause. 
  - Each statement then should be block-indented on its own line.
  - Closing brace should be on its own line and indented to align with the pattern clause start position.
  
    ```ballerina
    function typeGuard1((string, int) | (int, boolean) | int | float x) returns string {
        match x {
            var (s, i) if s is string => {
                string matchedString = "Matched with string : " + s + " added text with " + io:sprintf("%s", i);
                return matchedString;
            }
            var (s, i) if s is int => {
                string matchedInt = "Matched with int : " + io:sprintf("%s", s + 4) + " with " + io:sprintf("%s", i);
                return matchedInt;
            }
        }
        
        return "";
    }
    ```
* If the pattern clause block is empty then both braces should be on the same line and there shouldn't be any spaces
  between them.
  
  ```ballerina
    function typeGuard1((string, int) | (int, boolean) | int | float x) returns string {
        match x {
            var (s, i) if s is string => {}
            var (s, i) if s is int => {}
        }
        
        return "";
    }
  ```
## Transaction Statement

* Each optional clause(`onretry`, `committed` and `aborted`) should start on the same line 
  as the closing brace of the previous clause.
* If blocks are empty there should be an empty line between braces. 
  
  ```ballerina
  function func1() {
      transaction with retries = 2 {
          
      } onretry {
          
      } aborted {
          
      } committed {
          
      }
  }
  ```

