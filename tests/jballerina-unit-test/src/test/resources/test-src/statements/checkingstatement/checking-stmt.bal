function testInvocationExpr() {
    error? e = checkingFunc1();
    assertTrue(e is error && e.message() == "msg1");
    e = checkingFunc2();
    assertTrue(e is null);
    e = checkingFunc3();
    assertTrue(e is error && e.message() == "msg3");
}

function checkingFunc1() returns error? {
    check func1();
}

function func1() returns error? {
    return error("msg1");
}

function checkingFunc2() returns error? {
    check func2();
}

function func2() returns error? {
    return;
}

function checkingFunc3() returns error? {
    check func3();
}

function func3() returns error {
    return error("msg3");
}

function testNilLiteral() {
    check (); // no error
}

function testVariableReference() {
    () n = ();
    check n; // no error
    error? e1 = checkingFunc4();
    assertTrue(e1 is error && e1.message() == "msg4");
    error? e2 = checkingFunc5();
    assertTrue(e2 is ());
    error e3 = checkingFunc6();
    assertTrue(e3 is error && e3.message() == "msg6");
}

function checkingFunc4() returns error? {
    error? e = error("msg4");
    check e;
}

function checkingFunc5() returns error? {
    error? e = ();
    check e; // no error
}

function checkingFunc6() returns error {
    error e = error("msg6");
    check e;  
    return error("new msg");
}

type REC1 record {
    () n;
};

type REC2 record {
    error? e;
};

function testFieldAccessExpr() {
    REC1 rec = { n: () };
    check rec.n; // no error    
    error? e = checkingFunc7();
    assertTrue(e is error && e.message() == "msg7");
}

function checkingFunc7() returns error? {
    REC2 rec = { e: error("msg7") };
    check rec.e;
}

map<error> errMap = { "e1": error("msg8") };

function testMemberAccessExpr() {
    error? e = checkingFunc8();
    assertTrue(e is error && e.message() == "msg8");
}

function checkingFunc8() returns error? {
    check errMap["e2"];
    check errMap["e1"];
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(anydata actual) {
    assertEquality(true, actual);
}

function assertFalse(anydata actual) {
    assertEquality(false, actual);
}

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
