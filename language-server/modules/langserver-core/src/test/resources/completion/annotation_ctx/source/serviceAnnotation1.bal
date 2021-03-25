import ballerina/module1;

listener module1:Listener testListener = new(9090);

public type AnnotationType record {
    string foo;
    int bar?;
};

public const annotation AnnotationType serviceAnnotation1 on service;

public const annotation serviceAnnotation2 on service;

@
service on testListener {
    remote function testRemoteFunction() {
        
    }
}

public function getInt() returns int {
    return 899;
}
