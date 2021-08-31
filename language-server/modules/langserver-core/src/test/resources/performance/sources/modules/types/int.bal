// Implementation specific to basic type int.

type IntSubtype readonly & Range[];

// Ranges are inclusive
// Require min <= max
type Range readonly & record {|
    int min;
    int max;
|};


public function intConst(int value) returns SemType {
    IntSubtype t = [{ min: value, max: value }];
    return uniformSubtype(UT_INT, t);
}

function validIntWidth(boolean signed, int bits) returns error? {
    if bits <= 0 {
        return error((bits == 0 ? "zero" : "negative") + " width in bits");
    }
    if signed {
        if bits > 64 {
            return error("width of signed integers limited to 64");
        }
    }
    else {
        if bits > 63 {
            return error("width of unsigned integers limited to 63");
        }
    }
}

public function validIntWidthSigned(int bits) returns error? => validIntWidth(true, bits);
public function validIntWidthUnsigned(int bits) returns error? => validIntWidth(false, bits);

public function intWidthSigned(int bits) returns SemType {
    checkpanic validIntWidth(true, bits);
    if bits == 64 {
        return INT;
    }
    IntSubtype t = [{ min: -(1 << (bits - 1)), max: (1 << (bits - 1)) - 1 }];
    return uniformSubtype(UT_INT, t);
}

public function intWidthUnsigned(int bits) returns SemType {
    checkpanic validIntWidth(false, bits);
    IntSubtype t = [{ min: 0, max: (1 << bits) - 1 }];
    return uniformSubtype(UT_INT, t);
}

// Widen to UnsignedN
function intSubtypeWidenUnsigned(SubtypeData d) returns SubtypeData {
    if d is boolean {
        return d;
    }
    IntSubtype v = <IntSubtype>d;
    if v[0].min < 0 {
        return true;
    }
    Range r = v[v.length() - 1];
    int i = 8;
    while i <= 32 {
        if r.max < (1 << i) {
            IntSubtype w = [{ min: 0, max: (1 << i) - 1 }];
            return w;
        }
        i = i * 2;
    }
    return true;
}

function intSubtypeSingleValue(SubtypeData d) returns int? {
    if d is boolean {
        return ();
    }
    IntSubtype v = <IntSubtype>d;
    if v.length() != 1 {
        return ();
    }
    int min = v[0].min;
    if min != v[0].max {
        return ();
    }
    return min;
}

function intSubtypeContains(SubtypeData d, int n) returns boolean {
    if d is boolean {
        return d;
    }
    IntSubtype v = <IntSubtype>d;
    foreach Range r in v {
        if r.min <= n && n <= r.max {
            return true;
        }
    }
    return false;
}

function intSubtypeUnion(SubtypeData d1, SubtypeData d2) returns SubtypeData {
    IntSubtype v1 = <IntSubtype>d1;
    IntSubtype v2 = <IntSubtype>d2;
    Range[] v = rangeListUnion(v1, v2);
    if v.length() == 1 && v[0].min == int:MIN_VALUE && v[0].max == int:MAX_VALUE {
        return true;
    }
    return v.cloneReadOnly();
}

function intSubtypeIntersect(SubtypeData d1, SubtypeData d2) returns SubtypeData {
    IntSubtype v1 = <IntSubtype>d1;
    IntSubtype v2 = <IntSubtype>d2;
    Range[] v = rangeListIntersect(v1, v2);
    if v.length() == 0 {
        return false;
    }
    return v.cloneReadOnly();
}

function intSubtypeDiff(SubtypeData d1, SubtypeData d2) returns SubtypeData {
    IntSubtype v1 = <IntSubtype>d1;
    IntSubtype v2 = <IntSubtype>d2;

    Range[] v = rangeListIntersect(v1, rangeListComplement(v2));
    if v.length() == 0 {
        return false;
    }
    return v.cloneReadOnly();
}

function intSubtypeComplement(SubtypeData d) returns SubtypeData {
    IntSubtype v = <IntSubtype>d;
    return rangeListComplement(v).cloneReadOnly();
}

function rangeListUnion(Range[] v1, Range[] v2) returns Range[] {
    Range[] result = [];
    int i1 = 0;
    int i2 = 0;
    int len1 = v1.length();
    int len2 = v2.length();
    while true {
        if i1 >= len1 {
            if i2 >= len2 {
                break;
            }
            rangeUnionPush(result, v2[i2]);
            i2 += 1;
        }
        else if i2 >= len2 {
            rangeUnionPush(result, v1[i1]);
            i1 += 1;
        }
        else {
            Range r1 = v1[i1];
            Range r2 = v2[i2];
            var combined = rangeUnion(r1, r2);
            if combined is Range {
                rangeUnionPush(result, combined);
                i1 += 1;
                i2 += 1;
            }
            else if combined < 0 {
                rangeUnionPush(result, r1);
                i1 += 1;
            }
            else {
                rangeUnionPush(result, r2);
                i2 += 1;
            }
        }
    }
    return result;
}

function rangeUnionPush(Range[] ranges, Range next) {
    int lastIndex = ranges.length() - 1;
    if lastIndex < 0 {
        ranges.push(next);
        return;
    }
    var combined = rangeUnion(ranges[lastIndex], next);
    if combined is Range {
        ranges[lastIndex] = combined;
    }
    else {
        ranges.push(next);
    }
}

// Returns a range if there is a single range representing the union of r1 and r1.
// -1 means union is empty because r1 is before r2, with no overlap
// 1 means union is empty because r2 is before r1 with no overlap
// Precondition r1 and r2 are non-empty
function rangeUnion(Range r1, Range r2) returns -1|1|Range {
    if r1.max < r2.min {
        // we can still merge if the ranges are adjacent
        if r1.max + 1 != r2.min {
            return -1;
        }
    }
    if r2.max < r1.min {
        // we can still merge if the ranges are adjacent
        if r2.max + 1 != r2.min {
            return 1;
        }
    }
    return { min: int:min(r1.min, r2.min), max: int:max(r1.max, r2.max) };
}
function rangeListIntersect(Range[] v1, Range[] v2) returns Range[] {
    Range[] result = [];
    int i1 = 0;
    int i2 = 0;
    int len1 = v1.length();
    int len2 = v2.length();
    while true {
        if i1 >= len1 || i2 >= len2 {
            break;
        }
        else {
            Range r1 = v1[i1];
            Range r2 = v2[i2];
            var combined = rangeIntersect(r1, r2);
            if combined is Range {
                // no need for rangeUnionPush here
                result.push(combined);
                i1 += 1;
                i2 += 1;
            }
            else if combined < 0 {
                i1 += 1;
            }
            else {
                i2 += 1;
            }
        }
    }
    return result;
}

// when Range is returned, it is non-empty and the intersection of r1 and r2
// -1 means empty intersection because r1 before r2
// 1 means empty intersection because r1 after r2
function rangeIntersect(Range r1, Range r2) returns -1|1|Range {
    // there are two possibilities for no overlap
    if r1.max < r2.min {
        return -1;
    }
    if r2.max < r1.min {
        return 1;
    }
    // we know they have a non-empty overlap
    return { min: int:max(r1.min, r2.min), max: int:min(r1.max, r2.max) };

}

// precondition v is not empty
function rangeListComplement(Range[] v) returns Range[] {
    Range[] result = [];
    int len = v.length();
    int min = v[0].min;
    if min > int:MIN_VALUE {
        result.push({min: int:MIN_VALUE, max: min - 1});
    }
    foreach int i in 1 ..< len {
        result.push({ min: v[i - 1].max + 1, max: v[i].min - 1 });
    }
    int max = v[v.length() - 1].max;
    if max < int:MAX_VALUE {
        result.push({ min: max + 1, max: int:MAX_VALUE });
    }
    return result;
}

final UniformTypeOps intOps = {
    union: intSubtypeUnion,
    intersect: intSubtypeIntersect,
    diff: intSubtypeDiff,
    complement: intSubtypeComplement,
    isEmpty: notIsEmpty
};