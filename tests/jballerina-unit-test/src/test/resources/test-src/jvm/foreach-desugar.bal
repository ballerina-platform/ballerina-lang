function test() {
    int sum = 0;
    foreach int i in 0...4 {
        sum += i;
    }
    assertEquality(sum, 10);
}

function test2() {
    int sum = 0;
    foreach int i in 0..<4 {
        sum += i;
    }
    assertEquality(sum, 6);
}

function test3() {
    int sum = 0;
    foreach int i in 0..<4 {
        sum += i;
    }

    foreach int i in 0...4 {
        sum += i;
    }
    assertEquality(sum, 16);
}

function testWithControlFlow() {
    int sum = 0;
    foreach int i in 0...10 {
        if i > 5 {
            break;
        }
        if i % 2 != 0 {
            continue;
        }
        sum += i;
    }
    assertEquality(sum, 6);
}

function foo() {
    int[] vals = [1, 2, 3];
    int sum = 0;
    int i = 0;
    while i < vals.length() {
        int val = vals[i];
        sum += val;
    }
    assertEquality(sum, 6);
}

function testIterOnArray() {
    int[] vals = [1, 2, 3];
    int sum = 0;
    foreach int val in vals {
        sum += val;
    }
    assertEquality(sum, 6);
}


function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error("AssertionError",
                message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
