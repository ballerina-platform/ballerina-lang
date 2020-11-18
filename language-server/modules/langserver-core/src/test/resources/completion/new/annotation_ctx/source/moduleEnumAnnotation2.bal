import ballerina/module1;

public type AnnotationType record {
    string foo;
    int bar?;
};

public annotation AnnotationType typeAnnotation1 on type;

public const annotation typeAnnotation2 on type;

public annotation typeAnnotation3 on type;

@m
public enum TestEnum {
    ENUM_FIELD1
}
