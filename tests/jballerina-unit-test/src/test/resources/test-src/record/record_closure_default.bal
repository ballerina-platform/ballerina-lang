function recordWithClosureInDefaults() returns error? {
    final int x = 20;

    record {
        string name;
        int age = x;
    } person = { name: "Pubudu" };

    x = 25;

    assertEquality(20, person.age);

    var personType = typeof person;
    x = 26;
    check createUsingCloneWithType(personType);

}

function createUsingCloneWithType(typedesc<record { string name; int age; }> personType) returns error? {
    var rec = { name: "Manu" };
    var person2 = check rec.cloneWithType(personType);
    assertEquality(26, person2.age);
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
