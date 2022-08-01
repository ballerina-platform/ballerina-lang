public function testAttachedFunction() {
    var foo = object {
        function getI() returns function () returns int {
            int p1 = 300;
            int p2 = 200;
            var func = function() returns int => p1 + p2;
            return func;
        }
    };

    var funcy = foo.getI();
    assertValueEquality(500, funcy());
}

public function testAttachedFunctionWithArgs() {
    var foo = object {
        function getI(int x) returns function () returns int {
            int p1 = 200;
            var func = function() returns int => p1 + x;
            return func;
        }
    };

    var funcy = foo.getI(23);
    assertValueEquality(223, funcy());
}

public function testAttachedFunctionWithMultipleArgs() {
    var foo = object {
        function getI(int x, int y) returns function () returns int {
            int p1 = 300;
            int p2 = 200;
            var func = function() returns int => p1 + p2 + x + y;
            return func;
        }
    };

    var funcy = foo.getI(500, 23);
    assertValueEquality(1023, funcy());
}

public function testAttachedFunctionReturningClosureWithArgs() {
    var foo = object {
        function getI(int x, int y) returns function (int) returns int {
            int p1 = 300;
            int p2 = 200;
            var func = function(int z) returns int => p1 + p2 + x + y + z;
            return func;
        }
    };

    var funcy = foo.getI(500, 23);
    assertValueEquality(1030, funcy(7));
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
