import ballerina/test;
import ballerina/io;

(any|error)[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any|error... args) {
    string str = "";
    foreach var s in args {
        str += string.convert(s);
    }
    outputs[counter] = str;
    counter += 1;
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();
    test:assertEquals(outputs[1], "Created Time: 2017-03-28T23:42:45.554-05:00");
    test:assertEquals(outputs[2], "Parsed Time: 2017-06-26T09:46:22.444-05:00");

    string r1 = <string>outputs[15];
    test:assertTrue(r1.contains("After adding a duration: 20"));
    string r2 = <string>outputs[16];
    test:assertTrue(r2.contains("After subtracting a duration: 20"));
    string rs1 = <string>outputs[17];
    test:assertTrue(rs1.contains("Before converting the time zone: 201"));
    string rs2 = <string>outputs[18];
    test:assertTrue(rs2.contains("After converting the time zone: 20"));
}
