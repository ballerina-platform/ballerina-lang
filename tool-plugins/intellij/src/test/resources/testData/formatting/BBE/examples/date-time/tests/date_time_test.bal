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

@test:Config
function testFunc() {
    // Invoking the main function
    main();
    test:assertEquals("Created Time: 2017-03-28T23:42:45.554-05:00", outputs[1]);
    test:assertEquals("Parsed Time: 2017-06-26T09:46:22.444-05:00", outputs[2]);

    string r1 = <string>outputs[15];
    test:assertTrue(r1.contains("After add duration: 20"));
    string r2 = <string>outputs[16];
    test:assertTrue(r2.contains("After subtract duration: 20"));
    string rs1 = <string>outputs[17];
    test:assertTrue(rs1.contains("Before convert zone: 201"));
    string rs2 = <string>outputs[18];
    test:assertTrue(rs2.contains("After convert zone:20"));
}
