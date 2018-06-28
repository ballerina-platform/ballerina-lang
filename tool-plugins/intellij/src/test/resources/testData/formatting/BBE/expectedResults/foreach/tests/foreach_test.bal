import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function
@test:Mock {
    packageName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[counter] = s[0];
    counter++;
}

@test:Config {
    enable: false
}
function testFunc() {
    // Invoking the main function
    main();
    test:assertEquals("Iterating over a string array:-", outputs[0]);
    test:assertEquals("fruit: apple", outputs[1]);
    test:assertEquals("fruit: banana", outputs[2]);
    test:assertEquals("fruit: cherry", outputs[3]);

    test:assertEquals("letter: a, word: apple", outputs[5]);
    test:assertEquals("letter: b, word: banana", outputs[6]);
    test:assertEquals("letter: c, word: cherry", outputs[7]);

    //TODO : Add the rest of the assertions
}
