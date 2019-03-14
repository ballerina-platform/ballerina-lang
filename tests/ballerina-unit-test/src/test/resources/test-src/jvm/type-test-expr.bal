
function testValueTypeInUnion() returns string {
    int|string x = "hello";
    int y = 10;
    if (x is int) {
        return "int";
    } else {
        return "string";
    }
}

function testUnionTypeInUnion() returns string {
    int|string|float x = 5;
    if (x is int|float) {
        return "numeric";
    } else {
        return "string";
    }
}

function testNestedTypeCheck() returns (any, any, any) {
    return (bar(true), bar(1234), bar("hello"));
}

function bar (string | int | boolean i)  returns string {
    if (i is int){
        return "int";
    } else if (i is string | boolean) {
        if (i is string) {
            return "string";
        } else if (i is boolean) {
            return "boolean";
        }
    }

    return "n/a";
}

function testTypeInAny() returns (string) {
    any a = "This is working";
    if (a is string) {
        return "string value: " + <string> a;
    } else if(a is int) {
        return "int";
    } else {
        return "any";
    }
}

function testNilType() returns (string) {
    any a = ();
    if (a is string) {
        return "string";
    } else if(a is int) {
        return "int";
    } else if(a is ()) {
        return "nil";
    }else {
        return "any";
    }
}

type A1 record {
    int x = 0;
};

type B1 record {
    int x = 0;
    string y = "";
};

function testSimpleRecordTypes_1() returns string {
    A1 a1 = {};
    any a = a1;
     if (a is A1) {
        return "a is A1";
    } else if (a is B1) {
        return "a is B1";
    }

    return "n/a";
}

function testSimpleRecordTypes_2() returns (boolean, boolean) {
    B1 b = {};
    any a = b;
    return (a is A1, a is B1);
}

type A2 record {
    int x = 0;
};

type B2 record {
    int x = 0;
};

function testSimpleRecordTypes_3() returns (boolean, boolean) {
    B2 b = {};
    any a = b;
    return (a is A2, a is B2);
}
