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

    map map1 = { line1: "No. 20", line2: "Palm Grove", city: "Colombo 03", country: "Sri Lanka" };
    string out1 = "Sri Lanka";
    string out2 = "Colombo 03";
    map map2 = { "line1": "No. 20", "line2": "Palm Grove", "city": "Colombo 03", "country": "Sri Lanka" };
    string[] map3 = ["line1", "line2", "city", "country", "postalCode"];
    int out3 = 5;
    map map4 = { "line1": "No. 20", "line2": "Palm Grove", "city": "Colombo 03", "country": "Sri Lanka" };
    string index = "100892N";

    test:assertEquals(outputs[0], map1);
    test:assertEquals(outputs[1], out1);
    test:assertEquals(outputs[2], out2);
    test:assertEquals(outputs[3], map2);
    test:assertEquals(outputs[4], map3);
    test:assertEquals(outputs[5], out3);
    test:assertEquals(outputs[6], map4);
    test:assertEquals(outputs[7], index);
    test:assertEquals(outputs[8], index);
}
