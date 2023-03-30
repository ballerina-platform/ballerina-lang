type AnnotationData record {|

|};

annotation AnnotationData MyAnnotation1 on record field;
annotation AnnotationData MyAnnotation2 on type;

public function main() {
    record {int x; int y;} r = {
        x: 1,
        y: 2
    };

    typedesc<record {}> td = typeof r;
    td.@
}
