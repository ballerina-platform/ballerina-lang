function testOnFailEdgeTestcases() {
    testUnreachableCodeWithIf();
    testMultiLevelOnFail();
}

function testUnreachableCodeWithIf(){
    int i = 0;
    string str = "";

    while (i < 3) {
        if(i == 2) {
            str += " -> Before error thrown";
            fail getError();
        }

        str += " -> Inside loop: " + i.toString() + ",";
        i = i + 1;
    } on fail error e {
        str += " -> Error caught!";
    }

    assertEquality(" -> Inside loop: 0, -> Inside loop: 1, -> Before error thrown -> Error caught!", str);
}

function testMultiLevelOnFail() {
    int i = 0;
    string str = "";

    while (i < 2) {
        do {
            str += " -> Before error thrown, ";
            fail getError();
        } on fail error e {
            str += " -> Error caught at level #1";
        }

        i = i + 1;
    } on fail error e {
        str += " -> Error caught at levet #2";
    }

    assertEquality(" -> Before error thrown,  -> Error caught at level #1 -> Before error thrown,  -> Error caught at level #1", str);
}

function getError() returns error {
    error err = error("Custom Error");
    return err;
}

//-------------------------------------------------------------------------------

type AssertionError error;

function assertTrue(any|error actual) {
    assertEquality(true, actual);
}

function assertFalse(any|error actual) {
    assertEquality(false, actual);
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic AssertionError("AssertionError", message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
