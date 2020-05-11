import ballerina/test;
import ballerina/io;

string[] outputs = [];
int counter = 0;

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
    main();
    test:assertEquals(outputs[0], "maths : 80");
    test:assertEquals(outputs[1], "physics : 75");
    test:assertEquals(outputs[2], "chemistry : 65");
    test:assertEquals(outputs[3], "Average grade: 73.33333333333333");
    test:assertEquals(outputs[4], "Mapped and filtered letter grades: {\"maths\":\"A\", \"physics\":\"B\"}");
    test:assertEquals(outputs[5], "Average grade using iterable ops: 73.33333333333333");
}
