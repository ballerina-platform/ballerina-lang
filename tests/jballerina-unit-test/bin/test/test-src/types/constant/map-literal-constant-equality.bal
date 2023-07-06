
// boolean ---------------------------------------------------

const map<map<boolean>> bm3 = { "key3": bm1, "key4": bm2 };
const map<boolean> bm1 = { "key1": true };
const map<boolean> bm2 = { "key2": false };

const map<map<boolean>> bm3_new = { "key3": bm1_new, "key4": bm2_new };
const map<boolean> bm1_new = { "key1": true };
const map<boolean> bm2_new = { "key2": false };

const map<map<boolean>> bm7 = { "key3": { "key1": true }, "key2": { "key4": false } };
const map<map<boolean>> bm8 = { "key3": { "key1": true }, "key2": { "key4": false } };

function testSimpleBooleanMapValueEqualityUsingSameReference() returns boolean {
    return bm1 == bm1; // true
}

function testSimpleBooleanMapValueEqualityUsingDifferentReference() returns boolean {
    return bm1 == bm1_new; // true
}

function testSimpleBooleanMapReferenceEqualityUsingSameReference() returns boolean {
    return bm1 === bm1; // true
}

function testSimpleBooleanMapReferenceEqualityUsingDifferentReference() returns boolean {
    return bm1 === bm1_new; // false
}

// -----------------------------------------------------------

function testComplexBooleanMapValueEqualityUsingSameReference() returns boolean {
    return bm3 == bm3; // true
}

function testComplexBooleanMapValueEqualityUsingDifferentReference() returns boolean {
    return bm3 == bm3_new; // true
}

function testComplexBooleanMapReferenceEqualityUsingSameReference() returns boolean {
    return bm3 === bm3; // true
}

function testComplexBooleanMapReferenceEqualityUsingDifferentReference() returns boolean {
    return bm3 === bm3_new; // false
}

// -----------------------------------------------------------

function testComplexBooleanMapValueEqualityUsingMapAccessSameReference() returns boolean {
    return bm3["key3"] == bm3["key3"]; // true
}

function testComplexBooleanMapValueEqualityUsingMapAccessDifferentReference() returns boolean {
    return bm3["key3"] == bm3_new["key3"]; // true
}

function testComplexBooleanMapReferenceEqualityUsingMapAccessSameReference() returns boolean {
    return bm1 === bm3["key3"]; // true
}

function testComplexBooleanMapReferenceEqualityUsingMapAccessDifferentReference() returns boolean {
    return bm3["key3"] === bm3_new["key3"]; // false
}

// -----------------------------------------------------------

function testSimpleBooleanMapValueEqualityUsingSameReferenceInMap() returns boolean {
    return bm7["key3"] == bm7["key3"]; // true
}

function testSimpleBooleanMapValueEqualityUsingDifferentReferenceInMap() returns boolean {
    return bm7["key3"] == bm1; // true
}

function testSimpleBooleanMapReferenceEqualityUsingSameReferenceInMap() returns boolean {
    return bm7["key3"]=== bm7["key3"]; // true
}

function testSimpleBooleanMapReferenceEqualityUsingDifferentReferenceInMap() returns boolean {
    return bm7["key3"] === bm1; // false
}

// -----------------------------------------------------------

function testComplexBooleanMapValueEquality() returns boolean {
    return bm7 == bm8; // true
}

function testComplexBooleanMapReferenceEquality() returns boolean {
    return bm7 === bm8; // false
}

function testSimpleBooleanMapValueEqualityInDifferentMap() returns boolean {
    return bm7["key3"] == bm8["key3"]; // true
}

function testSimpleBooleanMapReferenceEqualityInDifferentMap() returns boolean {
    return bm7["key3"] === bm8["key3"]; // false
}

// int -------------------------------------------------------

const map<map<int>> im3 = { "key3": im1, "key4": im2 };
const map<int> im1 = { "key1": 1 };
const map<int> im2 = { "key2": 2 };

const map<map<int>> im3_new = { "key3": im1_new, "key4": im2_new };
const map<int> im1_new = { "key1": 1 };
const map<int> im2_new = { "key2": 2 };

const map<map<int>> im7 = { "key3": { "key1": 1 }, "key4": { "key2": 2 } };
const map<map<int>> im8 = { "key3": { "key1": 1 }, "key4": { "key2": 2 } };

function testSimpleIntMapValueEqualityUsingSameReference() returns boolean {
    return im1 == im1; // true
}

function testSimpleIntMapValueEqualityUsingDifferentReference() returns boolean {
    return im1 == im1_new; // true
}

function testSimpleIntMapReferenceEqualityUsingSameReference() returns boolean {
    return im1 === im1; // true
}

function testSimpleIntMapReferenceEqualityUsingDifferentReference() returns boolean {
    return im1 === im1_new; // false
}

// -----------------------------------------------------------

function testComplexIntMapValueEqualityUsingSameReference() returns boolean {
    return im3 == im3; // true
}

function testComplexIntMapValueEqualityUsingDifferentReference() returns boolean {
    return im3 == im3_new; // true
}

function testComplexIntMapReferenceEqualityUsingSameReference() returns boolean {
    return im3 === im3; // true
}

function testComplexIntMapReferenceEqualityUsingDifferentReference() returns boolean {
    return im3 === im3_new; // false
}

// -----------------------------------------------------------

function testComplexIntMapValueEqualityUsingMapAccessSameReference() returns boolean {
    return im3["key3"] == im3["key3"]; // true
}

function testComplexIntMapValueEqualityUsingMapAccessDifferentReference() returns boolean {
    return im3["key3"] == im3_new["key3"]; // true
}

function testComplexIntMapReferenceEqualityUsingMapAccessSameReference() returns boolean {
    return im1 === im3["key3"]; // true
}

function testComplexIntMapReferenceEqualityUsingMapAccessDifferentReference() returns boolean {
    return im3["key3"] === im3_new["key3"]; // false
}

// -----------------------------------------------------------

function testSimpleIntMapValueEqualityUsingSameReferenceInMap() returns boolean {
    return im7["key3"] == im7["key3"]; // true
}

function testSimpleIntMapValueEqualityUsingDifferentReferenceInMap() returns boolean {
    return im7["key3"] == im1; // true
}

function testSimpleIntMapReferenceEqualityUsingSameReferenceInMap() returns boolean {
    return im7["key3"] === im7["key3"]; // true
}

function testSimpleIntMapReferenceEqualityUsingDifferentReferenceInMap() returns boolean {
    return im7["key3"] === im1; // false
}

// -----------------------------------------------------------

function testComplexIntMapValueEquality() returns boolean {
    return im7 == im8; // true
}

function testComplexIntMapReferenceEquality() returns boolean {
    return im7 === im8; // false
}

function testSimpleIntMapValueEqualityInDifferentMap() returns boolean {
    return im7["key3"] == im8["key3"]; // true
}

function testSimpleIntMapReferenceEqualityInDifferentMap() returns boolean {
    return im7["key3"] === im8["key3"]; // false
}

// byte ------------------------------------------------------

const map<map<byte>> bytem3 = { "key3": bytem1, "key4": bytem2 };
const map<byte> bytem1 = { "key1": 10 };
const map<byte> bytem2 = { "key2": 20 };

const map<map<byte>> bytem3_new = { "key3": bytem1_new, "key4": bytem2_new };
const map<byte> bytem1_new = { "key1": 10 };
const map<byte> bytem2_new = { "key2": 20 };

const map<map<byte>> bytem7 = { "key3": { "key1": 10 }, "key4": { "key2": 20 } };
const map<map<byte>> bytem8 = { "key3": { "key1": 10 }, "key4": { "key2": 20 } };

function testSimpleByteMapValueEqualityUsingSameReference() returns boolean {
    return bytem1 == bytem1; // true
}

function testSimpleByteMapValueEqualityUsingDifferentReference() returns boolean {
    return bytem1 == bytem1_new; // true
}

function testSimpleByteMapReferenceEqualityUsingSameReference() returns boolean {
    return bytem1 === bytem1; // true
}

function testSimpleByteMapReferenceEqualityUsingDifferentReference() returns boolean {
    return bytem1 === bytem1_new; // false
}

// -----------------------------------------------------------

function testComplexByteMapValueEqualityUsingSameReference() returns boolean {
    return bytem3 == bytem3; // true
}

function testComplexByteMapValueEqualityUsingDifferentReference() returns boolean {
    return bytem3 == bytem3_new; // true
}

function testComplexByteMapReferenceEqualityUsingSameReference() returns boolean {
    return bytem3 === bytem3; // true
}

function testComplexByteMapReferenceEqualityUsingDifferentReference() returns boolean {
    return bytem3 === bytem3_new; // false
}

// -----------------------------------------------------------

function testComplexByteMapValueEqualityUsingMapAccessSameReference() returns boolean {
    return bytem3["key3"] == bytem3["key3"]; // true
}

function testComplexByteMapValueEqualityUsingMapAccessDifferentReference() returns boolean {
    return bytem3["key3"] == bytem3_new["key3"]; // true
}

function testComplexByteMapReferenceEqualityUsingMapAccessSameReference() returns boolean {
    return bytem1 === bytem3["key3"]; // true
}

function testComplexByteMapReferenceEqualityUsingMapAccessDifferentReference() returns boolean {
    return bytem3["key3"] === bytem3_new["key3"]; // false
}

// -----------------------------------------------------------

function testSimpleByteMapValueEqualityUsingSameReferenceInMap() returns boolean {
    return bytem7["key3"] == bytem7["key3"]; // true
}

function testSimpleByteMapValueEqualityUsingDifferentReferenceInMap() returns boolean {
    return bytem7["key3"] == bytem1; // true
}

function testSimpleByteMapReferenceEqualityUsingSameReferenceInMap() returns boolean {
    return bytem7["key3"] === bytem7["key3"]; // true
}

function testSimpleByteMapReferenceEqualityUsingDifferentReferenceInMap() returns boolean {
    return bytem7["key3"] === bytem1; // false
}

// -----------------------------------------------------------

function testComplexByteMapValueEquality() returns boolean {
    return bytem7 == bytem8; // true
}

function testComplexByteMapReferenceEquality() returns boolean {
    return bytem7 === bytem8; // false
}

function testSimpleByteMapValueEqualityInDifferentMap() returns boolean {
    return bytem7["key3"] == bytem8["key3"]; // true
}

function testSimpleByteMapReferenceEqualityInDifferentMap() returns boolean {
    return bytem7["key3"] === bytem8["key3"]; // false
}

// float -----------------------------------------------------

const map<map<float>> fm3 = { "key3": fm1, "key4": fm2 };
const map<float> fm1 = { "key1": 2.0 };
const map<float> fm2 = { "key2": 4.0 };

const map<map<float>> fm3_new = { "key3": fm1_new, "key4": fm2_new };
const map<float> fm1_new = { "key1": 2.0 };
const map<float> fm2_new = { "key2": 4.0 };

const map<map<float>> fm7 = { "key3": { "key1": 2.0 }, "key4": { "key2": 4.0 } };
const map<map<float>> fm8 = { "key3": { "key1": 2.0 }, "key4": { "key2": 4.0 } };

function testSimpleFloatMapValueEqualityUsingSameReference() returns boolean {
    return fm1 == fm1; // true
}

function testSimpleFloatMapValueEqualityUsingDifferentReference() returns boolean {
    return fm1 == fm1_new; // true
}

function testSimpleFloatMapReferenceEqualityUsingSameReference() returns boolean {
    return fm1 === fm1; // true
}

function testSimpleFloatMapReferenceEqualityUsingDifferentReference() returns boolean {
    return fm1 === fm1_new; // false
}

// -----------------------------------------------------------

function testComplexFloatMapValueEqualityUsingSameReference() returns boolean {
    return fm3 == fm3; // true
}

function testComplexFloatMapValueEqualityUsingDifferentReference() returns boolean {
    return fm3 == fm3_new; // true
}

function testComplexFloatMapReferenceEqualityUsingSameReference() returns boolean {
    return fm3 === fm3; // true
}

function testComplexFloatMapReferenceEqualityUsingDifferentReference() returns boolean {
    return fm3 === fm3_new; // false
}

// -----------------------------------------------------------

function testComplexFloatMapValueEqualityUsingMapAccessSameReference() returns boolean {
    return fm3["key3"] == fm3["key3"]; // true
}

function testComplexFloatMapValueEqualityUsingMapAccessDifferentReference() returns boolean {
    return fm3["key3"] == fm3_new["key3"]; // true
}

function testComplexFloatMapReferenceEqualityUsingMapAccessSameReference() returns boolean {
    return fm1 === fm3["key3"]; // true
}

function testComplexFloatMapReferenceEqualityUsingMapAccessDifferentReference() returns boolean {
    return fm3["key3"] === fm3_new["key3"]; // false
}

// -----------------------------------------------------------

function testSimpleFloatMapValueEqualityUsingSameReferenceInMap() returns boolean {
    return fm7["key3"] == fm7["key3"]; // true
}

function testSimpleFloatMapValueEqualityUsingDifferentReferenceInMap() returns boolean {
    return fm7["key3"] == fm1; // true
}

function testSimpleFloatMapReferenceEqualityUsingSameReferenceInMap() returns boolean {
    return fm7["key3"] === fm7["key3"]; // true
}

function testSimpleFloatMapReferenceEqualityUsingDifferentReferenceInMap() returns boolean {
    return fm7["key3"] === fm1; // false
}

// -----------------------------------------------------------

function testComplexFloatMapValueEquality() returns boolean {
    return fm7 == fm8; // true
}

function testComplexFloatMapReferenceEquality() returns boolean {
    return fm7 === fm8; // false
}

function testSimpleFloatMapValueEqualityInDifferentMap() returns boolean {
    return fm7["key3"] == fm8["key3"]; // true
}

function testSimpleFloatMapReferenceEqualityInDifferentMap() returns boolean {
    return fm7["key3"] === fm8["key3"]; // false
}

// decimal ---------------------------------------------------

const map<map<decimal>> dm3 = { "key3": dm1, "key4": dm2 };
const map<decimal> dm1 = { "key1": 100 };
const map<decimal> dm2 = { "key2": 200 };

const map<map<decimal>> dm3_new = { "key3": dm1_new, "key4": dm2_new };
const map<decimal> dm1_new = { "key1": 100 };
const map<decimal> dm2_new = { "key2": 200 };

const map<map<decimal>> dm7 = { "key3": { "key1": 100 }, "key4": { "key2": 200 } };
const map<map<decimal>> dm8 = { "key3": { "key1": 100 }, "key4": { "key2": 200 } };

function testSimpleDecimalMapValueEqualityUsingSameReference() returns boolean {
    return dm1 == dm1; // true
}

function testSimpleDecimalMapValueEqualityUsingDifferentReference() returns boolean {
    return dm1 == dm1_new; // true
}

function testSimpleDecimalMapReferenceEqualityUsingSameReference() returns boolean {
    return dm1 === dm1; // true
}

function testSimpleDecimalMapReferenceEqualityUsingDifferentReference() returns boolean {
    return dm1 === dm1_new; // false
}

// -----------------------------------------------------------

function testComplexDecimalMapValueEqualityUsingSameReference() returns boolean {
    return dm3 == dm3; // true
}

function testComplexDecimalMapValueEqualityUsingDifferentReference() returns boolean {
    return dm3 == dm3_new; // true
}

function testComplexDecimalMapReferenceEqualityUsingSameReference() returns boolean {
    return dm3 === dm3; // true
}

function testComplexDecimalMapReferenceEqualityUsingDifferentReference() returns boolean {
    return dm3 === dm3_new; // false
}

// -----------------------------------------------------------

function testComplexDecimalMapValueEqualityUsingMapAccessSameReference() returns boolean {
    return dm3["key3"] == dm3["key3"]; // true
}

function testComplexDecimalMapValueEqualityUsingMapAccessDifferentReference() returns boolean {
    return dm3["key3"] == dm3_new["key3"]; // true
}

function testComplexDecimalMapReferenceEqualityUsingMapAccessSameReference() returns boolean {
    return dm1 === dm3["key3"]; // true
}

function testComplexDecimalMapReferenceEqualityUsingMapAccessDifferentReference() returns boolean {
    return dm3["key3"] === dm3_new["key3"]; // false
}

// -----------------------------------------------------------

function testSimpleDecimalMapValueEqualityUsingSameReferenceInMap() returns boolean {
    return dm7["key3"] == dm7["key3"]; // true
}

function testSimpleDecimalMapValueEqualityUsingDifferentReferenceInMap() returns boolean {
    return dm7["key3"] == dm1; // true
}

function testSimpleDecimalMapReferenceEqualityUsingSameReferenceInMap() returns boolean {
    return dm7["key3"] === dm7["key3"]; // true
}

function testSimpleDecimalMapReferenceEqualityUsingDifferentReferenceInMap() returns boolean {
    return dm7["key3"] === dm1; // false
}

// -----------------------------------------------------------

function testComplexDecimalMapValueEquality() returns boolean {
    return dm7 == dm8; // true
}

function testComplexDecimalMapReferenceEquality() returns boolean {
    return dm7 === dm8; // false
}

function testSimpleDecimalMapValueEqualityInDifferentMap() returns boolean {
    return dm7["key3"] == dm8["key3"]; // true
}

function testSimpleDecimalMapReferenceEqualityInDifferentMap() returns boolean {
    return dm7["key3"] === dm8["key3"]; // false
}

// string ----------------------------------------------------

const map<map<string>> sm3 = { "key3": sm1, "key4": sm2 };
const map<string> sm1 = { "key1": "value1" };
const map<string> sm2 = { "key2": "value2" };

const map<map<string>> sm3_new = { "key3": sm1_new, "key4": sm2_new };
const map<string> sm1_new = { "key1": "value1" };
const map<string> sm2_new = { "key2": "value2" };

const map<map<string>> sm7 = { "key3": { "key1": "value1" }, "key4": { "key2": "value2" } };
const map<map<string>> sm8 = { "key3": { "key1": "value1" }, "key4": { "key2": "value2" } };

function testSimpleStringMapValueEqualityUsingSameReference() returns boolean {
    return sm1 == sm1; // true
}

function testSimpleStringMapValueEqualityUsingDifferentReference() returns boolean {
    return sm1 == sm1_new; // true
}

function testSimpleStringMapReferenceEqualityUsingSameReference() returns boolean {
    return sm1 === sm1; // true
}

function testSimpleStringMapReferenceEqualityUsingDifferentReference() returns boolean {
    return sm1 === sm1_new; // false
}

// -----------------------------------------------------------

function testComplexStringMapValueEqualityUsingSameReference() returns boolean {
    return sm3 == sm3; // true
}

function testComplexStringMapValueEqualityUsingDifferentReference() returns boolean {
    return sm3 == sm3_new; // true
}

function testComplexStringMapReferenceEqualityUsingSameReference() returns boolean {
    return sm3 === sm3; // true
}

function testComplexStringMapReferenceEqualityUsingDifferentReference() returns boolean {
    return sm3 === sm3_new; // false
}

// -----------------------------------------------------------

function testComplexStringMapValueEqualityUsingMapAccessSameReference() returns boolean {
    return sm3["key3"] == sm3["key3"]; // true
}

function testComplexStringMapValueEqualityUsingMapAccessDifferentReference() returns boolean {
    return sm3["key3"] == sm3_new["key3"]; // true
}

function testComplexStringMapReferenceEqualityUsingMapAccessSameReference() returns boolean {
    return sm1 === sm3["key3"]; // true
}

function testComplexStringMapReferenceEqualityUsingMapAccessDifferentReference() returns boolean {
    return sm3["key3"] === sm3_new["key3"]; // false
}

// -----------------------------------------------------------

function testSimpleStringMapValueEqualityUsingSameReferenceInMap() returns boolean {
    return sm7["key3"] == sm7["key3"]; // true
}

function testSimpleStringMapValueEqualityUsingDifferentReferenceInMap() returns boolean {
    return sm7["key3"] == sm1; // true
}

function testSimpleStringMapReferenceEqualityUsingSameReferenceInMap() returns boolean {
    return sm7["key3"] === sm7["key3"]; // true
}

function testSimpleStringMapReferenceEqualityUsingDifferentReferenceInMap() returns boolean {
    return sm7["key3"] === sm1; // false
}

// -----------------------------------------------------------

function testComplexStringMapValueEquality() returns boolean {
    return sm7 == sm8; // true
}

function testComplexStringMapReferenceEquality() returns boolean {
    return sm7 === sm8; // false
}

function testSimpleStringMapValueEqualityInDifferentMap() returns boolean {
    return sm7["key3"] == sm8["key3"]; // true
}

function testSimpleStringMapReferenceEqualityInDifferentMap() returns boolean {
    return sm7["key3"] === sm8["key3"]; // false
}

// nil -------------------------------------------------------

const map<map<()>> nm3 = { "key3": nm1, "key4": nm2 };
const map<()> nm1 = { "key1": () };
const map<()> nm2 = { "key2": () };

const map<map<()>> nm3_new = { "key3": nm1_new, "key4": nm2_new };
const map<()> nm1_new = { "key1": () };
const map<()> nm2_new = { "key2": () };

const map<map<()>> nm7 = { "key3": { "key1": () }, "key4": { "key2": () } };
const map<map<()>> nm8 = { "key3": { "key1": () }, "key4": { "key2": () } };

function testSimpleNilMapValueEqualityUsingSameReference() returns boolean {
    return nm1 == nm1; // true
}

function testSimpleNilMapValueEqualityUsingDifferentReference() returns boolean {
    return nm1 == nm1_new; // true
}

function testSimpleNilMapReferenceEqualityUsingSameReference() returns boolean {
    return nm1 === nm1; // true
}

function testSimpleNilMapReferenceEqualityUsingDifferentReference() returns boolean {
    return nm1 === nm1_new; // false
}

// -----------------------------------------------------------

function testComplexNilMapValueEqualityUsingSameReference() returns boolean {
    return nm3 == nm3; // true
}

function testComplexNilMapValueEqualityUsingDifferentReference() returns boolean {
    return nm3 == nm3_new; // true
}

function testComplexNilMapReferenceEqualityUsingSameReference() returns boolean {
    return nm3 === nm3; // true
}

function testComplexNilMapReferenceEqualityUsingDifferentReference() returns boolean {
    return nm3 === nm3_new; // false
}

// -----------------------------------------------------------

function testComplexNilMapValueEqualityUsingMapAccessSameReference() returns boolean {
    return nm3["key3"] == nm3["key3"]; // true
}

function testComplexNilMapValueEqualityUsingMapAccessDifferentReference() returns boolean {
    return nm3["key3"] == nm3_new["key3"]; // true
}

function testComplexNilMapReferenceEqualityUsingMapAccessSameReference() returns boolean {
    return nm1 === nm3["key3"]; // true
}

function testComplexNilMapReferenceEqualityUsingMapAccessDifferentReference() returns boolean {
    return nm3["key3"] === nm3_new["key3"]; // false
}

// -----------------------------------------------------------

function testSimpleNilMapValueEqualityUsingSameReferenceInMap() returns boolean {
    return nm7["key3"] == nm7["key3"]; // true
}

function testSimpleNilMapValueEqualityUsingDifferentReferenceInMap() returns boolean {
    return nm7["key3"] == nm1; // true
}

function testSimpleNilMapReferenceEqualityUsingSameReferenceInMap() returns boolean {
    return nm7["key3"] === nm7["key3"]; // true
}

function testSimpleNilMapReferenceEqualityUsingDifferentReferenceInMap() returns boolean {
    return nm7["key3"] === nm1; // false
}

// -----------------------------------------------------------

function testComplexNilMapValueEquality() returns boolean {
    return nm7 == nm8; // true
}

function testComplexNilMapReferenceEquality() returns boolean {
    return nm7 === nm8; // false
}

function testSimpleNilMapValueEqualityInDifferentMap() returns boolean {
    return nm7["key3"] == nm8["key3"]; // true
}

function testSimpleNilMapReferenceEqualityInDifferentMap() returns boolean {
    return nm7["key3"] === nm8["key3"]; // false
}
