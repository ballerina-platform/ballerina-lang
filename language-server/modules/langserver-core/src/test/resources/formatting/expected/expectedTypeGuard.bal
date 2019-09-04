function testValueTypeInUnion() returns string {
    int | string x = 5;
    if (x is int) {
        int y = x;
        return "int: " + string.convert(y);
    } else {
        return "string";
    }
}

type A record {
    string a;
};

type B record {
    string b;
    string c;
};

function testSimpleRecordTypes_1() returns string {
    A x = {a: "foo"};

    any y = x;
    if (y
        is A) {
        return y.a;
    } else if (y is B) {
        return y.b + "-" + y.c;
    }

    return "n/a";
}

function testSimpleTernary() returns string {
    any a = "hello";
    return a is string ? a : "not a string";
}

function testMultipleTypeGuardsWithAndOperator() returns int {
    int | string x = 5;
    any y = 7;
    if (x is int && y is int) {
        return x + y;
    } else {
        return -1;
    }
}

function testMultipleTypeGuardsWithAndOperatorInTernary() returns int {
    int | string x = 5;
    any y = 7;
    return (x is int && y
        is int) ? x + y : -1;
}

function testTypeGuardInElse_1() returns string {
    int | string x = 5;
    if (x
        is int) {
        int y = x;
        return "int: " + string.convert(y);
    } else {
        return "string";
    }
}

function testTypeGuardInElse_3() returns string {
    int | string | float | boolean x = true;
    int | string | float | boolean y = false;
    if (x is int | string) {
        if (y
            is string) {
            return "y is string: " + y;
        } else if (y
            is int) {
            int i = y;
            return "y is float: " + string.convert(i);
        } else {
            return "x is int|string";
        }
    } else if (
        x is float) {
        float f = x;
        return "float: " + string.convert(f);
    } else {
        if (y is
            int) {
            int i = y;
            return "x is boolean and y is int: " + string.convert(i);
        } else if (y is string) {
            return "x is boolean and y is string: " + y;
        } else if (y is float) {
            float f = y;
            return "x is boolean and y is float: " + string.convert(f);
        } else {
            boolean b = y;
            return "x is boolean and y is boolean: " + string.convert(b);
        }
    }
}
