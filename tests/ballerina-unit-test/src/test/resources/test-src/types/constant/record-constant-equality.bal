
// boolean ---------------------------------------------------

const record {| record {| boolean...; |}...; |} bm3 = { key3: bm1, key4: bm2 };
const record {| boolean...; |} bm1 = { key1: true };
const record {| boolean...; |} bm2 = { key2: false };

const record {| record {| boolean...; |}...; |} bm3_new = { key3: bm1_new, key4: bm2_new };
const record {| boolean...; |} bm1_new = { key1: true };
const record {| boolean...; |} bm2_new = { key2: false };

const record {| record {| boolean...; |}...; |} bm7 = { key3: { key1: true }, key2: { key4: false } };
const record {| record {| boolean...; |}...; |} bm8 = { key3: { key1: true }, key2: { key4: false } };

function testSimpleBooleanRecordValueEqualityUsingSameReference() returns boolean {
    return bm1 == bm1; // true
}

function testSimpleBooleanRecordValueEqualityUsingDifferentReference() returns boolean {
    return bm1 == bm1_new; // true
}

function testSimpleBooleanRecordReferenceEqualityUsingSameReference() returns boolean {
    return bm1 === bm1; // true
}

function testSimpleBooleanRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return bm1 === bm1_new; // false
}

// -----------------------------------------------------------

function testComplexBooleanRecordValueEqualityUsingSameReference() returns boolean {
    return bm3 == bm3; // true
}

function testComplexBooleanRecordValueEqualityUsingDifferentReference() returns boolean {
    return bm3 == bm3_new; // true
}

function testComplexBooleanRecordReferenceEqualityUsingSameReference() returns boolean {
    return bm3 === bm3; // true
}

function testComplexBooleanRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return bm3 === bm3_new; // false
}

// -----------------------------------------------------------

function testComplexBooleanRecordValueEqualityUsingRecordAccessSameReference() returns boolean {
    return bm3.key3 == bm3.key3; // true
}

function testComplexBooleanRecordValueEqualityUsingRecordAccessDifferentReference() returns boolean {
    return bm3.key3 == bm3_new.key3; // true
}

function testComplexBooleanRecordReferenceEqualityUsingRecordAccessSameReference() returns boolean {
    return bm1 === bm3.key3; // true
}

function testComplexBooleanRecordReferenceEqualityUsingRecordAccessDifferentReference() returns boolean {
    return bm3.key3 === bm3_new.key3; // false
}

// -----------------------------------------------------------

function testSimpleBooleanRecordValueEqualityUsingSameReferenceInRecord() returns boolean {
    return bm7.key3 == bm7.key3; // true
}

function testSimpleBooleanRecordValueEqualityUsingDifferentReferenceInRecord() returns boolean {
    return bm7.key3 == bm1; // true
}

function testSimpleBooleanRecordReferenceEqualityUsingSameReferenceInRecord() returns boolean {
    return bm7.key3 === bm7.key3; // true
}

function testSimpleBooleanRecordReferenceEqualityUsingDifferentReferenceInRecord() returns boolean {
    return bm7.key3 === bm1; // false
}

// -----------------------------------------------------------

function testComplexBooleanRecordValueEquality() returns boolean {
    return bm7 == bm8; // true
}

function testComplexBooleanRecordReferenceEquality() returns boolean {
    return bm7 === bm8; // false
}

function testSimpleBooleanRecordValueEqualityInDifferentRecord() returns boolean {
    return bm7.key3 == bm8.key3; // true
}

function testSimpleBooleanRecordReferenceEqualityInDifferentRecord() returns boolean {
    return bm7.key3 === bm8.key3; // false
}

// int -------------------------------------------------------

const record {| record {| int...; |}...; |} im3 = { key3: im1, key4: im2 };
const record {| int...; |} im1 = { key1: 1 };
const record {| int...; |} im2 = { key2: 2 };

const record {| record {| int...; |}...; |} im3_new = { key3: im1_new, key4: im2_new };
const record {| int...; |} im1_new = { key1: 1 };
const record {| int...; |} im2_new = { key2: 2 };

const record {| record {| int...; |}...; |} im7 = { key3: { key1: 1 }, key4: { key2: 2 } };
const record {| record {| int...; |}...; |} im8 = { key3: { key1: 1 }, key4: { key2: 2 } };

function testSimpleIntRecordValueEqualityUsingSameReference() returns boolean {
    return im1 == im1; // true
}

function testSimpleIntRecordValueEqualityUsingDifferentReference() returns boolean {
    return im1 == im1_new; // true
}

function testSimpleIntRecordReferenceEqualityUsingSameReference() returns boolean {
    return im1 === im1; // true
}

function testSimpleIntRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return im1 === im1_new; // false
}

// -----------------------------------------------------------

function testComplexIntRecordValueEqualityUsingSameReference() returns boolean {
    return im3 == im3; // true
}

function testComplexIntRecordValueEqualityUsingDifferentReference() returns boolean {
    return im3 == im3_new; // true
}

function testComplexIntRecordReferenceEqualityUsingSameReference() returns boolean {
    return im3 === im3; // true
}

function testComplexIntRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return im3 === im3_new; // false
}

// -----------------------------------------------------------

function testComplexIntRecordValueEqualityUsingRecordAccessSameReference() returns boolean {
    return im3.key3 == im3.key3; // true
}

function testComplexIntRecordValueEqualityUsingRecordAccessDifferentReference() returns boolean {
    return im3.key3 == im3_new.key3; // true
}

function testComplexIntRecordReferenceEqualityUsingRecordAccessSameReference() returns boolean {
    return im1 === im3.key3; // true
}

function testComplexIntRecordReferenceEqualityUsingRecordAccessDifferentReference() returns boolean {
    return im3.key3 === im3_new.key3; // false
}

// -----------------------------------------------------------

function testSimpleIntRecordValueEqualityUsingSameReferenceInRecord() returns boolean {
    return im7.key3 == im7.key3; // true
}

function testSimpleIntRecordValueEqualityUsingDifferentReferenceInRecord() returns boolean {
    return im7.key3 == im1; // true
}

function testSimpleIntRecordReferenceEqualityUsingSameReferenceInRecord() returns boolean {
    return im7.key3 === im7.key3; // true
}

function testSimpleIntRecordReferenceEqualityUsingDifferentReferenceInRecord() returns boolean {
    return im7.key3 === im1; // false
}

// -----------------------------------------------------------

function testComplexIntRecordValueEquality() returns boolean {
    return im7 == im8; // true
}

function testComplexIntRecordReferenceEquality() returns boolean {
    return im7 === im8; // false
}

function testSimpleIntRecordValueEqualityInDifferentRecord() returns boolean {
    return im7.key3 == im8.key3; // true
}

function testSimpleIntRecordReferenceEqualityInDifferentRecord() returns boolean {
    return im7.key3 === im8.key3; // false
}

// byte ------------------------------------------------------

const record {| record {| byte...; |}...; |} bytem3 = { key3: bytem1, key4: bytem2 };
const record {| byte...; |} bytem1 = { key1: 10 };
const record {| byte...; |} bytem2 = { key2: 20 };

const record {| record {| byte...; |}...; |} bytem3_new = { key3: bytem1_new, key4: bytem2_new };
const record {| byte...; |} bytem1_new = { key1: 10 };
const record {| byte...; |} bytem2_new = { key2: 20 };

const record {| record {| byte...; |}...; |} bytem7 = { key3: { key1: 10 }, key4: { key2: 20 } };
const record {| record {| byte...; |}...; |} bytem8 = { key3: { key1: 10 }, key4: { key2: 20 } };

function testSimpleByteRecordValueEqualityUsingSameReference() returns boolean {
    return bytem1 == bytem1; // true
}

function testSimpleByteRecordValueEqualityUsingDifferentReference() returns boolean {
    return bytem1 == bytem1_new; // true
}

function testSimpleByteRecordReferenceEqualityUsingSameReference() returns boolean {
    return bytem1 === bytem1; // true
}

function testSimpleByteRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return bytem1 === bytem1_new; // false
}

// -----------------------------------------------------------

function testComplexByteRecordValueEqualityUsingSameReference() returns boolean {
    return bytem3 == bytem3; // true
}

function testComplexByteRecordValueEqualityUsingDifferentReference() returns boolean {
    return bytem3 == bytem3_new; // true
}

function testComplexByteRecordReferenceEqualityUsingSameReference() returns boolean {
    return bytem3 === bytem3; // true
}

function testComplexByteRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return bytem3 === bytem3_new; // false
}

// -----------------------------------------------------------

function testComplexByteRecordValueEqualityUsingRecordAccessSameReference() returns boolean {
    return bytem3.key3 == bytem3.key3; // true
}

function testComplexByteRecordValueEqualityUsingRecordAccessDifferentReference() returns boolean {
    return bytem3.key3 == bytem3_new.key3; // true
}

function testComplexByteRecordReferenceEqualityUsingRecordAccessSameReference() returns boolean {
    return bytem1 === bytem3.key3; // true
}

function testComplexByteRecordReferenceEqualityUsingRecordAccessDifferentReference() returns boolean {
    return bytem3.key3 === bytem3_new.key3; // false
}

// -----------------------------------------------------------

function testSimpleByteRecordValueEqualityUsingSameReferenceInRecord() returns boolean {
    return bytem7.key3 == bytem7.key3; // true
}

function testSimpleByteRecordValueEqualityUsingDifferentReferenceInRecord() returns boolean {
    return bytem7.key3 == bytem1; // true
}

function testSimpleByteRecordReferenceEqualityUsingSameReferenceInRecord() returns boolean {
    return bytem7.key3 === bytem7.key3; // true
}

function testSimpleByteRecordReferenceEqualityUsingDifferentReferenceInRecord() returns boolean {
    return bytem7.key3 === bytem1; // false
}

// -----------------------------------------------------------

function testComplexByteRecordValueEquality() returns boolean {
    return bytem7 == bytem8; // true
}

function testComplexByteRecordReferenceEquality() returns boolean {
    return bytem7 === bytem8; // false
}

function testSimpleByteRecordValueEqualityInDifferentRecord() returns boolean {
    return bytem7.key3 == bytem8.key3; // true
}

function testSimpleByteRecordReferenceEqualityInDifferentRecord() returns boolean {
    return bytem7.key3 === bytem8.key3; // false
}

// float -----------------------------------------------------

const record {| record {| float...; |}...; |} fm3 = { key3: fm1, key4: fm2 };
const record {| float...; |} fm1 = { key1: 2.0 };
const record {| float...; |} fm2 = { key2: 4.0 };

const record {| record {| float...; |}...; |} fm3_new = { key3: fm1_new, key4: fm2_new };
const record {| float...; |} fm1_new = { key1: 2.0 };
const record {| float...; |} fm2_new = { key2: 4.0 };

const record {| record {| float...; |}...; |} fm7 = { key3: { key1: 2.0 }, key4: { key2: 4.0 } };
const record {| record {| float...; |}...; |} fm8 = { key3: { key1: 2.0 }, key4: { key2: 4.0 } };

function testSimpleFloatRecordValueEqualityUsingSameReference() returns boolean {
    return fm1 == fm1; // true
}

function testSimpleFloatRecordValueEqualityUsingDifferentReference() returns boolean {
    return fm1 == fm1_new; // true
}

function testSimpleFloatRecordReferenceEqualityUsingSameReference() returns boolean {
    return fm1 === fm1; // true
}

function testSimpleFloatRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return fm1 === fm1_new; // false
}

// -----------------------------------------------------------

function testComplexFloatRecordValueEqualityUsingSameReference() returns boolean {
    return fm3 == fm3; // true
}

function testComplexFloatRecordValueEqualityUsingDifferentReference() returns boolean {
    return fm3 == fm3_new; // true
}

function testComplexFloatRecordReferenceEqualityUsingSameReference() returns boolean {
    return fm3 === fm3; // true
}

function testComplexFloatRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return fm3 === fm3_new; // false
}

// -----------------------------------------------------------

function testComplexFloatRecordValueEqualityUsingRecordAccessSameReference() returns boolean {
    return fm3.key3 == fm3.key3; // true
}

function testComplexFloatRecordValueEqualityUsingRecordAccessDifferentReference() returns boolean {
    return fm3.key3 == fm3_new.key3; // true
}

function testComplexFloatRecordReferenceEqualityUsingRecordAccessSameReference() returns boolean {
    return fm1 === fm3.key3; // true
}

function testComplexFloatRecordReferenceEqualityUsingRecordAccessDifferentReference() returns boolean {
    return fm3.key3 === fm3_new.key3; // false
}

// -----------------------------------------------------------

function testSimpleFloatRecordValueEqualityUsingSameReferenceInRecord() returns boolean {
    return fm7.key3 == fm7.key3; // true
}

function testSimpleFloatRecordValueEqualityUsingDifferentReferenceInRecord() returns boolean {
    return fm7.key3 == fm1; // true
}

function testSimpleFloatRecordReferenceEqualityUsingSameReferenceInRecord() returns boolean {
    return fm7.key3 === fm7.key3; // true
}

function testSimpleFloatRecordReferenceEqualityUsingDifferentReferenceInRecord() returns boolean {
    return fm7.key3 === fm1; // false
}

// -----------------------------------------------------------

function testComplexFloatRecordValueEquality() returns boolean {
    return fm7 == fm8; // true
}

function testComplexFloatRecordReferenceEquality() returns boolean {
    return fm7 === fm8; // false
}

function testSimpleFloatRecordValueEqualityInDifferentRecord() returns boolean {
    return fm7.key3 == fm8.key3; // true
}

function testSimpleFloatRecordReferenceEqualityInDifferentRecord() returns boolean {
    return fm7.key3 === fm8.key3; // false
}

// decimal ---------------------------------------------------

const record {| record {| decimal...; |}...; |} dm3 = { key3: dm1, key4: dm2 };
const record {| decimal...; |} dm1 = { key1: 100 };
const record {| decimal...; |} dm2 = { key2: 200 };

const record {| record {| decimal...; |}...; |} dm3_new = { key3: dm1_new, key4: dm2_new };
const record {| decimal...; |} dm1_new = { key1: 100 };
const record {| decimal...; |} dm2_new = { key2: 200 };

const record {| record {| decimal...; |}...; |} dm7 = { key3: { key1: 100 }, key4: { key2: 200 } };
const record {| record {| decimal...; |}...; |} dm8 = { key3: { key1: 100 }, key4: { key2: 200 } };

function testSimpleDecimalRecordValueEqualityUsingSameReference() returns boolean {
    return dm1 == dm1; // true
}

function testSimpleDecimalRecordValueEqualityUsingDifferentReference() returns boolean {
    return dm1 == dm1_new; // true
}

function testSimpleDecimalRecordReferenceEqualityUsingSameReference() returns boolean {
    return dm1 === dm1; // true
}

function testSimpleDecimalRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return dm1 === dm1_new; // false
}

// -----------------------------------------------------------

function testComplexDecimalRecordValueEqualityUsingSameReference() returns boolean {
    return dm3 == dm3; // true
}

function testComplexDecimalRecordValueEqualityUsingDifferentReference() returns boolean {
    return dm3 == dm3_new; // true
}

function testComplexDecimalRecordReferenceEqualityUsingSameReference() returns boolean {
    return dm3 === dm3; // true
}

function testComplexDecimalRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return dm3 === dm3_new; // false
}

// -----------------------------------------------------------

function testComplexDecimalRecordValueEqualityUsingRecordAccessSameReference() returns boolean {
    return dm3.key3 == dm3.key3; // true
}

function testComplexDecimalRecordValueEqualityUsingRecordAccessDifferentReference() returns boolean {
    return dm3.key3 == dm3_new.key3; // true
}

function testComplexDecimalRecordReferenceEqualityUsingRecordAccessSameReference() returns boolean {
    return dm1 === dm3.key3; // true
}

function testComplexDecimalRecordReferenceEqualityUsingRecordAccessDifferentReference() returns boolean {
    return dm3.key3 === dm3_new.key3; // false
}

// -----------------------------------------------------------

function testSimpleDecimalRecordValueEqualityUsingSameReferenceInRecord() returns boolean {
    return dm7.key3 == dm7.key3; // true
}

function testSimpleDecimalRecordValueEqualityUsingDifferentReferenceInRecord() returns boolean {
    return dm7.key3 == dm1; // true
}

function testSimpleDecimalRecordReferenceEqualityUsingSameReferenceInRecord() returns boolean {
    return dm7.key3 === dm7.key3; // true
}

function testSimpleDecimalRecordReferenceEqualityUsingDifferentReferenceInRecord() returns boolean {
    return dm7.key3 === dm1; // false
}

// -----------------------------------------------------------

function testComplexDecimalRecordValueEquality() returns boolean {
    return dm7 == dm8; // true
}

function testComplexDecimalRecordReferenceEquality() returns boolean {
    return dm7 === dm8; // false
}

function testSimpleDecimalRecordValueEqualityInDifferentRecord() returns boolean {
    return dm7.key3 == dm8.key3; // true
}

function testSimpleDecimalRecordReferenceEqualityInDifferentRecord() returns boolean {
    return dm7.key3 === dm8.key3; // false
}

// string ----------------------------------------------------

const record {| record {| string...; |}...; |} sm3 = { key3: sm1, key4: sm2 };
const record {| string...; |} sm1 = { key1: "value1" };
const record {| string...; |} sm2 = { key2: "value2" };

const record {| record {| string...; |}...; |} sm3_new = { key3: sm1_new, key4: sm2_new };
const record {| string...; |} sm1_new = { key1: "value1" };
const record {| string...; |} sm2_new = { key2: "value2" };

const record {| record {| string...; |}...; |} sm7 = { key3: { key1: "value1" }, key4: { key2: "value2" } };
const record {| record {| string...; |}...; |} sm8 = { key3: { key1: "value1" }, key4: { key2: "value2" } };

function testSimpleStringRecordValueEqualityUsingSameReference() returns boolean {
    return sm1 == sm1; // true
}

function testSimpleStringRecordValueEqualityUsingDifferentReference() returns boolean {
    return sm1 == sm1_new; // true
}

function testSimpleStringRecordReferenceEqualityUsingSameReference() returns boolean {
    return sm1 === sm1; // true
}

function testSimpleStringRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return sm1 === sm1_new; // false
}

// -----------------------------------------------------------

function testComplexStringRecordValueEqualityUsingSameReference() returns boolean {
    return sm3 == sm3; // true
}

function testComplexStringRecordValueEqualityUsingDifferentReference() returns boolean {
    return sm3 == sm3_new; // true
}

function testComplexStringRecordReferenceEqualityUsingSameReference() returns boolean {
    return sm3 === sm3; // true
}

function testComplexStringRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return sm3 === sm3_new; // false
}

// -----------------------------------------------------------

function testComplexStringRecordValueEqualityUsingRecordAccessSameReference() returns boolean {
    return sm3.key3 == sm3.key3; // true
}

function testComplexStringRecordValueEqualityUsingRecordAccessDifferentReference() returns boolean {
    return sm3.key3 == sm3_new.key3; // true
}

function testComplexStringRecordReferenceEqualityUsingRecordAccessSameReference() returns boolean {
    return sm1 === sm3.key3; // true
}

function testComplexStringRecordReferenceEqualityUsingRecordAccessDifferentReference() returns boolean {
    return sm3.key3 === sm3_new.key3; // false
}

// -----------------------------------------------------------

function testSimpleStringRecordValueEqualityUsingSameReferenceInRecord() returns boolean {
    return sm7.key3 == sm7.key3; // true
}

function testSimpleStringRecordValueEqualityUsingDifferentReferenceInRecord() returns boolean {
    return sm7.key3 == sm1; // true
}

function testSimpleStringRecordReferenceEqualityUsingSameReferenceInRecord() returns boolean {
    return sm7.key3 === sm7.key3; // true
}

function testSimpleStringRecordReferenceEqualityUsingDifferentReferenceInRecord() returns boolean {
    return sm7.key3 === sm1; // false
}

// -----------------------------------------------------------

function testComplexStringRecordValueEquality() returns boolean {
    return sm7 == sm8; // true
}

function testComplexStringRecordReferenceEquality() returns boolean {
    return sm7 === sm8; // false
}

function testSimpleStringRecordValueEqualityInDifferentRecord() returns boolean {
    return sm7.key3 == sm8.key3; // true
}

function testSimpleStringRecordReferenceEqualityInDifferentRecord() returns boolean {
    return sm7.key3 === sm8.key3; // false
}

// nil -------------------------------------------------------

const record {| record {| ()...; |}...; |} nm3 = { key3: nm1, key4: nm2 };
const record {| ()...; |} nm1 = { key1: () };
const record {| ()...; |} nm2 = { key2: () };

const record {| record {| ()...; |}...; |} nm3_new = { key3: nm1_new, key4: nm2_new };
const record {| ()...; |} nm1_new = { key1: () };
const record {| ()...; |} nm2_new = { key2: () };

const record {| record {| ()...; |}...; |} nm7 = { key3: { key1: () }, key4: { key2: () } };
const record {| record {| ()...; |}...; |} nm8 = { key3: { key1: () }, key4: { key2: () } };

function testSimpleNilRecordValueEqualityUsingSameReference() returns boolean {
    return nm1 == nm1; // true
}

function testSimpleNilRecordValueEqualityUsingDifferentReference() returns boolean {
    return nm1 == nm1_new; // true
}

function testSimpleNilRecordReferenceEqualityUsingSameReference() returns boolean {
    return nm1 === nm1; // true
}

function testSimpleNilRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return nm1 === nm1_new; // false
}

// -----------------------------------------------------------

function testComplexNilRecordValueEqualityUsingSameReference() returns boolean {
    return nm3 == nm3; // true
}

function testComplexNilRecordValueEqualityUsingDifferentReference() returns boolean {
    return nm3 == nm3_new; // true
}

function testComplexNilRecordReferenceEqualityUsingSameReference() returns boolean {
    return nm3 === nm3; // true
}

function testComplexNilRecordReferenceEqualityUsingDifferentReference() returns boolean {
    return nm3 === nm3_new; // false
}

// -----------------------------------------------------------

function testComplexNilRecordValueEqualityUsingRecordAccessSameReference() returns boolean {
    return nm3.key3 == nm3.key3; // true
}

function testComplexNilRecordValueEqualityUsingRecordAccessDifferentReference() returns boolean {
    return nm3.key3 == nm3_new.key3; // true
}

function testComplexNilRecordReferenceEqualityUsingRecordAccessSameReference() returns boolean {
    return nm1 === nm3.key3; // true
}

function testComplexNilRecordReferenceEqualityUsingRecordAccessDifferentReference() returns boolean {
    return nm3.key3 === nm3_new.key3; // false
}

// -----------------------------------------------------------

function testSimpleNilRecordValueEqualityUsingSameReferenceInRecord() returns boolean {
    return nm7.key3 == nm7.key3; // true
}

function testSimpleNilRecordValueEqualityUsingDifferentReferenceInRecord() returns boolean {
    return nm7.key3 == nm1; // true
}

function testSimpleNilRecordReferenceEqualityUsingSameReferenceInRecord() returns boolean {
    return nm7.key3 === nm7.key3; // true
}

function testSimpleNilRecordReferenceEqualityUsingDifferentReferenceInRecord() returns boolean {
    return nm7.key3 === nm1; // false
}

// -----------------------------------------------------------

function testComplexNilRecordValueEquality() returns boolean {
    return nm7 == nm8; // true
}

function testComplexNilRecordReferenceEquality() returns boolean {
    return nm7 === nm8; // false
}

function testSimpleNilRecordValueEqualityInDifferentRecord() returns boolean {
    return nm7.key3 == nm8.key3; // true
}

function testSimpleNilRecordReferenceEqualityInDifferentRecord() returns boolean {
    return nm7.key3 === nm8.key3; // false
}
