type AnnotationData record {|

|};

annotation AnnotationData MyAnnotationParameter on parameter;

annotation AnnotationData MyAnnotationObjectFunction on object function;

annotation AnnotationData MyAnnotationService on service;

annotation AnnotationData MyAnnotationClass on class;

function testFunction(@MyAnnotationParameter int a = 1) returns int {
    return 0;
}

public function main() {
    typedesc<any> td = typeof testFunction();
    AnnotationData? unionResult = td.@;
}
