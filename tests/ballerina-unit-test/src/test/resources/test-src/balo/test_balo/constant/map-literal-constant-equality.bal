import testorg/foo version v1;

// -----------------------------------------------------------

function testSimpleBooleanMapValueEqualityUsingSameReference() returns boolean {
    return foo:bm1 == foo:bm1;
}

function testSimpleBooleanMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:bm1 == foo:bm1_new;
}

function testSimpleBooleanMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:bm1 === foo:bm1;
}

function testSimpleBooleanMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:bm1 === foo:bm1_new;
}

// -----------------------------------------------------------

function testComplexBooleanMapValueEqualityUsingSameReference() returns boolean {
    return foo:bm3 == foo:bm3;
}

function testComplexBooleanMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:bm3 == foo:bm3_new;
}

function testComplexBooleanMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:bm3 === foo:bm3;
}

function testComplexBooleanMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:bm3 === foo:bm3_new;
}

// -----------------------------------------------------------

function testComplexBooleanMapValueEqualityUsingMapAccessSameReference() returns boolean {
    return foo:bm3.key3 == foo:bm3.key3;
}

function testComplexBooleanMapValueEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:bm3.key3 == foo:bm3_new.key3;
}

function testComplexBooleanMapReferenceEqualityUsingMapAccessSameReference() returns boolean {
    return foo:bm1 === foo:bm3.key3;
}

function testComplexBooleanMapReferenceEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:bm3.key3 === foo:bm3_new.key3;
}

// -----------------------------------------------------------

function testSimpleIntMapValueEqualityUsingSameReference() returns boolean {
    return foo:bm1 == foo:bm1;
}

function testSimpleIntMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:bm1 == foo:bm1_new;
}

function testSimpleIntMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:bm1 === foo:bm1;
}

function testSimpleIntMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:bm1 === foo:bm1_new;
}

// -----------------------------------------------------------

function testComplexIntMapValueEqualityUsingSameReference() returns boolean {
    return foo:bm3 == foo:bm3;
}

function testComplexIntMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:bm3 == foo:bm3_new;
}

function testComplexIntMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:bm3 === foo:bm3;
}

function testComplexIntMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:bm3 === foo:bm3_new;
}

// -----------------------------------------------------------

function testComplexIntMapValueEqualityUsingMapAccessSameReference() returns boolean {
    return foo:im3.key3 == foo:im3.key3;
}

function testComplexIntMapValueEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:im3.key3 == foo:im3_new.key3;
}

function testComplexIntMapReferenceEqualityUsingMapAccessSameReference() returns boolean {
    return foo:im1 === foo:im3.key3;
}

function testComplexIntMapReferenceEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:im3.key3 === foo:im3_new.key3;
}

// -----------------------------------------------------------

function testSimpleByteMapValueEqualityUsingSameReference() returns boolean {
    return foo:bytem1 == foo:bytem1;
}

function testSimpleByteMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:bytem1 == foo:bytem1_new;
}

function testSimpleByteMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:bytem1 === foo:bytem1;
}

function testSimpleByteMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:bytem1 === foo:bytem1_new;
}

// -----------------------------------------------------------

function testComplexByteMapValueEqualityUsingSameReference() returns boolean {
    return foo:bytem3 == foo:bytem3;
}

function testComplexByteMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:bytem3 == foo:bytem3_new;
}

function testComplexByteMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:bytem3 === foo:bytem3;
}

function testComplexByteMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:bytem3 === foo:bytem3_new;
}

// -----------------------------------------------------------

function testComplexByteMapValueEqualityUsingMapAccessSameReference() returns boolean {
    return foo:bytem3.key3 == foo:bytem3.key3;
}

function testComplexByteMapValueEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:bytem3.key3 == foo:bytem3_new.key3;
}

function testComplexByteMapReferenceEqualityUsingMapAccessSameReference() returns boolean {
    return foo:bytem1 === foo:bytem3.key3;
}

function testComplexByteMapReferenceEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:bytem3.key3 === foo:bytem3_new.key3;
}

// -----------------------------------------------------------

function testSimpleFloatMapValueEqualityUsingSameReference() returns boolean {
    return foo:fm1 == foo:fm1;
}

function testSimpleFloatMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:fm1 == foo:fm1_new;
}

function testSimpleFloatMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:fm1 === foo:fm1;
}

function testSimpleFloatMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:fm1 === foo:fm1_new;
}

// -----------------------------------------------------------

function testComplexFloatMapValueEqualityUsingSameReference() returns boolean {
    return foo:fm3 == foo:fm3;
}

function testComplexFloatMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:fm3 == foo:fm3_new;
}

function testComplexFloatMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:fm3 === foo:fm3;
}

function testComplexFloatMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:fm3 === foo:fm3_new;
}

// -----------------------------------------------------------

function testComplexFloatMapValueEqualityUsingMapAccessSameReference() returns boolean {
    return foo:fm3.key3 == foo:fm3.key3;
}

function testComplexFloatMapValueEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:fm3.key3 == foo:fm3_new.key3;
}

function testComplexFloatMapReferenceEqualityUsingMapAccessSameReference() returns boolean {
    return foo:fm1 === foo:fm3.key3;
}

function testComplexFloatMapReferenceEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:fm3.key3 === foo:fm3_new.key3;
}

// -----------------------------------------------------------

function testSimpleDecimalMapValueEqualityUsingSameReference() returns boolean {
    return foo:dm1 == foo:dm1;
}

function testSimpleDecimalMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:dm1 == foo:dm1_new;
}

function testSimpleDecimalMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:dm1 === foo:dm1;
}

function testSimpleDecimalMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:dm1 === foo:dm1_new;
}

// -----------------------------------------------------------

function testComplexDecimalMapValueEqualityUsingSameReference() returns boolean {
    return foo:dm3 == foo:dm3;
}

function testComplexDecimalMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:dm3 == foo:dm3_new;
}

function testComplexDecimalMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:dm3 === foo:dm3;
}

function testComplexDecimalMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:dm3 === foo:dm3_new;
}

// -----------------------------------------------------------

function testComplexDecimalMapValueEqualityUsingMapAccessSameReference() returns boolean {
    return foo:dm3.key3 == foo:dm3.key3;
}

function testComplexDecimalMapValueEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:dm3.key3 == foo:dm3_new.key3;
}

function testComplexDecimalMapReferenceEqualityUsingMapAccessSameReference() returns boolean {
    return foo:dm1 === foo:dm3.key3;
}

function testComplexDecimalMapReferenceEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:dm3.key3 === foo:dm3_new.key3;
}

// -----------------------------------------------------------

function testSimpleStringMapValueEqualityUsingSameReference() returns boolean {
    return foo:sm1 == foo:sm1;
}

function testSimpleStringMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:sm1 == foo:sm1_new;
}

function testSimpleStringMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:sm1 === foo:sm1;
}

function testSimpleStringMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:sm1 === foo:sm1_new;
}

// -----------------------------------------------------------

function testComplexStringMapValueEqualityUsingSameReference() returns boolean {
    return foo:sm3 == foo:sm3;
}

function testComplexStringMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:sm3 == foo:sm3_new;
}

function testComplexStringMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:sm3 === foo:sm3;
}

function testComplexStringMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:sm3 === foo:sm3_new;
}

// -----------------------------------------------------------

function testComplexStringMapValueEqualityUsingMapAccessSameReference() returns boolean {
    return foo:sm3.key3 == foo:sm3.key3;
}

function testComplexStringMapValueEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:sm3.key3 == foo:sm3_new.key3;
}

function testComplexStringMapReferenceEqualityUsingMapAccessSameReference() returns boolean {
    return foo:sm1 === foo:sm3.key3;
}

function testComplexStringMapReferenceEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:sm3.key3 === foo:sm3_new.key3;
}

// -----------------------------------------------------------

function testSimpleNilMapValueEqualityUsingSameReference() returns boolean {
    return foo:nm1 == foo:nm1;
}

function testSimpleNilMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:nm1 == foo:nm1_new;
}

function testSimpleNilMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:nm1 === foo:nm1;
}

function testSimpleNilMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:nm1 === foo:nm1_new;
}

// -----------------------------------------------------------

function testComplexNilMapValueEqualityUsingSameReference() returns boolean {
    return foo:nm3 == foo:nm3;
}

function testComplexNilMapValueEqualityUsingDifferentReference() returns boolean {
    return foo:nm3 == foo:nm3_new;
}

function testComplexNilMapReferenceEqualityUsingSameReference() returns boolean {
    return foo:nm3 === foo:nm3;
}

function testComplexNilMapReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:nm3 === foo:nm3_new;
}

// -----------------------------------------------------------

function testComplexNilMapValueEqualityUsingMapAccessSameReference() returns boolean {
    return foo:nm3.key3 == foo:nm3.key3;
}

function testComplexNilMapValueEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:nm3.key3 == foo:nm3_new.key3;
}

function testComplexNilMapReferenceEqualityUsingMapAccessSameReference() returns boolean {
    return foo:nm1 === foo:nm3.key3;
}

function testComplexNilMapReferenceEqualityUsingMapAccessDifferentReference() returns boolean {
    return foo:nm3.key3 === foo:nm3_new.key3;
}
