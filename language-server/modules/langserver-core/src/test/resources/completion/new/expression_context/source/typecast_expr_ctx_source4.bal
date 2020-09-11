import ballerina/module1;

function getTDesc() returns int {
    int value1;
    string value2 = <> value1
    
    int value3 = 12;
    return value3;
}

type AnnotationType record {
    string foo;  
    int bar?;
};

type ErrorName error<map<anydata>>;

annotation AnnotationType a1 on service;

