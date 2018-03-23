$ ballerina run taint-checking.bal
error: ./Test1.bal:28:48: tainted value passed to sensitive parameter 'sqlQuery'
error: ./Test1.bal:32:32: tainted value passed to sensitive parameter 'securitySensitiveParameter'
compilation contains errors
