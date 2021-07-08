int i = 100;

function foo() {
    final int i = 10;
    object {
        int x;
        function bar(int b) returns int;
    } b = object {
        int x = i;
        function bar(int b) returns int {
            return b + self.x;
        }
    };
    var lambda = b.bar;
    int y = lambda(45);
    assertValueEquality(55, y);
}

type AssertionError distinct error;

const ASSERTION_ERROR_REASON = "AssertionError";

function assertValueEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
