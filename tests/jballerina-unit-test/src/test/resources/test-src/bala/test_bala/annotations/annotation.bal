import testorg/foo;

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
