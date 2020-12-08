import ballerina/module1;

public type AnnotationType record {
    string foo;
    int bar?;
};

public annotation AnnotationType classAnnotation1 on class;

public annotation classAnnotation2 on class;

public const annotation classAnnotation3 on source class;

@module1:t
class TestClass {
    
}
