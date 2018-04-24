import ballerina/test;
import ballerina/io;
import ballerina/os;

any[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function
@test:Mock {
    packageName: "ballerina.io",
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
    // TODO : Enable when runcommand support is available.
    //var x = os:runCommand("pwd");
    //string assertString = "File exists in " + x + "./tmp/src/test.txt";
    //test:assertEquals(assertString, outputs[0]);
    test:assertEquals("Iterating through directory content", outputs[1]);
    test:assertEquals("./tmp/dst", outputs[2]);
    test:assertEquals("./tmp/src", outputs[3]);
    var a = <string>outputs[4];
    test:assertTrue(a.contains("File Details, test.txt"));
    test:assertEquals("Folder deleted successfully", outputs[5]);
}
