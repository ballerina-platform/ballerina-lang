
// boolean ---------------------------------------------------

const record {| record {| boolean...; |}...; |} br3 = { key3: br1, key4: br2 };
const record {| boolean...; |} br1 = { key1: true };
const record {| boolean...; |} br2 = { key2: false };

const record {| record {| boolean...; |}...; |} br3_new = { key3: br1_new, key4: br2_new };
const record {| boolean...; |} br1_new = { key1: true };
const record {| boolean...; |} br2_new = { key2: false };

const record {| record {| boolean...; |}...; |} br7 = { key3: { key1: true }, key2: { key4: false } };
const record {| record {| boolean...; |}...; |} br8 = { key3: { key1: true }, key2: { key4: false } };

function testSimpleBooleanRecordValueEqualityUsingSameReference() returns boolean {
    return br1 == br1; // true
}

function testSimpleBooleanRecordValueEqualityUsingDifferentReference() returns boolean {
    return br1 == br1_new; // true
}

function testSimpleBooleanRecordReferenceEqualityUsingSameReference() returns boolean {
    return br1 === br1; // true
}

function testSimpleBooleanRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return br1 === br1_new; // false
}

// -----------------------------------------------------------

function testComplexBooleanRecordValueEqualityUsingSameReference() returns boolean {
    return br3 == br3; // true
}

function testComplexBooleanRecordValueEqualityUsingDifferentReference() returns boolean {
    return br3 == br3_new; // true
}

function testComplexBooleanRecordReferenceEqualityUsingSameReference() returns boolean {
    return br3 === br3; // true
}

function testComplexBooleanRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return br3 === br3_new; // false
}

// -----------------------------------------------------------

function testComplexBooleanRecordValueEqualityUsingRecordAccessSameReference() returns boolean {
    return br3.key3 == br3.key3; // true
}

function testComplexBooleanRecordValueEqualityUsingRecordAccessDifferentReference() returns boolean {
    return br3.key3 == br3_new.key3; // true
}

function testComplexBooleanRecordReferenceEqualityUsingRecordAccessSameReference() returns boolean {
    return br1 === br3.key3; // true
}

function testComplexBooleanRecordReferenceEqualityUsingRecordAccessDifferentReference() returns boolean {
    return br3.key3 === br3_new.key3; // false
}

// -----------------------------------------------------------

function testSimpleBooleanRecordValueEqualityUsingSameReferenceInRecord() returns boolean {
    return br7.key3 == br7.key3; // true
}

function testSimpleBooleanRecordValueEqualityUsingDifferentReferenceInRecord() returns boolean {
    return br7.key3 == br1; // true
}

function testSimpleBooleanRecordReferenceEqualityUsingSameReferenceInRecord() returns boolean {
    return br7.key3 === br7.key3; // true
}

function testSimpleBooleanRecordReferenceEqualityUsingDifferentReferenceInRecord() returns boolean {
    return br7.key3 === br1; // false
}

// -----------------------------------------------------------

function testComplexBooleanRecordValueEquality() returns boolean {
    return br7 == br8; // true
}

function testComplexBooleanRecordReferenceEquality() returns boolean {
    return br7 === br8; // false
}

function testSimpleBooleanRecordValueEqualityInDifferentRecord() returns boolean {
    return br7.key3 == br8.key3; // true
}

function testSimpleBooleanRecordReferenceEqualityInDifferentRecord() returns boolean {
    return br7.key3 === br8.key3; // false
}

// int -------------------------------------------------------

const record {| record {| int...; |}...; |} ir3 = { key3: ir1, key4: ir2 };
const record {| int...; |} ir1 = { key1: 1 };
const record {| int...; |} ir2 = { key2: 2 };

const record {| record {| int...; |}...; |} ir3_new = { key3: ir1_new, key4: ir2_new };
const record {| int...; |} ir1_new = { key1: 1 };
const record {| int...; |} ir2_new = { key2: 2 };

const record {| record {| int...; |}...; |} ir7 = { key3: { key1: 1 }, key4: { key2: 2 } };
const record {| record {| int...; |}...; |} ir8 = { key3: { key1: 1 }, key4: { key2: 2 } };

function testSimpleIntRecordValueEqualityUsingSameReference() returns boolean {
    return ir1 == ir1; // true
}

function testSimpleIntRecordValueEqualityUsingDifferentReference() returns boolean {
    return ir1 == ir1_new; // true
}

function testSimpleIntRecordReferenceEqualityUsingSameReference() returns boolean {
    return ir1 === ir1; // true
}

function testSimpleIntRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return ir1 === ir1_new; // false
}

// -----------------------------------------------------------

function testComplexIntRecordValueEqualityUsingSameReference() returns boolean {
    return ir3 == ir3; // true
}

function testComplexIntRecordValueEqualityUsingDifferentReference() returns boolean {
    return ir3 == ir3_new; // true
}

function testComplexIntRecordReferenceEqualityUsingSameReference() returns boolean {
    return ir3 === ir3; // true
}

function testComplexIntRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return ir3 === ir3_new; // false
}

// -----------------------------------------------------------

function testComplexIntRecordValueEqualityUsingRecordAccessSameReference() returns boolean {
    return ir3.key3 == ir3.key3; // true
}

function testComplexIntRecordValueEqualityUsingRecordAccessDifferentReference() returns boolean {
    return ir3.key3 == ir3_new.key3; // true
}

function testComplexIntRecordReferenceEqualityUsingRecordAccessSameReference() returns boolean {
    return ir1 === ir3.key3; // true
}

function testComplexIntRecordReferenceEqualityUsingRecordAccessDifferentReference() returns boolean {
    return ir3.key3 === ir3_new.key3; // false
}

// -----------------------------------------------------------

function testSimpleIntRecordValueEqualityUsingSameReferenceInRecord() returns boolean {
    return ir7.key3 == ir7.key3; // true
}

function testSimpleIntRecordValueEqualityUsingDifferentReferenceInRecord() returns boolean {
    return ir7.key3 == ir1; // true
}

function testSimpleIntRecordReferenceEqualityUsingSameReferenceInRecord() returns boolean {
    return ir7.key3 === ir7.key3; // true
}

function testSimpleIntRecordReferenceEqualityUsingDifferentReferenceInRecord() returns boolean {
    return ir7.key3 === ir1; // false
}

// -----------------------------------------------------------

function testComplexIntRecordValueEquality() returns boolean {
    return ir7 == ir8; // true
}

function testComplexIntRecordReferenceEquality() returns boolean {
    return ir7 === ir8; // false
}

function testSimpleIntRecordValueEqualityInDifferentRecord() returns boolean {
    return ir7.key3 == ir8.key3; // true
}

function testSimpleIntRecordReferenceEqualityInDifferentRecord() returns boolean {
    return ir7.key3 === ir8.key3; // false
}

// byte ------------------------------------------------------

const record {| record {| byte...; |}...; |} byter3 = { key3: byter1, key4: byter2 };
const record {| byte...; |} byter1 = { key1: 10 };
const record {| byte...; |} byter2 = { key2: 20 };

const record {| record {| byte...; |}...; |} byter3_new = { key3: byter1_new, key4: byter2_new };
const record {| byte...; |} byter1_new = { key1: 10 };
const record {| byte...; |} byter2_new = { key2: 20 };

const record {| record {| byte...; |}...; |} byter7 = { key3: { key1: 10 }, key4: { key2: 20 } };
const record {| record {| byte...; |}...; |} byter8 = { key3: { key1: 10 }, key4: { key2: 20 } };

function testSimpleByteRecordValueEqualityUsingSameReference() returns boolean {
    return byter1 == byter1; // true
}

function testSimpleByteRecordValueEqualityUsingDifferentReference() returns boolean {
    return byter1 == byter1_new; // true
}

function testSimpleByteRecordReferenceEqualityUsingSameReference() returns boolean {
    return byter1 === byter1; // true
}

function testSimpleByteRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return byter1 === byter1_new; // false
}

// -----------------------------------------------------------

function testComplexByteRecordValueEqualityUsingSameReference() returns boolean {
    return byter3 == byter3; // true
}

function testComplexByteRecordValueEqualityUsingDifferentReference() returns boolean {
    return byter3 == byter3_new; // true
}

function testComplexByteRecordReferenceEqualityUsingSameReference() returns boolean {
    return byter3 === byter3; // true
}

function testComplexByteRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return byter3 === byter3_new; // false
}

// -----------------------------------------------------------

function testComplexByteRecordValueEqualityUsingRecordAccessSameReference() returns boolean {
    return byter3.key3 == byter3.key3; // true
}

function testComplexByteRecordValueEqualityUsingRecordAccessDifferentReference() returns boolean {
    return byter3.key3 == byter3_new.key3; // true
}

function testComplexByteRecordReferenceEqualityUsingRecordAccessSameReference() returns boolean {
    return byter1 === byter3.key3; // true
}

function testComplexByteRecordReferenceEqualityUsingRecordAccessDifferentReference() returns boolean {
    return byter3.key3 === byter3_new.key3; // false
}

// -----------------------------------------------------------

function testSimpleByteRecordValueEqualityUsingSameReferenceInRecord() returns boolean {
    return byter7.key3 == byter7.key3; // true
}

function testSimpleByteRecordValueEqualityUsingDifferentReferenceInRecord() returns boolean {
    return byter7.key3 == byter1; // true
}

function testSimpleByteRecordReferenceEqualityUsingSameReferenceInRecord() returns boolean {
    return byter7.key3 === byter7.key3; // true
}

function testSimpleByteRecordReferenceEqualityUsingDifferentReferenceInRecord() returns boolean {
    return byter7.key3 === byter1; // false
}

// -----------------------------------------------------------

function testComplexByteRecordValueEquality() returns boolean {
    return byter7 == byter8; // true
}

function testComplexByteRecordReferenceEquality() returns boolean {
    return byter7 === byter8; // false
}

function testSimpleByteRecordValueEqualityInDifferentRecord() returns boolean {
    return byter7.key3 == byter8.key3; // true
}

function testSimpleByteRecordReferenceEqualityInDifferentRecord() returns boolean {
    return byter7.key3 === byter8.key3; // false
}

// float -----------------------------------------------------

const record {| record {| float...; |}...; |} fr3 = { key3: fr1, key4: fr2 };
const record {| float...; |} fr1 = { key1: 2.0 };
const record {| float...; |} fr2 = { key2: 4.0 };

const record {| record {| float...; |}...; |} fr3_new = { key3: fr1_new, key4: fr2_new };
const record {| float...; |} fr1_new = { key1: 2.0 };
const record {| float...; |} fr2_new = { key2: 4.0 };

const record {| record {| float...; |}...; |} fr7 = { key3: { key1: 2.0 }, key4: { key2: 4.0 } };
const record {| record {| float...; |}...; |} fr8 = { key3: { key1: 2.0 }, key4: { key2: 4.0 } };

function testSimpleFloatRecordValueEqualityUsingSameReference() returns boolean {
    return fr1 == fr1; // true
}

function testSimpleFloatRecordValueEqualityUsingDifferentReference() returns boolean {
    return fr1 == fr1_new; // true
}

function testSimpleFloatRecordReferenceEqualityUsingSameReference() returns boolean {
    return fr1 === fr1; // true
}

function testSimpleFloatRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return fr1 === fr1_new; // false
}

// -----------------------------------------------------------

function testComplexFloatRecordValueEqualityUsingSameReference() returns boolean {
    return fr3 == fr3; // true
}

function testComplexFloatRecordValueEqualityUsingDifferentReference() returns boolean {
    return fr3 == fr3_new; // true
}

function testComplexFloatRecordReferenceEqualityUsingSameReference() returns boolean {
    return fr3 === fr3; // true
}

function testComplexFloatRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return fr3 === fr3_new; // false
}

// -----------------------------------------------------------

function testComplexFloatRecordValueEqualityUsingRecordAccessSameReference() returns boolean {
    return fr3.key3 == fr3.key3; // true
}

function testComplexFloatRecordValueEqualityUsingRecordAccessDifferentReference() returns boolean {
    return fr3.key3 == fr3_new.key3; // true
}

function testComplexFloatRecordReferenceEqualityUsingRecordAccessSameReference() returns boolean {
    return fr1 === fr3.key3; // true
}

function testComplexFloatRecordReferenceEqualityUsingRecordAccessDifferentReference() returns boolean {
    return fr3.key3 === fr3_new.key3; // false
}

// -----------------------------------------------------------

function testSimpleFloatRecordValueEqualityUsingSameReferenceInRecord() returns boolean {
    return fr7.key3 == fr7.key3; // true
}

function testSimpleFloatRecordValueEqualityUsingDifferentReferenceInRecord() returns boolean {
    return fr7.key3 == fr1; // true
}

function testSimpleFloatRecordReferenceEqualityUsingSameReferenceInRecord() returns boolean {
    return fr7.key3 === fr7.key3; // true
}

function testSimpleFloatRecordReferenceEqualityUsingDifferentReferenceInRecord() returns boolean {
    return fr7.key3 === fr1; // false
}

// -----------------------------------------------------------

function testComplexFloatRecordValueEquality() returns boolean {
    return fr7 == fr8; // true
}

function testComplexFloatRecordReferenceEquality() returns boolean {
    return fr7 === fr8; // false
}

function testSimpleFloatRecordValueEqualityInDifferentRecord() returns boolean {
    return fr7.key3 == fr8.key3; // true
}

function testSimpleFloatRecordReferenceEqualityInDifferentRecord() returns boolean {
    return fr7.key3 === fr8.key3; // false
}

// decimal ---------------------------------------------------

const record {| record {| decimal...; |}...; |} dr3 = { key3: dr1, key4: dr2 };
const record {| decimal...; |} dr1 = { key1: 100 };
const record {| decimal...; |} dr2 = { key2: 200 };

const record {| record {| decimal...; |}...; |} dr3_new = { key3: dr1_new, key4: dr2_new };
const record {| decimal...; |} dr1_new = { key1: 100 };
const record {| decimal...; |} dr2_new = { key2: 200 };

const record {| record {| decimal...; |}...; |} dr7 = { key3: { key1: 100 }, key4: { key2: 200 } };
const record {| record {| decimal...; |}...; |} dr8 = { key3: { key1: 100 }, key4: { key2: 200 } };

function testSimpleDecimalRecordValueEqualityUsingSameReference() returns boolean {
    return dr1 == dr1; // true
}

function testSimpleDecimalRecordValueEqualityUsingDifferentReference() returns boolean {
    return dr1 == dr1_new; // true
}

function testSimpleDecimalRecordReferenceEqualityUsingSameReference() returns boolean {
    return dr1 === dr1; // true
}

function testSimpleDecimalRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return dr1 === dr1_new; // false
}

// -----------------------------------------------------------

function testComplexDecimalRecordValueEqualityUsingSameReference() returns boolean {
    return dr3 == dr3; // true
}

function testComplexDecimalRecordValueEqualityUsingDifferentReference() returns boolean {
    return dr3 == dr3_new; // true
}

function testComplexDecimalRecordReferenceEqualityUsingSameReference() returns boolean {
    return dr3 === dr3; // true
}

function testComplexDecimalRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return dr3 === dr3_new; // false
}

// -----------------------------------------------------------

function testComplexDecimalRecordValueEqualityUsingRecordAccessSameReference() returns boolean {
    return dr3.key3 == dr3.key3; // true
}

function testComplexDecimalRecordValueEqualityUsingRecordAccessDifferentReference() returns boolean {
    return dr3.key3 == dr3_new.key3; // true
}

function testComplexDecimalRecordReferenceEqualityUsingRecordAccessSameReference() returns boolean {
    return dr1 === dr3.key3; // true
}

function testComplexDecimalRecordReferenceEqualityUsingRecordAccessDifferentReference() returns boolean {
    return dr3.key3 === dr3_new.key3; // false
}

// -----------------------------------------------------------

function testSimpleDecimalRecordValueEqualityUsingSameReferenceInRecord() returns boolean {
    return dr7.key3 == dr7.key3; // true
}

function testSimpleDecimalRecordValueEqualityUsingDifferentReferenceInRecord() returns boolean {
    return dr7.key3 == dr1; // true
}

function testSimpleDecimalRecordReferenceEqualityUsingSameReferenceInRecord() returns boolean {
    return dr7.key3 === dr7.key3; // true
}

function testSimpleDecimalRecordReferenceEqualityUsingDifferentReferenceInRecord() returns boolean {
    return dr7.key3 === dr1; // false
}

// -----------------------------------------------------------

function testComplexDecimalRecordValueEquality() returns boolean {
    return dr7 == dr8; // true
}

function testComplexDecimalRecordReferenceEquality() returns boolean {
    return dr7 === dr8; // false
}

function testSimpleDecimalRecordValueEqualityInDifferentRecord() returns boolean {
    return dr7.key3 == dr8.key3; // true
}

function testSimpleDecimalRecordReferenceEqualityInDifferentRecord() returns boolean {
    return dr7.key3 === dr8.key3; // false
}

// string ----------------------------------------------------

const record {| record {| string...; |}...; |} sr3 = { key3: sr1, key4: sr2 };
const record {| string...; |} sr1 = { key1: "value1" };
const record {| string...; |} sr2 = { key2: "value2" };

const record {| record {| string...; |}...; |} sr3_new = { key3: sr1_new, key4: sr2_new };
const record {| string...; |} sr1_new = { key1: "value1" };
const record {| string...; |} sr2_new = { key2: "value2" };

const record {| record {| string...; |}...; |} sr7 = { key3: { key1: "value1" }, key4: { key2: "value2" } };
const record {| record {| string...; |}...; |} sr8 = { key3: { key1: "value1" }, key4: { key2: "value2" } };

function testSimpleStringRecordValueEqualityUsingSameReference() returns boolean {
    return sr1 == sr1; // true
}

function testSimpleStringRecordValueEqualityUsingDifferentReference() returns boolean {
    return sr1 == sr1_new; // true
}

function testSimpleStringRecordReferenceEqualityUsingSameReference() returns boolean {
    return sr1 === sr1; // true
}

function testSimpleStringRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return sr1 === sr1_new; // false
}

// -----------------------------------------------------------

function testComplexStringRecordValueEqualityUsingSameReference() returns boolean {
    return sr3 == sr3; // true
}

function testComplexStringRecordValueEqualityUsingDifferentReference() returns boolean {
    return sr3 == sr3_new; // true
}

function testComplexStringRecordReferenceEqualityUsingSameReference() returns boolean {
    return sr3 === sr3; // true
}

function testComplexStringRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return sr3 === sr3_new; // false
}

// -----------------------------------------------------------

function testComplexStringRecordValueEqualityUsingRecordAccessSameReference() returns boolean {
    return sr3.key3 == sr3.key3; // true
}

function testComplexStringRecordValueEqualityUsingRecordAccessDifferentReference() returns boolean {
    return sr3.key3 == sr3_new.key3; // true
}

function testComplexStringRecordReferenceEqualityUsingRecordAccessSameReference() returns boolean {
    return sr1 === sr3.key3; // true
}

function testComplexStringRecordReferenceEqualityUsingRecordAccessDifferentReference() returns boolean {
    return sr3.key3 === sr3_new.key3; // false
}

// -----------------------------------------------------------

function testSimpleStringRecordValueEqualityUsingSameReferenceInRecord() returns boolean {
    return sr7.key3 == sr7.key3; // true
}

function testSimpleStringRecordValueEqualityUsingDifferentReferenceInRecord() returns boolean {
    return sr7.key3 == sr1; // true
}

function testSimpleStringRecordReferenceEqualityUsingSameReferenceInRecord() returns boolean {
    return sr7.key3 === sr7.key3; // true
}

function testSimpleStringRecordReferenceEqualityUsingDifferentReferenceInRecord() returns boolean {
    return sr7.key3 === sr1; // false
}

// -----------------------------------------------------------

function testComplexStringRecordValueEquality() returns boolean {
    return sr7 == sr8; // true
}

function testComplexStringRecordReferenceEquality() returns boolean {
    return sr7 === sr8; // false
}

function testSimpleStringRecordValueEqualityInDifferentRecord() returns boolean {
    return sr7.key3 == sr8.key3; // true
}

function testSimpleStringRecordReferenceEqualityInDifferentRecord() returns boolean {
    return sr7.key3 === sr8.key3; // false
}

// nil -------------------------------------------------------

const record {| record {| ()...; |}...; |} nr3 = { key3: nr1, key4: nr2 };
const record {| ()...; |} nr1 = { key1: () };
const record {| ()...; |} nr2 = { key2: () };

const record {| record {| ()...; |}...; |} nr3_new = { key3: nr1_new, key4: nr2_new };
const record {| ()...; |} nr1_new = { key1: () };
const record {| ()...; |} nr2_new = { key2: () };

const record {| record {| ()...; |}...; |} nr7 = { key3: { key1: () }, key4: { key2: () } };
const record {| record {| ()...; |}...; |} nr8 = { key3: { key1: () }, key4: { key2: () } };

function testSimpleNilRecordValueEqualityUsingSameReference() returns boolean {
    return nr1 == nr1; // true
}

function testSimpleNilRecordValueEqualityUsingDifferentReference() returns boolean {
    return nr1 == nr1_new; // true
}

function testSimpleNilRecordReferenceEqualityUsingSameReference() returns boolean {
    return nr1 === nr1; // true
}

function testSimpleNilRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return nr1 === nr1_new; // false
}

// -----------------------------------------------------------

function testComplexNilRecordValueEqualityUsingSameReference() returns boolean {
    return nr3 == nr3; // true
}

function testComplexNilRecordValueEqualityUsingDifferentReference() returns boolean {
    return nr3 == nr3_new; // true
}

function testComplexNilRecordReferenceEqualityUsingSameReference() returns boolean {
    return nr3 === nr3; // true
}

function testComplexNilRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return nr3 === nr3_new; // false
}

// -----------------------------------------------------------

function testComplexNilRecordValueEqualityUsingRecordAccessSameReference() returns boolean {
    return nr3.key3 == nr3.key3; // true
}

function testComplexNilRecordValueEqualityUsingRecordAccessDifferentReference() returns boolean {
    return nr3.key3 == nr3_new.key3; // true
}

function testComplexNilRecordReferenceEqualityUsingRecordAccessSameReference() returns boolean {
    return nr1 === nr3.key3; // true
}

function testComplexNilRecordReferenceEqualityUsingRecordAccessDifferentReference() returns boolean {
    return nr3.key3 === nr3_new.key3; // false
}

// -----------------------------------------------------------

function testSimpleNilRecordValueEqualityUsingSameReferenceInRecord() returns boolean {
    return nr7.key3 == nr7.key3; // true
}

function testSimpleNilRecordValueEqualityUsingDifferentReferenceInRecord() returns boolean {
    return nr7.key3 == nr1; // true
}

function testSimpleNilRecordReferenceEqualityUsingSameReferenceInRecord() returns boolean {
    return nr7.key3 === nr7.key3; // true
}

function testSimpleNilRecordReferenceEqualityUsingDifferentReferenceInRecord() returns boolean {
    return nr7.key3 === nr1; // false
}

// -----------------------------------------------------------

function testComplexNilRecordValueEquality() returns boolean {
    return nr7 == nr8; // true
}

function testComplexNilRecordReferenceEquality() returns boolean {
    return nr7 === nr8; // false
}

function testSimpleNilRecordValueEqualityInDifferentRecord() returns boolean {
    return nr7.key3 == nr8.key3; // true
}

function testSimpleNilRecordReferenceEqualityInDifferentRecord() returns boolean {
    return nr7.key3 === nr8.key3; // false
}
