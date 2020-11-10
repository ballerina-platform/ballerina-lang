import ballerina/module1;

public type AnnotationType record {
    string foo;
    int bar?;
};

public const annotation AnnotationType sourceAnnotationAnnotation1 on source annotation;

public const annotation sourceAnnotationAnnotation2 on source annotation;

@
public annotation fieldAnnotation1 on field;
