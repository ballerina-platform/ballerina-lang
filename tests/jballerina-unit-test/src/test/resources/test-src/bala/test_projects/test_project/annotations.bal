public type SomeConfiguration record {
    int numVal;
    string textVal;
    boolean conditionVal;
    record {   int nestNumVal;
        string nextTextVal;
    }   recordVal;
};

public annotation SomeConfiguration ConfigAnnotation on function;

public type OtherConfiguration record {
    int i;
};

public annotation OtherConfiguration ObjMethodAnnot on function;

public class MyClass {
    public function foo(int i) returns string => i.toString();

    @ObjMethodAnnot {i: 102}
    public function bar(int i) returns string => i.toString();
}

type AnnotRecordOne record {|
    string value;
|};

annotation AnnotRecordOne annotOne on type, field;

public function testAnnotationsOnLocalRecordFields() returns record {string x; string y;} {
    record {@annotOne {value: "10"} string x; string y;} r = {x : "", y: ""};
    return r;
}

public function testRecordFieldAnnotationsOnReturnType() returns record {@annotOne {value: "100"} string x; string y;} {
    return {x : "", y: ""};
}

public function testTupleFieldAnnotationsOnReturnType() returns [@annotOne {value: "100"} string] {
    return [""];
}

public function testAnnotationsOnLocalTupleFields() returns [@annotOne {value: "10"} string] {
    [@annotOne {value: "10"} string] r = [""];
    return r;
}
