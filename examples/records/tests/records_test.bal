import ballerina/test;
import ballerina/io;

string[] outputs = [];
int counter = 0;

// This is the mock function that replaces the real function.
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[counter] = <string>s[0];
    counter += 1;
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();
    // final state of p1 has changed. hence following assertion
    test:assertEquals(outputs[0], "{name:\"\", age:0, grades:{maths:0, physics:0, chemistry:0}}");
    // final state of p2 has changed. hence following assertion
    test:assertEquals(outputs[1],
        "{name:\"John Doe\", age:17, grades:{maths:80, physics:75, chemistry:65}}");
    test:assertEquals(outputs[2], "John Doe");
    test:assertEquals(outputs[3], "John Doe");
    test:assertEquals(outputs[4], "80");
    test:assertEquals(outputs[5], "{name:\"Peter\", age:19, grades:{maths:0, physics:0, chemistry:0}}");
    test:assertEquals(outputs[6], "{name:\"John Doe\", age:17, grades:{maths:80, physics:75, chemistry:65}}");
    test:assertEquals(outputs[7], "{name:\"Peter\", age:19, grades:{maths:0, physics:0, chemistry:0}, department:\"Computer Science\"}");
    test:assertEquals(outputs[8], "maths : 80");
    test:assertEquals(outputs[9], "physics : 75");
    test:assertEquals(outputs[10], "chemistry : 65");
    test:assertEquals(outputs[11], "Average grade: 73.33333333333333");
    test:assertEquals(outputs[12], "{\"maths\":\"A\", \"physics\":\"B\"}");
    test:assertEquals(outputs[13], "Average grade using iterable ops: 73.33333333333333");
}
