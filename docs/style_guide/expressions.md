# Expressions

## Record Literals

* When formatting fields in record literal 
    - Can arrange fields horizontally 
      - Shouldn't have any spaces before comma.
      - Should have a single space after comma. 
      
      ```ballerina
      
      Person p = {name: "john", age: 20}; 
      
      ``` 
    - But if at least one field is splitted in to a new line then all the fields should splitted in to a new lines.
      
      If record literal is as below
      
      ```ballerina
      
      Person p = {name: "john",
      age: 20}; 
      
      ``` 
      then all the fields should be move in to new lines and should indent.
      
      ```ballerina
      Person p = {
          name: "john",
          age: 20
      };
      ```


## Tuple

* Arrange tuple on a single line.

  ```ballerina

  (string, int) tuple = ("john", 20);

  ```
* If it is not possible to arrange tuple on a single line, may be with line length limit,
  each field in the tuple should be on its own block indented line.
  
  - Trailing comma always should be on a end of a line and inline with a field.
  - Parentheses act as a block and indented as blocks. So space before opening parentheses
    and closing parentheses is on it's own line and indented to align with the parent.
  
    ```ballerina
  
    (string, int) tuple = (
        nameOfEmployee,
        ageOfTheEmployee
    );
  
    ```
## Function Invocation