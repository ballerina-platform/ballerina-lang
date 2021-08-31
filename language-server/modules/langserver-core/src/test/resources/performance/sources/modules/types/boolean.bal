// Implementation specific to basic type boolean.

type BooleanSubtype readonly & record {|
    boolean value;
|};

public function booleanConst(boolean value) returns SemType {
    BooleanSubtype t = { value };
    return uniformSubtype(UT_BOOLEAN, t);
}

function booleanSubtypeSingleValue(SubtypeData d) returns boolean? {
    if d is boolean {
        return ();
    }
    BooleanSubtype b = <BooleanSubtype>d;
    return b.value;
}

function booleanSubtypeContains(SubtypeData d, boolean b) returns boolean {
    if d is boolean {
        return d;
    }
    BooleanSubtype r = <BooleanSubtype>d;
    return r.value == b;
}

function booleanSubtypeUnion(SubtypeData d1, SubtypeData d2) returns SubtypeData {
    BooleanSubtype v1 = <BooleanSubtype>d1;
    BooleanSubtype v2 = <BooleanSubtype>d2;
    return v1.value == v2.value ? v1 : true;
}

function booleanSubtypeIntersect(SubtypeData d1, SubtypeData d2) returns SubtypeData {
    BooleanSubtype v1 = <BooleanSubtype>d1;
    BooleanSubtype v2 = <BooleanSubtype>d2;
    return v1.value == v2.value ? v1 : false;
}

function booleanSubtypeDiff(SubtypeData d1, SubtypeData d2) returns SubtypeData {
    BooleanSubtype v1 = <BooleanSubtype>d1;
    BooleanSubtype v2 = <BooleanSubtype>d2;
    return v1.value == v2.value ? false : v1;
}

function booleanSubtypeComplement(SubtypeData d) returns SubtypeData {
    BooleanSubtype v = <BooleanSubtype>d;
    BooleanSubtype t = { value: !v.value };
    return t;
}

final UniformTypeOps booleanOps = {
    union: booleanSubtypeUnion,
    intersect: booleanSubtypeIntersect,
    diff: booleanSubtypeDiff,
    complement: booleanSubtypeComplement,
    isEmpty: notIsEmpty
};
