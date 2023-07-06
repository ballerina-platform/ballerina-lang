final string|error notString = error("error!");

var x = object {
    string|error b = notString;

    function init() returns error? {}
};

public function testOCEDependOnGlobalVariable() {
    var y = checkpanic x;

    if (y.b is error) {
        assertTrue(true);
    } else {
        assertTrue(false);
    }

    if y.b is string {
        assertTrue(false);
    } else {
        assertTrue(true);
    }
}

type AssertionError distinct error;
const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(any|error actual) {
    if actual is boolean && actual {
        return;
    }
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON, message = "expected 'true', found '" + actualValAsString + "'");
}
