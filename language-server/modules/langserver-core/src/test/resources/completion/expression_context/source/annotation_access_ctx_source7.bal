type AnnotationData record {|

|};

annotation AnnotationData MyAnnotation on record field;

public function main() {
    record {int x; int y;} r = {
        x: 1,
        y: 2
    };

    typedesc<record {}> td = typeof r;
    td.@
}
