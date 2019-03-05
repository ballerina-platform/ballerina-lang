import testorg/foo version v1;
import testorg/bar version v1;

// -----------------------------------------------------------

function testSimpleBooleanMapValueEqualityUsingSameReference() returns boolean {
    return foo:bm1 == bar:bm1;
}

function testSimpleBooleanMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:bm1 == bar:bm1_new;
}

function testSimpleBooleanMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:bm1 === bar:bm1;
}

function testSimpleBooleanMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:bm1 === bar:bm1_new;
}

// -----------------------------------------------------------

function testComplexBooleanMapValueEqualityUsingSameReference() returns boolean {
    return foo:bm3 == bar:bm3;
}

function testComplexBooleanMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:bm3 == bar:bm3_new;
}

function testComplexBooleanMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:bm3 === bar:bm3;
}

function testComplexBooleanMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:bm3 === bar:bm3_new;
}

// -----------------------------------------------------------

function testSimpleIntMapValueEqualityUsingSameReference() returns boolean {
    return foo:bm1 == bar:bm1;
}

function testSimpleIntMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:bm1 == bar:bm1_new;
}

function testSimpleIntMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:bm1 === bar:bm1;
}

function testSimpleIntMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:bm1 === bar:bm1_new;
}

// -----------------------------------------------------------

function testComplexIntMapValueEqualityUsingSameReference() returns boolean {
    return foo:bm3 == bar:bm3;
}

function testComplexIntMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:bm3 == bar:bm3_new;
}

function testComplexIntMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:bm3 === bar:bm3;
}

function testComplexIntMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:bm3 === bar:bm3_new;
}

// -----------------------------------------------------------

function testSimpleByteMapValueEqualityUsingSameReference() returns boolean {
    return foo:bytem1 == bar:bytem1;
}

function testSimpleByteMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:bytem1 == bar:bytem1_new;
}

function testSimpleByteMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:bytem1 === bar:bytem1;
}

function testSimpleByteMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:bytem1 === bar:bytem1_new;
}

// -----------------------------------------------------------

function testComplexByteMapValueEqualityUsingSameReference() returns boolean {
    return foo:bytem3 == bar:bytem3;
}

function testComplexByteMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:bytem3 == bar:bytem3_new;
}

function testComplexByteMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:bytem3 === bar:bytem3;
}

function testComplexByteMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:bytem3 === bar:bytem3_new;
}

// -----------------------------------------------------------

function testSimpleFloatMapValueEqualityUsingSameReference() returns boolean {
    return foo:fm1 == bar:fm1;
}

function testSimpleFloatMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:fm1 == bar:fm1_new;
}

function testSimpleFloatMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:fm1 === bar:fm1;
}

function testSimpleFloatMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:fm1 === bar:fm1_new;
}

// -----------------------------------------------------------

function testComplexFloatMapValueEqualityUsingSameReference() returns boolean {
    return foo:fm3 == bar:fm3;
}

function testComplexFloatMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:fm3 == bar:fm3_new;
}

function testComplexFloatMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:fm3 === bar:fm3;
}

function testComplexFloatMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:fm3 === bar:fm3_new;
}

// -----------------------------------------------------------

function testSimpleDecimalMapValueEqualityUsingSameReference() returns boolean {
    return foo:dm1 == bar:dm1;
}

function testSimpleDecimalMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:dm1 == bar:dm1_new;
}

function testSimpleDecimalMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:dm1 === bar:dm1;
}

function testSimpleDecimalMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:dm1 === bar:dm1_new;
}

// -----------------------------------------------------------

function testComplexDecimalMapValueEqualityUsingSameReference() returns boolean {
    return foo:dm3 == bar:dm3;
}

function testComplexDecimalMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:dm3 == bar:dm3_new;
}

function testComplexDecimalMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:dm3 === bar:dm3;
}

function testComplexDecimalMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:dm3 === bar:dm3_new;
}

// -----------------------------------------------------------

function testSimpleStringMapValueEqualityUsingSameReference() returns boolean {
    return foo:sm1 == bar:sm1;
}

function testSimpleStringMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:sm1 == bar:sm1_new;
}

function testSimpleStringMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:sm1 === bar:sm1;
}

function testSimpleStringMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:sm1 === bar:sm1_new;
}

// -----------------------------------------------------------

function testComplexStringMapValueEqualityUsingSameReference() returns boolean {
    return foo:sm3 == bar:sm3;
}

function testComplexStringMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:sm3 == bar:sm3_new;
}

function testComplexStringMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:sm3 === bar:sm3;
}

function testComplexStringMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:sm3 === bar:sm3_new;
}

// -----------------------------------------------------------

function testSimpleNilMapValueEqualityUsingSameReference() returns boolean {
    return foo:nm1 == bar:nm1;
}

function testSimpleNilMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:nm1 == bar:nm1_new;
}

function testSimpleNilMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:nm1 === bar:nm1;
}

function testSimpleNilMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:nm1 === bar:nm1_new;
}

// -----------------------------------------------------------

function testComplexNilMapValueEqualityUsingSameReference() returns boolean {
    return foo:nm3 == bar:nm3;
}

function testComplexNilMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:nm3 == bar:nm3_new;
}

function testComplexNilMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:nm3 === bar:nm3;
}

function testComplexNilMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:nm3 === bar:nm3_new;
}
