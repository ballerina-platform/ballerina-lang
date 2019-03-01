# Statements

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

* Each pattern available in match should block-indented on a own line.
* Closing brace should indent and align with parent.

  ```ballerina
  Foo foo1 = {
      s: "S",
      i: 23,
      f: 5.6
  };
  
  match foo1 {
      var {s, i: integer, f} => return "Matched Values : " + s + ", " + integer + ", " + f;
  } 
  
  ```

 