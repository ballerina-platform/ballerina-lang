# Statements

A statement can be one of the following:

- assignment statement
- if statement
- iterate statement
- while statement
- break statement
- fork/join statement
- try/catch statement
- throw statement
- return statement
- reply statement
- worker initiation statement
- worker join statement
- action invocation statement
- comment statement

## Assignment statement

Assignment statements are encoded as follows:
```
VariableAccessor = Expression;
```
A `VariableAccessor` is one of:
- VariableName
- VariableAccessor'['ArrayIndex']'
- VariableAccessor'['MapIndex']'
- VariableAccessor.FieldName

## If statement

An `if` statement provides a way to perform conditional execution.
```
if (BooleanExpression) {
    Statement;*
}
[else if (BooleanExpression) {
    Statement;*
}]* [else {
    Statement;*
}]
```

## Iterate statement

An `iterate` statement provides a way to iterate through an iterator.
```
iterate (VariableType VariableName : Iterator) {
  Statement;+
}
```

## While statement

A `while` statement provides a way to execute a series of statements as long as a Boolean expression is met. 
```
while (BooleanExpression) {
    Statement;+
}
```

## Break statement

A `break` statement allows one to terminate the immediately enclosing loop.
This is only allowed within the `iterate` or `while` constructs.
```
break;
```

## Fork/Join statement

A `fork` statement allows one to replicate a message to any number of parallel
workers and have them independently operate on the copies of the message. The `join`
part of the `fork` statement allows one to define how the caller of `fork`
will wait for the parallel workers to complete.

```
fork (MessageName) {
  worker WorkerName (message VariableName) {
    VariableDeclaration;*
    Statement;+
    [reply MessageName;]
  }+       
} [join (JoinCondition) (message[] VariableName) {
  Statement;*
} timeout (Expression) (message[] VariableName) {
  Statement;*  
}]
```
Note that if the `join` clause is missing, it is equivalent to waiting for all workers to complete and ignorning the results.

The `JoinCondition` is one of the following:
- `any IntegerValue [(WorkerNameList)]`: wait for any k (i.e., the IntegerValue) of the given workers or any of the workers
- `all [(WorkerNameList)]`: wait for all given workers or all of the workers

where `WorkerNameList` is a list of comma-separated names of workers.

When the `JoinCondition` has been satisfied, the corresponding slots of the message array will be filled with the returned messages from the workers in the workers' lexical order. If the condition asks for up to some number of results to be available to satisfy the condition, it may be the case that more than that number are available by the time the statements within the join condition are executed. If a particular worker has completed but not sent a response message, or not yet completed, the corresponding message slot will be null.

The `timeout` clause allows one to specify a maximum time (in milliseconds) within which the join condition must be satisfied.
