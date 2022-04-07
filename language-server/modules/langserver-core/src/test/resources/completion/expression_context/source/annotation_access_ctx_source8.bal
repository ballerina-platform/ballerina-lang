type AnnotationData record {|

|};

annotation AnnotationData MyAnnotation on object field;

class MyClass {
    int n;

    function init(int n) {
        self.n = n;
    }
}

public function main() {
    MyClass x = new MyClass(1234);

    typedesc<object {}> td = typeof x;
    td.@
}
