import ballerina/module1;

function test() returns error?{
    int? result = (check testCheck())?.
}

function testCheck() returns Type2|error {
    return {field1: 0,
    field2: 0};
}

public type Type2 record {
    int field1;
    int field2;
}
