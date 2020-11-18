import ballerina/module1;

public type AnnotationType record {
    string foo;
    int bar?;
};

public const annotation AnnotationType sourceWorkerAnnotation1 on source worker;

public const annotation sourceWorkerAnnotation2 on source worker;

public function getFuture() returns future<int> {
    return @module1:s start getInt();
}

public function getInt() returns int {
    return 899;
}
