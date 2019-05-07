// boolean ---------------------------------------------------

record {| record {| boolean...; |}...; |} bm10 = { bm10k: bm11 };
const record {| boolean...; |} bm11 = { bm11k: true };

function updateNestedConstantBooleanRecordValueWithExistingKey() {
    bm10.bm10k.bm11k = false; // panic
}

function updateNestedConstantBooleanRecordValueWithNewKey() {
    bm10.bm10k.newKey = false; // panic
}

function updateReturnedConstantBooleanRecordWithExistingKey() {
    record {| boolean...; |} m = getBooleanRecord();
    m.bm11k = false; // panic
}

function updateReturnedConstantBooleanRecord2WithNewKey() {
    record {| boolean...; |} m = getBooleanRecord();
    m.newKey = false; // panic
}

function getBooleanRecord() returns record {| boolean...; |} {
    return bm11;
}

// -----------------------------------------------------------

record {| boolean...; |}[] ba1 = [bm11];

function updateConstantBooleanRecordValueInArrayWithExistingKey() {
    ba1[0].bm11k = false; // panic
}

function updateConstantBooleanRecordValueInArrayWithNewKey() {
    ba1[0].newKey = false; // panic
}

function getConstantBooleanRecordValueInArray() returns boolean {
    return ba1[0].bm11k;
}

// int -------------------------------------------------------

record {| record {| int...; |}...; |} im10 = { im10k: im11 };
const record {| int...; |} im11 = { im11k: 10 };

function updateNestedConstantIntRecordValueWithExistingKey() {
    im10.im10k.im11k = 20; // panic
}

function updateNestedConstantIntRecordValueWithNewKey() {
    im10.im10k.newKey = 20; // panic
}

function updateReturnedConstantIntRecordWithExistingKey() {
    record {| int...; |} m = getIntRecord();
    m.im11k = 20; // panic
}

function updateReturnedConstantIntRecord2WithNewKey() {
    record {| int...; |} m = getIntRecord();
    m.newKey = 20; // panic
}

function getIntRecord() returns record {| int...; |} {
    return im11;
}

// -----------------------------------------------------------

record {| int...; |}[] ia1 = [im11];

function updateConstantIntRecordValueInArrayWithExistingKey() {
    ia1[0].im11k = 20; // panic
}

function updateConstantIntRecordValueInArrayWithNewKey() {
    ia1[0].newKey = 20; // panic
}

function getConstantIntRecordValueInArray() returns int {
    return ia1[0].im11k;
}

// byte ------------------------------------------------------

record {| record {| byte...; |}...; |} bytem10 = { bytem10k: bytem11 };
const record {| byte...; |} bytem11 = { bytem11k: 4 };

function updateNestedConstantByteRecordValueWithExistingKey() {
    bytem10.bytem10k.bytem11k = 8; // panic
}

function updateNestedConstantByteRecordValueWithNewKey() {
    bytem10.bytem10k.newKey = 8; // panic
}

function updateReturnedConstantByteRecordWithExistingKey() {
    record {| byte...; |} m = getByteRecord();
    m.bytem11k = 8; // panic
}

function updateReturnedConstantByteRecord2WithNewKey() {
    record {| byte...; |} m = getByteRecord();
    m.newKey = 8; // panic
}

function getByteRecord() returns record {| byte...; |} {
    return bytem11;
}

// -----------------------------------------------------------

record {| byte...; |}[] bytea1 = [bytem11];

function updateConstantByteRecordValueInArrayWithExistingKey() {
    bytea1[0].bytem11k = 8; // panic
}

function updateConstantByteRecordValueInArrayWithNewKey() {
    bytea1[0].newKey = 8; // panic
}

function getConstantByteRecordValueInArray() returns byte {
    return bytea1[0].bytem11k;
}

// float -----------------------------------------------------

record {| record {| float...; |}...; |} fm10 = { fm10k: fm11 };
const record {| float...; |} fm11 = { fm11k: 40.0 };

function updateNestedConstantFloatRecordValueWithExistingKey() {
    fm10.fm10k.fm11k = 15.0; // panic
}

function updateNestedConstantFloatRecordValueWithNewKey() {
    fm10.fm10k.newKey = 15.0; // panic
}

function updateReturnedConstantFloatRecordWithExistingKey() {
    record {| float...; |} m = getFloatRecord();
    m.fm11k = 15.0; // panic
}

function updateReturnedConstantFloatRecord2WithNewKey() {
    record {| float...; |} m = getFloatRecord();
    m.newKey = 15.0; // panic
}

function getFloatRecord() returns record {| float...; |} {
    return fm11;
}

// -----------------------------------------------------------

record {| float...; |}[] fa1 = [fm11];

function updateConstantFloatRecordValueInArrayWithExistingKey() {
    fa1[0].fm11k = 15.0; // panic
}

function updateConstantFloatRecordValueInArrayWithNewKey() {
    fa1[0].newKey = 15.0; // panic
}

function getConstantFloatRecordValueInArray() returns float {
    return fa1[0].fm11k;
}

// decimal ---------------------------------------------------

record {| record {| decimal...; |}...; |} dm10 = { dm10k: dm11 };
const record {| decimal...; |} dm11 = { dm11k: 125 };

function updateNestedConstantDecimalRecordValueWithExistingKey() {
    dm10.dm10k.dm11k = 200; // panic
}

function updateNestedConstantDecimalRecordValueWithNewKey() {
    dm10.dm10k.newKey = 200; // panic
}

function updateReturnedConstantDecimalRecordWithExistingKey() {
    record {| decimal...; |} m = getDecimalRecord();
    m.dm11k = 200; // panic
}

function updateReturnedConstantDecimalRecord2WithNewKey() {
    record {| decimal...; |} m = getDecimalRecord();
    m.newKey = 200; // panic
}

function getDecimalRecord() returns record {| decimal...; |} {
    return dm11;
}

// -----------------------------------------------------------

record {| decimal...; |}[] da1 = [dm11];

function updateConstantDecimalRecordValueInArrayWithExistingKey() {
    da1[0].dm11k = 200; // panic
}

function updateConstantDecimalRecordValueInArrayWithNewKey() {
    da1[0].newKey = 200; // panic
}

function getConstantDecimalRecordValueInArray() returns decimal {
    return da1[0].dm11k;
}

// string ----------------------------------------------------

record {| record {| string...; |}...; |} sm10 = { sm10k: sm11 };
const record {| string...; |} sm11 = { sm11k: "sm11v" };

function updateNestedConstantStringRecordValueWithExistingKey() {
    sm10.sm10k.sm11k = "sm11nv"; // panic
}

function updateNestedConstantStringRecordValueWithNewKey() {
    sm10.sm10k.newKey = "newValue"; // panic
}

function updateReturnedConstantStringRecordWithExistingKey() {
    record {| string...; |} m = getStringRecord();
    m.sm11k = "sm11kn"; // panic
}

function updateReturnedConstantStringRecord2WithNewKey() {
    record {| string...; |} m = getStringRecord();
    m.newKey = "newValue"; // panic
}

function getStringRecord() returns record {| string...; |} {
    return sm11;
}

// -----------------------------------------------------------

record {| string...; |}[] sa1 = [sm11];

function updateConstantStringRecordValueInArrayWithExistingKey() {
    sa1[0].sm11k = "sm11nv"; // panic
}

function updateConstantStringRecordValueInArrayWithNewKey() {
    sa1[0].newKey = "newValue"; // panic
}

function getConstantStringRecordValueInArray() returns string {
    return sa1[0].sm11k;
}

// nil -------------------------------------------------------

record {| record {| ()...; |}...; |} nm10 = { nm10k: nm11 };
const record {| ()...; |} nm11 = { nm11k: () };

function updateNestedConstantNilRecordValueWithExistingKey() {
    nm10.nm10k.nm11k = (); // panic
}

function updateNestedConstantNilRecordValueWithNewKey() {
    nm10.nm10k.newKey = (); // panic
}

function updateReturnedConstantNilRecordWithExistingKey() {
    record {| ()...; |} m = getNilRecord();
    m.nm11k = (); // panic
}

function updateReturnedConstantNilRecord2WithNewKey() {
    record {| ()...; |} m = getNilRecord();
    m.newKey = (); // panic
}

function getNilRecord() returns record {| ()...; |} {
    return nm11;
}

// -----------------------------------------------------------

record {| ()...; |}[] na1 = [nm11];

function updateConstantNilRecordValueInArrayWithExistingKey() {
    na1[0].nm11k = (); // panic
}

function updateConstantNilRecordValueInArrayWithNewKey() {
    na1[0].newKey = (); // panic
}

function getConstantNilRecordValueInArray() returns () {
    return na1[0].nm11k;
}
