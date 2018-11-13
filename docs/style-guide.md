## Formatting guide to ballerina language

> Formatting is one of the most argued topic when it comes to a programming language. To avoid these arguments we decide to come up with a opinionated guide for Ballerina source code formatting. Users can follow their own formatting but we recommend to use this formatting guide as all the IDE tools will support only this format. Also following this guide will form a standard style across all developers. 

1. Top level indentation standard - no spaces
   >Top level code blocks like functions, services, endpoints, type definitions, imports, and global variables will be indent with an empty space hence they will be indent to be in 1st column.
2. Block Indentation Standard - 4 spaces
   > We are using 4 spaces when indenting. Reason to use spaces instead of tab is spaces are consistent across different platforms when tab size varies.
3. Column limit - 120
   > Maximum column limit is 120 if exceeded exceeding line should be break in to multiple lines (line-wrapped). 
4. One statement per line
   > Two statements cannot be on the same line so each statement is followed by a line break.
5. Whitespace
    1. Vertical whitespace
       > Single new line will always appear. Single blank line will appear between statements where it improves the readability. It is neither encourage nor discourage to limit the number of blank lines between statements or blocks but we recommend to use blanks lines as below.
       -  It is best to have a new line between different type of top level blocks such as `import`, `global` `variables`, `functions`, `services`, `endpoints` and `type definitions`. 
       -  It is best to have a new line between statements (such as variable definition) and block statements (such as if statement).
    2. Horizontal whitespace
       >  It is recommend to follow the following rules when applying white spaces on a line. Following will list down where a single whitespace can apply.
        -  Keywords such as `function`, `endpoint`, `service`, `type` , `if`, `while`, `catch`, `finally`, `try`, `transaction` and `any type` should followed by a single space. Exceptions are as below.
            1.  For `service` keyword single space is added only if service definition doesn’t have a service type.
            2.  For types like map which followed by `<>`
            3.  When types are used in a array definition which followed by `[ ]`
        -  Any `binary` and `ternary` operator should start and end with a Single space. Exceptions are as below.
            1.  This will be ignore if the operator is rest parameter (`string...`).
            2.  sealed types (`!...`)
            3.  post and pre increment (`++`) and decrement (`--`).
        -  `:` will be start with empty space and followed by single space if it is a key value pair except
            1.  in package invocation it is start with and followed by a empty space.
            2.  Also this rule is ignored when it is a ternary operation.
        -  Before opening curly brace there should be a space. Exceptions to this as below.
            1.  In XML referencing a xml namespace as a attribute
            2.  table column expression
                ```ballerina
                table {
                  {key id, name, address}
                }
                ```
            3.  table data expression
                ```ballerina
                table {
                  {id, name, address},
                  [{"1", "test", "No:123 hty RD"}]
                }
                ```
        -  Before opening bracket in a array literal there should be a space. Exception to this is as below.
            1.  Opening bracket of a table data list fronted by a new line and indented relative to parent.
            2.  Opening bracket of a array variable value access, rest variable value access and map variable reference.
            3.  Opening bracket of a xml attribute add `x1@[“foo”] = “bar”`
6. Braces
   > It is recommended to format braces with the `Kernighan and Ritchie’s style (“Egyptian brackets”)`. Opening brace should be in the end of the current line and closing brace in the next.
    1. Empty blocks
       > Empty blocks are recommended to follow below rules.
          - No line breaks before opening brace. Only single space is recommended.
          - Line break after opening brace.
          - Blank line between opening and closing braces.
          - Line break before closing brace.
          - Closing brace should be align with the start position of the opening brace’s line.
    2. Non-Empty blocks
       > Non Empty blocks are recommended to follow below rules.
          - No line breaks before opening brace. Only single space is recommended.
          - Line break after opening brace.
          - Line break before closing brace.
          - Closing brace should be align with the start position of the opening brace’s line. 
    3. Brace usage
       > There are places where braces required and optional. Both of these type of usages follow the same formatting style except in table data and table column. Please look in specific constructs for more information on how to style these two syntaxes.
7. Parentheses
    1. Grouping parentheses
       > Applying parentheses for grouping expressions is optional. Using grouping parentheses will increase the readability and reduce misinterpretation as this can change the operator precedence.
    2. Empty parentheses
       > Empty parentheses are recommended to follow below rules.
          - No line break before the opening parentheses. Only single space is recommended. This rule will be ignored and use empty space when the construct is function, resources and function calls. Also this rule will be ignored if the construct is tuple destructure statement as it fronted by a line break.
          - Empty space before closing parentheses.
    3. Non-empty parentheses
       > Non-empty parentheses are recommended to follow below rules.
          - No line breaks before the opening parentheses. Only single space is recommended. This rule will be ignored and use empty space when the construct is function, resources and function calls.Also this rule will be ignored if the construct is tuple destructure statement as it fronted by a line break.
          - No line breaks before the closing parentheses.
          - Empty space before the closing parentheses.
    4. Parentheses usage
       > There are places where parentheses required and optional. These places will follow the same rules described in previous sections when they have parentheses. 
8. Where to break
   > It is recommended to break the line when it reaches the maximum column size. But when break it is recommended to follow below rules.
      - If it is a binary or ternary operation break before operators
      - If it is a dotted operation break before the dot operator.
      - If it is a comma separated list break before comma.
      - If it is a pipe (`|`) separated statement such as union or type definition break before pipe.
   > When break remove all the horizontal white spaces available in front of the breaking point and add a new line.
9.  Horizontal alignment
    > Horizontal alignment is only consider when writing comments and documentations.
      - Documentation must be aligned with the parent definition’s start
      - Comments must be align with the following statement or definition’s start. (TBD)
10. Specific Operators
    1.  Double Colon (`::`)
        > This is used only when implementing an abstract function of a object and to bind the implementation of the function to the object.
        ```ballerina
        function ObjectName::getName(int a, int b) {
             
        }
        ```
    2.  Ellipsis (`...`)
        >  This is used to define rest parameters, rest arguments, sealed types, record rest field, integer range.
    3.  Elvis (`?:`)
        >  This is used as a binary operation. This will start and followed by single space. 
    4.  Closed range (`..<`)
        > This is only used in integer range definition to define closed integer range. There shouldn’t be any whitespace associated at the start or at the end of this operator.  