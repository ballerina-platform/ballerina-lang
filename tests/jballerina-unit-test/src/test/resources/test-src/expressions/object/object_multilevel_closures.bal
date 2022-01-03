public function closureArrayPush() {

    int[] i = [];

    Foo f = object {
        function foo() returns BAZ {
            BAZ j = object {
                function add(int x) {
                    i.push(x);
                }
                function get() returns int {
                    return i.pop();
                }
            };
            return j;
        }
    };
    BAZ j = f.foo();
    j.add(10);
    assertValueEquality(j.get(), 10);
}

type BAZ object {
    function add(int i);
    function get() returns int;
};

type Foo object {
    function foo() returns BAZ;
};

type AssertionError distinct error;
const ASSERTION_ERROR_REASON = "AssertionError";

function assertValueEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
