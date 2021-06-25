import ballerina/module1;

public type AnnotationType record {
    string foo;
    int bar?;
};

public annotation AnnotationType parameterAnnotation1 on parameter;

public annotation pa2 on parameter;

function name(@m int a, int b = 12, string... rest) returns int {
    return 12;
}
