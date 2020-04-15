import testorg/foo;

// boolean ---------------------------------------------------

function updateReturnedConstantBooleanMapWithExistingKey() {
    map<boolean> m = getBooleanMap();
    m["bm11k"] = false; // panic
}

function updateReturnedConstantBooleanMap2WithNewKey() {
    map<boolean> m = getBooleanMap();
    m["newKey"] = false; // panic
}

function getBooleanMap() returns map<boolean> {
    return foo:bm11;
}

// -----------------------------------------------------------

function updateConstantBooleanMapValueInArrayWithExistingKey() {
    var ba = foo:getBA1();
    ba[0]["bm11k"] = false; // panic
}

function updateConstantBooleanMapValueInArrayWithNewKey() {
    var ba = foo:getBA1();
    ba[0]["newKey"] = false; // panic
}

function getConstantBooleanMapValueInArray() returns boolean? {
    var ba = foo:getBA1();

    return ba[0]["bm11k"];
}

// int -------------------------------------------------------

function updateNestedConstantIntMapValueWithExistingKey() {
    var im = foo:getIM10();
    im["im10k"]["im11k"] = 20; // panic
}

function updateNestedConstantIntMapValueWithNewKey() {
    var im = foo:getIM10();
    im["im10k"]["newKey"] = 20; // panic
}

function updateReturnedConstantIntMapWithExistingKey() {
    map<int> m = getIntMap();
    m["im11k"] = 20; // panic
}

function updateReturnedConstantIntMap2WithNewKey() {
    map<int> m = getIntMap();
    m["newKey"] = 20; // panic
}

function getIntMap() returns map<int> {
    return foo:im11;
}

// -----------------------------------------------------------

function updateConstantIntMapValueInArrayWithExistingKey() {
    var a1 = foo:getIA1();
    a1[0]["im11k"] = 20; // panic
}

function updateConstantIntMapValueInArrayWithNewKey() {
    var a1 = foo:getIA1();
    a1[0]["newKey"] = 20; // panic
}

function getConstantIntMapValueInArray() returns int? {
    return foo:getIA1()[0]["im11k"];
}

// byte ------------------------------------------------------

function updateNestedConstantByteMapValueWithExistingKey() {
    var b =foo:getBYTEM10();
    b["bytem10k"]["bytem11k"] = 8; // panic
}

function updateNestedConstantByteMapValueWithNewKey() {
    var b =foo:getBYTEM10();
    b["bytem10k"]["newKey"] = 8; // panic
}

function updateReturnedConstantByteMapWithExistingKey() {
    map<byte> m = getByteMap();
    m["bytem11k"] = 8; // panic
}

function updateReturnedConstantByteMap2WithNewKey() {
    map<byte> m = getByteMap();
    m["newKey"] = 8; // panic
}

function getByteMap() returns map<byte> {
    return foo:bytem11;
}

// -----------------------------------------------------------

function updateConstantByteMapValueInArrayWithExistingKey() {
    var b = foo:getBYTEA1();
    b[0]["bytem11k"]   = 8; // panic
}

function updateConstantByteMapValueInArrayWithNewKey() {
    var b = foo:getBYTEA1();
    b[0]["newKey"] = 8; // panic
}

function getConstantByteMapValueInArray() returns byte? {
    return foo:getBYTEA1()[0]["bytem11k"];
}

// float -----------------------------------------------------

function updateNestedConstantFloatMapValueWithExistingKey() {
    var fm = foo:getFM10();
    fm["fm10k"]["fm11k"] = 15.0; // panic
}

function updateNestedConstantFloatMapValueWithNewKey() {
    var fm = foo:getFM10();
    fm["fm10k"]["newKey"] = 15.0; // panic
}

function updateReturnedConstantFloatMapWithExistingKey() {
    map<float> m = getFloatMap();
    m["fm11k"] = 15.0; // panic
}

function updateReturnedConstantFloatMap2WithNewKey() {
    map<float> m = getFloatMap();
    m["newKey"] = 15.0; // panic
}

function getFloatMap() returns map<float> {
    return foo:fm11;
}

// -----------------------------------------------------------

function updateConstantFloatMapValueInArrayWithExistingKey() {
    var fa = foo:getFA1();
    fa[0]["fm11k"] = 15.0; // panic
}

function updateConstantFloatMapValueInArrayWithNewKey() {
    var fa = foo:getFA1();
    fa[0]["newKey"] = 15.0; // panic
}

function getConstantFloatMapValueInArray() returns float? {
    return foo:getFA1()[0]["fm11k"];
}

// decimal ---------------------------------------------------

function updateNestedConstantDecimalMapValueWithExistingKey() {
    var dm = foo:getDM10();
    dm["dm10k"]["dm11k"] = 200; // panic
}

function updateNestedConstantDecimalMapValueWithNewKey() {
    var dm = foo:getDM10();
    dm["dm10k"]["newKey"] = 200; // panic
}

function updateReturnedConstantDecimalMapWithExistingKey() {
    map<decimal> m = getDecimalMap();
    m["dm11k"] = 200; // panic
}

function updateReturnedConstantDecimalMap2WithNewKey() {
    map<decimal> m = getDecimalMap();
    m["newKey"] = 200; // panic
}

function getDecimalMap() returns map<decimal> {
    return foo:dm11;
}

// -----------------------------------------------------------

function updateConstantDecimalMapValueInArrayWithExistingKey() {
    var da = foo:getDA1();
    da[0]["dm11k"] = 200; // panic
}

function updateConstantDecimalMapValueInArrayWithNewKey() {
    var da = foo:getDA1();
    da[0]["newKey"] = 200; // panic
}

function getConstantDecimalMapValueInArray() returns decimal? {
    return foo:getDA1()[0]["dm11k"];
}

// string ----------------------------------------------------

function updateNestedConstantStringMapValueWithExistingKey() {
    var sm = foo:getSM10();
    sm["sm10k"]["sm11k"] = "sm11nv"; // panic
}

function updateNestedConstantStringMapValueWithNewKey() {
    var sm = foo:getSM10();
    sm["sm10k"]["newKey"] = "newValue"; // panic
}

function updateReturnedConstantStringMapWithExistingKey() {
    map<string> m = getStringMap();
    m["sm11k"] = "sm11kn"; // panic
}

function updateReturnedConstantStringMap2WithNewKey() {
    map<string> m = getStringMap();
    m["newKey"] = "newValue"; // panic
}

function getStringMap() returns map<string> {
    return foo:sm11;
}

// -----------------------------------------------------------

function updateConstantStringMapValueInArrayWithExistingKey() {
    var sa = foo:getSA1();
    sa[0]["sm11k"] = "sm11nv"; // panic
}

function updateConstantStringMapValueInArrayWithNewKey() {
    var sa = foo:getSA1();
    sa[0]["newKey"] = "newValue"; // panic
}

function getConstantStringMapValueInArray() returns string? {
    return foo:getSA1()[0]["sm11k"];
}

// nil -------------------------------------------------------

function updateNestedConstantNilMapValueWithExistingKey() {
    var nm = foo:getNM10();
    nm["nm10k"]["nm11k"] = (); // panic
}

function updateNestedConstantNilMapValueWithNewKey() {
    var nm = foo:getNM10();
    nm["nm10k"]["newKey"] = (); // panic
}

function updateReturnedConstantNilMapWithExistingKey() {
    map<()> m = getNilMap();
    m["nm11k"] = (); // panic
}

function updateReturnedConstantNilMap2WithNewKey() {
    map<()> m = getNilMap();
    m["newKey"] = (); // panic
}

function getNilMap() returns map<()> {
    return foo:nm11;
}

// -----------------------------------------------------------

function updateConstantNilMapValueInArrayWithExistingKey() {
    var na = foo:getNA1();
    na[0]["nm11k"] = (); // panic
}

function updateConstantNilMapValueInArrayWithNewKey() {
    var na = foo:getNA1();
    na[0]["newKey"] = (); // panic
}

function getConstantNilMapValueInArray() returns () {
    return foo:getNA1()[0]["nm11k"];
}
