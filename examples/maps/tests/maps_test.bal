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

    test:assertEquals(map1, outputs[0]);
    test:assertEquals(out1, outputs[1]);
    test:assertEquals(out2, outputs[2]);
    test:assertEquals(map2, outputs[3]);
    test:assertEquals(map3, outputs[4]);
    test:assertEquals(out3, outputs[5]);
    test:assertEquals(map4, outputs[6]);
    test:assertEquals(index, outputs[7]);
}
