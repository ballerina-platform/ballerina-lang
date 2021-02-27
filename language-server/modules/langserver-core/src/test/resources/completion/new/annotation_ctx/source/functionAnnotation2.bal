import ballerina/module1;

@m
function testFunctionAnnotations() {
    
}

public type AnnotationType record {
    string foo;
    int bar?;
};

public annotation AnnotationType functionAnnotation1 on function;

public annotation functionAnnotation2 on function;
