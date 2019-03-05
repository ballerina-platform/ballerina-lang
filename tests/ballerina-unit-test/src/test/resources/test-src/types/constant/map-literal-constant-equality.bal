const map<map<boolean>> bm3 = { "key3": bm1, "key4": bm2 };
const map<boolean> bm1 = { "key1": true };
const map<boolean> bm2 = { "key2": false };

const map<map<boolean>> bm3_new = { "key3": bm1_new, "key4": bm2_new };
const map<boolean> bm1_new = { "key1": true };
const map<boolean> bm2_new = { "key2": false };

function testSimpleBooleanMapValueEqualityUsingSameReference() returns boolean {
    return bm1 == bm1;
}

function testSimpleBooleanMapValueEqualityUsingDifferentReference() returns boolean {
    return bm1 == bm1_new;
}

function testSimpleBooleanMapReferenceEqualityUsingSameReference() returns boolean {
    return bm1 === bm1;
}

function testSimpleBooleanMapReferenceEqualityUsingDifferentReference() returns boolean {
    return bm1 === bm1_new;
}

// -----------------------------------------------------------

function testComplexBooleanMapValueEqualityUsingSameReference() returns boolean {
    return bm3 == bm3;
}

function testComplexBooleanMapValueEqualityUsingDifferentReference() returns boolean {
    return bm3 == bm3_new;
}

function testComplexBooleanMapReferenceEqualityUsingSameReference() returns boolean {
    return bm3 === bm3;
}

function testComplexBooleanMapReferenceEqualityUsingDifferentReference() returns boolean {
    return bm3 === bm3_new;
}

// -----------------------------------------------------------

function testSimpleBooleanMapValueEqualityUsingMapAccessSameReference() returns boolean {
    return bm3.key3 === bm3.key3;
}

function testSimpleBooleanMapValueEqualityUsingMapAccessDifferentReference() returns boolean {
    return bm3.key3 == bm3_new.key3;
}

function testSimpleBooleanMapReferenceEqualityUsingMapAccessSameReference() returns boolean {
    return bm1 === bm3.key3;
}

function testSimpleBooleanMapReferenceEqualityUsingMapAccessDifferentReference() returns boolean {
    return bm3.key3 === bm3_new.key3;
}

// -----------------------------------------------------------

const map<map<int>> im3 = { "key3": im1, "key4": im2 };
const map<int> im1 = { "key1": 1 };
const map<int> im2 = { "key2": 2 };

const map<map<int>> im3_new = { "key3": im1_new, "key4": im2_new };
const map<int> im1_new = { "key1": 1 };
const map<int> im2_new = { "key2": 2 };

function testSimpleIntMapValueEqualityUsingSameReference() returns boolean {
    return im1 == im1;
}

function testSimpleIntMapValueEqualityUsingDifferentReference() returns boolean {
    return im1 == im1_new;
}

function testSimpleIntMapReferenceEqualityUsingSameReference() returns boolean {
    return im1 === im1;
}

function testSimpleIntMapReferenceEqualityUsingDifferentReference() returns boolean {
    return im1 === im1_new;
}

// -----------------------------------------------------------

function testComplexIntMapValueEqualityUsingSameReference() returns boolean {
    return im3 == im3;
}

function testComplexIntMapValueEqualityUsingDifferentReference() returns boolean {
    return im3 == im3_new;
}

function testComplexIntMapReferenceEqualityUsingSameReference() returns boolean {
    return im3 === im3;
}

function testComplexIntMapReferenceEqualityUsingDifferentReference() returns boolean {
    return im3 === im3_new;
}

// -----------------------------------------------------------

function testSimpleIntMapValueEqualityUsingMapAccessSameReference() returns boolean {
    return im3.key3 === im3.key3;
}

function testSimpleIntMapValueEqualityUsingMapAccessDifferentReference() returns boolean {
    return im3.key3 == im3_new.key3;
}

function testSimpleIntMapReferenceEqualityUsingMapAccessSameReference() returns boolean {
    return im1 === im3.key3;
}

function testSimpleIntMapReferenceEqualityUsingMapAccessDifferentReference() returns boolean {
    return im3.key3 === im3_new.key3;
}

// -----------------------------------------------------------

const map<map<byte>> bytem3 = { "key3": bytem1, "key4": bytem2 };
const map<byte> bytem1 = { "key1": 10 };
const map<byte> bytem2 = { "key2": 20 };

const map<map<byte>> bytem3_new = { "key3": bytem1_new, "key4": bytem2_new };
const map<byte> bytem1_new = { "key1": 10 };
const map<byte> bytem2_new = { "key2": 20 };

function testSimpleByteMapValueEqualityUsingSameReference() returns boolean {
    return bytem1 == bytem1;
}

function testSimpleByteMapValueEqualityUsingDifferentReference() returns boolean {
    return bytem1 == bytem1_new;
}

function testSimpleByteMapReferenceEqualityUsingSameReference() returns boolean {
    return bytem1 === bytem1;
}

function testSimpleByteMapReferenceEqualityUsingDifferentReference() returns boolean {
    return bytem1 === bytem1_new;
}

// -----------------------------------------------------------

function testComplexByteMapValueEqualityUsingSameReference() returns boolean {
    return bytem3 == bytem3;
}

function testComplexByteMapValueEqualityUsingDifferentReference() returns boolean {
    return bytem3 == bytem3_new;
}

function testComplexByteMapReferenceEqualityUsingSameReference() returns boolean {
    return bytem3 === bytem3;
}

function testComplexByteMapReferenceEqualityUsingDifferentReference() returns boolean {
    return bytem3 === bytem3_new;
}

// -----------------------------------------------------------

function testSimpleByteMapValueEqualityUsingMapAccessSameReference() returns boolean {
    return bytem3.key3 === bytem3.key3;
}

function testSimpleByteMapValueEqualityUsingMapAccessDifferentReference() returns boolean {
    return bytem3.key3 == bytem3_new.key3;
}

function testSimpleByteMapReferenceEqualityUsingMapAccessSameReference() returns boolean {
    return bytem1 === bytem3.key3;
}

function testSimpleByteMapReferenceEqualityUsingMapAccessDifferentReference() returns boolean {
    return bytem3.key3 === bytem3_new.key3;
}

// -----------------------------------------------------------

const map<map<float>> fm3 = { "key3": fm1, "key4": fm2 };
const map<float> fm1 = { "key1": 2.0 };
const map<float> fm2 = { "key2": 4.0 };

const map<map<float>> fm3_new = { "key3": fm1_new, "key4": fm2_new };
const map<float> fm1_new = { "key1": 2.0 };
const map<float> fm2_new = { "key2": 4.0 };

function testSimpleFloatMapValueEqualityUsingSameReference() returns boolean {
    return fm1 == fm1;
}

function testSimpleFloatMapValueEqualityUsingDifferentReference() returns boolean {
    return fm1 == fm1_new;
}

function testSimpleFloatMapReferenceEqualityUsingSameReference() returns boolean {
    return fm1 === fm1;
}

function testSimpleFloatMapReferenceEqualityUsingDifferentReference() returns boolean {
    return fm1 === fm1_new;
}

// -----------------------------------------------------------

function testComplexFloatMapValueEqualityUsingSameReference() returns boolean {
    return fm3 == fm3;
}

function testComplexFloatMapValueEqualityUsingDifferentReference() returns boolean {
    return fm3 == fm3_new;
}

function testComplexFloatMapReferenceEqualityUsingSameReference() returns boolean {
    return fm3 === fm3;
}

function testComplexFloatMapReferenceEqualityUsingDifferentReference() returns boolean {
    return fm3 === fm3_new;
}

// -----------------------------------------------------------

function testSimpleFloatMapValueEqualityUsingMapAccessSameReference() returns boolean {
    return fm3.key3 === fm3.key3;
}

function testSimpleFloatMapValueEqualityUsingMapAccessDifferentReference() returns boolean {
    return fm3.key3 == fm3_new.key3;
}

function testSimpleFloatMapReferenceEqualityUsingMapAccessSameReference() returns boolean {
    return fm1 === fm3.key3;
}

function testSimpleFloatMapReferenceEqualityUsingMapAccessDifferentReference() returns boolean {
    return fm3.key3 === fm3_new.key3;
}

// -----------------------------------------------------------

const map<map<decimal>> dm3 = { "key3": dm1, "key4": dm2 };
const map<decimal> dm1 = { "key1": 100 };
const map<decimal> dm2 = { "key2": 200 };

const map<map<decimal>> dm3_new = { "key3": dm1_new, "key4": dm2_new };
const map<decimal> dm1_new = { "key1": 100 };
const map<decimal> dm2_new = { "key2": 200 };

function testSimpleDecimalMapValueEqualityUsingSameReference() returns boolean {
    return dm1 == dm1;
}

function testSimpleDecimalMapValueEqualityUsingDifferentReference() returns boolean {
    return dm1 == dm1_new;
}

function testSimpleDecimalMapReferenceEqualityUsingSameReference() returns boolean {
    return dm1 === dm1;
}

function testSimpleDecimalMapReferenceEqualityUsingDifferentReference() returns boolean {
    return dm1 === dm1_new;
}

// -----------------------------------------------------------

function testComplexDecimalMapValueEqualityUsingSameReference() returns boolean {
    return dm3 == dm3;
}

function testComplexDecimalMapValueEqualityUsingDifferentReference() returns boolean {
    return dm3 == dm3_new;
}

function testComplexDecimalMapReferenceEqualityUsingSameReference() returns boolean {
    return dm3 === dm3;
}

function testComplexDecimalMapReferenceEqualityUsingDifferentReference() returns boolean {
    return dm3 === dm3_new;
}

// -----------------------------------------------------------

function testSimpleDecimalMapValueEqualityUsingMapAccessSameReference() returns boolean {
    return dm3.key3 === dm3.key3;
}

function testSimpleDecimalMapValueEqualityUsingMapAccessDifferentReference() returns boolean {
    return dm3.key3 == dm3_new.key3;
}

function testSimpleDecimalMapReferenceEqualityUsingMapAccessSameReference() returns boolean {
    return dm1 === dm3.key3;
}

function testSimpleDecimalMapReferenceEqualityUsingMapAccessDifferentReference() returns boolean {
    return dm3.key3 === dm3_new.key3;
}

// -----------------------------------------------------------

const map<map<string>> sm3 = { "key3": sm1, "key4": sm2 };
const map<string> sm1 = { "key1": "value1" };
const map<string> sm2 = { "key2": "value2" };

const map<map<string>> sm3_new = { "key3": sm1_new, "key4": sm2_new };
const map<string> sm1_new = { "key1": "value1" };
const map<string> sm2_new = { "key2": "value2" };

function testSimpleStringMapValueEqualityUsingSameReference() returns boolean {
    return sm1 == sm1;
}

function testSimpleStringMapValueEqualityUsingDifferentReference() returns boolean {
    return sm1 == sm1_new;
}

function testSimpleStringMapReferenceEqualityUsingSameReference() returns boolean {
    return sm1 === sm1;
}

function testSimpleStringMapReferenceEqualityUsingDifferentReference() returns boolean {
    return sm1 === sm1_new;
}

// -----------------------------------------------------------

function testComplexStringMapValueEqualityUsingSameReference() returns boolean {
    return sm3 == sm3;
}

function testComplexStringMapValueEqualityUsingDifferentReference() returns boolean {
    return sm3 == sm3_new;
}

function testComplexStringMapReferenceEqualityUsingSameReference() returns boolean {
    return sm3 === sm3;
}

function testComplexStringMapReferenceEqualityUsingDifferentReference() returns boolean {
    return sm3 === sm3_new;
}

// -----------------------------------------------------------

function testSimpleStringMapValueEqualityUsingMapAccessSameReference() returns boolean {
    return sm3.key3 === sm3.key3;
}

function testSimpleStringMapValueEqualityUsingMapAccessDifferentReference() returns boolean {
    return sm3.key3 == sm3_new.key3;
}

function testSimpleStringMapReferenceEqualityUsingMapAccessSameReference() returns boolean {
    return sm1 === sm3.key3;
}

function testSimpleStringMapReferenceEqualityUsingMapAccessDifferentReference() returns boolean {
    return sm3.key3 === sm3_new.key3;
}

// -----------------------------------------------------------

const map<map<()>> nm3 = { "key3": nm1, "key4": nm2 };
const map<()> nm1 = { "key1": () };
const map<()> nm2 = { "key2": () };

const map<map<()>> nm3_new = { "key3": nm1_new, "key4": nm2_new };
const map<()> nm1_new = { "key1": () };
const map<()> nm2_new = { "key2": () };

function testSimpleNilMapValueEqualityUsingSameReference() returns boolean {
    return nm1 == nm1;
}

function testSimpleNilMapValueEqualityUsingDifferentReference() returns boolean {
    return nm1 == nm1_new;
}

function testSimpleNilMapReferenceEqualityUsingSameReference() returns boolean {
    return nm1 === nm1;
}

function testSimpleNilMapReferenceEqualityUsingDifferentReference() returns boolean {
    return nm1 === nm1_new;
}

// -----------------------------------------------------------

function testComplexNilMapValueEqualityUsingSameReference() returns boolean {
    return nm3 == nm3;
}

function testComplexNilMapValueEqualityUsingDifferentReference() returns boolean {
    return nm3 == nm3_new;
}

function testComplexNilMapReferenceEqualityUsingSameReference() returns boolean {
    return nm3 === nm3;
}

function testComplexNilMapReferenceEqualityUsingDifferentReference() returns boolean {
    return nm3 === nm3_new;
}

// -----------------------------------------------------------

function testSimpleNilMapValueEqualityUsingMapAccessSameReference() returns boolean {
    return nm3.key3 === nm3.key3;
}

function testSimpleNilMapValueEqualityUsingMapAccessDifferentReference() returns boolean {
    return nm3.key3 == nm3_new.key3;
}

function testSimpleNilMapReferenceEqualityUsingMapAccessSameReference() returns boolean {
    return nm1 === nm3.key3;
}

function testSimpleNilMapReferenceEqualityUsingMapAccessDifferentReference() returns boolean {
    return nm3.key3 === nm3_new.key3;
}
