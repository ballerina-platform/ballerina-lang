import ballerina/module1;

public type AnnotationType record {
    string foo;
    int bar?;
};

public annotation AnnotationType parameterAnnotation1 on parameter;

public annotation pa2 on parameter;

function name(int a, int b = 12, @module1: string... rest) returns int {
    return 12;
}
