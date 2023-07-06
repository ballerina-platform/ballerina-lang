import ballerina/module1;

public type AnnotationType record {
    string foo;
    int bar?;
};

public annotation AnnotationType returnAnnotation1 on return;

public annotation returnAnnotation2 on return;

function name() returns @module1: int {
    
}