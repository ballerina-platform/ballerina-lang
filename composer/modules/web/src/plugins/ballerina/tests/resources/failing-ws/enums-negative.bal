enum kind {
    PLUS,
    MINUS,
    AND,
    OR,
    NOT,
    GREATER_THAN
}

function testIncompatibleTypes() {
    kind k = kind.PLUS;

    if (k == 1) {
        int a = 0;
    }


}

function testUndefinedEnumerator() {
    kind k = kind.LESS_THAN;
}

function testInvalidEnumAccess() {
    kind k = kind.MINUS;
    kind p = k.PLUS;
}

function testEnumAssignment() {
    kind k = kind.MINUS;
    kind.PLUS = kind.PLUS;
}

enum state {
    foo,
    bar
}

function main(string... arg) {
    state x = state;
    print(x);
}


