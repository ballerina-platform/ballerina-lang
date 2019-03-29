# Expressions

## Record literals

* If a record literal is empty,
  - place both braces on the same line of the record.
  - do not keep any spaces between the opening and closing braces.
      
    Example,
    ```ballerina
    Person p = {};
    ```
* If a record literal is not empty, put the closing brace on its
  own line. Also, indent and alignedalign it with the starting position of the statement.
  
  Example,
  ```ballerina
  Person p = {
      name: "john",
      age: 20
  };
  ```
### Fields
* In a record literal, fields can be arranged on a single line. Then,
  - do not keep spaces in a record field before the comma.
  - keep only one space after the comma.
      
    Example,
    ```ballerina  
    Person p = {name: "john", age: 20}; 
    ``` 
* Do not keep any spaces between the key and the colon.
* Keep only one space between the colon and the value.
  
  Example,
  ```ballerina
  Person person = {
      name: "john", // in this field Key is the "name" and value is "john".
  };
  ```
* If at least one field is splitted on to a new line, then split all the fields to new lines. 
      
  For example, if the record literal is as follows,
  ```ballerina
      
  Person p = {name: "john",
  age: 20}; 
      
  ``` 
  then, move all the fields to new lines and indent each field as a block as follows.
  
  ```ballerina
  Person p = {
      name: "john",
      age: 20
  };
  ``` 
## Map literal

* For Map literals, follow the same formatting guidelines as [record literals](#record-literals). 

## Tuple

* Always, place a tuple in a single line.

  Example,
  ```ballerina

  (string, int) tuple = ("john", 20);

  ```
* If it is not possible to place a tuple on a single line due to it exceeding the maximum line length limit,
  put each field in the tuple on its own block-indented line. Also,
  
  - always, add the trailing comma at the end of a line and inline with a field.
  - Parentheses act as a block and are indented as blocks. Thereofre, keep the space before the opening parentheses
    and closing parentheses on it's own line and indent it to align it with the parent.
  
    Example,
    ```ballerina
  
    (string, int) tuple = (
        nameOfEmployee,
        ageOfTheEmployee
    );
  
    ```
## Function invocation

* Always, place the function invocation in a single line.
* Do not keep spaces between the function name, opening parentheses `(`, first argument, and closing parentheses.
* Do not keep spaces between the argument and the trailing comma `,`

  Example,
  ```ballerina
  setAgeForEmployee(employeeName, employeeID);
  ```
* When invoking a function from another module, do not keep spaces around `:`.
  
  Example,
  ```ballerina
  io:println("john");
  ```
  
* If it is unable to keep the function invocation in a single line due to it exceeding the max line length,
  split each argument on to its own block-indented line.
  
  - Always, place the trailing comma at the end of the line and inline with an argument.
  - Do not keep space before the opening parentheses.
  - Keep the closing parentheses on it's own line and indent it to align with the starting 
    position of the statement.
    
    Example,
    ```ballerina
    
    setAgeForEmployee(
        employeeName,
        employeeID
    );
    
    ```
## Array literals

* Place simple arrays in a single line. Also,
  - do not keep any spaces between the opening bracket and value.
  - do not keep any spaces between the closing bracket and value.
  - do not keep any spaces between a value and a trailing comma.
  
    Exampole,
    ```ballerina
    string[] names = ["john", "doe", "jane", "doe"];
    ```
* If an array cannot be placed on a single line due to it exceeding the max line length,
  split each value in the array to its own block-indented line. Also,
  
  - place the trailing comma at the end of the line and inline with a value.
  - place the opening bracket on the same line where the statement starts.
  - place the closing bracket on it's own line and indent it to align with the starting 
    position of the statement.
    
    Example,
    ```ballerina
    
    string[] names = [
        "john",
        "doe",
        "jane",
        "doe"
    ];
    
    ```
## Type casting

* Do not keep spaces between the type and the angle brackets (i.e., `<string>`).
* Do not keep spaces between the closing angle bracket and value reference, which will be be casted.

  Example,
  ```ballerina
  string name = <string>json.name;
  ```
## Table literal
* In a table literal, indent the table column and table data array as a block in its own line.
* Place the table column definition in a single line.
  
  Example,
  ```ballerina
      table<Employee> employee = table {
          {key id, name, address}
      };
  ```
* If it is unable to have the table column definition in a single line due to it exceeding the max line length,
  split the table column definition to a new line just after a comma and indent it with relation 
  to the starting position of the table column definition.
  
  Example,
  ```ballerina
  table<Employee> employee = table {
      {key id,
          name, address},
      [{"1", "test", "No:123 hty RD"}]
  };
  ```
  
* Place table data array in a single line. 
  
  Example,
  ```ballerina
  table<Employee> employee = table {
      {id, name, address},
      [{"1", "test", "No:123 hty RD"}]
  }
  ```
* If it is unable to keep the table data array in a single line due to it exceeding the max line length,
  indent each table data as a block in it own line.
  
  Example,
  ```ballerina
  table<Employee> employee = table {
      {id, name, address},
      [
          {"1", "john", "No:123"},
          {"2", "jane", "No:342"}
      ]
  }
  ```
