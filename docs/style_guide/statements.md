# Statements

## Flow control

### If statement

* Always, enclose the condition with parentheses in the If statement.
* Do not keep spaces between the condition expression and parentheses.
* Block indent each statement of the if statemnt in its own line.

  Example,
  ```ballerina
  if (age >= 20 && age < 25) {
      inSallaryRange = true;
  } 
  ```
* Keep the `else` and `else if` keywords in the same line with the previous `if` or `else if` block's
  closing brace `}`.
  
  - Keep a single space between the closing brace and `else` keyword.
  - Block indent each statement in its own line.
  
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

* To have an `if` or `else if` block without a condition, keep a simple expression as the condition within the enclosing parentheses.
  
    Example,
    ```ballerina
    if inProperSallaryRange {
        return 1;
    } else if inSallaryRange {
        return 2;
    }
    
    ```
#### Empty block

* Do not have any empty `if`, `else if`, or `else` blocks.
* Howevber, if there are empty `if`, `else`, or `else if` blocks,
  - keep a single space before the opening brace.
  - add an empty line between the opening and closing braces.
  - indent the closing brace and align it as follows.
    - If the brace is related to the`if` block, then indent the brace to align it with the position of the
      `if` keyword.
    - If the brace if related to an `else` or `else if` block, then indent the brace to align it with 
      the closing brace of the previous block.
      
  Example,
  ```ballerina
  if (inProperSallaryRange) {
      
  } else if (inSallaryRange) {
      
  } else {
      
  }
  ```
  >**Info:** Above guidelines for empty blocks are valid if only an `if` block is available.
  
  Example,
  ```ballerina
  if (inProperSallaryRange) {
  
  }
  ```

## Match statement
* Indent the closing brace of a match statement to align it with the starting position of the match statement.

  Example,
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
* If a pattern clause has more than one statement, then
  - add a single space in the same line as the pattern clause (before the opening brace). 
  - block indent each statement in its own line.
  - indent the closing brace in its own line to align with the starting position of the pattern clause.
  
    Example,
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
* If the pattern clause block is empty, then keep both braces in the same line and do not keep spaces
  between them.
  
  Example,
  ```ballerina
    function typeGuard1((string, int) | (int, boolean) | int | float x) returns string {
        match x {
            var (s, i) if s is string => {}
            var (s, i) if s is int => {}
        }
        
        return "";
    }
  ```
## Transaction statement

* Start each optional clause(`onretry`, `committed` and `aborted`) in the same line 
  as the closing brace of the previous clause.
* If blocks are empty, add an empty line between the braces. 
  
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

