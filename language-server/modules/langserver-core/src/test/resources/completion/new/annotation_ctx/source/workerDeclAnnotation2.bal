import ballerina/module1;

public type AnnotationType record {
    string foo;
    int bar?;
};

public const annotation AnnotationType sourceWorkerAnnotation1 on source worker;

public const annotation sourceWorkerAnnotation2 on source worker;

public function main() {

    @m
    worker w1 {
        int b = 2;
    }

    worker w2 {
        int a = 1;
    }
}
