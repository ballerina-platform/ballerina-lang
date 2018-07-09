import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function that will replace the real function.
@test:Mock {
    packageName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[counter] = s[0];
    counter++;
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();
    // final state of p1 has changed. hence following assertion
    test:assertEquals(<string>outputs[0], "{name:\"Peter\", age:35, parent:null, status:\"\", occupation:\"Ballerina Developer\"}");
    // final state of p2 has changed. hence following assertion
    test:assertEquals(<string>outputs[1],
        "{name:\"Jack\", age:10, parent:{name:\"Peter\", age:35, parent:null, status:\"\", occupation:\"Ballerina Developer\"}, status:\"\"}");
    test:assertEquals(<string>outputs[2], "Jack");
    test:assertEquals(<string>outputs[3], "Jack");
    test:assertEquals(outputs[4], 0);
    test:assertEquals(<string>outputs[5], "{name:\"Peter\", age:35, parent:null, status:\"\", occupation:\"Ballerina Developer\"}");
    test:assertEquals(<string>outputs[6],
        "{name:\"Jack\", age:10, parent:{name:\"Peter\", age:35, parent:null, status:\"\", occupation:\"Ballerina Developer\"}, status:\"\"}");
}
