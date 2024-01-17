type AnnotationData record {|

|};

annotation AnnotationData MyAnnotationService on service;

annotation AnnotationData MyAnnotationObjectField on object field;

annotation AnnotationData MyAnnotationReturn on parameter;

annotation AnnotationData MyAnnotationClass on class;

class MyClass {
    int n;

    function init(int n) {
        self.n = n;
    }
}

public function main() {
    MyClass x = new MyClass(1234);

    typedesc<typedesc<MyClass>> td = typeof MyClass;
    td.@
}
