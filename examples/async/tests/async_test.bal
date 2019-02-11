import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function that replaces the real function.
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}

public function mockPrint(any... s) {
    lock{
        outputs[counter] = s[0];
        counter += 1;
    }
}

@test:Config
function testFunc() {
    // Invoke the main function.
    main();
    string[] expected = [];
    expected[1] = "false";
    expected[2] = "false";
    expected[3] = "true";
    expected[5] = "false";
    expected[6] = "123";
    expected[7] = "true";
    expected[8] = "400";
    expected[9] = "{\"first_field\":100, \"second_field\":27, \"third_field\":\"Hello Moose!!\"}";
    expected[10] = "first field of record --> 100";
    expected[11] = "second field of record --> 27";
    expected[12] = "third field of record --> Hello Moose!!";
    foreach var k in 1...12 {
        if (k == 4) {
            continue;
        }
        test:assertTrue(testExist(expected[k]), msg = expected[k]);
    }
}

function testExist(string text) returns boolean {
    foreach var i in 1...12 {
        if (i == 4) {
            continue;
        }
        string out = string.convert(outputs[i]);
        if (out.contains(text)) {
            return true;
        }
    }
    return false;
}
