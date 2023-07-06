import ballerina/module1;

public type AnnotationType record {
    string foo;
    int bar?;
};

public annotation AnnotationType typeAnnotation1 on type;

public const annotation typeAnnotation2 on type;

public annotation typeAnnotation3 on type;

@module1:t
public type TEST_TYPE int;
