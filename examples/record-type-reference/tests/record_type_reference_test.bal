import ballerina/test;
import ballerina/io;

string[] outputs = [];
int counter = 0;

@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[counter] = "";
    foreach var v in s {
        outputs[counter] += string.convert(v);
    }
    counter += 1;
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();
    test:assertEquals(outputs[0], "{age:25, name:\"John Doe\", designation:\"Software Engineer\"}");
    test:assertEquals(outputs[1], "{age:25, name:\"Jane Doe\", designation:\"UX Engineer\"}");
    test:assertEquals(outputs[2], "{age:35, name:\"Mark\", designation:\"Engineering Manager\", " +
        "team:[{age:25, name:\"John Doe\", designation:\"Software Engineer\"}, " +
        "{age:25, name:\"Jane Doe\", designation:\"UX Engineer\"}], company:\"XYZ Inc.\"}");
}
