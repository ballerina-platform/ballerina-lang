import ballerina/module1;

function matchTest(any v) returns string {
    int[] arr = [];
    
    int|error hello = testFunc();
}

function testFunc() returns int|error {
    return 1234;
}
