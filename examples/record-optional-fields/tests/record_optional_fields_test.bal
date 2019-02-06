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
    test:assertEquals(outputs[0], "Person with non-defaultable required field set: {lname:\"\", fname:\"John\", gender:\"male\"}");
    test:assertEquals(outputs[1], "Age: 25");
    test:assertEquals(outputs[2], "Updated person with optional field set: {lname:\"\", fname:\"John\", gender:\"male\", age:25}");
    test:assertEquals(outputs[3], "Person with values assigned to required fields: {lname:\"Doe\", fname:\"Jane\", gender:\"female\"}");
}
