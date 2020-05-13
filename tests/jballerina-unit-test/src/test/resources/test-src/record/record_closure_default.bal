function recordWithClosureInDefaults(){
    int x = 20;

    record {
        string name;
        int age = x;
    } person = { name: "Pubudu" };

    x = 25;

    assertEquality(20, person.age);
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
