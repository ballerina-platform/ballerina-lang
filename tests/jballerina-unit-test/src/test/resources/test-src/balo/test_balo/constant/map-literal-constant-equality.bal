import testorg/foo;

// boolean ---------------------------------------------------

function testSimpleBooleanMapValueEqualityUsingSameReference() returns boolean {
    return foo:bm1 == foo:bm1; // true
}

function testSimpleBooleanMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:bm1 == foo:bm1_new; // true
}

function testSimpleBooleanMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:bm1 === foo:bm1; // true
}

function testSimpleBooleanMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:bm1 === foo:bm1_new; // false
}

// -----------------------------------------------------------

function testComplexBooleanMapValueEqualityUsingSameReference() returns boolean {
    return foo:bm3 == foo:bm3; // true
}

function testComplexBooleanMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:bm3 == foo:bm3_new; // true
}

function testComplexBooleanMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:bm3 === foo:bm3; // true
}

function testComplexBooleanMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:bm3 === foo:bm3_new; // false
}

// -----------------------------------------------------------

function testComplexBooleanMapValueEqualityUsingMapAccessSameReference() returns boolean {
    return foo:bm3["key5"] == foo:bm3["key5"]; // true
}

function testComplexBooleanMapValueEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:bm3["key5"] == foo:bm3_new["key5"]; // true
}

function testComplexBooleanMapReferenceEqualityUsingMapAccessSameReference() returns boolean {
    return foo:bm1 === foo:bm3["key5"]; // true
}

function testComplexBooleanMapReferenceEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:bm3["key5"] === foo:bm3_new["key5"]; // false
}

// -----------------------------------------------------------

function testSimpleBooleanMapValueEqualityUsingSameReferenceInMap() returns boolean {
    return foo:bm7["key5"] == foo:bm7["key5"]; // true
}

function testSimpleBooleanMapValueEqualityUsingDifferentReferenceInMap() returns boolean {
    return foo:bm7["key5"] == foo:bm1; // true
}

function testSimpleBooleanMapReferenceEqualityUsingSameReferenceInMap() returns boolean {
    return foo:bm7["key5"] === foo:bm7["key5"]; // true
}

function testSimpleBooleanMapReferenceEqualityUsingDifferentReferenceInMap() returns boolean {
    return foo:bm7["key5"] === foo:bm1; // false
}

// -----------------------------------------------------------

function testComplexBooleanMapValueEquality() returns boolean {
    return foo:bm7 == foo:bm8; // true
}

function testComplexBooleanMapReferenceEquality() returns boolean {
    return foo:bm7 === foo:bm8; // false
}

function testSimpleBooleanMapValueEqualityInDifferentMap() returns boolean {
    return foo:bm7["key5"] == foo:bm8["key5"]; // true
}

function testSimpleBooleanMapReferenceEqualityInDifferentMap() returns boolean {
    return foo:bm7["key5"] === foo:bm8["key5"]; // false
}

// int -------------------------------------------------------

function testSimpleIntMapValueEqualityUsingSameReference() returns boolean {
    return foo:im1 == foo:im1; // true
}

function testSimpleIntMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:im1 == foo:im1_new; // true
}

function testSimpleIntMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:im1 === foo:im1; // true
}

function testSimpleIntMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:im1 === foo:im1_new; // false
}

// -----------------------------------------------------------

function testComplexIntMapValueEqualityUsingSameReference() returns boolean {
    return foo:im3 == foo:im3; // true
}

function testComplexIntMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:im3 == foo:im3_new; // true
}

function testComplexIntMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:im3 === foo:im3; // true
}

function testComplexIntMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:im3 === foo:im3_new; // false
}

// -----------------------------------------------------------

function testComplexIntMapValueEqualityUsingMapAccessSameReference() returns boolean {
    return foo:im3["key5"] == foo:im3["key5"]; // true
}

function testComplexIntMapValueEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:im3["key5"] == foo:im3_new["key5"]; // true
}

function testComplexIntMapReferenceEqualityUsingMapAccessSameReference() returns boolean {
    return foo:im1 === foo:im3["key5"]; // true
}

function testComplexIntMapReferenceEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:im3["key5"] === foo:im3_new["key5"]; // false
}

// -----------------------------------------------------------

function testSimpleIntMapValueEqualityUsingSameReferenceInMap() returns boolean {
    return foo:im7["key5"] == foo:im7["key5"]; // true
}

function testSimpleIntMapValueEqualityUsingDifferentReferenceInMap() returns boolean {
    return foo:im7["key5"] == foo:im1; // true
}

function testSimpleIntMapReferenceEqualityUsingSameReferenceInMap() returns boolean {
    return foo:im7["key5"] === foo:im7["key5"]; // true
}

function testSimpleIntMapReferenceEqualityUsingDifferentReferenceInMap() returns boolean {
    return foo:im7["key5"] === foo:im1; // false
}

// -----------------------------------------------------------

function testComplexIntMapValueEquality() returns boolean {
    return foo:im7 == foo:im8; // true
}

function testComplexIntMapReferenceEquality() returns boolean {
    return foo:im7 === foo:im8; // false
}

function testSimpleIntMapValueEqualityInDifferentMap() returns boolean {
    return foo:im7["key5"] == foo:im8["key5"]; // true
}

function testSimpleIntMapReferenceEqualityInDifferentMap() returns boolean {
    return foo:im7["key5"] === foo:im8["key5"]; // false
}

// byte ------------------------------------------------------

function testSimpleByteMapValueEqualityUsingSameReference() returns boolean {
    return foo:bytem1 == foo:bytem1; // true
}

function testSimpleByteMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:bytem1 == foo:bytem1_new; // true
}

function testSimpleByteMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:bytem1 === foo:bytem1; // true
}

function testSimpleByteMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:bytem1 === foo:bytem1_new; // false
}

// -----------------------------------------------------------

function testComplexByteMapValueEqualityUsingSameReference() returns boolean {
    return foo:bytem3 == foo:bytem3; // true
}

function testComplexByteMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:bytem3 == foo:bytem3_new; // true
}

function testComplexByteMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:bytem3 === foo:bytem3; // true
}

function testComplexByteMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:bytem3 === foo:bytem3_new; // false
}

// -----------------------------------------------------------

function testComplexByteMapValueEqualityUsingMapAccessSameReference() returns boolean {
    return foo:bytem3["key5"] == foo:bytem3["key5"]; // true
}

function testComplexByteMapValueEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:bytem3["key5"] == foo:bytem3_new["key5"]; // true
}

function testComplexByteMapReferenceEqualityUsingMapAccessSameReference() returns boolean {
    return foo:bytem1 === foo:bytem3["key5"]; // true
}

function testComplexByteMapReferenceEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:bytem3["key5"] === foo:bytem3_new["key5"]; // false
}

// -----------------------------------------------------------

function testSimpleByteMapValueEqualityUsingSameReferenceInMap() returns boolean {
    return foo:bytem7["key5"] == foo:bytem7["key5"]; // true
}

function testSimpleByteMapValueEqualityUsingDifferentReferenceInMap() returns boolean {
    return foo:bytem7["key5"] == foo:bytem1; // true
}

function testSimpleByteMapReferenceEqualityUsingSameReferenceInMap() returns boolean {
    return foo:bytem7["key5"] === foo:bytem7["key5"]; // true
}

function testSimpleByteMapReferenceEqualityUsingDifferentReferenceInMap() returns boolean {
    return foo:bytem7["key5"] === foo:bytem1; // false
}

// -----------------------------------------------------------

function testComplexByteMapValueEquality() returns boolean {
    return foo:bytem7 == foo:bytem8; // true
}

function testComplexByteMapReferenceEquality() returns boolean {
    return foo:bytem7 === foo:bytem8; // false
}

function testSimpleByteMapValueEqualityInDifferentMap() returns boolean {
    return foo:bytem7["key5"] == foo:bytem8["key5"]; // true
}

function testSimpleByteMapReferenceEqualityInDifferentMap() returns boolean {
    return foo:bytem7["key5"] === foo:bytem8["key5"]; // false
}

// float -----------------------------------------------------

function testSimpleFloatMapValueEqualityUsingSameReference() returns boolean {
    return foo:fm1 == foo:fm1; // true
}

function testSimpleFloatMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:fm1 == foo:fm1_new; // true
}

function testSimpleFloatMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:fm1 === foo:fm1; // true
}

function testSimpleFloatMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:fm1 === foo:fm1_new; // false
}

// -----------------------------------------------------------

function testComplexFloatMapValueEqualityUsingSameReference() returns boolean {
    return foo:fm3 == foo:fm3; // true
}

function testComplexFloatMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:fm3 == foo:fm3_new; // true
}

function testComplexFloatMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:fm3 === foo:fm3; // true
}

function testComplexFloatMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:fm3 === foo:fm3_new; // false
}

// -----------------------------------------------------------

function testComplexFloatMapValueEqualityUsingMapAccessSameReference() returns boolean {
    return foo:fm3["key5"] == foo:fm3["key5"]; // true
}

function testComplexFloatMapValueEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:fm3["key5"] == foo:fm3_new["key5"]; // true
}

function testComplexFloatMapReferenceEqualityUsingMapAccessSameReference() returns boolean {
    return foo:fm1 === foo:fm3["key5"]; // true
}

function testComplexFloatMapReferenceEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:fm3["key5"] === foo:fm3_new["key5"]; // false
}

// -----------------------------------------------------------

function testSimpleFloatMapValueEqualityUsingSameReferenceInMap() returns boolean {
    return foo:fm7["key5"] == foo:fm7["key5"]; // true
}

function testSimpleFloatMapValueEqualityUsingDifferentReferenceInMap() returns boolean {
    return foo:fm7["key5"] == foo:fm1; // true
}

function testSimpleFloatMapReferenceEqualityUsingSameReferenceInMap() returns boolean {
    return foo:fm7["key5"] === foo:fm7["key5"]; // true
}

function testSimpleFloatMapReferenceEqualityUsingDifferentReferenceInMap() returns boolean {
    return foo:fm7["key5"] === foo:fm1; // false
}

// -----------------------------------------------------------

function testComplexFloatMapValueEquality() returns boolean {
    return foo:fm7 == foo:fm8; // true
}

function testComplexFloatMapReferenceEquality() returns boolean {
    return foo:fm7 === foo:fm8; // false
}

function testSimpleFloatMapValueEqualityInDifferentMap() returns boolean {
    return foo:fm7["key5"] == foo:fm8["key5"]; // true
}

function testSimpleFloatMapReferenceEqualityInDifferentMap() returns boolean {
    return foo:fm7["key5"] === foo:fm8["key5"]; // false
}

// decimal ---------------------------------------------------

function testSimpleDecimalMapValueEqualityUsingSameReference() returns boolean {
    return foo:dm1 == foo:dm1; // true
}

function testSimpleDecimalMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:dm1 == foo:dm1_new; // true
}

function testSimpleDecimalMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:dm1 === foo:dm1; // true
}

function testSimpleDecimalMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:dm1 === foo:dm1_new; // false
}

// -----------------------------------------------------------

function testComplexDecimalMapValueEqualityUsingSameReference() returns boolean {
    return foo:dm3 == foo:dm3; // true
}

function testComplexDecimalMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:dm3 == foo:dm3_new; // true
}

function testComplexDecimalMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:dm3 === foo:dm3; // true
}

function testComplexDecimalMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:dm3 === foo:dm3_new; // false
}

// -----------------------------------------------------------

function testComplexDecimalMapValueEqualityUsingMapAccessSameReference() returns boolean {
    return foo:dm3["key5"] == foo:dm3["key5"]; // true
}

function testComplexDecimalMapValueEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:dm3["key5"] == foo:dm3_new["key5"]; // true
}

function testComplexDecimalMapReferenceEqualityUsingMapAccessSameReference() returns boolean {
    return foo:dm1 === foo:dm3["key5"]; // true
}

function testComplexDecimalMapReferenceEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:dm3["key5"] === foo:dm3_new["key5"]; // false
}

// -----------------------------------------------------------

function testSimpleDecimalMapValueEqualityUsingSameReferenceInMap() returns boolean {
    return foo:dm7["key5"] == foo:dm7["key5"]; // true
}

function testSimpleDecimalMapValueEqualityUsingDifferentReferenceInMap() returns boolean {
    return foo:dm7["key5"] == foo:dm1; // true
}

function testSimpleDecimalMapReferenceEqualityUsingSameReferenceInMap() returns boolean {
    return foo:dm7["key5"] === foo:dm7["key5"]; // true
}

function testSimpleDecimalMapReferenceEqualityUsingDifferentReferenceInMap() returns boolean {
    return foo:dm7["key5"] === foo:dm1; // false
}

// -----------------------------------------------------------

function testComplexDecimalMapValueEquality() returns boolean {
    return foo:dm7 == foo:dm8; // true
}

function testComplexDecimalMapReferenceEquality() returns boolean {
    return foo:dm7 === foo:dm8; // false
}

function testSimpleDecimalMapValueEqualityInDifferentMap() returns boolean {
    return foo:dm7["key5"] == foo:dm8["key5"]; // true
}

function testSimpleDecimalMapReferenceEqualityInDifferentMap() returns boolean {
    return foo:dm7["key5"] === foo:dm8["key5"]; // false
}

// string ----------------------------------------------------

function testSimpleStringMapValueEqualityUsingSameReference() returns boolean {
    return foo:sm1 == foo:sm1; // true
}

function testSimpleStringMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:sm1 == foo:sm1_new; // true
}

function testSimpleStringMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:sm1 === foo:sm1; // true
}

function testSimpleStringMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:sm1 === foo:sm1_new; // false
}

// -----------------------------------------------------------

function testComplexStringMapValueEqualityUsingSameReference() returns boolean {
    return foo:sm3 == foo:sm3; // true
}

function testComplexStringMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:sm3 == foo:sm3_new; // true
}

function testComplexStringMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:sm3 === foo:sm3; // true
}

function testComplexStringMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:sm3 === foo:sm3_new; // false
}

// -----------------------------------------------------------

function testComplexStringMapValueEqualityUsingMapAccessSameReference() returns boolean {
    return foo:sm3["key5"] == foo:sm3["key5"]; // true
}

function testComplexStringMapValueEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:sm3["key5"] == foo:sm3_new["key5"]; // true
}

function testComplexStringMapReferenceEqualityUsingMapAccessSameReference() returns boolean {
    return foo:sm1 === foo:sm3["key5"]; // true
}

function testComplexStringMapReferenceEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:sm3["key5"] === foo:sm3_new["key5"]; // false
}

// -----------------------------------------------------------

function testSimpleStringMapValueEqualityUsingSameReferenceInMap() returns boolean {
    return foo:sm7["key5"] == foo:sm7["key5"]; // true
}

function testSimpleStringMapValueEqualityUsingDifferentReferenceInMap() returns boolean {
    return foo:sm7["key5"] == foo:sm1; // true
}

function testSimpleStringMapReferenceEqualityUsingSameReferenceInMap() returns boolean {
    return foo:sm7["key5"] === foo:sm7["key5"]; // true
}

function testSimpleStringMapReferenceEqualityUsingDifferentReferenceInMap() returns boolean {
    return foo:sm7["key5"] === foo:sm1; // false
}

// -----------------------------------------------------------

function testComplexStringMapValueEquality() returns boolean {
    return foo:sm7 == foo:sm8; // true
}

function testComplexStringMapReferenceEquality() returns boolean {
    return foo:sm7 === foo:sm8; // false
}

function testSimpleStringMapValueEqualityInDifferentMap() returns boolean {
    return foo:sm7["key5"] == foo:sm8["key5"]; // true
}

function testSimpleStringMapReferenceEqualityInDifferentMap() returns boolean {
    return foo:sm7["key5"] === foo:sm8["key5"]; // false
}

// nil -------------------------------------------------------

function testSimpleNilMapValueEqualityUsingSameReference() returns boolean {
    return foo:nm1 == foo:nm1; // true
}

function testSimpleNilMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:nm1 == foo:nm1_new; // true
}

function testSimpleNilMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:nm1 === foo:nm1; // true
}

function testSimpleNilMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:nm1 === foo:nm1_new; // false
}

// -----------------------------------------------------------

function testComplexNilMapValueEqualityUsingSameReference() returns boolean {
    return foo:nm3 == foo:nm3; // true
}

function testComplexNilMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:nm3 == foo:nm3_new; // true
}

function testComplexNilMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:nm3 === foo:nm3; // true
}

function testComplexNilMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:nm3 === foo:nm3_new; // false
}

// -----------------------------------------------------------

function testComplexNilMapValueEqualityUsingMapAccessSameReference() returns boolean {
    return foo:nm3["key5"] == foo:nm3["key5"]; // true
}

function testComplexNilMapValueEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:nm3["key5"] == foo:nm3_new["key5"]; // true
}

function testComplexNilMapReferenceEqualityUsingMapAccessSameReference() returns boolean {
    return foo:nm1 === foo:nm3["key5"]; // true
}

function testComplexNilMapReferenceEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:nm3["key5"] === foo:nm3_new["key5"]; // false
}

// -----------------------------------------------------------

function testSimpleNilMapValueEqualityUsingSameReferenceInMap() returns boolean {
    return foo:nm7["key5"] == foo:nm7["key5"]; // true
}

function testSimpleNilMapValueEqualityUsingDifferentReferenceInMap() returns boolean {
    return foo:nm7["key5"] == foo:nm1; // true
}

function testSimpleNilMapReferenceEqualityUsingSameReferenceInMap() returns boolean {
    return foo:nm7["key5"] === foo:nm7["key5"]; // true
}

function testSimpleNilMapReferenceEqualityUsingDifferentReferenceInMap() returns boolean {
    return foo:nm7["key5"] === foo:nm1; // false
}

// -----------------------------------------------------------

function testComplexNilMapValueEquality() returns boolean {
    return foo:nm7 == foo:nm8; // true
}

function testComplexNilMapReferenceEquality() returns boolean {
    return foo:nm7 === foo:nm8; // false
}

function testSimpleNilMapValueEqualityInDifferentMap() returns boolean {
    return foo:nm7["key5"] == foo:nm8["key5"]; // true
}

function testSimpleNilMapReferenceEqualityInDifferentMap() returns boolean {
    return foo:nm7["key5"] === foo:nm8["key5"]; // false
}
