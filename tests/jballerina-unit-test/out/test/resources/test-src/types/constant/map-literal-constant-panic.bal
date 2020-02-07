// boolean ---------------------------------------------------

map<map<boolean>> bm10 = { "bm10k": bm11 };
const map<boolean> bm11 = { "bm11k": true };

function updateNestedConstantBooleanMapValueWithExistingKey() {
    bm10["bm10k"]["bm11k"] = false; // panic
}

function updateNestedConstantBooleanMapValueWithNewKey() {
    bm10["bm10k"]["newKey"] = false; // panic
}

function updateReturnedConstantBooleanMapWithExistingKey() {
    map<boolean> m = getBooleanMap();
    m["bm11k"] = false; // panic
}

function updateReturnedConstantBooleanMap2WithNewKey() {
    map<boolean> m = getBooleanMap();
    m["newKey"] = false; // panic
}

function getBooleanMap() returns map<boolean> {
    return bm11;
}

// -----------------------------------------------------------

map<boolean>[] ba1 = [bm11];

function updateConstantBooleanMapValueInArrayWithExistingKey() {
    ba1[0]["bm11k"] = false; // panic
}

function updateConstantBooleanMapValueInArrayWithNewKey() {
    ba1[0]["newKey"] = false; // panic
}

function getConstantBooleanMapValueInArray() returns boolean {
    return ba1[0].get("bm11k");
}

// int -------------------------------------------------------

map<map<int>> im10 = { "im10k": im11 };
const map<int> im11 = { "im11k": 10 };

function updateNestedConstantIntMapValueWithExistingKey() {
    im10["im10k"]["im11k"] = 20; // panic
}

function updateNestedConstantIntMapValueWithNewKey() {
    im10["im10k"]["newKey"] = 20; // panic
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
    return im11;
}

// -----------------------------------------------------------

map<int>[] ia1 = [im11];

function updateConstantIntMapValueInArrayWithExistingKey() {
    ia1[0]["im11k"] = 20; // panic
}

function updateConstantIntMapValueInArrayWithNewKey() {
    ia1[0]["newKey"] = 20; // panic
}

function getConstantIntMapValueInArray() returns int {
    return ia1[0].get("newKey");
}

// byte ------------------------------------------------------

map<map<byte>> bytem10 = { "bytem10k": bytem11 };
const map<byte> bytem11 = { "bytem11k": 4 };

function updateNestedConstantByteMapValueWithExistingKey() {
    bytem10["bytem10k"]["bytem11k"] = 8; // panic
}

function updateNestedConstantByteMapValueWithNewKey() {
    bytem10["bytem10k"]["newKey"] = 8; // panic
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
    return bytem11;
}

// -----------------------------------------------------------

map<byte>[] bytea1 = [bytem11];

function updateConstantByteMapValueInArrayWithExistingKey() {
    bytea1[0]["bytem11k"] = 8; // panic
}

function updateConstantByteMapValueInArrayWithNewKey() {
    bytea1[0]["bytem11k"] = 8; // panic
}

function getConstantByteMapValueInArray() returns byte {
    return bytea1[0].get("bytem11k");
}

// float -----------------------------------------------------

map<map<float>> fm10 = { "fm10k": fm11 };
const map<float> fm11 = { "fm11k": 40.0 };

function updateNestedConstantFloatMapValueWithExistingKey() {
    fm10["fm10k"]["fm11k"] = 15.0; // panic
}

function updateNestedConstantFloatMapValueWithNewKey() {
    fm10["fm10k"]["newKey"] = 15.0; // panic
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
    return fm11;
}

// -----------------------------------------------------------

map<float>[] fa1 = [fm11];

function updateConstantFloatMapValueInArrayWithExistingKey() {
    fa1[0]["fm11k"] = 15.0; // panic
}

function updateConstantFloatMapValueInArrayWithNewKey() {
    fa1[0]["newKey"] = 15.0; // panic
}

function getConstantFloatMapValueInArray() returns float {
    return fa1[0].get("fm11k");
}

// decimal ---------------------------------------------------

map<map<decimal>> dm10 = { "dm10k": dm11 };
const map<decimal> dm11 = { "dm11k": 125 };

function updateNestedConstantDecimalMapValueWithExistingKey() {
    dm10["dm10k"]["dm11k"] = 200; // panic
}

function updateNestedConstantDecimalMapValueWithNewKey() {
    dm10["dm10k"]["newKey"] = 200; // panic
}

function updateReturnedConstantDecimalMapWithExistingKey() {
    map<decimal> m = getDecimalMap();
    m["dm10k"] = 200; // panic
}

function updateReturnedConstantDecimalMap2WithNewKey() {
    map<decimal> m = getDecimalMap();
    m["newKey"] = 200; // panic
}

function getDecimalMap() returns map<decimal> {
    return dm11;
}

// -----------------------------------------------------------

map<decimal>[] da1 = [dm11];

function updateConstantDecimalMapValueInArrayWithExistingKey() {
    da1[0]["dm11k"] = 200; // panic
}

function updateConstantDecimalMapValueInArrayWithNewKey() {
    da1[0]["newKey"] = 200; // panic
}

function getConstantDecimalMapValueInArray() returns decimal {
    return da1[0].get("dm11k");
}

// string ----------------------------------------------------

map<map<string>> sm10 = { "sm10k": sm11 };
const map<string> sm11 = { "sm11k": "sm11v" };

function updateNestedConstantStringMapValueWithExistingKey() {
    sm10["sm10k"]["sm11k"] = "sm11nv"; // panic
}

function updateNestedConstantStringMapValueWithNewKey() {
    sm10["sm10k"]["newKey"] = "newValue"; // panic
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
    return sm11;
}

// -----------------------------------------------------------

map<string>[] sa1 = [sm11];

function updateConstantStringMapValueInArrayWithExistingKey() {
    sa1[0]["sm11k"] = "sm11nv"; // panic
}

function updateConstantStringMapValueInArrayWithNewKey() {
    sa1[0]["newKey"] = "newValue"; // panic
}

function getConstantStringMapValueInArray() returns string {
    return sa1[0].get("newKey");
}

// nil -------------------------------------------------------

map<map<()>> nm10 = { "nm10k": nm11 };
const map<()> nm11 = { "nm11k": () };

function updateNestedConstantNilMapValueWithExistingKey() {
    nm10["nm10k"]["nm11k"] = (); // panic
}

function updateNestedConstantNilMapValueWithNewKey() {
    nm10["nm10k"]["newKey"] = (); // panic
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
    return nm11;
}

// -----------------------------------------------------------

map<()>[] na1 = [nm11];

function updateConstantNilMapValueInArrayWithExistingKey() {
    na1[0]["nm11k"] = (); // panic
}

function updateConstantNilMapValueInArrayWithNewKey() {
    na1[0]["newKey"] = (); // panic
}

function getConstantNilMapValueInArray() returns () {
    return na1[0]["nm11k"];
}
