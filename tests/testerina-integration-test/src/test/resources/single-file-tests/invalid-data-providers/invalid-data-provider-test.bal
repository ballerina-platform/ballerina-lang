import ballerina/test;
import ballerina/io;

// tests an invalid data provider

@test:Config{
    dataProvider:"invalidDataGen"
}
function testInvalidDataProvider(string result) {
            
    io:println("Input params: ["+result.toString()+"]");
    int|error resultErr = trap result.cloneWithType(int);
}

function invalidDataGen() returns (int[][]) {
    return [[1]];
}
