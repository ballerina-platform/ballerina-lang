import ballerina/reflect;

type BR1 record {|
    boolean key1;
    boolean key2;
|};

type BR2 record {|
    boolean key3;
    boolean key4;
|};

type BR3 record {|
    BR1 key5;
    BR2 key6;
    anydata...;
|};

type BR4 record {|
    boolean key8;
    boolean key9;
|};

const BR4 br4 = { key8: true, key9: false };

const BR3 br3 = { key5: br1, key6: br2, key7: br4 };
const BR1 br1 = { key1: true, key2: false };
const BR2 br2 = { key3: false, key4: true };

function testSimpleBooleanConstRecord() returns BR1 {
    return br1;
}

function testComplexBooleanConstRecord() returns BR3 {
    return br3;
}

// -----------------------------------------------------------

type IR1 record {|
    int key1;
    int key2;
|};

type IR2 record {|
    int key3;
    int key4;
|};

type IR3 record {|
    IR1 key5;
    IR2 key6;
    anydata...;
|};

type IR4 record {|
    int key8;
    int key9;
|};

const IR4 ir4 = { key8: 8, key9: 9 };

const IR3 ir3 = { key5: ir1, key6: ir2, key7: ir4 };
const IR1 ir1 = { key1: 1, key2: 2 };
const IR2 ir2 = { key3: 3, key4: 4 };

function testSimpleIntConstRecord() returns IR1 {
    return ir1;
}

function testComplexIntConstRecord() returns IR3 {
    return ir3;
}

// -----------------------------------------------------------

type BYTER1 record {|
    byte key1;
    byte key2;
|};

type BYTER2 record {|
    byte key3;
    byte key4;
|};

type BYTER3 record {|
    BYTER1 key5;
    BYTER2 key6;
    anydata...;
|};

type BYTER4 record {|
    byte key8;
    byte key9;
|};

const BYTER4 byter4 = { key8: 80, key9: 90 };

const BYTER3 byter3 = { key5: byter1, key6: byter2, key7: byter4 };
const BYTER1 byter1 = { key1: 10, key2: 20 };
const BYTER2 byter2 = { key3: 30, key4: 40 };

function testSimpleByteConstRecord() returns BYTER1 {
    return byter1;
}

function testComplexByteConstRecord() returns BYTER3 {
    return byter3;
}

// -----------------------------------------------------------

type FR1 record {|
    float key1;
    float key2;
|};

type FR2 record {|
    float key3;
    float key4;
|};

type FR3 record {|
    FR1 key5;
    FR2 key6;
    anydata...;
|};

type FR4 record {|
    float key8;
    float key9;
|};

const FR4 fr4 = { key8: 8.0, key9: 9.0 };

const FR3 fr3 = { key5: fr1, key6: fr2, key7: fr4 };
const FR1 fr1 = { key1: 1.0, key2: 2.0 };
const FR2 fr2 = { key3: 3.0, key4: 4.0 };

function testSimpleFloatConstRecord() returns FR1 {
    return fr1;
}

function testComplexFloatConstRecord() returns FR3 {
    return fr3;
}

// -----------------------------------------------------------

type DR1 record {|
    decimal key1;
    decimal key2;
|};

type DR2 record {|
    decimal key3;
    decimal key4;
|};

type DR3 record {|
    DR1 key5;
    DR2 key6;
    anydata...;
|};

type DR4 record {|
    decimal key8;
    decimal key9;
|};

const DR4 dr4 = { key8: 800, key9: 900 };

const DR3 dr3 = { key5: dr1, key6: dr2, key7: dr4 };
const DR1 dr1 = { key1: 100, key2: 200 };
const DR2 dr2 = { key3: 300, key4: 400 };

function testSimpleDecimalConstRecord() returns DR1 {
    return dr1;
}

function testComplexDecimalConstRecord() returns DR3 {
    return dr3;
}

// -----------------------------------------------------------

type SR1 record {|
    string key1;
    string key2;
|};

type SR2 record {|
    string key3;
    string key4;
|};

type SR3 record {|
    SR1 key5;
    SR2 key6;
    anydata...;
|};

type SR4 record {|
    string key8;
    string key9;
|};

const SR4 sr4 = { key8: "value8", key9: "value9" };

const SR3 sr3 = { key5: sr1, key6: sr2, key7: sr4 };
const SR1 sr1 = { key1: "value1", key2: "value2" };
const SR2 sr2 = { key3: "value3", key4: "value4" };

function testSimpleStringConstRecord() returns SR1 {
    return sr1;
}

function testComplexStringConstRecord() returns SR3 {
    return sr3;
}

// -----------------------------------------------------------

type NR1 record {|
    () key1;
    () key2;
|};

type NR2 record {|
    () key3;
    () key4;
|};

type NR3 record {|
    NR1 key5;
    NR2 key6;
    anydata...;
|};

type NR4 record {|
    () key8;
    () key9;
|};

const NR4 nr4 = { key8: (), key9: () };

const NR3 nr3 = { key5: nr1, key6: nr2, key7: nr4 };
const NR1 nr1 = { key1: (), key2: () };
const NR2 nr2 = { key3: (), key4: () };

function testSimpleNilConstRecord() returns NR1 {
    return nr1;
}

function testComplexNilConstRecord() returns NR3 {
    return nr3;
}

// -----------------------------------------------------------

const record {| record {| record {| string...; |}...; |}...; |} r3 = { k3: r2 };

const record {| record {| string...; |}...; |} r2 = { k2: r1 };

const record {| string...; |} r1 = { k1: sVal };

const sVal = "v1";

function testComplexConstRecord() returns record {| record {| record {| string...; |}...; |}...; |} {
    return r3;
}

// -----------------------------------------------------------

const record {| boolean...; |} br5 = { br5k: true };
const record {| boolean...; |} br6 = { br6kn: br5.br5k };

function testBooleanConstKeyReference() returns record {| boolean...; |} {
    return br6;
}

// -----------------------------------------------------------

const record {| int...; |} ir5 = { ir5k: 123 };
const record {| int...; |} ir6 = { ir6kn: ir5.ir5k };

function testIntConstKeyReference() returns record {| int...; |} {
    return ir6;
}

// -----------------------------------------------------------

const record {| byte...; |} byter5 = { byter5k: 64 };
const record {| byte...; |} byter6 = { byter6kn: byter5.byter5k };

function testByteConstKeyReference() returns record {| byte...; |} {
    return byter6;
}

// -----------------------------------------------------------

const record {| float...; |} fr5 = { fr5k: 12.5 };
const record {| float...; |} fr6 = { fr6kn: fr5.fr5k };

function testFloatConstKeyReference() returns record {| float...; |} {
    return fr6;
}

// -----------------------------------------------------------

const record {| decimal...; |} dr5 = { dr5k: 5.56 };
const record {| decimal...; |} dr6 = { dr6kn: dr5.dr5k };

function testDecimalConstKeyReference() returns record {| decimal...; |} {
    return dr6;
}

// -----------------------------------------------------------

const record {| string...; |} sr5 = { sr5k: "sr5v" };
const record {| string...; |} sr6 = { sr6kn: sr5.sr5k };

function testStringConstKeyReference() returns record {| string...; |} {
    return sr6;
}

// -----------------------------------------------------------

const record {| ()...; |} nr5 = { nr5k: () };
const record {| ()...; |} nr6 = { nr6kn: nr5.nr5k };

function testNullConstKeyReference() returns record {| ()...; |} {
    return nr6;
}

// -----------------------------------------------------------

function testBooleanConstKeyReferenceInLocalVar() returns boolean {
    boolean b = br5.br5k;
    return b;
}

// -----------------------------------------------------------

function testIntConstKeyReferenceInLocalVar() returns int {
    int i = ir5.ir5k;
    return i;
}

// -----------------------------------------------------------

function testByteConstKeyReferenceInLocalVar() returns byte {
    byte b = byter5.byter5k;
    return b;
}

// -----------------------------------------------------------

function testFloatConstKeyReferenceInLocalVar() returns float {
    float f = fr5.fr5k;
    return f;
}

// -----------------------------------------------------------

function testDecimalConstKeyReferenceInLocalVar() returns decimal {
    decimal d = dr5.dr5k;
    return d;
}

// -----------------------------------------------------------

function testStringConstKeyReferenceInLocalVar() returns string {
    string s = sr5.sr5k;
    return s;
}

// -----------------------------------------------------------

function testNullConstKeyReferenceInLocalVar() returns () {
    () n = nr5.nr5k;
    return n;
}

// annotations -----------------------------------------------

const sConst = "Ballerina";
const iConst = 100;
const record {| string...; |} mConst = { mKey: "mValue" };

public type TestConfig record {|
    string s;
    int i;
    record {| string...; |} m;
|};

public annotation<function> testAnnotation TestConfig;

@testAnnotation {
    s: sConst,
    i: iConst,
    m: mConst
}
function testFunction() {

}

function testConstInAnnotations() returns reflect:annotationData[] {
    return reflect:getFunctionAnnotations(testFunction);
}
