public function testFields() {
    final int varK = 20;
    var foo = object {
        int p1 = varK;
        function getI() returns int {
            return self.p1;
        }
    };

    var inty = foo.getI();
    assertValueEquality(20, inty);
}

public function testMultipleFields() {
    final int varK = 20;
    final int varL = 23;
    var foo = object {
        int p1 = varK;
        int p2 = varL;
        function getI() returns int {
            return self.p1 + self.p2;
        }
    };

    var inty = foo.getI();
    assertValueEquality(43, inty);
}

public function testMultipleFieldsAndArg() {
    final int varK = 20;
    final int varL = 23;
    var foo = object {
        int p1 = varK;
        int p2 = varL;
        function getI(int x) returns int {
            return self.p1 + self.p2 + x;
        }
    };

    var inty = foo.getI(7);
    assertValueEquality(50, inty);
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
