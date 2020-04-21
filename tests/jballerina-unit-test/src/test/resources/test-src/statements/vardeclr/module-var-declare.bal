int i;
string s;
int a;
int b;
error er;

const ERROR_REASON = "Error Reason";
const ASSERTION_ERROR_REASON = "AssertionError";

function __init() {
    i = 10;
    s = "Test string";
    int x = 2;
    a = x + 10;
    b = 31 + foo();
    er = error(ERROR_REASON, message = "error message");
}

function foo() returns int {
    return 1;
}

function testModuleVarDeclaration() {
    if (i == 10 && s == "Test string" && a == 12 && b == 32) {
        return;
    }

    string? msg = er.detail()?.message;
    if (msg is string && <string> msg == "error message") {
        return;
    }

    panic error(ASSERTION_ERROR_REASON, message = "expected 'true', found 'false'");
}