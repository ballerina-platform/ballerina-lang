import ballerina/module1;

public type AnnotationType record {
    string foo;
    int bar?;
};

public const annotation AnnotationType sourceConstAnnotation1 on source const;

public const annotation sourceConstAnnotation2 on source const;

@
public const int TEST_CONST = 12;
