$ ballerina run taint-checking.bal
error: ./taint-checking.bal:37:12: tainted value passed to sensitive parameter 'sqlQuery'
error: ./taint-checking.bal:42:32: tainted value passed to sensitive parameter 'secureParameter'
error: ./taint-checking.bal:63:36: tainted value passed to sensitive parameter 'secureParameter'
error: ./taint-checking.bal:69:36: tainted value passed to sensitive parameter 'secureParameter'
compilation contains errors
