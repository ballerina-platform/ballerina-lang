# Expressions

## Record Literals

* If record literal is empty
  - Both braces should be placed on the same line as the record.
  - There shouldn't be any spaces between opening and closing brace.
      
    ```ballerina
    Person p = {};
    ```
* If record literal is not empty closing brace should be put on its
  own line and indent and aligned with the start position of the statement.
  
  ```ballerina
  Person p = {
      name: "john",
      age: 20
  };
  ```
### Fields
* In a record literal fields can be arranged on a single line. Then
  - A record field shouldn't have any spaces before comma.
  - There Should be only one space after comma.
      
    ```ballerina  
    Person p = {name: "john", age: 20}; 
    ``` 
* There shouldn't be any spaces between key and colon.
* There should be only one space between the colon and the value.
  
  ```ballerina
  Person person = {
      name: "john", // in this field Key is the "name" and value is "john".
  };
  ```
* If at least one field is splitted in to a new line then all the fields should 
  split in to new lines. For a example if record literal is as below
      
  ```ballerina
      
  Person p = {name: "john",
  age: 20}; 
      
  ``` 
  then all the fields should be move in to new lines and should be block-indented.
      
  ```ballerina
  Person p = {
      name: "john",
      age: 20
  };
  ``` 
## Map Literal

* Map literals uses the same formatting guidelines as [record literals](#record-literals). 

## Tuple

* Always try to Arrange tuple on a single line.

  ```ballerina

  (string, int) tuple = ("john", 20);

  ```
* If it is not possible to arrange tuple on a single line, may be with line length limit,
  each field in the tuple should be on its own block-indented line.
  
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

* Always try to keep function invocation in a single line.
* No spaces between function name and the opening parentheses `(`,
  opening parentheses and first argument and last parentheses and the last parentheses.
* No space between argument and the trailing comma `,`

  ```ballerina
  setAgeForEmployee(employeeName, employeeID);
  ```
* When invoking a function from another module there are no spaces around `:`.
  
  ```ballerina
  io:println("john");
  ```
  
* If unable to keep the function invocation on a single line, due to exceeding max line length,
  each argument should be splitted in to its own block-indented line.
  
  - Trailing comma always should be on a end of the line and inline with a argument.
  - No space before opening parentheses
  - closing parentheses is on it's own line and indented to align with the start 
    position of the statement.
    
    ```ballerina
    
    setAgeForEmployee(
        employeeName,
        employeeID
    );
    
    ```
## Array Literals

* For simple arrays should placed on a single line.
  - No spaces around the brackets.
  - No spaces between the opening bracket and value.
  - No spaces between the closing bracket and value.
  - No spaces between a value and a trailing comma.
  
  ```ballerina
  string[] names = ["john", "doe", "jane", "doe"];
  ```
* If array cannot be placed on a single line, due to exceeding the max line length,
  each value should be splitted in to its own block-indented line.
  
  - Trailing comma always should be on a end of the line and inline with a value.
  - Single space before the opening bracket.
  - Closing bracket is on it's own line and indented to align with the parent.
    
    ```ballerina
    
    string[] names = [
        "john",
        "doe",
        "jane",
        "doe"
    ];
    
    ```
## Type Casting

* No spaces around angle brackets `<>`.
* No spaces around the type. `<string>`.
* No spaces between the closing angle bracket and reference to be converted.

  ```ballerina
  string name = <string>json.name;
  ```
## Table Literal
* Elements in table literal each should block-indent on its own line.
* Table column definition should be place on a single line.
  
  ```ballerina
      table<Employee> employee = table {
          {key id, name, address}
      };
  ```
* If table column definition cannot be put on a single line, due to exceeding max line length,
  table column definition should split in to a new line just after a comma and indent related 
  to start position of the table column definition.
  
  ```ballerina
  table<Employee> employee = table {
      {key id,
          name, address},
      [{"1", "test", "No:123 hty RD"}]
  };
  ```
  
* Table data array should be placed on a single line. 
  
  ```ballerina
  table<Employee> employee = table {
      {id, name, address},
      [{"1", "test", "No:123 hty RD"}]
  }
  ```
* If table data array cannot be put on a single line, due to exceeding max line length,
  each table data should be block-indented on it own line.
  
  ```ballerina
  table<Employee> employee = table {
      {id, name, address},
      [
          {"1", "john", "No:123"},
          {"2", "jane", "No:342"}
      ]
  }
  ```
