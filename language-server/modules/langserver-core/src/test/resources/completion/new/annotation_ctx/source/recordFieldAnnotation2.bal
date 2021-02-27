import ballerina/module1;

public type AnnotationType record {
    string foo;
    int bar?;
};

public annotation AnnotationType fieldAnnotation1 on field;

public annotation fieldAnnotation2 on field;

public annotation AnnotationType objectFieldAnnotation1 on object field;

public annotation objectFieldAnnotation2 on object field;

public annotation AnnotationType recordFieldAnnotation1 on record field;

public annotation recordFieldAnnotation2 on record field;

type RecordName record {
    @m
    int field1?;
};
