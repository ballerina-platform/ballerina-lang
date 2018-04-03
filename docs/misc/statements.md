# Statements

A statement can be one of the following:

- assignment
- if
- while
- break
- fork/join
- try/catch and throw: see [Exception Handling](exceptions.md)
- return
- reply
- worker initiation/invocation/join: see [Workers](workers.md)
- action invocation: see [Actions](actions.md)
- comment

The rest of this page describes the statements you can use in your Ballerina program. 

## Assignment

An assignment statement allows you to assign a value to a variable accessor. In the Composer, you can drag the Assignment icon ![alt-text](../images/icons/assign.png "Assignment icon") from the tool palette to the canvas to add the statement to your program. 

An assignment statement is defined as follows:

```
VariableAccessor = Expression;
```

where a `VariableAccessor` is one of:

- VariableName
- VariableAccessor'['ArrayIndex']'
- VariableAccessor'['MapIndex']'
- VariableAccessor.FieldName

## If-else

An `if-else` statement provides a way to perform conditional execution. In the Composer, you can drag the If-Else icon ![alt-text](../images/icons/if-else.png "If-Else icon") from the tool palette to the canvas to add the statement to your program. 

An `if-else` statement is defined as follows: 
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

## While

A `while` statement provides a way to execute a series of statements as long as a Boolean expression is met. In the Composer, you can drag the While icon ![alt-text](../images/icons/while.png "While icon") from the tool palette to the canvas to add the statement to your program. 

A `while` statement is defined as follows:

```
while (BooleanExpression) {
    Statement;+
}
```

## Break

A `break` statement allows you to terminate the immediately enclosing loop. This is only allowed within the `while` construct. In the Composer, you can drag the Break icon ![alt-text](../images/icons/break.png "Break icon") from the tool palette to the canvas. 

A `break` statement is defined as follows:

```
break;
```

## Fork/Join

Workers are independent actors that do not necessarily need to know about the existence of each other. However, there are some situations where you need to execute workers so that they work on pieces of a given task and complete it as a unit to fullfill the entire task. Fork-Join construct is built for this kind of scenario where you need to wait until all the workers have completed their tasks or have timed out. 

A `fork` statement allows you to replicate a message to any number of parallel workers and have them independently operate on the copies of the message. The `join` part of the `fork` statement allows you to define how the caller of `fork` will wait for the parallel workers to complete. 

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

In this scenario, `WorkerNameList` is a list of comma-separated names of workers.

> **Note:** The join condition "any k" where k != 1 is not yet implemented.

When the `JoinCondition` has been satisfied, the corresponding slots of the message array will be filled with the returned messages from the workers in the workers' lexical order. If the condition asks for up to some number of results to be available to satisfy the condition, it may be the case that more than that number are available by the time the statements within the join condition are executed. If a particular worker has completed but not sent a response message, or not yet completed, the corresponding message slot will be null.

The `timeout` clause allows one to specify a maximum time (in seconds) within which the join condition must be satisfied.

## Return

The Return statement evaluates the expression, stops the current function, and returns the result of the expression to the caller. In the Composer, you can drag the Return icon ![alt-text](../images/icons/return.png "Return icon") from the tool palette to the canvas to add the statement to your program. 

A `return` statement is defined as follows:

```
return Expression*;
```

## Reply

The Reply statement sends the request message back to the client. In the Composer, you can drag the Reply icon ![alt-text](../images/icons/reply.png "Reply icon") from the tool palette to the canvas to add the statement to your program. 

A `reply` statement is defined as follows:

```
reply Message?;
```

Note that when you use Reply, the request message with its original HTTP headers is sent back to the client. These headers typically have information that is useful in the client -> server direction but not in the server -> client direction. If you want to strip those headers before sending the message back to the client, you can use the `convertToResponse` function from the `ballerina.net.http` package.

## Comment

Comments are quite different in Ballerina in comparison to other languages. Comments are only allowed as a statement, i.e., only inside a resource, action, or function.

Ballerina has designed structured mechanisms via annotations to document all outer-level Ballerina constructs (services, resources, etc.), and comments only play the role of providing a comment about the logic of a resource, action, or function.

Any statement that starts with the characters `//` is a comment.
