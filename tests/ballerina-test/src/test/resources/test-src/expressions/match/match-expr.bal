function testMatchExpr() {
    string x = foo() but { int => "default1", 
                            float => "default2", 
                            error => "default3" 
                };
}

function foo() returns (string|int|float|error) {
	return 4;
}