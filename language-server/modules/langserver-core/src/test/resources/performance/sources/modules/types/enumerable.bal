// EnumerableTypes are types in which each expressible subtype can be reasonably
// represented by a list of values. counter-eg: `int` is not a EnumerableType since
// uint32 can't be reasonably represented by listing all values.
type EnumerableType float|string;

type EnumerableSubtype FloatSubtype|StringSubtype;

const LT = -1;
const EQ = 0;
const GT = 1;

type Order GT|EQ|LT;

function enumerableSubtypeUnion(EnumerableSubtype t1, EnumerableSubtype t2, EnumerableType[] result) returns boolean {
    boolean b1 = t1.allowed;
    boolean b2 = t2.allowed;
    boolean allowed;
    if b1 && b2 {
        enumerableListUnion(t1.values, t2.values, result);
        allowed = true;
    }
    else if !b1 && !b2 {
        enumerableListIntersect(t1.values, t2.values, result);
        allowed = false;
    }
    else if b1 && !b2 {
        enumerableListDiff(t2.values, t1.values, result);
        allowed = false;
    }
    else {
         // !b1 && b2
        enumerableListDiff(t1.values, t2.values, result);
        allowed = false;
    }
    return allowed;
}

function enumerableSubtypeIntersect(EnumerableSubtype t1, EnumerableSubtype t2, EnumerableType[] result) returns boolean {
    boolean b1 = t1.allowed;
    boolean b2 = t2.allowed;
    boolean allowed;
    if b1 && b2 {
        enumerableListIntersect(t1.values, t2.values, result);
        allowed = true;
    }
    else if !b1 && !b2 {
        enumerableListUnion(t1.values, t2.values, result);
        allowed = false;
    }
    else if b1 && !b2 {
        enumerableListDiff(t1.values, t2.values, result);
        allowed = true;
    }
    else {
        // !b1 && b2
        enumerableListDiff(t2.values, t1.values, result);
        allowed = true;
    }
    return allowed;
}

function enumerableListUnion(EnumerableType[] v1, EnumerableType[] v2, EnumerableType[] result) {
    int i1 = 0;
    int i2 = 0;
    int len1 = v1.length();
    int len2 = v2.length();
    while true {
        if i1 >= len1 {
            if i2 >= len2 {
                break;
            }
            result.push(v2[i2]);
            i2 += 1;
        }
        else if i2 >= len2 {
            result.push(v1[i1]);
            i1 += 1;
        }
        else {
            EnumerableType s1 = v1[i1];
            EnumerableType s2 = v2[i2];
            match compareEnumerable(s1, s2) {
                EQ => {
                    result.push(s1);
                    i1 += 1;
                    i2 += 1;
                }
                LT => {
                    result.push(s1);
                    i1 += 1;
                }
                GT => {
                    result.push(s2);
                    i2 += 1;
                }
            }
        }
    }
}

function enumerableListIntersect(EnumerableType[] v1, EnumerableType[] v2, EnumerableType[] result) {
    int i1 = 0;
    int i2 = 0;
    int len1 = v1.length();
    int len2 = v2.length();
    while true {
        if i1 >= len1 || i2 >= len2 {
            break;
        }
        else {
            EnumerableType s1 = v1[i1];
            EnumerableType s2 = v2[i2];
            match compareEnumerable(s1, s2) {
                EQ => {
                    result.push(s1);
                    i1 += 1;
                    i2 += 1;
                }
                LT => {
                    i1 += 1;
                }
                GT => {
                    i2 += 1;
                }
            }
        }
    }
}

function enumerableListDiff(EnumerableType[] v1, EnumerableType[] v2, EnumerableType[] result) {
    int i1 = 0;
    int i2 = 0;
    int len1 = v1.length();
    int len2 = v2.length();
    while true {
        if i1 >= len1 {
            break;
        }
        if i2 >= len2 {
            result.push(v1[i1]);
            i1 += 1;
        }
        else {
            EnumerableType s1 = v1[i1];
            EnumerableType s2 = v2[i2];
            match compareEnumerable(s1, s2) {
                EQ => {
                    i1 += 1;
                    i2 += 1;
                }
                LT => {
                    result.push(s1);
                    i1 += 1;
                }
                GT => {
                    i2 += 1;
                }
            }
        }
    }
}

function compareEnumerable(EnumerableType v1, EnumerableType v2) returns Order {
    if v1 is string {
        string s2 = <string>v2;
        return v1 == s2 ? EQ : (v1 < s2 ? LT : GT);
    }
    else {
        float f2 = <float>v2;
        // JBUG: #17977 can't use `==`
        if floatEq(v1, f2) {
            return EQ;
        }
        else if floatEq(v1, float:NaN) {
             return LT;
        }
        else if floatEq(f2, float:NaN) {
            return GT;
        }
        else if v1 < f2 {
            return LT;
        }
        return GT;
    }
}

// XXX remove this after JBUG #17977 is fixed
function floatEq(float f1, float f2) returns boolean {
    if float:isNaN(f1) {
        if float:isNaN(f2) {
            return true;
        }
        else {
            return false;
        }
    }
    return f1 == f2;
}
