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

function testMatchExprWithNonMatchingPatterns(string|int|float|error a) returns string {
    string x = foo(a) but { 
                    int => "value1", 
                    map => "value2", 
                    string => "value3",
                    json j => "value4"
                };
    return x;
}

function testMatchExprWithMissingPatterns(string|int|float|error|map a) returns string {
    string x = a but { 
                    int => "value1",
                    map j => "value2"
                };
    return x;
}


function testIncompatibleTypesInPatterns(string|json a) returns string {
    string x = a but { 
                    json j => j + "value4"
                };
    return x;
}
