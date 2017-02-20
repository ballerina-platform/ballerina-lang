# Expressions

Similar to languages such as Java, Go, etc, Ballerina expressions can be of various types.

Basic expression grammar is as follows:

```
expression
    :   literalValue                                    # literalExpression
    |   variableReference                               # variableReferenceExpression
    |   backtickString                                  # templateExpression
    |   functionName argumentList                       # functionInvocationExpression
    |   actionInvocation argumentList                   # actionInvocationExpression
    |   '(' typeName ')' expression                     # typeCastingExpression
    |   ('+' | '-' | '!') expression                    # unaryExpression
    |   '(' expression ')'                              # bracedExpression
    |   expression '^' expression                       # binaryPowExpression
    |   expression ('/' | '*' | '%') expression         # binaryDivMulModExpression
    |   expression ('+' | '-') expression               # binaryAddSubExpression
    |   expression ('<=' | '>=' | '>' | '<') expression # binaryCompareExpression
    |   expression ('==' | '!=') expression             # binaryEqualExpression
    |   expression '&&' expression                      # binaryAndExpression
    |   expression '||' expression                      # binaryOrExpression
    |   '[]'                                            # arrayInitExpression
    |   '[' expressionList ']'                          # arrayInitExpression // couldn't match empty arrays with:  '[' expressionList? ']' hence writing in two branches
    |   '{' mapStructInitKeyValueList? '}'              # refTypeInitExpression
    |   'create' typeName argumentList                  # connectorInitExpression
    ;
```

> NOTE: This section is not yet complete; see grammar file for syntax specification.
