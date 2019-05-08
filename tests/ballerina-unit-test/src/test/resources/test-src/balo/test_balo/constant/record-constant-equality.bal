import testorg/foo version v1;

// boolean ---------------------------------------------------

function testSimpleBooleanRecordValueEqualityUsingSameReference() returns boolean {
    return foo:br1 == foo:br1; // true
}

function testSimpleBooleanRecordValueEqualityUsingDifferentReference() returns boolean {
    return foo:br1 == foo:br1_new; // true
}

function testSimpleBooleanRecordReferenceEqualityUsingSameReference() returns boolean {
    return foo:br1 === foo:br1; // true
}

function testSimpleBooleanRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:br1 === foo:br1_new; // false
}

// -----------------------------------------------------------

function testComplexBooleanRecordValueEqualityUsingSameReference() returns boolean {
    return foo:br3 == foo:br3; // true
}

function testComplexBooleanRecordValueEqualityUsingDifferentReference() returns boolean {
    return foo:br3 == foo:br3_new; // true
}

function testComplexBooleanRecordReferenceEqualityUsingSameReference() returns boolean {
    return foo:br3 === foo:br3; // true
}

function testComplexBooleanRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:br3 === foo:br3_new; // false
}

// -----------------------------------------------------------

function testComplexBooleanRecordValueEqualityUsingRecordAccessSameReference() returns boolean {
    return foo:br3.key5 == foo:br3.key5; // true
}

function testComplexBooleanRecordValueEqualityUsingRecordAccessDifferentReference() returns boolean {
    return foo:br3.key5 == foo:br3_new.key5; // true
}

function testComplexBooleanRecordReferenceEqualityUsingRecordAccessSameReference() returns boolean {
    return foo:br1 === foo:br3.key5; // true
}

function testComplexBooleanRecordReferenceEqualityUsingRecordAccessDifferentReference() returns boolean {
    return foo:br3.key5 === foo:br3_new.key5; // false
}

// -----------------------------------------------------------

function testSimpleBooleanRecordValueEqualityUsingSameReferenceInRecord() returns boolean {
    return foo:br7.key5 == foo:br7.key5; // true
}

function testSimpleBooleanRecordValueEqualityUsingDifferentReferenceInRecord() returns boolean {
    return foo:br7.key5 == foo:br1; // true
}

function testSimpleBooleanRecordReferenceEqualityUsingSameReferenceInRecord() returns boolean {
    return foo:br7.key5 === foo:br7.key5; // true
}

function testSimpleBooleanRecordReferenceEqualityUsingDifferentReferenceInRecord() returns boolean {
    return foo:br7.key5 === foo:br1; // false
}

// -----------------------------------------------------------

function testComplexBooleanRecordValueEquality() returns boolean {
    return foo:br7 == foo:br8; // true
}

function testComplexBooleanRecordReferenceEquality() returns boolean {
    return foo:br7 === foo:br8; // false
}

function testSimpleBooleanRecordValueEqualityInDifferentRecord() returns boolean {
    return foo:br7.key5 == foo:br8.key5; // true
}

function testSimpleBooleanRecordReferenceEqualityInDifferentRecord() returns boolean {
    return foo:br7.key5 === foo:br8.key5; // false
}

// int -------------------------------------------------------

function testSimpleIntRecordValueEqualityUsingSameReference() returns boolean {
    return foo:ir1 == foo:ir1; // true
}

function testSimpleIntRecordValueEqualityUsingDifferentReference() returns boolean {
    return foo:ir1 == foo:ir1_new; // true
}

function testSimpleIntRecordReferenceEqualityUsingSameReference() returns boolean {
    return foo:ir1 === foo:ir1; // true
}

function testSimpleIntRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:ir1 === foo:ir1_new; // false
}

// -----------------------------------------------------------

function testComplexIntRecordValueEqualityUsingSameReference() returns boolean {
    return foo:ir3 == foo:ir3; // true
}

function testComplexIntRecordValueEqualityUsingDifferentReference() returns boolean {
    return foo:ir3 == foo:ir3_new; // true
}

function testComplexIntRecordReferenceEqualityUsingSameReference() returns boolean {
    return foo:ir3 === foo:ir3; // true
}

function testComplexIntRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:ir3 === foo:ir3_new; // false
}

// -----------------------------------------------------------

function testComplexIntRecordValueEqualityUsingRecordAccessSameReference() returns boolean {
    return foo:ir3.key5 == foo:ir3.key5; // true
}

function testComplexIntRecordValueEqualityUsingRecordAccessDifferentReference() returns boolean {
    return foo:ir3.key5 == foo:ir3_new.key5; // true
}

function testComplexIntRecordReferenceEqualityUsingRecordAccessSameReference() returns boolean {
    return foo:ir1 === foo:ir3.key5; // true
}

function testComplexIntRecordReferenceEqualityUsingRecordAccessDifferentReference() returns boolean {
    return foo:ir3.key5 === foo:ir3_new.key5; // false
}

// -----------------------------------------------------------

function testSimpleIntRecordValueEqualityUsingSameReferenceInRecord() returns boolean {
    return foo:ir7.key5 == foo:ir7.key5; // true
}

function testSimpleIntRecordValueEqualityUsingDifferentReferenceInRecord() returns boolean {
    return foo:ir7.key5 == foo:ir1; // true
}

function testSimpleIntRecordReferenceEqualityUsingSameReferenceInRecord() returns boolean {
    return foo:ir7.key5 === foo:ir7.key5; // true
}

function testSimpleIntRecordReferenceEqualityUsingDifferentReferenceInRecord() returns boolean {
    return foo:ir7.key5 === foo:ir1; // false
}

// -----------------------------------------------------------

function testComplexIntRecordValueEquality() returns boolean {
    return foo:ir7 == foo:ir8; // true
}

function testComplexIntRecordReferenceEquality() returns boolean {
    return foo:ir7 === foo:ir8; // false
}

function testSimpleIntRecordValueEqualityInDifferentRecord() returns boolean {
    return foo:ir7.key5 == foo:ir8.key5; // true
}

function testSimpleIntRecordReferenceEqualityInDifferentRecord() returns boolean {
    return foo:ir7.key5 === foo:ir8.key5; // false
}

// byte ------------------------------------------------------

function testSimpleByteRecordValueEqualityUsingSameReference() returns boolean {
    return foo:byter1 == foo:byter1; // true
}

function testSimpleByteRecordValueEqualityUsingDifferentReference() returns boolean {
    return foo:byter1 == foo:byter1_new; // true
}

function testSimpleByteRecordReferenceEqualityUsingSameReference() returns boolean {
    return foo:byter1 === foo:byter1; // true
}

function testSimpleByteRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:byter1 === foo:byter1_new; // false
}

// -----------------------------------------------------------

function testComplexByteRecordValueEqualityUsingSameReference() returns boolean {
    return foo:byter3 == foo:byter3; // true
}

function testComplexByteRecordValueEqualityUsingDifferentReference() returns boolean {
    return foo:byter3 == foo:byter3_new; // true
}

function testComplexByteRecordReferenceEqualityUsingSameReference() returns boolean {
    return foo:byter3 === foo:byter3; // true
}

function testComplexByteRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:byter3 === foo:byter3_new; // false
}

// -----------------------------------------------------------

function testComplexByteRecordValueEqualityUsingRecordAccessSameReference() returns boolean {
    return foo:byter3.key5 == foo:byter3.key5; // true
}

function testComplexByteRecordValueEqualityUsingRecordAccessDifferentReference() returns boolean {
    return foo:byter3.key5 == foo:byter3_new.key5; // true
}

function testComplexByteRecordReferenceEqualityUsingRecordAccessSameReference() returns boolean {
    return foo:byter1 === foo:byter3.key5; // true
}

function testComplexByteRecordReferenceEqualityUsingRecordAccessDifferentReference() returns boolean {
    return foo:byter3.key5 === foo:byter3_new.key5; // false
}

// -----------------------------------------------------------

function testSimpleByteRecordValueEqualityUsingSameReferenceInRecord() returns boolean {
    return foo:byter7.key5 == foo:byter7.key5; // true
}

function testSimpleByteRecordValueEqualityUsingDifferentReferenceInRecord() returns boolean {
    return foo:byter7.key5 == foo:byter1; // true
}

function testSimpleByteRecordReferenceEqualityUsingSameReferenceInRecord() returns boolean {
    return foo:byter7.key5 === foo:byter7.key5; // true
}

function testSimpleByteRecordReferenceEqualityUsingDifferentReferenceInRecord() returns boolean {
    return foo:byter7.key5 === foo:byter1; // false
}

// -----------------------------------------------------------

function testComplexByteRecordValueEquality() returns boolean {
    return foo:byter7 == foo:byter8; // true
}

function testComplexByteRecordReferenceEquality() returns boolean {
    return foo:byter7 === foo:byter8; // false
}

function testSimpleByteRecordValueEqualityInDifferentRecord() returns boolean {
    return foo:byter7.key5 == foo:byter8.key5; // true
}

function testSimpleByteRecordReferenceEqualityInDifferentRecord() returns boolean {
    return foo:byter7.key5 === foo:byter8.key5; // false
}

// float -----------------------------------------------------

function testSimpleFloatRecordValueEqualityUsingSameReference() returns boolean {
    return foo:fr1 == foo:fr1; // true
}

function testSimpleFloatRecordValueEqualityUsingDifferentReference() returns boolean {
    return foo:fr1 == foo:fr1_new; // true
}

function testSimpleFloatRecordReferenceEqualityUsingSameReference() returns boolean {
    return foo:fr1 === foo:fr1; // true
}

function testSimpleFloatRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:fr1 === foo:fr1_new; // false
}

// -----------------------------------------------------------

function testComplexFloatRecordValueEqualityUsingSameReference() returns boolean {
    return foo:fr3 == foo:fr3; // true
}

function testComplexFloatRecordValueEqualityUsingDifferentReference() returns boolean {
    return foo:fr3 == foo:fr3_new; // true
}

function testComplexFloatRecordReferenceEqualityUsingSameReference() returns boolean {
    return foo:fr3 === foo:fr3; // true
}

function testComplexFloatRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:fr3 === foo:fr3_new; // false
}

// -----------------------------------------------------------

function testComplexFloatRecordValueEqualityUsingRecordAccessSameReference() returns boolean {
    return foo:fr3.key5 == foo:fr3.key5; // true
}

function testComplexFloatRecordValueEqualityUsingRecordAccessDifferentReference() returns boolean {
    return foo:fr3.key5 == foo:fr3_new.key5; // true
}

function testComplexFloatRecordReferenceEqualityUsingRecordAccessSameReference() returns boolean {
    return foo:fr1 === foo:fr3.key5; // true
}

function testComplexFloatRecordReferenceEqualityUsingRecordAccessDifferentReference() returns boolean {
    return foo:fr3.key5 === foo:fr3_new.key5; // false
}

// -----------------------------------------------------------

function testSimpleFloatRecordValueEqualityUsingSameReferenceInRecord() returns boolean {
    return foo:fr7.key5 == foo:fr7.key5; // true
}

function testSimpleFloatRecordValueEqualityUsingDifferentReferenceInRecord() returns boolean {
    return foo:fr7.key5 == foo:fr1; // true
}

function testSimpleFloatRecordReferenceEqualityUsingSameReferenceInRecord() returns boolean {
    return foo:fr7.key5 === foo:fr7.key5; // true
}

function testSimpleFloatRecordReferenceEqualityUsingDifferentReferenceInRecord() returns boolean {
    return foo:fr7.key5 === foo:fr1; // false
}

// -----------------------------------------------------------

function testComplexFloatRecordValueEquality() returns boolean {
    return foo:fr7 == foo:fr8; // true
}

function testComplexFloatRecordReferenceEquality() returns boolean {
    return foo:fr7 === foo:fr8; // false
}

function testSimpleFloatRecordValueEqualityInDifferentRecord() returns boolean {
    return foo:fr7.key5 == foo:fr8.key5; // true
}

function testSimpleFloatRecordReferenceEqualityInDifferentRecord() returns boolean {
    return foo:fr7.key5 === foo:fr8.key5; // false
}

// decimal ---------------------------------------------------

function testSimpleDecimalRecordValueEqualityUsingSameReference() returns boolean {
    return foo:dr1 == foo:dr1; // true
}

function testSimpleDecimalRecordValueEqualityUsingDifferentReference() returns boolean {
    return foo:dr1 == foo:dr1_new; // true
}

function testSimpleDecimalRecordReferenceEqualityUsingSameReference() returns boolean {
    return foo:dr1 === foo:dr1; // true
}

function testSimpleDecimalRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:dr1 === foo:dr1_new; // false
}

// -----------------------------------------------------------

function testComplexDecimalRecordValueEqualityUsingSameReference() returns boolean {
    return foo:dr3 == foo:dr3; // true
}

function testComplexDecimalRecordValueEqualityUsingDifferentReference() returns boolean {
    return foo:dr3 == foo:dr3_new; // true
}

function testComplexDecimalRecordReferenceEqualityUsingSameReference() returns boolean {
    return foo:dr3 === foo:dr3; // true
}

function testComplexDecimalRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:dr3 === foo:dr3_new; // false
}

// -----------------------------------------------------------

function testComplexDecimalRecordValueEqualityUsingRecordAccessSameReference() returns boolean {
    return foo:dr3.key5 == foo:dr3.key5; // true
}

function testComplexDecimalRecordValueEqualityUsingRecordAccessDifferentReference() returns boolean {
    return foo:dr3.key5 == foo:dr3_new.key5; // true
}

function testComplexDecimalRecordReferenceEqualityUsingRecordAccessSameReference() returns boolean {
    return foo:dr1 === foo:dr3.key5; // true
}

function testComplexDecimalRecordReferenceEqualityUsingRecordAccessDifferentReference() returns boolean {
    return foo:dr3.key5 === foo:dr3_new.key5; // false
}

// -----------------------------------------------------------

function testSimpleDecimalRecordValueEqualityUsingSameReferenceInRecord() returns boolean {
    return foo:dr7.key5 == foo:dr7.key5; // true
}

function testSimpleDecimalRecordValueEqualityUsingDifferentReferenceInRecord() returns boolean {
    return foo:dr7.key5 == foo:dr1; // true
}

function testSimpleDecimalRecordReferenceEqualityUsingSameReferenceInRecord() returns boolean {
    return foo:dr7.key5 === foo:dr7.key5; // true
}

function testSimpleDecimalRecordReferenceEqualityUsingDifferentReferenceInRecord() returns boolean {
    return foo:dr7.key5 === foo:dr1; // false
}

// -----------------------------------------------------------

function testComplexDecimalRecordValueEquality() returns boolean {
    return foo:dr7 == foo:dr8; // true
}

function testComplexDecimalRecordReferenceEquality() returns boolean {
    return foo:dr7 === foo:dr8; // false
}

function testSimpleDecimalRecordValueEqualityInDifferentRecord() returns boolean {
    return foo:dr7.key5 == foo:dr8.key5; // true
}

function testSimpleDecimalRecordReferenceEqualityInDifferentRecord() returns boolean {
    return foo:dr7.key5 === foo:dr8.key5; // false
}

// string ----------------------------------------------------

function testSimpleStringRecordValueEqualityUsingSameReference() returns boolean {
    return foo:sr1 == foo:sr1; // true
}

function testSimpleStringRecordValueEqualityUsingDifferentReference() returns boolean {
    return foo:sr1 == foo:sr1_new; // true
}

function testSimpleStringRecordReferenceEqualityUsingSameReference() returns boolean {
    return foo:sr1 === foo:sr1; // true
}

function testSimpleStringRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:sr1 === foo:sr1_new; // false
}

// -----------------------------------------------------------

function testComplexStringRecordValueEqualityUsingSameReference() returns boolean {
    return foo:sr3 == foo:sr3; // true
}

function testComplexStringRecordValueEqualityUsingDifferentReference() returns boolean {
    return foo:sr3 == foo:sr3_new; // true
}

function testComplexStringRecordReferenceEqualityUsingSameReference() returns boolean {
    return foo:sr3 === foo:sr3; // true
}

function testComplexStringRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:sr3 === foo:sr3_new; // false
}

// -----------------------------------------------------------

function testComplexStringRecordValueEqualityUsingRecordAccessSameReference() returns boolean {
    return foo:sr3.key5 == foo:sr3.key5; // true
}

function testComplexStringRecordValueEqualityUsingRecordAccessDifferentReference() returns boolean {
    return foo:sr3.key5 == foo:sr3_new.key5; // true
}

function testComplexStringRecordReferenceEqualityUsingRecordAccessSameReference() returns boolean {
    return foo:sr1 === foo:sr3.key5; // true
}

function testComplexStringRecordReferenceEqualityUsingRecordAccessDifferentReference() returns boolean {
    return foo:sr3.key5 === foo:sr3_new.key5; // false
}

// -----------------------------------------------------------

function testSimpleStringRecordValueEqualityUsingSameReferenceInRecord() returns boolean {
    return foo:sr7.key5 == foo:sr7.key5; // true
}

function testSimpleStringRecordValueEqualityUsingDifferentReferenceInRecord() returns boolean {
    return foo:sr7.key5 == foo:sr1; // true
}

function testSimpleStringRecordReferenceEqualityUsingSameReferenceInRecord() returns boolean {
    return foo:sr7.key5 === foo:sr7.key5; // true
}

function testSimpleStringRecordReferenceEqualityUsingDifferentReferenceInRecord() returns boolean {
    return foo:sr7.key5 === foo:sr1; // false
}

// -----------------------------------------------------------

function testComplexStringRecordValueEquality() returns boolean {
    return foo:sr7 == foo:sr8; // true
}

function testComplexStringRecordReferenceEquality() returns boolean {
    return foo:sr7 === foo:sr8; // false
}

function testSimpleStringRecordValueEqualityInDifferentRecord() returns boolean {
    return foo:sr7.key5 == foo:sr8.key5; // true
}

function testSimpleStringRecordReferenceEqualityInDifferentRecord() returns boolean {
    return foo:sr7.key5 === foo:sr8.key5; // false
}

// nil -------------------------------------------------------

function testSimpleNilRecordValueEqualityUsingSameReference() returns boolean {
    return foo:nr1 == foo:nr1; // true
}

function testSimpleNilRecordValueEqualityUsingDifferentReference() returns boolean {
    return foo:nr1 == foo:nr1_new; // true
}

function testSimpleNilRecordReferenceEqualityUsingSameReference() returns boolean {
    return foo:nr1 === foo:nr1; // true
}

function testSimpleNilRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:nr1 === foo:nr1_new; // false
}

// -----------------------------------------------------------

function testComplexNilRecordValueEqualityUsingSameReference() returns boolean {
    return foo:nr3 == foo:nr3; // true
}

function testComplexNilRecordValueEqualityUsingDifferentReference() returns boolean {
    return foo:nr3 == foo:nr3_new; // true
}

function testComplexNilRecordReferenceEqualityUsingSameReference() returns boolean {
    return foo:nr3 === foo:nr3; // true
}

function testComplexNilRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:nr3 === foo:nr3_new; // false
}

// -----------------------------------------------------------

function testComplexNilRecordValueEqualityUsingRecordAccessSameReference() returns boolean {
    return foo:nr3.key5 == foo:nr3.key5; // true
}

function testComplexNilRecordValueEqualityUsingRecordAccessDifferentReference() returns boolean {
    return foo:nr3.key5 == foo:nr3_new.key5; // true
}

function testComplexNilRecordReferenceEqualityUsingRecordAccessSameReference() returns boolean {
    return foo:nr1 === foo:nr3.key5; // true
}

function testComplexNilRecordReferenceEqualityUsingRecordAccessDifferentReference() returns boolean {
    return foo:nr3.key5 === foo:nr3_new.key5; // false
}

// -----------------------------------------------------------

function testSimpleNilRecordValueEqualityUsingSameReferenceInRecord() returns boolean {
    return foo:nr7.key5 == foo:nr7.key5; // true
}

function testSimpleNilRecordValueEqualityUsingDifferentReferenceInRecord() returns boolean {
    return foo:nr7.key5 == foo:nr1; // true
}

function testSimpleNilRecordReferenceEqualityUsingSameReferenceInRecord() returns boolean {
    return foo:nr7.key5 === foo:nr7.key5; // true
}

function testSimpleNilRecordReferenceEqualityUsingDifferentReferenceInRecord() returns boolean {
    return foo:nr7.key5 === foo:nr1; // false
}

// -----------------------------------------------------------

function testComplexNilRecordValueEquality() returns boolean {
    return foo:nr7 == foo:nr8; // true
}

function testComplexNilRecordReferenceEquality() returns boolean {
    return foo:nr7 === foo:nr8; // false
}

function testSimpleNilRecordValueEqualityInDifferentRecord() returns boolean {
    return foo:nr7.key5 == foo:nr8.key5; // true
}

function testSimpleNilRecordReferenceEqualityInDifferentRecord() returns boolean {
    return foo:nr7.key5 === foo:nr8.key5; // false
}
