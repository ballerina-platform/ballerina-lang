import testorg/foo;
import testorg/bar;

// boolean ---------------------------------------------------

function testSimpleBooleanMapValueEqualityUsingSameReference() returns boolean {
    return foo:bm1 == bar:bm1; // true
}

function testSimpleBooleanMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:bm1 == bar:bm1_new; // true
}

function testSimpleBooleanMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:bm1 === bar:bm1; // true
}

function testSimpleBooleanMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:bm1 === bar:bm1_new; // false
}

// -----------------------------------------------------------

function testComplexBooleanMapValueEqualityUsingSameReference() returns boolean {
    return foo:bm3 == bar:bm3; // true
}

function testComplexBooleanMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:bm3 == bar:bm3_new; // true
}

function testComplexBooleanMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:bm3 === bar:bm3; // false
}

function testComplexBooleanMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:bm3 === bar:bm3_new; // false
}

// -----------------------------------------------------------

function testComplexBooleanMapValueEqualityUsingMapAccessSameReference() returns boolean {
    return foo:bm3["key5"] == bar:bm3["key5"]; // true
}

function testComplexBooleanMapValueEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:bm3["key5"] == bar:bm3_new["key5"]; // true
}

function testComplexBooleanMapReferenceEqualityUsingMapAccessSameReference() returns boolean {
    return foo:bm1 === bar:bm3["key5"]; // false
}

function testComplexBooleanMapReferenceEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:bm3["key5"] === bar:bm3_new["key5"]; // false
}

// -----------------------------------------------------------

function testSimpleBooleanMapValueEqualityUsingSameReferenceInMap() returns boolean {
    return foo:bm7["key5"] == bar:bm7["key5"]; // true
}

function testSimpleBooleanMapValueEqualityUsingDifferentReferenceInMap() returns boolean {
    return foo:bm7["key5"] == bar:bm1; // true
}

function testSimpleBooleanMapReferenceEqualityUsingSameReferenceInMap() returns boolean {
    return foo:bm7["key5"] === bar:bm7["key5"]; // false
}

function testSimpleBooleanMapReferenceEqualityUsingDifferentReferenceInMap() returns boolean {
    return foo:bm7["key5"] === bar:bm1; // false
}

// -----------------------------------------------------------

function testComplexBooleanMapValueEquality() returns boolean {
    return foo:bm7 == bar:bm8; // true
}

function testComplexBooleanMapReferenceEquality() returns boolean {
    return foo:bm7 === bar:bm8; // false
}

function testSimpleBooleanMapValueEqualityInDifferentMap() returns boolean {
    return foo:bm7["key5"] == bar:bm8["key5"]; // true
}

function testSimpleBooleanMapReferenceEqualityInDifferentMap() returns boolean {
    return foo:bm7["key5"] === bar:bm8["key5"]; // false
}

// int -------------------------------------------------------

function testSimpleIntMapValueEqualityUsingSameReference() returns boolean {
    return foo:im1 == bar:im1; // true
}

function testSimpleIntMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:im1 == bar:im1_new; // true
}

function testSimpleIntMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:im1 === bar:im1; // false
}

function testSimpleIntMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:im1 === bar:im1_new; // false
}

// -----------------------------------------------------------

function testComplexIntMapValueEqualityUsingSameReference() returns boolean {
    return foo:im3 == bar:im3; // true
}

function testComplexIntMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:im3 == bar:im3_new; // true
}

function testComplexIntMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:im3 === bar:im3; // false
}

function testComplexIntMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:im3 === bar:im3_new; // false
}

// -----------------------------------------------------------

function testComplexIntMapValueEqualityUsingMapAccessSameReference() returns boolean {
    return foo:im3["key5"] == bar:im3["key5"]; // true
}

function testComplexIntMapValueEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:im3["key5"] == bar:im3_new["key5"]; // true
}

function testComplexIntMapReferenceEqualityUsingMapAccessSameReference() returns boolean {
    return foo:im1 === bar:im3["key5"]; // false
}

function testComplexIntMapReferenceEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:im3["key5"] === bar:im3_new["key5"]; // false
}

// -----------------------------------------------------------

function testSimpleIntMapValueEqualityUsingSameReferenceInMap() returns boolean {
    return foo:im7["key5"] == bar:im7["key5"]; // true
}

function testSimpleIntMapValueEqualityUsingDifferentReferenceInMap() returns boolean {
    return foo:im7["key5"] == bar:im1; // true
}

function testSimpleIntMapReferenceEqualityUsingSameReferenceInMap() returns boolean {
    return foo:im7["key5"] === bar:im7["key5"]; // false
}

function testSimpleIntMapReferenceEqualityUsingDifferentReferenceInMap() returns boolean {
    return foo:im7["key5"] === bar:im1; // false
}

// -----------------------------------------------------------

function testComplexIntMapValueEquality() returns boolean {
    return foo:im7 == bar:im8; // true
}

function testComplexIntMapReferenceEquality() returns boolean {
    return foo:im7 === bar:im8; // false
}

function testSimpleIntMapValueEqualityInDifferentMap() returns boolean {
    return foo:im7["key5"] == bar:im8["key5"]; // true
}

function testSimpleIntMapReferenceEqualityInDifferentMap() returns boolean {
    return foo:im7["key5"] === bar:im8["key5"]; // false
}

// byte ------------------------------------------------------

function testSimpleByteMapValueEqualityUsingSameReference() returns boolean {
    return foo:bytem1 == bar:bytem1; // true
}

function testSimpleByteMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:bytem1 == bar:bytem1_new; // true
}

function testSimpleByteMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:bytem1 === bar:bytem1; // false
}

function testSimpleByteMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:bytem1 === bar:bytem1_new; // false
}

// -----------------------------------------------------------

function testComplexByteMapValueEqualityUsingSameReference() returns boolean {
    return foo:bytem3 == bar:bytem3; // true
}

function testComplexByteMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:bytem3 == bar:bytem3_new; // true
}

function testComplexByteMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:bytem3 === bar:bytem3; // false
}

function testComplexByteMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:bytem3 === bar:bytem3_new; // false
}

// -----------------------------------------------------------

function testComplexByteMapValueEqualityUsingMapAccessSameReference() returns boolean {
    return foo:bytem3["key5"] == bar:bytem3["key5"]; // true
}

function testComplexByteMapValueEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:bytem3["key5"] == bar:bytem3_new["key5"]; // true
}

function testComplexByteMapReferenceEqualityUsingMapAccessSameReference() returns boolean {
    return foo:bytem1 === bar:bytem3["key5"]; // false
}

function testComplexByteMapReferenceEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:bytem3["key5"] === bar:bytem3_new["key5"]; // false
}

// -----------------------------------------------------------

function testSimpleByteMapValueEqualityUsingSameReferenceInMap() returns boolean {
    return foo:bytem7["key5"] == bar:bytem7["key5"]; // true
}

function testSimpleByteMapValueEqualityUsingDifferentReferenceInMap() returns boolean {
    return foo:bytem7["key5"] == bar:bytem1; // true
}

function testSimpleByteMapReferenceEqualityUsingSameReferenceInMap() returns boolean {
    return foo:bytem7["key5"] === bar:bytem7["key5"]; // false
}

function testSimpleByteMapReferenceEqualityUsingDifferentReferenceInMap() returns boolean {
    return foo:bytem7["key5"] === bar:bytem1; // false
}

// -----------------------------------------------------------

function testComplexByteMapValueEquality() returns boolean {
    return foo:bytem7 == bar:bytem8; // true
}

function testComplexByteMapReferenceEquality() returns boolean {
    return foo:bytem7 === bar:bytem8; // false
}

function testSimpleByteMapValueEqualityInDifferentMap() returns boolean {
    return foo:bytem7["key5"] == bar:bytem8["key5"]; // true
}

function testSimpleByteMapReferenceEqualityInDifferentMap() returns boolean {
    return foo:bytem7["key5"] === bar:bytem8["key5"]; // false
}

// float -----------------------------------------------------

function testSimpleFloatMapValueEqualityUsingSameReference() returns boolean {
    return foo:fm1 == bar:fm1; // true
}

function testSimpleFloatMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:fm1 == bar:fm1_new; // true
}

function testSimpleFloatMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:fm1 === bar:fm1; // false
}

function testSimpleFloatMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:fm1 === bar:fm1_new; // false
}

// -----------------------------------------------------------

function testComplexFloatMapValueEqualityUsingSameReference() returns boolean {
    return foo:fm3 == bar:fm3; // true
}

function testComplexFloatMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:fm3 == bar:fm3_new; // true
}

function testComplexFloatMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:fm3 === bar:fm3; // false
}

function testComplexFloatMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:fm3 === bar:fm3_new; // false
}

// -----------------------------------------------------------

function testComplexFloatMapValueEqualityUsingMapAccessSameReference() returns boolean {
    return foo:fm3["key5"] == bar:fm3["key5"]; // true
}

function testComplexFloatMapValueEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:fm3["key5"] == bar:fm3_new["key5"]; // true
}

function testComplexFloatMapReferenceEqualityUsingMapAccessSameReference() returns boolean {
    return foo:fm1 === bar:fm3["key5"]; // false
}

function testComplexFloatMapReferenceEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:fm3["key5"] === bar:fm3_new["key5"]; // false
}

// -----------------------------------------------------------

function testSimpleFloatMapValueEqualityUsingSameReferenceInMap() returns boolean {
    return foo:fm7["key5"] == bar:fm7["key5"]; // true
}

function testSimpleFloatMapValueEqualityUsingDifferentReferenceInMap() returns boolean {
    return foo:fm7["key5"] == bar:fm1; // true
}

function testSimpleFloatMapReferenceEqualityUsingSameReferenceInMap() returns boolean {
    return foo:fm7["key5"] === bar:fm7["key5"]; // false
}

function testSimpleFloatMapReferenceEqualityUsingDifferentReferenceInMap() returns boolean {
    return foo:fm7["key5"] === bar:fm1; // false
}

// -----------------------------------------------------------

function testComplexFloatMapValueEquality() returns boolean {
    return foo:fm7 == bar:fm8; // true
}

function testComplexFloatMapReferenceEquality() returns boolean {
    return foo:fm7 === bar:fm8; // false
}

function testSimpleFloatMapValueEqualityInDifferentMap() returns boolean {
    return foo:fm7["key5"] == bar:fm8["key5"]; // true
}

function testSimpleFloatMapReferenceEqualityInDifferentMap() returns boolean {
    return foo:fm7["key5"] === bar:fm8["key5"]; // false
}

// decimal ---------------------------------------------------

function testSimpleDecimalMapValueEqualityUsingSameReference() returns boolean {
    return foo:dm1 == bar:dm1; // true
}

function testSimpleDecimalMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:dm1 == bar:dm1_new; // true
}

function testSimpleDecimalMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:dm1 === bar:dm1; // false
}

function testSimpleDecimalMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:dm1 === bar:dm1_new; // false
}

// -----------------------------------------------------------

function testComplexDecimalMapValueEqualityUsingSameReference() returns boolean {
    return foo:dm3 == bar:dm3; // true
}

function testComplexDecimalMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:dm3 == bar:dm3_new; // true
}

function testComplexDecimalMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:dm3 === bar:dm3; // false
}

function testComplexDecimalMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:dm3 === bar:dm3_new; // false
}

// -----------------------------------------------------------

function testComplexDecimalMapValueEqualityUsingMapAccessSameReference() returns boolean {
    return foo:dm3["key5"] == bar:dm3["key5"]; // true
}

function testComplexDecimalMapValueEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:dm3["key5"] == bar:dm3_new["key5"]; // true
}

function testComplexDecimalMapReferenceEqualityUsingMapAccessSameReference() returns boolean {
    return foo:dm1 === bar:dm3["key5"]; // false
}

function testComplexDecimalMapReferenceEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:dm3["key5"] === bar:dm3_new["key5"]; // false
}

// -----------------------------------------------------------

function testSimpleDecimalMapValueEqualityUsingSameReferenceInMap() returns boolean {
    return foo:dm7["key5"] == bar:dm7["key5"]; // true
}

function testSimpleDecimalMapValueEqualityUsingDifferentReferenceInMap() returns boolean {
    return foo:dm7["key5"] == bar:dm1; // true
}

function testSimpleDecimalMapReferenceEqualityUsingSameReferenceInMap() returns boolean {
    return foo:dm7["key5"] === bar:dm7["key5"]; // false
}

function testSimpleDecimalMapReferenceEqualityUsingDifferentReferenceInMap() returns boolean {
    return foo:dm7["key5"] === bar:dm1; // false
}

// -----------------------------------------------------------

function testComplexDecimalMapValueEquality() returns boolean {
    return foo:dm7 == bar:dm8; // true
}

function testComplexDecimalMapReferenceEquality() returns boolean {
    return foo:dm7 === bar:dm8; // false
}

function testSimpleDecimalMapValueEqualityInDifferentMap() returns boolean {
    return foo:dm7["key5"] == bar:dm8["key5"]; // true
}

function testSimpleDecimalMapReferenceEqualityInDifferentMap() returns boolean {
    return foo:dm7["key5"] === bar:dm8["key5"]; // false
}

// string ----------------------------------------------------

function testSimpleStringMapValueEqualityUsingSameReference() returns boolean {
    return foo:sm1 == bar:sm1; // true
}

function testSimpleStringMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:sm1 == bar:sm1_new; // true
}

function testSimpleStringMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:sm1 === bar:sm1; // false
}

function testSimpleStringMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:sm1 === bar:sm1_new; // false
}

// -----------------------------------------------------------

function testComplexStringMapValueEqualityUsingSameReference() returns boolean {
    return foo:sm3 == bar:sm3; // true
}

function testComplexStringMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:sm3 == bar:sm3_new; // true
}

function testComplexStringMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:sm3 === bar:sm3; // false
}

function testComplexStringMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:sm3 === bar:sm3_new; // false
}

// -----------------------------------------------------------

function testComplexStringMapValueEqualityUsingMapAccessSameReference() returns boolean {
    return foo:sm3["key5"] == bar:sm3["key5"]; // true
}

function testComplexStringMapValueEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:sm3["key5"] == bar:sm3_new["key5"]; // true
}

function testComplexStringMapReferenceEqualityUsingMapAccessSameReference() returns boolean {
    return foo:sm1 === bar:sm3["key5"]; // false
}

function testComplexStringMapReferenceEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:sm3["key5"] === bar:sm3_new["key5"]; // false
}

// -----------------------------------------------------------

function testSimpleStringMapValueEqualityUsingSameReferenceInMap() returns boolean {
    return foo:sm7["key5"] == bar:sm7["key5"]; // true
}

function testSimpleStringMapValueEqualityUsingDifferentReferenceInMap() returns boolean {
    return foo:sm7["key5"] == bar:sm1; // true
}

function testSimpleStringMapReferenceEqualityUsingSameReferenceInMap() returns boolean {
    return foo:sm7["key5"] === bar:sm7["key5"]; // false
}

function testSimpleStringMapReferenceEqualityUsingDifferentReferenceInMap() returns boolean {
    return foo:sm7["key5"] === bar:sm1; // false
}

// -----------------------------------------------------------

function testComplexStringMapValueEquality() returns boolean {
    return foo:sm7 == bar:sm8; // true
}

function testComplexStringMapReferenceEquality() returns boolean {
    return foo:sm7 === bar:sm8; // false
}

function testSimpleStringMapValueEqualityInDifferentMap() returns boolean {
    return foo:sm7["key5"] == bar:sm8["key5"]; // true
}

function testSimpleStringMapReferenceEqualityInDifferentMap() returns boolean {
    return foo:sm7["key5"] === bar:sm8["key5"]; // false
}

// nil -------------------------------------------------------

function testSimpleNilMapValueEqualityUsingSameReference() returns boolean {
    return foo:nm1 == bar:nm1; // true
}

function testSimpleNilMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:nm1 == bar:nm1_new; // true
}

function testSimpleNilMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:nm1 === bar:nm1; // false
}

function testSimpleNilMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:nm1 === bar:nm1_new; // false
}

// -----------------------------------------------------------

function testComplexNilMapValueEqualityUsingSameReference() returns boolean {
    return foo:nm3 == bar:nm3; // true
}

function testComplexNilMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:nm3 == bar:nm3_new; // true
}

function testComplexNilMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:nm3 === bar:nm3; // false
}

function testComplexNilMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:nm3 === bar:nm3_new; // false
}

// -----------------------------------------------------------

function testComplexNilMapValueEqualityUsingMapAccessSameReference() returns boolean {
    return foo:nm3["key5"] == bar:nm3["key5"]; // true
}

function testComplexNilMapValueEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:nm3["key5"] == bar:nm3_new["key5"]; // true
}

function testComplexNilMapReferenceEqualityUsingMapAccessSameReference() returns boolean {
    return foo:nm1 === bar:nm3["key5"]; // false
}

function testComplexNilMapReferenceEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:nm3["key5"] === bar:nm3_new["key5"]; // false
}

// -----------------------------------------------------------

function testSimpleNilMapValueEqualityUsingSameReferenceInMap() returns boolean {
    return foo:nm7["key5"] == bar:nm7["key5"]; // true
}

function testSimpleNilMapValueEqualityUsingDifferentReferenceInMap() returns boolean {
    return foo:nm7["key5"] == bar:nm1; // true
}

function testSimpleNilMapReferenceEqualityUsingSameReferenceInMap() returns boolean {
    return foo:nm7["key5"] === bar:nm7["key5"]; // false
}

function testSimpleNilMapReferenceEqualityUsingDifferentReferenceInMap() returns boolean {
    return foo:nm7["key5"] === bar:nm1; // false
}

// -----------------------------------------------------------

function testComplexNilMapValueEquality() returns boolean {
    return foo:nm7 == bar:nm8; // true
}

function testComplexNilMapReferenceEquality() returns boolean {
    return foo:nm7 === bar:nm8; // false
}

function testSimpleNilMapValueEqualityInDifferentMap() returns boolean {
    return foo:nm7["key5"] == bar:nm8["key5"]; // true
}

function testSimpleNilMapReferenceEqualityInDifferentMap() returns boolean {
    return foo:nm7["key5"] === bar:nm8["key5"]; // false
}
