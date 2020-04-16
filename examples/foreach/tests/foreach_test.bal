import ballerina/test;

any[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    string printString = "";
    foreach var val in s {
        printString += val.toString();
    }
    outputs[counter] = printString;
    counter += 1;
}

@test:Config {}
function testFunc() {
    // Invoking the main function
    main();
    test:assertEquals(outputs[0], "Iterating a string array :");
    test:assertEquals(outputs[1], "fruit: apple");
    test:assertEquals(outputs[2], "fruit: banana");
    test:assertEquals(outputs[3], "fruit: cherry");

    test:assertEquals(outputs[4], "\nIterating a map :");
    test:assertEquals(outputs[5], "apple");
    test:assertEquals(outputs[6], "banana");
    test:assertEquals(outputs[7], "cherry");
    test:assertEquals(outputs[8], "letter: a, word: apple");
    test:assertEquals(outputs[9], "letter: b, word: banana");
    test:assertEquals(outputs[10], "letter: c, word: cherry");

    test:assertEquals(outputs[11], "\nIterating a JSON object :");
    test:assertEquals(outputs[12], "string value: apple");
    test:assertEquals(outputs[13], "json array value: red green");
    test:assertEquals(outputs[14], "int value: 5");

    test:assertEquals(outputs[15], "\nIterating a JSON array :");
    test:assertEquals(outputs[16], "color 0: red");
    test:assertEquals(outputs[17], "color 1: green");

    test:assertEquals(outputs[18], "\nIterating XML :");
    test:assertEquals(outputs[19], "xml at 0: <name>Sherlock Holmes</name>");
    test:assertEquals(outputs[20], "xml at 1: <author>Sir Arthur Conan Doyle</author>");

    test:assertEquals(outputs[21], "\nIterating a closed integer range :");
    test:assertEquals(outputs[22], "summation from 1 to 10 is 55");

    test:assertEquals(outputs[23], "\nIterating a half open integer range :");
    test:assertEquals(outputs[24], "summation from 1 to 10 excluding 10 is 45");
}
