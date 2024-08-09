import testorg/foo;
import ballerina/jballerina.java;

@foo:ConfigAnnotation {
    numVal: 10,
    textVal: "text",
    conditionVal: false,
    recordVal: { nestNumVal: 20, nextTextVal: "nestText" }
}
function someFunction(string arg) returns int {
    return 10;
}

function testNonBallerinaAnnotations() returns foo:SomeConfiguration? {
    var tDesc = typeof someFunction;
    return tDesc.@foo:ConfigAnnotation;
}

function testAnnotOnBoundMethod() {
    foo:MyClass c = new;
    typedesc t1 = typeof c.foo;
    assertEquality((), t1.@foo:ObjMethodAnnot);

    typedesc t2 = typeof c.bar;
    foo:OtherConfiguration? r = t2.@foo:ObjMethodAnnot;
    assertEquality(true, r is foo:OtherConfiguration);
    foo:OtherConfiguration rec = <foo:OtherConfiguration> r;
    assertEquality(102, rec.i);
}

function testAnnotOnRecordFields() {
    map<any> m = getLocalRecordAnnotations(typeof foo:testAnnotationsOnLocalRecordFields(), "$field$.x");
    assertEquality({value : "10"} , <map<anydata>>m["testorg/foo:1:annotOne"]);
    m = getLocalRecordAnnotations(typeof foo:testRecordFieldAnnotationsOnReturnType(), "$field$.x");
    assertEquality({value : "100"} , <map<anydata>>m["testorg/foo:1:annotOne"]);

}

function testAnnotOnTupleFields() {
    map<any> m = getAnonymousTupleAnnotations(typeof foo:testAnnotationsOnLocalTupleFields(), "$field$.0");
    assertEquality({value : "10"} , <map<anydata>>m["testorg/foo:1:annotOne"]);
    m = getAnonymousTupleAnnotations(typeof foo:testTupleFieldAnnotationsOnReturnType(), "$field$.0");
    assertEquality({value : "100"} , <map<anydata>>m["testorg/foo:1:annotOne"]);
    m = getAnonymousTupleAnnotations(typeof foo:g1, "$field$.0");
    assertEquality({value : "chiranS"} , <map<anydata>>m["testorg/foo:1:annotOne"]);
    m = getAnonymousTupleAnnotations(typeof foo:g1, "$field$.1");
    assertEquality({value : "k"} , <map<anydata>>m["testorg/foo:1:annotOne"]);
}

function getLocalRecordAnnotations(typedesc<any> obj, string annotName) returns map<any> =
@java:Method {
    'class: "org/ballerinalang/test/annotations/LocalRecordAnnotationTest",
    name: "getLocalRecordAnnotations"
} external;

function getAnonymousTupleAnnotations(typedesc<any> obj, string annotName) returns map<any> =
@java:Method {
    'class: "org/ballerinalang/test/annotations/AnonymousTupleAnnotationTest",
    name: "getAnonymousTupleAnnotations"
} external;

type AnnotationRecord record {|
    int minValue;
    int maxValue;
|};

annotation AnnotationRecord BarAnnotation on type;
annotation AnnotationRecord BarAnnotation2 on type;

@BarAnnotation {
    minValue: 18,
    maxValue: 36
}
type Bar int|float;
type Bar2 int|float;

@BarAnnotation2 {
    minValue: 18,
    maxValue: 36
}
type Bar3 Bar4;
type Bar4 Bar2;
type Bar5 Bar;

function testAnnotationsOnTypeRefTypes() {
    var f1 = function (typedesc td) returns AnnotationRecord? {
        return td.@BarAnnotation;
    };

    AnnotationRecord? 'annotation = f1(Bar);
    assertTrue('annotation is AnnotationRecord);
    AnnotationRecord rec = <AnnotationRecord> 'annotation;
    assertEquality(rec.minValue, 18);
    assertEquality(rec.maxValue, 36);

    'annotation = f1(Bar2);
    assertTrue('annotation is ());

    var f2 = function (typedesc td) returns AnnotationRecord? {
        return td.@BarAnnotation2;
    };

    'annotation = f2(Bar3);
    assertTrue('annotation is AnnotationRecord);
    rec = <AnnotationRecord> 'annotation;
    assertEquality(rec.minValue, 18);
    assertEquality(rec.maxValue, 36);

    'annotation = f2(Bar4);
    assertTrue('annotation is ());

    'annotation = f1(Bar5);
    assertTrue('annotation is ());

    'annotation = f2(Bar5);
    assertTrue('annotation is ());
}

@foo:annot {
    examples: foo:EXAMPLES
}
type Student record {|
    int id;
    string name;
|};

public const foo:Examples EXAMPLES_2 = {
    response: {
        headers: {
            "h1": "h1",
            "h2": "h2"
        }
    }
};

@foo:annot {
    examples: EXAMPLES_2
}
type Employee record {|
    int id;
    string name;
|};

function testConstAnnotationAccess() {
    Employee employee = {id: 1, name: "chirans"};
    typedesc<any> t = typeof employee;
    foo:AnnotationRecord? annot = t.@foo:annot;
    assertTrue(annot is foo:AnnotationRecord);
    foo:AnnotationRecord config = <foo:AnnotationRecord> annot;
    assertEquality({"response":{"headers": {"h1":"h1", "h2":"h2"}}}, config.examples);
    assertTrue(config.examples is readonly);

    Student student = {id: 2, name: "sachintha"};
    typedesc<any> s = typeof student;
    foo:AnnotationRecord? annot1 = s.@foo:annot;
    assertTrue(annot1 is foo:AnnotationRecord);
    foo:AnnotationRecord config1 = <foo:AnnotationRecord> annot1;
    assertEquality({"response":{}}, config1.examples);
    assertTrue(config1.examples is readonly);

    foo:Teacher teacher = {id: 1, name: "James"};
    typedesc<any> t1 = typeof teacher;
    foo:AnnotationRecord? annot2 = t1.@foo:annot;
    assertTrue(annot2 is foo:AnnotationRecord);
    foo:AnnotationRecord config2 = <foo:AnnotationRecord> annot2;
    assertEquality({"response":{}}, config2.examples);
    assertTrue(config2.examples is readonly);
}

function assertTrue(anydata actual) {
    assertEquality(true, actual);
}

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error(string `expected ${expected.toBalString()}, found ${actual.toBalString()}`);
}
