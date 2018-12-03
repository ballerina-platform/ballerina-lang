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
    if (counter < 6) {
        outputs[counter] = <string> s[0] + <string> s[1] + <string> s[2] + <string> s[3] + <string> s[4];
    } else {
        outputs[counter] = <string> s[0] + <string> s[1];
    }
    counter += 1;
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();
    test:assertEquals(outputs[0], "1 == 1 is true");
    test:assertEquals(outputs[1], "1 == 2 is false");
    test:assertEquals(outputs[2], "(1, 1.0, false) == (1, 1.0, false) is true");
    test:assertEquals(outputs[3], "(1, 1.0, false) == (11, 1.0, true) is false");
    test:assertEquals(outputs[4], "{name:\"Jane\", id:1100} == {name:\"Jane\", id:1100} is true");
    test:assertEquals(outputs[5], "{name:\"Jane\", id:1100} == {name:\"Anne\", id:1100} is false");
    test:assertEquals(outputs[6], "e4 === e5 is true");
    test:assertEquals(outputs[7], "e4 === e5 is false");
    test:assertEquals(outputs[8], "f1 === f2 is true");
    test:assertEquals(outputs[9], "f1 === f2 is false");
}
