public function testFieldsAndArgs() {
    final int varK = 20;

    var foo = object {
        int p0 = varK;
        function getI(int x, int y) returns function () returns int {
            int p1 = 300;
            int p2 = 200;
            var func = function() returns int {
                return p1 + p2 + x + y + self.p0;
            };
            return func;
        }
    };

    var funcy = foo.getI(500, 23);
    int res = 300 + 200 + 500 + 23 + 20;
    assertValueEquality(res, funcy());
}

final int j = 65;

public function testMultipleFields() {
    final int varK = 20;
    final int varJ = 27 + j;

    var foo = object {
        int p0 = varK;
        int p3 = varJ;
        function getI(int x, int y) returns function () returns int {
            int p1 = 300;
            int p2 = 200;
            var func = function() returns int => self.p3 + p1 + p2 + x + y + self.p0;
            return func;
        }
    };

    var funcy = foo.getI(500, 23);
    int res = (27 + 65) + 300 + 200 + 500 + 23 + 20;
    assertValueEquality(res, funcy());
}

public function testMultipleExpressionFieldsAndArg() {
    final int varK = 20;
    final int varJ = 27 + j;

    var foo = object {
        int p0 = varK;
        int p3 = varJ + j;
        function getI(int x, int y) returns function () returns int {
            int p1 = 300;
            int p2 = 200;
            var func = function() returns int => self.p3 + p1 + p2 + x + y + self.p0;
            return func;
        }
    };

    var funcy = foo.getI(500, 23);
    int res = (27 + 65) + 300 + 200 + 500 + 23 + 20;
    assertValueEquality(res, funcy());
}

const ASSERTION_ERROR_REASON = "AssertionError";

isolated function isEqual(anydata|error actual, anydata|error expected) returns boolean {
    if (actual is anydata && expected is anydata) {
        return (actual == expected);
    } else {
        return (actual === expected);
    }
}

function assertValueEquality(anydata|error expected, anydata|error actual) {
    if isEqual(actual, expected) {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
