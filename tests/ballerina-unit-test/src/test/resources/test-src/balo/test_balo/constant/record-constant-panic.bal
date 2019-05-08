import testorg/foo version v1;

// boolean ---------------------------------------------------

function updateNestedConstantBooleanRecordValueWithExistingKey() {
    foo:getBR10().br10k.br11k = false; // panic
}

function updateNestedConstantBooleanRecordValueWithNewKey() {
    foo:getBR10().br10k.newKey = false; // panic
}

function updateReturnedConstantBooleanRecordWithExistingKey() {
    record {| boolean...; |} r = getBooleanRecord();
    r.br11k = false; // panic
}

function updateReturnedConstantBooleanRecord2WithNewKey() {
    record {| boolean...; |} r = getBooleanRecord();
    r.newKey = false; // panic
}

function getBooleanRecord() returns record {| boolean...; |} {
    return foo:br11;
}

// -----------------------------------------------------------

function updateConstantBooleanRecordValueInArrayWithExistingKey() {
    foo:getBRA1()[0].br11k = false; // panic
}

function updateConstantBooleanRecordValueInArrayWithNewKey() {
    foo:getBRA1()[0].newKey = false; // panic
}

function getConstantBooleanRecordValueInArray() returns boolean {
    return foo:getBRA1()[0].br11k;
}

// int -------------------------------------------------------

function updateNestedConstantIntRecordValueWithExistingKey() {
    foo:getIR10().ir10k.ir11k = 20; // panic
}

function updateNestedConstantIntRecordValueWithNewKey() {
    foo:getIR10().ir10k.newKey = 20; // panic
}

function updateReturnedConstantIntRecordWithExistingKey() {
    record {| int...; |} r = getIntRecord();
    r.ir11k = 20; // panic
}

function updateReturnedConstantIntRecord2WithNewKey() {
    record {| int...; |} r = getIntRecord();
    r.newKey = 20; // panic
}

function getIntRecord() returns record {| int...; |} {
    return foo:ir11;
}

// -----------------------------------------------------------

function updateConstantIntRecordValueInArrayWithExistingKey() {
    foo:getIRA1()[0].ir11k = 20; // panic
}

function updateConstantIntRecordValueInArrayWithNewKey() {
    foo:getIRA1()[0].newKey = 20; // panic
}

function getConstantIntRecordValueInArray() returns int {
    return foo:getIRA1()[0].ir11k;
}

// byte ------------------------------------------------------

function updateNestedConstantByteRecordValueWithExistingKey() {
    foo:getBYTER10().byter10k.byter11k = 8; // panic
}

function updateNestedConstantByteRecordValueWithNewKey() {
    foo:getBYTER10().byter10k.newKey = 8; // panic
}

function updateReturnedConstantByteRecordWithExistingKey() {
    record {| byte...; |} r = getByteRecord();
    r.byter11k = 8; // panic
}

function updateReturnedConstantByteRecord2WithNewKey() {
    record {| byte...; |} r = getByteRecord();
    r.newKey = 8; // panic
}

function getByteRecord() returns record {| byte...; |} {
    return foo:byter11;
}

// -----------------------------------------------------------

function updateConstantByteRecordValueInArrayWithExistingKey() {
    foo:getBYTERA1()[0].byter11k = 8; // panic
}

function updateConstantByteRecordValueInArrayWithNewKey() {
    foo:getBYTERA1()[0].newKey = 8; // panic
}

function getConstantByteRecordValueInArray() returns byte {
    return foo:getBYTERA1()[0].byter11k;
}

// float -----------------------------------------------------

function updateNestedConstantFloatRecordValueWithExistingKey() {
    foo:getFR10().fr10k.fr11k = 15.0; // panic
}

function updateNestedConstantFloatRecordValueWithNewKey() {
    foo:getFR10().fr10k.newKey = 15.0; // panic
}

function updateReturnedConstantFloatRecordWithExistingKey() {
    record {| float...; |} r = getFloatRecord();
    r.fr11k = 15.0; // panic
}

function updateReturnedConstantFloatRecord2WithNewKey() {
    record {| float...; |} r = getFloatRecord();
    r.newKey = 15.0; // panic
}

function getFloatRecord() returns record {| float...; |} {
    return foo:fr11;
}

// -----------------------------------------------------------

function updateConstantFloatRecordValueInArrayWithExistingKey() {
    foo:getFRA1()[0].fr11k = 15.0; // panic
}

function updateConstantFloatRecordValueInArrayWithNewKey() {
    foo:getFRA1()[0].newKey = 15.0; // panic
}

function getConstantFloatRecordValueInArray() returns float {
    return foo:getFRA1()[0].fr11k;
}

// decimal ---------------------------------------------------

function updateNestedConstantDecimalRecordValueWithExistingKey() {
    foo:getDRM10().dr10k.dr11k = 200; // panic
}

function updateNestedConstantDecimalRecordValueWithNewKey() {
    foo:getDRM10().dr10k.newKey = 200; // panic
}

function updateReturnedConstantDecimalRecordWithExistingKey() {
    record {| decimal...; |} r = getDecimalRecord();
    r.dr11k = 200; // panic
}

function updateReturnedConstantDecimalRecord2WithNewKey() {
    record {| decimal...; |} r = getDecimalRecord();
    r.newKey = 200; // panic
}

function getDecimalRecord() returns record {| decimal...; |} {
    return foo:dr11;
}

// -----------------------------------------------------------

function updateConstantDecimalRecordValueInArrayWithExistingKey() {
    foo:getDRA1()[0].dr11k = 200; // panic
}

function updateConstantDecimalRecordValueInArrayWithNewKey() {
    foo:getDRA1()[0].newKey = 200; // panic
}

function getConstantDecimalRecordValueInArray() returns decimal {
    return foo:getDRA1()[0].dr11k;
}

// string ----------------------------------------------------

function updateNestedConstantStringRecordValueWithExistingKey() {
    foo:getSR10().sr10k.sr11k = "sr11nv"; // panic
}

function updateNestedConstantStringRecordValueWithNewKey() {
    foo:getSR10().sr10k.newKey = "newValue"; // panic
}

function updateReturnedConstantStringRecordWithExistingKey() {
    record {| string...; |} r = getStringRecord();
    r.sr11k = "sr11kn"; // panic
}

function updateReturnedConstantStringRecord2WithNewKey() {
    record {| string...; |} r = getStringRecord();
    r.newKey = "newValue"; // panic
}

function getStringRecord() returns record {| string...; |} {
    return foo:sr11;
}

// -----------------------------------------------------------

function updateConstantStringRecordValueInArrayWithExistingKey() {
    foo:getSRA1()[0].sr11k = "sr11nv"; // panic
}

function updateConstantStringRecordValueInArrayWithNewKey() {
    foo:getSRA1()[0].newKey = "newValue"; // panic
}

function getConstantStringRecordValueInArray() returns string {
    return foo:getSRA1()[0].sr11k;
}

// nil -------------------------------------------------------

function updateNestedConstantNilRecordValueWithExistingKey() {
    foo:getNR10().nr10k.nr11k = (); // panic
}

function updateNestedConstantNilRecordValueWithNewKey() {
    foo:getNR10().nr10k.newKey = (); // panic
}

function updateReturnedConstantNilRecordWithExistingKey() {
    record {| ()...; |} r = getNilRecord();
    r.nr11k = (); // panic
}

function updateReturnedConstantNilRecord2WithNewKey() {
    record {| ()...; |} r = getNilRecord();
    r.newKey = (); // panic
}

function getNilRecord() returns record {| ()...; |} {
    return foo:nr11;
}

// -----------------------------------------------------------

function updateConstantNilRecordValueInArrayWithExistingKey() {
    foo:getNRA1()[0].nr11k = (); // panic
}

function updateConstantNilRecordValueInArrayWithNewKey() {
    foo:getNRA1()[0].newKey = (); // panic
}

function getConstantNilRecordValueInArray() returns () {
    return foo:getNRA1()[0].nr11k;
}
