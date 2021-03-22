import ballerina/module1;

public type AnnotationType record {
    string foo;
    int bar?;
};

public const annotation AnnotationType sourceListenerAnnotation1 on source listener;

public const annotation sourceListenerAnnotation2 on source listener;

@m
module1:Listener testListener = new(9090);
