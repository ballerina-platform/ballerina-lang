import ballerina/module1;

function getTDesc() returns typedesc<service> {
    service ser = @a1 {
        foo: "a1"
    } service {
        resource function res() {
        }
    };

    typedesc<service> td = typeof ser;
    
    return td;
}

function testAnnotationAccess() {
    string message = "This is a test message!";
    getTDesc().@
    int testInt = 123;
}

type AnnotationType record {
    string foo;  
    int bar?;
};

annotation AnnotationType a1 on service;

