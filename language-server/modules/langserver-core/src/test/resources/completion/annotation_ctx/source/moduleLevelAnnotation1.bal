public type AnnotationType record {
    string foo;
    int bar?;
};

public annotation AnnotationType annotationCommon;

public annotation AnnotationType a1 on service;

public annotation AnnotationType a2 on function;

annotation AnnotationType a3 on service;

@