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
    map<any> m = getLocalTupleAnnotations(typeof foo:testAnnotationsOnLocalTupleFields(), "$field$.0");
    assertEquality({value : "10"} , <map<anydata>>m["testorg/foo:1:annotOne"]);
    m = getLocalTupleAnnotations(typeof foo:testTupleFieldAnnotationsOnReturnType(), "$field$.0");
    assertEquality({value : "100"} , <map<anydata>>m["testorg/foo:1:annotOne"]);
}

function getLocalRecordAnnotations(typedesc<any> obj, string annotName) returns map<any> =
@java:Method {
    'class: "org/ballerinalang/test/annotations/LocalRecordAnnotationTest",
    name: "getLocalRecordAnnotations"
} external;

function getLocalTupleAnnotations(typedesc<any> obj, string annotName) returns map<any> =
@java:Method {
    'class: "org/ballerinalang/test/annotations/LocalTupleAnnotationTest",
    name: "getLocalTupleAnnotations"
} external;

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error(string `expected ${expected.toBalString()}, found ${actual.toBalString()}`);
}
