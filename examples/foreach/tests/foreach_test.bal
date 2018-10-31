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
    outputs[counter] = s[0];
    counter += 1;
}

@test:Config {
    enable: false
}
function testFunc() {
    // Invoking the main function
    main();
    test:assertEquals(outputs[0], "Iterating over a string array:-");
    test:assertEquals(outputs[1], "fruit: apple");
    test:assertEquals(outputs[2], "fruit: banana");
    test:assertEquals(outputs[3], "fruit: cherry");

    test:assertEquals(outputs[5], "letter: a, word: apple");
    test:assertEquals(outputs[6], "letter: b, word: banana");
    test:assertEquals(outputs[7], "letter: c, word: cherry");

    //TODO : Add the rest of the assertions
}
