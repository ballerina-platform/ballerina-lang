import testorg/foo version v1;

import ballerina/reflect;

function testSimpleBooleanConstRecord() returns record {| boolean...; |} {
    return foo:br1;
}

function testComplexBooleanConstRecord() returns record {| record {| boolean...; |}...; |} {
    return foo:br3;
}

// -----------------------------------------------------------

function testSimpleIntConstRecord() returns record {| int...; |} {
    return foo:ir1;
}

function testComplexIntConstRecord() returns record {| record {| int...; |}...; |} {
    return foo:ir3;
}

// -----------------------------------------------------------

function testSimpleByteConstRecord() returns record {| byte...; |} {
    return foo:byter1;
}

function testComplexByteConstRecord() returns record {| record {| byte...; |}...; |} {
    return foo:byter3;
}

// -----------------------------------------------------------

function testSimpleFloatConstRecord() returns record {| float...; |} {
    return foo:fr1;
}

function testComplexFloatConstRecord() returns record {| record {| float...; |}...; |} {
    return foo:fr3;
}

// -----------------------------------------------------------

function testSimpleDecimalConstRecord() returns record {| decimal...; |} {
    return foo:dr1;
}

function testComplexDecimalConstRecord() returns record {| record {| decimal...; |}...; |} {
    return foo:dr3;
}

// -----------------------------------------------------------

function testSimpleStringConstRecord() returns record {| string...; |} {
    return foo:sr1;
}

function testComplexStringConstRecord() returns record {| record {| string...; |}...; |} {
    return foo:sr3;
}

// -----------------------------------------------------------

function testSimpleNilConstRecord() returns record {| ()...; |} {
    return foo:nr1;
}

function testComplexNilConstRecord() returns record {| record {| ()...; |}...; |} {
    return foo:nr3;
}

// -----------------------------------------------------------

function testComplexConstRecord() returns record {| record {| record {| string...; |}...; |}...; |} {
    return foo:r3;
}

// -----------------------------------------------------------

function testBooleanConstKeyReference() returns record {| boolean...; |} {
    return foo:br5;
}

function testIntConstKeyReference() returns record {| int...; |} {
    return foo:ir5;
}

function testByteConstKeyReference() returns record {| byte...; |} {
    return foo:byter5;
}

function testFloatConstKeyReference() returns record {| float...; |} {
    return foo:fr5;
}

function testDecimalConstKeyReference() returns record {| decimal...; |} {
    return foo:dr5;
}

function testStringConstKeyReference() returns record {| string...; |} {
    return foo:sr5;
}

function testNullConstKeyReference() returns record {| ()...; |} {
    return foo:nr5;
}

// -----------------------------------------------------------

function testBooleanConstKeyReferenceInLocalVar() returns boolean {
    boolean b = foo:br4.br4k;
    return b;
}

function testIntConstKeyReferenceInLocalVar() returns int {
    int i = foo:ir4.ir4k;
    return i;
}

function testByteConstKeyReferenceInLocalVar() returns byte {
    byte b = foo:byter4.byter4k;
    return b;
}

function testFloatConstKeyReferenceInLocalVar() returns float {
    float f = foo:fr4.fr4k;
    return f;
}

function testDecimalConstKeyReferenceInLocalVar() returns decimal {
    decimal d = foo:dr4.dr4k;
    return d;
}

function testStringConstKeyReferenceInLocalVar() returns string {
    string s = foo:sr4.sr4k;
    return s;
}

function testNullConstKeyReferenceInLocalVar() returns () {
    () n = foo:nr4.nr4k;
    return n;
}

// annotations -----------------------------------------------

@foo:recordTestAnnotation {
    s: foo:srConst,
    i: foo:irConst,
    m: foo:mrConst
}
function testFunction() {

}

function testConstInAnnotations() returns reflect:annotationData[] {
    return reflect:getFunctionAnnotations(testFunction);
}
