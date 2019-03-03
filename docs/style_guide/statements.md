# Statements

* Statement are indented as to the context (top level or in a block). 

## Assignment
### Compound Assignment

## Flow Control

### If Statement

* It is recommended to have condition enclosing parentheses all the time.
* No spaces between condition and parentheses.
* Statements in if block, each should block-indented in their own line.

  ```ballerina
  if (age >= 20 && age < 25) {
      inSallaryRange = true;
  } 
  ```
* `else` and `else if` keyword should be on the same line of previous `if` or `else if` block's
  closing brace `}`.
  
  - Single space between closing brace and `else` keyword.
  - In else block, single space should be added between `else` and `{` of the block.
  - Each statements should block-indented in their own line.
  
    ```ballerina
  
    if (sallary >= 2000 && sallary < 2500) {
        inSallaryRange = true;
    } else if (sallary >= 25 && sallary < 30) {
        inProperSallaryRange = true;
    } else {
        notInSalaryRange = false;
    }
  
    ```

* To have `if` or `else if` block without condition enclosing parentheses. 

  - Expression should be simple and single expression.
  - There should be single spaces around condition expression.
  
    ```ballerina
    if inProperSallaryRange {
        return 1;
    } else if inSallaryRange {
        return 2;
    }
    
    ```
#### Empty Block

* Not recommended to have empty `if`, `else if` and `else` statements.
* So if there is an empty if statement
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

## Match Statement

* Each pattern statement available in match should block-indented on a own line.
* Closing brace should indent and align with parent.

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

### Match Patterns

* All patterns should block-indent.
* There should be space around `=>`.
* If pattern only contains one statement it is recommended to put all in a single line.

  ```ballerina
  function foo(string | int | boolean a) returns string {
      match a {
          12 => return "Value is '12'";
      }
  
      return "Value is 'Default'";
  }
  ```
* If pattern has more than one statement then it is recommended to use block notation.
  - Before opening brace there should be a single space and it should be on the same line as the pattern. 
  - Each statement then should be block-indented on a own line.
  - Closing brace should be on it own line and indented to align with the pattern start position.
  
    ```ballerina
    function typeGuard1((string, int) | ClosedBar1 | ClosedBar2 | (int, boolean) | int | float x) returns string {
        match x {
            var (s, i) if s is string => {
                string matchedString = "Matched with string : " + s + " added text with " + io:sprintf("%s", i);
                return matchedString;
            }
            var (s, i) if s is float => {
                string matchedFloat = "Matched with float : " + io:sprintf("%s", s + 4.5) + " with " + io:sprintf("%s", i);
                return matchedFloat;
            }
        }
    }
    ```
## Worker Statement

