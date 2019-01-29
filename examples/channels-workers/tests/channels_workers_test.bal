import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function
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
    // Invoking the main function
    main();
    string[] expected = [];
    expected[0] = "w1 Waiting for a message...";
    expected[1] = "Sent message from w2";
    expected[2] = "w1 Waiting for a null key message...";
    expected[3] = "Sent message from w3";
    expected[4] = "Received ";
    foreach var k in 0...4 {
        test:assertTrue(testExist(expected[k]), msg = expected[k]);
    }
}

function testExist(string text) returns boolean {
    foreach var i in 0...5 {
        string out = string.convert(outputs[i]);
        if (out.contains(text)) {
            return true;
        }
    }

    return false;
}
