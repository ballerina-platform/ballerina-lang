import testorg/foo version v1;

// boolean ---------------------------------------------------

function updateNestedConstantBooleanMapValueWithExistingKey() {
    foo:getBM10().bm10k.bm11k = false; // panic
}

function updateNestedConstantBooleanMapValueWithNewKey() {
    foo:getBM10().bm10k.newKey = false; // panic
}

function updateReturnedConstantBooleanMapWithExistingKey() {
    map<boolean> m = getBooleanMap();
    m.bm11k = false; // panic
}

function updateReturnedConstantBooleanMap2WithNewKey() {
    map<boolean> m = getBooleanMap();
    m.newKey = false; // panic
}

function getBooleanMap() returns map<boolean> {
    return foo:bm11;
}

// -----------------------------------------------------------

function updateConstantBooleanMapValueInArrayWithExistingKey() {
    foo:getBA1()[0].bm11k = false; // panic
}

function updateConstantBooleanMapValueInArrayWithNewKey() {
    foo:getBA1()[0].newKey = false; // panic
}

function getConstantBooleanMapValueInArray() returns boolean {
    return foo:getBA1()[0].bm11k;
}

// int -------------------------------------------------------

function updateNestedConstantIntMapValueWithExistingKey() {
    foo:getIM10().im10k.im11k = 20; // panic
}

function updateNestedConstantIntMapValueWithNewKey() {
    foo:getIM10().im10k.newKey = 20; // panic
}

function updateReturnedConstantIntMapWithExistingKey() {
    map<int> m = getIntMap();
    m.im11k = 20; // panic
}

function updateReturnedConstantIntMap2WithNewKey() {
    map<int> m = getIntMap();
    m.newKey = 20; // panic
}

function getIntMap() returns map<int> {
    return foo:im11;
}

// -----------------------------------------------------------

function updateConstantIntMapValueInArrayWithExistingKey() {
    foo:getIA1()[0].im11k = 20; // panic
}

function updateConstantIntMapValueInArrayWithNewKey() {
    foo:getIA1()[0].newKey = 20; // panic
}

function getConstantIntMapValueInArray() returns int {
    return foo:getIA1()[0].im11k;
}

// byte ------------------------------------------------------

function updateNestedConstantByteMapValueWithExistingKey() {
    foo:getBYTEM10().bytem10k.bytem11k = 8; // panic
}

function updateNestedConstantByteMapValueWithNewKey() {
    foo:getBYTEM10().bytem10k.newKey = 8; // panic
}

function updateReturnedConstantByteMapWithExistingKey() {
    map<byte> m = getByteMap();
    m.bytem11k = 8; // panic
}

function updateReturnedConstantByteMap2WithNewKey() {
    map<byte> m = getByteMap();
    m.newKey = 8; // panic
}

function getByteMap() returns map<byte> {
    return foo:bytem11;
}

// -----------------------------------------------------------

function updateConstantByteMapValueInArrayWithExistingKey() {
    foo:getBYTEA1()[0].bytem11k = 8; // panic
}

function updateConstantByteMapValueInArrayWithNewKey() {
    foo:getBYTEA1()[0].newKey = 8; // panic
}

function getConstantByteMapValueInArray() returns byte {
    return foo:getBYTEA1()[0].bytem11k;
}

// float -----------------------------------------------------

function updateNestedConstantFloatMapValueWithExistingKey() {
    foo:getFM10().fm10k.fm11k = 15.0; // panic
}

function updateNestedConstantFloatMapValueWithNewKey() {
    foo:getFM10().fm10k.newKey = 15.0; // panic
}

function updateReturnedConstantFloatMapWithExistingKey() {
    map<float> m = getFloatMap();
    m.fm11k = 15.0; // panic
}

function updateReturnedConstantFloatMap2WithNewKey() {
    map<float> m = getFloatMap();
    m.newKey = 15.0; // panic
}

function getFloatMap() returns map<float> {
    return foo:fm11;
}

// -----------------------------------------------------------

function updateConstantFloatMapValueInArrayWithExistingKey() {
    foo:getFA1()[0].fm11k = 15.0; // panic
}

function updateConstantFloatMapValueInArrayWithNewKey() {
    foo:getFA1()[0].newKey = 15.0; // panic
}

function getConstantFloatMapValueInArray() returns float {
    return foo:getFA1()[0].fm11k;
}

// decimal ---------------------------------------------------

function updateNestedConstantDecimalMapValueWithExistingKey() {
    foo:getDM10().dm10k.dm11k = 200; // panic
}

function updateNestedConstantDecimalMapValueWithNewKey() {
    foo:getDM10().dm10k.newKey = 200; // panic
}

function updateReturnedConstantDecimalMapWithExistingKey() {
    map<decimal> m = getDecimalMap();
    m.dm11k = 200; // panic
}

function updateReturnedConstantDecimalMap2WithNewKey() {
    map<decimal> m = getDecimalMap();
    m.newKey = 200; // panic
}

function getDecimalMap() returns map<decimal> {
    return foo:dm11;
}

// -----------------------------------------------------------

function updateConstantDecimalMapValueInArrayWithExistingKey() {
    foo:getDA1()[0].dm11k = 200; // panic
}

function updateConstantDecimalMapValueInArrayWithNewKey() {
    foo:getDA1()[0].newKey = 200; // panic
}

function getConstantDecimalMapValueInArray() returns decimal {
    return foo:getDA1()[0].dm11k;
}

// string ----------------------------------------------------

function updateNestedConstantStringMapValueWithExistingKey() {
    foo:getSM10().sm10k.sm11k = "sm11nv"; // panic
}

function updateNestedConstantStringMapValueWithNewKey() {
    foo:getSM10().sm10k.newKey = "newValue"; // panic
}

function updateReturnedConstantStringMapWithExistingKey() {
    map<string> m = getStringMap();
    m.sm11k = "sm11kn"; // panic
}

function updateReturnedConstantStringMap2WithNewKey() {
    map<string> m = getStringMap();
    m.newKey = "newValue"; // panic
}

function getStringMap() returns map<string> {
    return foo:sm11;
}

// -----------------------------------------------------------

function updateConstantStringMapValueInArrayWithExistingKey() {
    foo:getSA1()[0].sm11k = "sm11nv"; // panic
}

function updateConstantStringMapValueInArrayWithNewKey() {
    foo:getSA1()[0].newKey = "newValue"; // panic
}

function getConstantStringMapValueInArray() returns string {
    return foo:getSA1()[0].sm11k;
}

// nil -------------------------------------------------------

function updateNestedConstantNilMapValueWithExistingKey() {
    foo:getNM10().nm10k.nm11k = (); // panic
}

function updateNestedConstantNilMapValueWithNewKey() {
    foo:getNM10().nm10k.newKey = (); // panic
}

function updateReturnedConstantNilMapWithExistingKey() {
    map<()> m = getNilMap();
    m.nm11k = (); // panic
}

function updateReturnedConstantNilMap2WithNewKey() {
    map<()> m = getNilMap();
    m.newKey = (); // panic
}

function getNilMap() returns map<()> {
    return foo:nm11;
}

// -----------------------------------------------------------

function updateConstantNilMapValueInArrayWithExistingKey() {
    foo:getNA1()[0].nm11k = (); // panic
}

function updateConstantNilMapValueInArrayWithNewKey() {
    foo:getNA1()[0].newKey = (); // panic
}

function getConstantNilMapValueInArray() returns () {
    return foo:getNA1()[0].nm11k;
}
