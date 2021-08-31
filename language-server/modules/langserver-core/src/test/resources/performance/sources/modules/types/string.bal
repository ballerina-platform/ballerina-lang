// Implementation specific to basic type string.

type StringSubtype readonly & record {|
    boolean allowed;
    string[] values;
|};

public function stringConst(string value) returns SemType {
    StringSubtype st = { allowed: true, values: [value] };
    return uniformSubtype(UT_STRING, st);
}

function stringSubtypeSingleValue(SubtypeData d) returns string? {
    if d is boolean {
        return ();
    }
    StringSubtype s = <StringSubtype>d;
    if !s.allowed {
        return ();
    }
    string[] values = s.values;
    if values.length() != 1 {
        return ();
    }
    return values[0];
}

function stringSubtypeContains(SubtypeData d, string s) returns boolean {
    if d is boolean {
        return d;
    }
    StringSubtype v = <StringSubtype>d;
    return v.values.indexOf(s) != () ? v.allowed : !v.allowed;
}

function stringSubtypeUnion(SubtypeData d1, SubtypeData d2) returns SubtypeData {
    string[] values = [];
    boolean allowed = enumerableSubtypeUnion(<StringSubtype>d1, <StringSubtype>d2, values);
    return createStringSubtype(allowed, values);
}

function stringSubtypeIntersect(SubtypeData d1, SubtypeData d2) returns SubtypeData {
    string[] values = [];
    boolean allowed = enumerableSubtypeIntersect(<StringSubtype>d1, <StringSubtype>d2, values);
    return createStringSubtype(allowed, values);
}

function stringSubtypeDiff(SubtypeData d1, SubtypeData d2) returns SubtypeData {
    return stringSubtypeIntersect(d1, stringSubtypeComplement(d2));
}

function stringSubtypeComplement(SubtypeData d) returns SubtypeData {
    StringSubtype s = <StringSubtype>d;
    return createStringSubtype(!s.allowed, s.values);
}

function createStringSubtype(boolean allowed, string[] values) returns SubtypeData {
    if values.length() == 0 {
        return !allowed;
    }
    StringSubtype res = { allowed, values: values.cloneReadOnly() };
    return res;
}

final UniformTypeOps stringOps = {
    union: stringSubtypeUnion,
    intersect: stringSubtypeIntersect,
    diff: stringSubtypeDiff,
    complement: stringSubtypeComplement,
    // Empty string sets don't use subtype representation.
    isEmpty: notIsEmpty
};
