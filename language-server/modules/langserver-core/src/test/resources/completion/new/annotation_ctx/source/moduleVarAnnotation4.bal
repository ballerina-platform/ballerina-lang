import ballerina/module1;

public type AnnotationType record {
    string foo;
    int bar?;
};

public const annotation AnnotationType sourceVarAnnotation1 on source var;

public const annotation sourceVarAnnotation2 on source var;

@module1:s
int testModuleVar = 12;
