// boolean ---------------------------------------------------

record {| record {| boolean...; |}...; |} br10 = { br10k: br11 };
const record {| boolean...; |} br11 = { br11k: true };

function updateNestedConstantBooleanRecordValueWithExistingKey() {
    br10.br10k.br11k = false; // panic
}

function updateNestedConstantBooleanRecordValueWithNewKey() {
    br10.br10k.newKey = false; // panic
}

function updateReturnedConstantBooleanRecordWithExistingKey() {
    record {| boolean...; |} m = getBooleanRecord();
    m.br11k = false; // panic
}

function updateReturnedConstantBooleanRecord2WithNewKey() {
    record {| boolean...; |} m = getBooleanRecord();
    m.newKey = false; // panic
}

function getBooleanRecord() returns record {| boolean...; |} {
    return br11;
}

// -----------------------------------------------------------

record {| boolean...; |}[] ba1 = [br11];

function updateConstantBooleanRecordValueInArrayWithExistingKey() {
    ba1[0].br11k = false; // panic
}

function updateConstantBooleanRecordValueInArrayWithNewKey() {
    ba1[0].newKey = false; // panic
}

function getConstantBooleanRecordValueInArray() returns boolean {
    return ba1[0].br11k;
}

// int -------------------------------------------------------

record {| record {| int...; |}...; |} ir10 = { ir10k: ir11 };
const record {| int...; |} ir11 = { ir11k: 10 };

function updateNestedConstantIntRecordValueWithExistingKey() {
    ir10.ir10k.ir11k = 20; // panic
}

function updateNestedConstantIntRecordValueWithNewKey() {
    ir10.ir10k.newKey = 20; // panic
}

function updateReturnedConstantIntRecordWithExistingKey() {
    record {| int...; |} m = getIntRecord();
    m.ir11k = 20; // panic
}

function updateReturnedConstantIntRecord2WithNewKey() {
    record {| int...; |} m = getIntRecord();
    m.newKey = 20; // panic
}

function getIntRecord() returns record {| int...; |} {
    return ir11;
}

// -----------------------------------------------------------

record {| int...; |}[] ia1 = [ir11];

function updateConstantIntRecordValueInArrayWithExistingKey() {
    ia1[0].ir11k = 20; // panic
}

function updateConstantIntRecordValueInArrayWithNewKey() {
    ia1[0].newKey = 20; // panic
}

function getConstantIntRecordValueInArray() returns int {
    return ia1[0].ir11k;
}

// byte ------------------------------------------------------

record {| record {| byte...; |}...; |} byter10 = { byter10k: byter11 };
const record {| byte...; |} byter11 = { byter11k: 4 };

function updateNestedConstantByteRecordValueWithExistingKey() {
    byter10.byter10k.byter11k = 8; // panic
}

function updateNestedConstantByteRecordValueWithNewKey() {
    byter10.byter10k.newKey = 8; // panic
}

function updateReturnedConstantByteRecordWithExistingKey() {
    record {| byte...; |} m = getByteRecord();
    m.byter11k = 8; // panic
}

function updateReturnedConstantByteRecord2WithNewKey() {
    record {| byte...; |} m = getByteRecord();
    m.newKey = 8; // panic
}

function getByteRecord() returns record {| byte...; |} {
    return byter11;
}

// -----------------------------------------------------------

record {| byte...; |}[] bytea1 = [byter11];

function updateConstantByteRecordValueInArrayWithExistingKey() {
    bytea1[0].byter11k = 8; // panic
}

function updateConstantByteRecordValueInArrayWithNewKey() {
    bytea1[0].newKey = 8; // panic
}

function getConstantByteRecordValueInArray() returns byte {
    return bytea1[0].byter11k;
}

// float -----------------------------------------------------

record {| record {| float...; |}...; |} fr10 = { fr10k: fr11 };
const record {| float...; |} fr11 = { fr11k: 40.0 };

function updateNestedConstantFloatRecordValueWithExistingKey() {
    fr10.fr10k.fr11k = 15.0; // panic
}

function updateNestedConstantFloatRecordValueWithNewKey() {
    fr10.fr10k.newKey = 15.0; // panic
}

function updateReturnedConstantFloatRecordWithExistingKey() {
    record {| float...; |} m = getFloatRecord();
    m.fr11k = 15.0; // panic
}

function updateReturnedConstantFloatRecord2WithNewKey() {
    record {| float...; |} m = getFloatRecord();
    m.newKey = 15.0; // panic
}

function getFloatRecord() returns record {| float...; |} {
    return fr11;
}

// -----------------------------------------------------------

record {| float...; |}[] fa1 = [fr11];

function updateConstantFloatRecordValueInArrayWithExistingKey() {
    fa1[0].fr11k = 15.0; // panic
}

function updateConstantFloatRecordValueInArrayWithNewKey() {
    fa1[0].newKey = 15.0; // panic
}

function getConstantFloatRecordValueInArray() returns float {
    return fa1[0].fr11k;
}

// decimal ---------------------------------------------------

record {| record {| decimal...; |}...; |} dr10 = { dr10k: dr11 };
const record {| decimal...; |} dr11 = { dr11k: 125 };

function updateNestedConstantDecimalRecordValueWithExistingKey() {
    dr10.dr10k.dr11k = 200; // panic
}

function updateNestedConstantDecimalRecordValueWithNewKey() {
    dr10.dr10k.newKey = 200; // panic
}

function updateReturnedConstantDecimalRecordWithExistingKey() {
    record {| decimal...; |} m = getDecimalRecord();
    m.dr11k = 200; // panic
}

function updateReturnedConstantDecimalRecord2WithNewKey() {
    record {| decimal...; |} m = getDecimalRecord();
    m.newKey = 200; // panic
}

function getDecimalRecord() returns record {| decimal...; |} {
    return dr11;
}

// -----------------------------------------------------------

record {| decimal...; |}[] da1 = [dr11];

function updateConstantDecimalRecordValueInArrayWithExistingKey() {
    da1[0].dr11k = 200; // panic
}

function updateConstantDecimalRecordValueInArrayWithNewKey() {
    da1[0].newKey = 200; // panic
}

function getConstantDecimalRecordValueInArray() returns decimal {
    return da1[0].dr11k;
}

// string ----------------------------------------------------

record {| record {| string...; |}...; |} sr10 = { sr10k: sr11 };
const record {| string...; |} sr11 = { sr11k: "sr11v" };

function updateNestedConstantStringRecordValueWithExistingKey() {
    sr10.sr10k.sr11k = "sr11nv"; // panic
}

function updateNestedConstantStringRecordValueWithNewKey() {
    sr10.sr10k.newKey = "newValue"; // panic
}

function updateReturnedConstantStringRecordWithExistingKey() {
    record {| string...; |} m = getStringRecord();
    m.sr11k = "sr11kn"; // panic
}

function updateReturnedConstantStringRecord2WithNewKey() {
    record {| string...; |} m = getStringRecord();
    m.newKey = "newValue"; // panic
}

function getStringRecord() returns record {| string...; |} {
    return sr11;
}

// -----------------------------------------------------------

record {| string...; |}[] sa1 = [sr11];

function updateConstantStringRecordValueInArrayWithExistingKey() {
    sa1[0].sr11k = "sr11nv"; // panic
}

function updateConstantStringRecordValueInArrayWithNewKey() {
    sa1[0].newKey = "newValue"; // panic
}

function getConstantStringRecordValueInArray() returns string {
    return sa1[0].sr11k;
}

// nil -------------------------------------------------------

record {| record {| ()...; |}...; |} nr10 = { nr10k: nr11 };
const record {| ()...; |} nr11 = { nr11k: () };

function updateNestedConstantNilRecordValueWithExistingKey() {
    nr10.nr10k.nr11k = (); // panic
}

function updateNestedConstantNilRecordValueWithNewKey() {
    nr10.nr10k.newKey = (); // panic
}

function updateReturnedConstantNilRecordWithExistingKey() {
    record {| ()...; |} m = getNilRecord();
    m.nr11k = (); // panic
}

function updateReturnedConstantNilRecord2WithNewKey() {
    record {| ()...; |} m = getNilRecord();
    m.newKey = (); // panic
}

function getNilRecord() returns record {| ()...; |} {
    return nr11;
}

// -----------------------------------------------------------

record {| ()...; |}[] na1 = [nr11];

function updateConstantNilRecordValueInArrayWithExistingKey() {
    na1[0].nr11k = (); // panic
}

function updateConstantNilRecordValueInArrayWithNewKey() {
    na1[0].newKey = (); // panic
}

function getConstantNilRecordValueInArray() returns () {
    return na1[0].nr11k;
}
