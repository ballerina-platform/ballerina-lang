int[] data = [1, -3, 5, -30, 4, 11, 25, 10];
int sum = 0;
int negSum = 0;
string output = "";

function add(int i) {
    sum = sum + i;
}

function addNeg(int i) {
    negSum = negSum + i;
}

function concatInt(int index, int value) {
    output = output + index.toString() + ":" + value.toString()  + " ";
}

function concatFloat(int index, float value) {
    output = output + index.toString() + ":" + value.toString() + " ";
}

function concatString(int index, string value) {
    output = output + index.toString() + ":" + value + " ";
}

function concatBoolean(int index, boolean value) {
    output = output + index.toString() + ":" + value.toString() + " ";
}

type person record {
    string name;
    int age;
};

function concatPerson(int index, person value) {
    output = output + index.toString() + ":" + "name=" +  value.name + ",age=" + value.age.toString() + " ";
}

function testIntArrayWithArityOne() returns int {
    sum = 0;
    foreach var i in data {
        add(i);
    }
    return sum;
}

function testIntArrayWithArityTwo() returns string {
    int[] ldata = [1, -3, 5, -30, 4, 11, 25, 10];
    output = "";
    int i = 0;
    foreach var v in ldata {
        concatInt(i, v);
        i += 1;
    }
    return output;
}


function testFloatArrayWithArityOne() returns string {
    float[] fdata = [1.123, -3.35244, 5.23, -30.45, 4.32, 11.56, 25.967, 10.345];
    output = "";
    foreach var v in fdata {
        concatFloat(0, v);
    }
    return output;
}

function testFloatArrayWithArityTwo() returns string {
    float[] fdata = [1.123, -3.35244, 5.23, -30.45, 4.32, 11.56, 25.967, 10.345];
    output = "";
    int i = 0;
    foreach var v in fdata {
        concatFloat(i, v);
        i += 1;
    }
    return output;
}

function testStringArrayWithArityOne() returns string {
    string[] sdata = ["foo", "bar", "bax" , "baz"];
    output = "";
    foreach var v in sdata {
        concatString(0, v);
    }
    return output;
}

function testStringArrayWithArityTwo() returns string {
    string[] sdata = ["foo", "bar", "bax" , "baz"];
    output = "";
    int i = 0;
    foreach var v in sdata {
        concatString(i, v);
        i += 1;
    }
    return output;
}

function testBooleanArrayWithArityOne() returns string {
    boolean[] bdata = [true, false, false, false, true, false];
    output = "";
    foreach var v in bdata {
        concatBoolean(0, v);
    }
    return output;
}

function testBooleanArrayWithArityTwo() returns string {
    boolean[] bdata = [true, false, false, false, true, false];
    output = "";
    int i = 0;
    foreach var v in bdata {
        concatBoolean(i, v);
        i += 1;
    }
    return output;
}

function testStructArrayWithArityOne() returns string {
    person bob = { name : "bob", age : 10};
    person tom = { name : "tom", age : 16};
    person[] tdata = [bob, tom];
    output = "";
    foreach var v in tdata {
        concatPerson(0, v);
    }
    return output;
}

function testStructArrayWithArityTwo() returns string {
    person bob = { name : "bob", age : 10};
    person tom = { name : "tom", age : 16};
    person[] tdata = [bob, tom];
    output = "";
    int i = 0;
    foreach var v in tdata {
        concatPerson(i, v);
        i += 1;
    }
    return output;
}

function testArrayInsertInt() returns string {
    int[] iArray = [0];
    iArray[3] = 3;
    iArray[6] = 6;
    output = "";
    int i = 0;
    foreach var v in iArray {
        concatInt(i, v);
        i += 1;
    }
    return output;
}

function testArrayInsertString() returns string {
    string[] sArray = ["d0"];
    sArray[3] = "d3";
    sArray[6] = "d6";
    output = "";
    int i = 0;
    foreach var v in sArray {
        concatString(i, v);
        i += 1;
    }
    return output;
}

function testArrayInsertInForeach() returns string {
    string[] sArray = ["d0", "d1", "d2"];
    output = "";
    int i = 0;
    foreach var v in sArray {
        int j = i + 5;
        sArray[j] = "d" + j.toString();
        concatString(i, v);
        i += 1;
    }
    return output;
}

function testBreak() returns string {
    string[] sArray = ["d0", "d1", "d2"];
    output = "";
    int i = 0;
    foreach var v in sArray {
        if (i == 1) {
            output = output + "break";
            break;
        }
        concatString(i, v);
        i += 1;
    }
    return output;
}

function testContinue() returns string {
    string[] sArray = ["d0", "d1", "d2"];
    output = "";
    int i = 0;
    foreach var v in sArray {
        if (i == 1) {
            output = output + "continue ";
            i += 1;
            continue;
        }
        concatString(i, v);
        i += 1;
    }
    return output;
}

function testReturn() returns string {
    string[] sArray = ["d0", "d1", "d2"];
    output = "";
    int i = 0;
    foreach var v in sArray {
        if (v == "d1") {
            return output;
        }
        concatString(i, v);
        i += 1;
    }
    return output;
}

function testArrayWithNullElements() returns string {
    output = "";
    string?[] sArray = ["d0", (), "d2", ()];
    int i = 0;
    foreach var v in sArray {
        if v is string {
            output = output + i.toString() + ":" + v.toString() + " ";
        } else {
           output = output + i.toString() + ": ";
        }
        i += 1;
    }
    return output;
}

function testEmptyArray() {
    output = "hello";
    foreach var item in [] {
        output += "hello";
    }
    assertEquality(output, "hello");
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
