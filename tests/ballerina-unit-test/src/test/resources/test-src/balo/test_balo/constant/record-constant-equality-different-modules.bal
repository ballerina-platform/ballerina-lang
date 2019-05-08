import testorg/foo version v1;
import testorg/bar version v1;

// boolean ---------------------------------------------------

function testSimpleBooleanRecordValueEqualityUsingSameReference() returns boolean {
    return foo:br1 == bar:br1; // true
}

function testSimpleBooleanRecordValueEqualityUsingDifferentReference() returns boolean {
    return foo:br1 == bar:br1_new; // true
}

function testSimpleBooleanRecordReferenceEqualityUsingSameReference() returns boolean {
    return foo:br1 === bar:br1; // true
}

function testSimpleBooleanRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:br1 === bar:br1_new; // false
}

// -----------------------------------------------------------

function testComplexBooleanRecordValueEqualityUsingSameReference() returns boolean {
    return foo:br3 == bar:br3; // true
}

function testComplexBooleanRecordValueEqualityUsingDifferentReference() returns boolean {
    return foo:br3 == bar:br3_new; // true
}

function testComplexBooleanRecordReferenceEqualityUsingSameReference() returns boolean {
    return foo:br3 === bar:br3; // false
}

function testComplexBooleanRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:br3 === bar:br3_new; // false
}

// -----------------------------------------------------------

function testComplexBooleanRecordValueEqualityUsingRecordAccessSameReference() returns boolean {
    return foo:br3.key5 == bar:br3.key5; // true
}

function testComplexBooleanRecordValueEqualityUsingRecordAccessDifferentReference() returns boolean {
    return foo:br3.key5 == bar:br3_new.key5; // true
}

function testComplexBooleanRecordReferenceEqualityUsingRecordAccessSameReference() returns boolean {
    return foo:br1 === bar:br3.key5; // false
}

function testComplexBooleanRecordReferenceEqualityUsingRecordAccessDifferentReference() returns boolean {
    return foo:br3.key5 === bar:br3_new.key5; // false
}

// -----------------------------------------------------------

function testSimpleBooleanRecordValueEqualityUsingSameReferenceInRecord() returns boolean {
    return foo:br7.key5 == bar:br7.key5; // true
}

function testSimpleBooleanRecordValueEqualityUsingDifferentReferenceInRecord() returns boolean {
    return foo:br7.key5 == bar:br1; // true
}

function testSimpleBooleanRecordReferenceEqualityUsingSameReferenceInRecord() returns boolean {
    return foo:br7.key5 === bar:br7.key5; // false
}

function testSimpleBooleanRecordReferenceEqualityUsingDifferentReferenceInRecord() returns boolean {
    return foo:br7.key5 === bar:br1; // false
}

// -----------------------------------------------------------

function testComplexBooleanRecordValueEquality() returns boolean {
    return foo:br7 == bar:br8; // true
}

function testComplexBooleanRecordReferenceEquality() returns boolean {
    return foo:br7 === bar:br8; // false
}

function testSimpleBooleanRecordValueEqualityInDifferentRecord() returns boolean {
    return foo:br7.key5 == bar:br8.key5; // true
}

function testSimpleBooleanRecordReferenceEqualityInDifferentRecord() returns boolean {
    return foo:br7.key5 === bar:br8.key5; // false
}

// int -------------------------------------------------------

function testSimpleIntRecordValueEqualityUsingSameReference() returns boolean {
    return foo:ir1 == bar:ir1; // true
}

function testSimpleIntRecordValueEqualityUsingDifferentReference() returns boolean {
    return foo:ir1 == bar:ir1_new; // true
}

function testSimpleIntRecordReferenceEqualityUsingSameReference() returns boolean {
    return foo:ir1 === bar:ir1; // false
}

function testSimpleIntRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:ir1 === bar:ir1_new; // false
}

// -----------------------------------------------------------

function testComplexIntRecordValueEqualityUsingSameReference() returns boolean {
    return foo:ir3 == bar:ir3; // true
}

function testComplexIntRecordValueEqualityUsingDifferentReference() returns boolean {
    return foo:ir3 == bar:ir3_new; // true
}

function testComplexIntRecordReferenceEqualityUsingSameReference() returns boolean {
    return foo:ir3 === bar:ir3; // false
}

function testComplexIntRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:ir3 === bar:ir3_new; // false
}

// -----------------------------------------------------------

function testComplexIntRecordValueEqualityUsingRecordAccessSameReference() returns boolean {
    return foo:ir3.key5 == bar:ir3.key5; // true
}

function testComplexIntRecordValueEqualityUsingRecordAccessDifferentReference() returns boolean {
    return foo:ir3.key5 == bar:ir3_new.key5; // true
}

function testComplexIntRecordReferenceEqualityUsingRecordAccessSameReference() returns boolean {
    return foo:ir1 === bar:ir3.key5; // false
}

function testComplexIntRecordReferenceEqualityUsingRecordAccessDifferentReference() returns boolean {
    return foo:ir3.key5 === bar:ir3_new.key5; // false
}

// -----------------------------------------------------------

function testSimpleIntRecordValueEqualityUsingSameReferenceInRecord() returns boolean {
    return foo:ir7.key5 == bar:ir7.key5; // true
}

function testSimpleIntRecordValueEqualityUsingDifferentReferenceInRecord() returns boolean {
    return foo:ir7.key5 == bar:ir1; // true
}

function testSimpleIntRecordReferenceEqualityUsingSameReferenceInRecord() returns boolean {
    return foo:ir7.key5 === bar:ir7.key5; // false
}

function testSimpleIntRecordReferenceEqualityUsingDifferentReferenceInRecord() returns boolean {
    return foo:ir7.key5 === bar:ir1; // false
}

// -----------------------------------------------------------

function testComplexIntRecordValueEquality() returns boolean {
    return foo:ir7 == bar:ir8; // true
}

function testComplexIntRecordReferenceEquality() returns boolean {
    return foo:ir7 === bar:ir8; // false
}

function testSimpleIntRecordValueEqualityInDifferentRecord() returns boolean {
    return foo:ir7.key5 == bar:ir8.key5; // true
}

function testSimpleIntRecordReferenceEqualityInDifferentRecord() returns boolean {
    return foo:ir7.key5 === bar:ir8.key5; // false
}

// byte ------------------------------------------------------

function testSimpleByteRecordValueEqualityUsingSameReference() returns boolean {
    return foo:byter1 == bar:byter1; // true
}

function testSimpleByteRecordValueEqualityUsingDifferentReference() returns boolean {
    return foo:byter1 == bar:byter1_new; // true
}

function testSimpleByteRecordReferenceEqualityUsingSameReference() returns boolean {
    return foo:byter1 === bar:byter1; // false
}

function testSimpleByteRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:byter1 === bar:byter1_new; // false
}

// -----------------------------------------------------------

function testComplexByteRecordValueEqualityUsingSameReference() returns boolean {
    return foo:byter3 == bar:byter3; // true
}

function testComplexByteRecordValueEqualityUsingDifferentReference() returns boolean {
    return foo:byter3 == bar:byter3_new; // true
}

function testComplexByteRecordReferenceEqualityUsingSameReference() returns boolean {
    return foo:byter3 === bar:byter3; // false
}

function testComplexByteRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:byter3 === bar:byter3_new; // false
}

// -----------------------------------------------------------

function testComplexByteRecordValueEqualityUsingRecordAccessSameReference() returns boolean {
    return foo:byter3.key5 == bar:byter3.key5; // true
}

function testComplexByteRecordValueEqualityUsingRecordAccessDifferentReference() returns boolean {
    return foo:byter3.key5 == bar:byter3_new.key5; // true
}

function testComplexByteRecordReferenceEqualityUsingRecordAccessSameReference() returns boolean {
    return foo:byter1 === bar:byter3.key5; // false
}

function testComplexByteRecordReferenceEqualityUsingRecordAccessDifferentReference() returns boolean {
    return foo:byter3.key5 === bar:byter3_new.key5; // false
}

// -----------------------------------------------------------

function testSimpleByteRecordValueEqualityUsingSameReferenceInRecord() returns boolean {
    return foo:byter7.key5 == bar:byter7.key5; // true
}

function testSimpleByteRecordValueEqualityUsingDifferentReferenceInRecord() returns boolean {
    return foo:byter7.key5 == bar:byter1; // true
}

function testSimpleByteRecordReferenceEqualityUsingSameReferenceInRecord() returns boolean {
    return foo:byter7.key5 === bar:byter7.key5; // false
}

function testSimpleByteRecordReferenceEqualityUsingDifferentReferenceInRecord() returns boolean {
    return foo:byter7.key5 === bar:byter1; // false
}

// -----------------------------------------------------------

function testComplexByteRecordValueEquality() returns boolean {
    return foo:byter7 == bar:byter8; // true
}

function testComplexByteRecordReferenceEquality() returns boolean {
    return foo:byter7 === bar:byter8; // false
}

function testSimpleByteRecordValueEqualityInDifferentRecord() returns boolean {
    return foo:byter7.key5 == bar:byter8.key5; // true
}

function testSimpleByteRecordReferenceEqualityInDifferentRecord() returns boolean {
    return foo:byter7.key5 === bar:byter8.key5; // false
}

// float -----------------------------------------------------

function testSimpleFloatRecordValueEqualityUsingSameReference() returns boolean {
    return foo:fr1 == bar:fr1; // true
}

function testSimpleFloatRecordValueEqualityUsingDifferentReference() returns boolean {
    return foo:fr1 == bar:fr1_new; // true
}

function testSimpleFloatRecordReferenceEqualityUsingSameReference() returns boolean {
    return foo:fr1 === bar:fr1; // false
}

function testSimpleFloatRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:fr1 === bar:fr1_new; // false
}

// -----------------------------------------------------------

function testComplexFloatRecordValueEqualityUsingSameReference() returns boolean {
    return foo:fr3 == bar:fr3; // true
}

function testComplexFloatRecordValueEqualityUsingDifferentReference() returns boolean {
    return foo:fr3 == bar:fr3_new; // true
}

function testComplexFloatRecordReferenceEqualityUsingSameReference() returns boolean {
    return foo:fr3 === bar:fr3; // false
}

function testComplexFloatRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:fr3 === bar:fr3_new; // false
}

// -----------------------------------------------------------

function testComplexFloatRecordValueEqualityUsingRecordAccessSameReference() returns boolean {
    return foo:fr3.key5 == bar:fr3.key5; // true
}

function testComplexFloatRecordValueEqualityUsingRecordAccessDifferentReference() returns boolean {
    return foo:fr3.key5 == bar:fr3_new.key5; // true
}

function testComplexFloatRecordReferenceEqualityUsingRecordAccessSameReference() returns boolean {
    return foo:fr1 === bar:fr3.key5; // false
}

function testComplexFloatRecordReferenceEqualityUsingRecordAccessDifferentReference() returns boolean {
    return foo:fr3.key5 === bar:fr3_new.key5; // false
}

// -----------------------------------------------------------

function testSimpleFloatRecordValueEqualityUsingSameReferenceInRecord() returns boolean {
    return foo:fr7.key5 == bar:fr7.key5; // true
}

function testSimpleFloatRecordValueEqualityUsingDifferentReferenceInRecord() returns boolean {
    return foo:fr7.key5 == bar:fr1; // true
}

function testSimpleFloatRecordReferenceEqualityUsingSameReferenceInRecord() returns boolean {
    return foo:fr7.key5 === bar:fr7.key5; // false
}

function testSimpleFloatRecordReferenceEqualityUsingDifferentReferenceInRecord() returns boolean {
    return foo:fr7.key5 === bar:fr1; // false
}

// -----------------------------------------------------------

function testComplexFloatRecordValueEquality() returns boolean {
    return foo:fr7 == bar:fr8; // true
}

function testComplexFloatRecordReferenceEquality() returns boolean {
    return foo:fr7 === bar:fr8; // false
}

function testSimpleFloatRecordValueEqualityInDifferentRecord() returns boolean {
    return foo:fr7.key5 == bar:fr8.key5; // true
}

function testSimpleFloatRecordReferenceEqualityInDifferentRecord() returns boolean {
    return foo:fr7.key5 === bar:fr8.key5; // false
}

// decimal ---------------------------------------------------

function testSimpleDecimalRecordValueEqualityUsingSameReference() returns boolean {
    return foo:dr1 == bar:dr1; // true
}

function testSimpleDecimalRecordValueEqualityUsingDifferentReference() returns boolean {
    return foo:dr1 == bar:dr1_new; // true
}

function testSimpleDecimalRecordReferenceEqualityUsingSameReference() returns boolean {
    return foo:dr1 === bar:dr1; // false
}

function testSimpleDecimalRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:dr1 === bar:dr1_new; // false
}

// -----------------------------------------------------------

function testComplexDecimalRecordValueEqualityUsingSameReference() returns boolean {
    return foo:dr3 == bar:dr3; // true
}

function testComplexDecimalRecordValueEqualityUsingDifferentReference() returns boolean {
    return foo:dr3 == bar:dr3_new; // true
}

function testComplexDecimalRecordReferenceEqualityUsingSameReference() returns boolean {
    return foo:dr3 === bar:dr3; // false
}

function testComplexDecimalRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:dr3 === bar:dr3_new; // false
}

// -----------------------------------------------------------

function testComplexDecimalRecordValueEqualityUsingRecordAccessSameReference() returns boolean {
    return foo:dr3.key5 == bar:dr3.key5; // true
}

function testComplexDecimalRecordValueEqualityUsingRecordAccessDifferentReference() returns boolean {
    return foo:dr3.key5 == bar:dr3_new.key5; // true
}

function testComplexDecimalRecordReferenceEqualityUsingRecordAccessSameReference() returns boolean {
    return foo:dr1 === bar:dr3.key5; // false
}

function testComplexDecimalRecordReferenceEqualityUsingRecordAccessDifferentReference() returns boolean {
    return foo:dr3.key5 === bar:dr3_new.key5; // false
}

// -----------------------------------------------------------

function testSimpleDecimalRecordValueEqualityUsingSameReferenceInRecord() returns boolean {
    return foo:dr7.key5 == bar:dr7.key5; // true
}

function testSimpleDecimalRecordValueEqualityUsingDifferentReferenceInRecord() returns boolean {
    return foo:dr7.key5 == bar:dr1; // true
}

function testSimpleDecimalRecordReferenceEqualityUsingSameReferenceInRecord() returns boolean {
    return foo:dr7.key5 === bar:dr7.key5; // false
}

function testSimpleDecimalRecordReferenceEqualityUsingDifferentReferenceInRecord() returns boolean {
    return foo:dr7.key5 === bar:dr1; // false
}

// -----------------------------------------------------------

function testComplexDecimalRecordValueEquality() returns boolean {
    return foo:dr7 == bar:dr8; // true
}

function testComplexDecimalRecordReferenceEquality() returns boolean {
    return foo:dr7 === bar:dr8; // false
}

function testSimpleDecimalRecordValueEqualityInDifferentRecord() returns boolean {
    return foo:dr7.key5 == bar:dr8.key5; // true
}

function testSimpleDecimalRecordReferenceEqualityInDifferentRecord() returns boolean {
    return foo:dr7.key5 === bar:dr8.key5; // false
}

// string ----------------------------------------------------

function testSimpleStringRecordValueEqualityUsingSameReference() returns boolean {
    return foo:sr1 == bar:sr1; // true
}

function testSimpleStringRecordValueEqualityUsingDifferentReference() returns boolean {
    return foo:sr1 == bar:sr1_new; // true
}

function testSimpleStringRecordReferenceEqualityUsingSameReference() returns boolean {
    return foo:sr1 === bar:sr1; // false
}

function testSimpleStringRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:sr1 === bar:sr1_new; // false
}

// -----------------------------------------------------------

function testComplexStringRecordValueEqualityUsingSameReference() returns boolean {
    return foo:sr3 == bar:sr3; // true
}

function testComplexStringRecordValueEqualityUsingDifferentReference() returns boolean {
    return foo:sr3 == bar:sr3_new; // true
}

function testComplexStringRecordReferenceEqualityUsingSameReference() returns boolean {
    return foo:sr3 === bar:sr3; // false
}

function testComplexStringRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:sr3 === bar:sr3_new; // false
}

// -----------------------------------------------------------

function testComplexStringRecordValueEqualityUsingRecordAccessSameReference() returns boolean {
    return foo:sr3.key5 == bar:sr3.key5; // true
}

function testComplexStringRecordValueEqualityUsingRecordAccessDifferentReference() returns boolean {
    return foo:sr3.key5 == bar:sr3_new.key5; // true
}

function testComplexStringRecordReferenceEqualityUsingRecordAccessSameReference() returns boolean {
    return foo:sr1 === bar:sr3.key5; // false
}

function testComplexStringRecordReferenceEqualityUsingRecordAccessDifferentReference() returns boolean {
    return foo:sr3.key5 === bar:sr3_new.key5; // false
}

// -----------------------------------------------------------

function testSimpleStringRecordValueEqualityUsingSameReferenceInRecord() returns boolean {
    return foo:sr7.key5 == bar:sr7.key5; // true
}

function testSimpleStringRecordValueEqualityUsingDifferentReferenceInRecord() returns boolean {
    return foo:sr7.key5 == bar:sr1; // true
}

function testSimpleStringRecordReferenceEqualityUsingSameReferenceInRecord() returns boolean {
    return foo:sr7.key5 === bar:sr7.key5; // false
}

function testSimpleStringRecordReferenceEqualityUsingDifferentReferenceInRecord() returns boolean {
    return foo:sr7.key5 === bar:sr1; // false
}

// -----------------------------------------------------------

function testComplexStringRecordValueEquality() returns boolean {
    return foo:sr7 == bar:sr8; // true
}

function testComplexStringRecordReferenceEquality() returns boolean {
    return foo:sr7 === bar:sr8; // false
}

function testSimpleStringRecordValueEqualityInDifferentRecord() returns boolean {
    return foo:sr7.key5 == bar:sr8.key5; // true
}

function testSimpleStringRecordReferenceEqualityInDifferentRecord() returns boolean {
    return foo:sr7.key5 === bar:sr8.key5; // false
}

// nil -------------------------------------------------------

function testSimpleNilRecordValueEqualityUsingSameReference() returns boolean {
    return foo:nr1 == bar:nr1; // true
}

function testSimpleNilRecordValueEqualityUsingDifferentReference() returns boolean {
    return foo:nr1 == bar:nr1_new; // true
}

function testSimpleNilRecordReferenceEqualityUsingSameReference() returns boolean {
    return foo:nr1 === bar:nr1; // false
}

function testSimpleNilRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:nr1 === bar:nr1_new; // false
}

// -----------------------------------------------------------

function testComplexNilRecordValueEqualityUsingSameReference() returns boolean {
    return foo:nr3 == bar:nr3; // true
}

function testComplexNilRecordValueEqualityUsingDifferentReference() returns boolean {
    return foo:nr3 == bar:nr3_new; // true
}

function testComplexNilRecordReferenceEqualityUsingSameReference() returns boolean {
    return foo:nr3 === bar:nr3; // false
}

function testComplexNilRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return foo:nr3 === bar:nr3_new; // false
}

// -----------------------------------------------------------

function testComplexNilRecordValueEqualityUsingRecordAccessSameReference() returns boolean {
    return foo:nr3.key5 == bar:nr3.key5; // true
}

function testComplexNilRecordValueEqualityUsingRecordAccessDifferentReference() returns boolean {
    return foo:nr3.key5 == bar:nr3_new.key5; // true
}

function testComplexNilRecordReferenceEqualityUsingRecordAccessSameReference() returns boolean {
    return foo:nr1 === bar:nr3.key5; // false
}

function testComplexNilRecordReferenceEqualityUsingRecordAccessDifferentReference() returns boolean {
    return foo:nr3.key5 === bar:nr3_new.key5; // false
}

// -----------------------------------------------------------

function testSimpleNilRecordValueEqualityUsingSameReferenceInRecord() returns boolean {
    return foo:nr7.key5 == bar:nr7.key5; // true
}

function testSimpleNilRecordValueEqualityUsingDifferentReferenceInRecord() returns boolean {
    return foo:nr7.key5 == bar:nr1; // true
}

function testSimpleNilRecordReferenceEqualityUsingSameReferenceInRecord() returns boolean {
    return foo:nr7.key5 === bar:nr7.key5; // false
}

function testSimpleNilRecordReferenceEqualityUsingDifferentReferenceInRecord() returns boolean {
    return foo:nr7.key5 === bar:nr1; // false
}

// -----------------------------------------------------------

function testComplexNilRecordValueEquality() returns boolean {
    return foo:nr7 == bar:nr8; // true
}

function testComplexNilRecordReferenceEquality() returns boolean {
    return foo:nr7 === bar:nr8; // false
}

function testSimpleNilRecordValueEqualityInDifferentRecord() returns boolean {
    return foo:nr7.key5 == bar:nr8.key5; // true
}

function testSimpleNilRecordReferenceEqualityInDifferentRecord() returns boolean {
    return foo:nr7.key5 === bar:nr8.key5; // false
}
