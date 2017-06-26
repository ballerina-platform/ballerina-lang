# To run the program, put the code in `errors.bal`
# and use `$BALLERINA_HOME/bin/ballerina run main errors.bal`.

$ $BALLERINA_HOME/bin/ballerina run main errors.bal
result 1 is 15
Error1, Not supported Operation, operation : multiply
result 2 is 50
Error2, Not supported Operation, operation : subtract
result 3 is 0
error: ballerina.lang.errors:Error, message:  / by zero
	at main(errors.bal:45)