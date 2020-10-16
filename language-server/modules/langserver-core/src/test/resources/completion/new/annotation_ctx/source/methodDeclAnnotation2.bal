import ballerina/module1;

public type AnnotationType record {
    string foo;
    int bar?;
};

public annotation AnnotationType functionAnnotation1 on function;

public annotation functionAnnotation2 on function;

public annotation AnnotationType objectFunctionAnnotation1 on object function;

public annotation objectFunctionAnnotation2 on object function;


type TestObject object {
    @m
    function testFunction();
};
