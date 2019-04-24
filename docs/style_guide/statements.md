# Statements

## Flow control

### If statement

* Always, enclose the condition with parentheses in the If statement.
* Do not keep spaces between the condition expression and parentheses.
* Block indent each statement of the `if`, `else if`, and `else` blocks in its own line.

  Example,
  ```ballerina
  if (sallary >= 2000 && sallary < 2500) {
      inSallaryRange = true;
  } else if (sallary >= 25 && sallary < 30) {
      inProperSallaryRange = true;
  } else {
      notInSalaryRange = false;
  }
  ```
* Keep the `else` and `else if` keywords in the same line with the matching `if` or `else if` block's
  closing brace separated only by a single space.

#### Empty block

* Do not have any empty `if`, `else if`, or `else` blocks.
* If empty add an empty line between the opening and closing braces.
      
  Example,
  ```ballerina
  if (inProperSallaryRange) {
      
  } else if (inSallaryRange) {
      
  } else {
      
  }
  ```

## Match statement

### Match patterns clause

* Block indent each pattern clause in its own line.
* Keep a single space before and after the `=>` sign.
* If a pattern clause contains only one statement, place it in the same line as the 
  pattern clause without curly braces enclosing it.

  Example,
  ```ballerina
  function foo(string | int | boolean a) returns string {
      match a {
          12 => return "Value is '12'";
      }
  
      return "Value is 'Default'";
  }
  ```
* If a pattern clause has more than one statement, block indent each statement in its own line.
  Example,
  ```ballerina
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
  ```
* If pattern body is empty, then keep it as an empty block.
  
  Example,
  ```ballerina
  match x {
      var (s, i) if s is string => {}
      var (s, i) if s is int => {}
  }
  ```
## Transaction statement

* Start each optional clause(`onretry`, `committed` and `aborted`) in the same line 
  as the closing brace of the matching clause.
* If `transaction`, `onretry`, `committed` and `aborted` blocks are empty, add an empty line between the braces. 
  
  Example,
  ```ballerina
  function func1() {
      transaction with retries = 2 {
          
      } onretry {
          
      } aborted {
          
      } committed {
          
      }
  }
  ```

