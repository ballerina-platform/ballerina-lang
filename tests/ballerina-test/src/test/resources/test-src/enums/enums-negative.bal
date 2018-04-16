public type kind "PLUS" | "MINUS" | "AND" | "OR" | "NOT" | "GREATER_THAN";

public kind not = "NOT";
public kind plus = "PLUS";
public kind minus = "MINUS";

function testIncompatibleTypes() {
    kind k = plus;

    if (k == 1) {
        int a = 0;
    }
}

function testUndefinedEnumerator() {
    kind k = "LESS_THAN";
}

function testInvalidEnumAccess() {
    kind k = minus;
    kind p = k.plus;
}

function testEnumAssignment() {
    kind k = minus;
    plus = kind.plus;
}


type state "foo" | "bar";
state foo = "foo";

function main(string[] arg) {
    state x = bar;
}


