import ballerina/io;

function foo(string|int|float|error a) returns (string|int|float|error) {
    return a;
}

function getError() returns error {
    return {message: "test error"};
}  

function testMatchExpr(string|int|float|error a) returns string {
    string x = foo(a) but { int => "value1", 
                            float => "value2", 
                            error => "value3",
                            string => "value4"  
                };
    return x;
}

function testMatchExprWithImplicitDefault(string|int|float|error a) returns string {
    string x = foo(a) but { int => "value1", 
                            float => "value2", 
                            error => "value3" 
                };
    return x;
}

function testMatchExprInUnaryOperator(string|int|float|error a) returns string {
    string x = "HELLO "  + <string> (foo(a) but { int => "value1", 
                                                  float => "value2", 
                                                  error => "value3" 
                });
    return x;
}

function testMatchExprInBinaryOperator(string|int|float|error a) returns string {
    string x = "HELLO "  + (foo(a) but { int => "value1", 
                                         float => "value2", 
                                         error => "value3" 
                });
    return x;
}

function testMatchExprInFuncCall(string|int|float|error a) returns string {
    string x = bar(foo(a) but { 
                    int => "value1", 
                    float => "value2", 
                    error => "value3" 
                });
    return x;
}

function bar(string b) returns string {
    return b;
}

function fooWithNull(string|int|float|error|() a) returns (string|int|float|error|()) {
    return a;
}

function testNestedMatchExpr(string|int|float|error|() a) returns string {
    string x = bar(fooWithNull(a) but { 
            int => "value1", 
            float => "value2", 
            string|() b => b but {
                    string => "value is string",
                () => "value is null"
            },
            error => "value3"
    });
    return x;
}
