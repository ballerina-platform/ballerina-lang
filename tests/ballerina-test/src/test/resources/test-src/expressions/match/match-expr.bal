import ballerina/io;

function testMatchExpr() {
    string x = "HELLO "  + <string> (foo() but { int => "default1", 
                            float => "default2", 
                            error => "default3" 
                });
    io:println("result: " + x);
}

function foo() returns (string|int|float|error) {
	return 1;
}