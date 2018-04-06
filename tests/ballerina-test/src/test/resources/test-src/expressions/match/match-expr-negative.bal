import ballerina/io;

function foo(string|int|float|error a) returns (string|int|float|error) {
    return a;
}

function testMatchExprWithMultipleReturnTypes(string|int|float|error a) returns string {
    string x = foo(a) but { int => 5, 
                            float => 5.7, 
                            error => "value3",
                            string => "value4"  
                };
    return x;
}

function testMatchExprWithUndefinedVar(string|int|float|error a) returns string {
    string x = foo(a) but { 
                    int => "value1", 
                    float => "value2", 
                    error => "value3",
                    string => s + "hello"
                };
    return x;
}
