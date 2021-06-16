string output = "";

function concatIntString(int i, string s) {
    output = output + i.toString() + ":" + s + " ";
}

function concatIntAny(int i, any a) {
    output = output + i.toString() + ":" + a.toString() + " ";
}

// ---------------------------------------------------------------------------------------------------------------------

function testStringWithSimpleVariableWithStringType() {
    output = "";
    string expected = "0:B 1:a 2:l 3:l 4:e 5:r 6:i 7:n 8:a ";
    string sdata = "Ballerina";

    int i = 0;
    foreach string s in sdata {
        concatIntString(i, s);
        i += 1;
    }
    assertEquals(expected, output);
}

// ---------------------------------------------------------------------------------------------------------------------

function testStringWithSimpleVariableWithCharType() {
    output = "";
    string expected = "0:B 1:a 2:l 3:l 4:e 5:r 6:i 7:n 8:a ";
    string sdata = "Ballerina";

    int i = 0;
    foreach string:Char s in sdata {
        concatIntString(i, s);
        i += 1;
    }
    assertEquals(expected, output);
}

function testStringWithSimpleVariableWithoutType() returns string {
    output = "";

    string sdata = "Ballerina";

    int i = 0;
    foreach var s in sdata {
        concatIntString(i, s);
        i += 1;
    }
    return output;
}

function testStringWithSimpleVariableWithType() returns string {
    output = "";

    string sdata = "Ballerina";

    int i = 0;
    foreach string s in sdata {
        concatIntString(i, s);
        i += 1;
    }
    return output;
}

// ---------------------------------------------------------------------------------------------------------------------

function testStringWithSimpleVariableWithAnydataType() returns string {
    output = "";

    string sdata = "Ballerina";

    int i = 0;
    foreach anydata s in sdata {
        concatIntAny(i, s);
        i += 1;
    }
    return output;
}

// ---------------------------------------------------------------------------------------------------------------------

function testStringWithSimpleVariableWithAnyType() returns string {
    output = "";

    string sdata = "Ballerina";

    int i = 0;
    foreach any s in sdata {
        concatIntAny(i, s);
        i += 1;
    }
    return output;
}


// ---------------------------------------------------------------------------------------------------------------------

function testIterationOnEmptyString() returns string {
    output = "";

    string sdata = "";

    int i = 0;
    foreach any s in sdata {
        concatIntAny(i, s);
        i += 1;
    }
    return output;
}

// ---------------------------------------------------------------------------------------------------------------------

function testIterationTypeCheck() {
    string foo = "foo";

    foreach var item in foo {
        string str = item;
        assertTrue(str is string:Char);
    }

}

// ---------------------------------------------------------------------------------------------------------------------

const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(boolean actual) {
    assertEquals(true, actual);
}

function assertEquals(anydata expected, anydata actual) {
    if (expected == actual) {
        return;
    }
    typedesc<anydata> expT = typeof expected;
    typedesc<anydata> actT = typeof actual;
    string msg = "expected [" + expected.toString() + "] of type [" + expT.toString()
                            + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
    panic error(ASSERTION_ERROR_REASON, message = msg);
}