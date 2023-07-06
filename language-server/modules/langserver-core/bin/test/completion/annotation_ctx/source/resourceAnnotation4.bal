import ballerina/module1;

listener module1:Listener testListener = new(9090);

public type AnnotationType record {
    string foo;
    int bar?;
};

public const annotation AnnotationType resourceAnnotation1 on function;

public const annotation AnnotationType resourceAnnotation2 on object function;

public const annotation resourceAnnotation3 on object function;

service on testListener {
    @module1:a
    resource function testRemoteFunction() {
        
    }
}

public function getInt() returns int {
    return 899;
}
