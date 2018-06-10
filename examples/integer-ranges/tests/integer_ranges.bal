import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function
@test:Mock {
    packageName: "ballerina.io",
    functionName: "println"
}
public function mockPrint(any... s) {
    string outstr;
    foreach str in s{
        outstr = outstr + <string>str;
    }
    outputs[counter] = outstr;
    counter++;
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();

    test:assertEquals("Iterating over an array created by a closed integer range", outputs[0]);
    test:assertEquals("25", outputs[1]);
    test:assertEquals("26", outputs[2]);
    test:assertEquals("27", outputs[3]);
    test:assertEquals("28", outputs[4]);
    test:assertEquals("29", outputs[5]);
    test:assertEquals("30", outputs[6]);
    test:assertEquals("\nIterating over an array created by a half open integer range", outputs[7]);
    test:assertEquals("25", outputs[8]);
    test:assertEquals("26", outputs[9]);
    test:assertEquals("27", outputs[10]);
    test:assertEquals("28", outputs[11]);
    test:assertEquals("29", outputs[12]);
    test:assertEquals("\nUsing an integer range in a foreach statement", outputs[13]);
    test:assertEquals("Index: 0, Value: fruit", outputs[14]);
    test:assertEquals("Index: 1, Value: tree", outputs[15]);
    test:assertEquals("Index: 2, Value: basket", outputs[16]);
}
