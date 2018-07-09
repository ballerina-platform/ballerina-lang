import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function that will replace the real function.
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
    // Invoking the main function.
    main();
    string op1 = "table<ballerina-examples/table:0.0.1:Employee> {index: [], primaryKey: [\"id\"],
    data: [{id:1, name:\"Mary\", salary:300.5}, {id:2, name:\"John\", salary:200.5},
    {id:3, name:\"Jim\", salary:330.5}]}";
    string op2 = "Adding record to table successful";
    string op3 = "Adding record to table successful";
    string op4 = "table<ballerina-examples/table:0.0.1:Employee> {index: [], primaryKey: [\"id\"],
    data: [{id:1, name:\"Jane\", salary:300.5}, {id:2, name:\"Anne\", salary:100.5},
    {id:3, name:\"John\", salary:400.5}, {id:4, name:\"Peter\", salary:150.0}]}";
    string op5 = "Using foreach: ";
    string op6 = "Name: Jane";
    string op7 = "Name: Anne";
    string op8 = "Name: John";
    string op9 = "Name: Peter";
    string op10 = "Using while loop: ";
    string op11 = "Average of Low salary: 125.25";
    string op12 = "Selected row count: 4";
    string op13 = "Deleted row count: 2";
    string js1 = "[{\"id\":1,\"name\":\"Jane\",\"salary\":300.5},{\"id\":3,\"name\":\"John\",\"salary\":400.5}]";
    xml xml1 = xml `<results><result><id>1</id><name>Jane</name><salary>300.5</salary></result><
    result><id>3</id><name>John</name><salary>400.5</salary></result></results>`;

    //test:assertEquals(op2, <string>outputs[0]);
    test:assertEquals(op2, <string>outputs[1]);
    test:assertEquals(op3, <string>outputs[2]);
    test:assertEquals(op5, <string>outputs[4]);
    test:assertEquals(op6, <string>outputs[5]);
    test:assertEquals(op7, <string>outputs[6]);
    test:assertEquals(op8, <string>outputs[7]);
    test:assertEquals(op9, <string>outputs[8]);
    test:assertEquals(op10, <string>outputs[9]);
    test:assertEquals(op6, <string>outputs[10]);
    test:assertEquals(op7, <string>outputs[11]);
    test:assertEquals(op8, <string>outputs[12]);
    test:assertEquals(op9, <string>outputs[13]);
    test:assertEquals(op11, <string>outputs[14]);
    test:assertEquals(op12, <string>outputs[15]);
    test:assertEquals(op13, <string>outputs[17]);
    test:assertEquals(js1, <string>outputs[19]);
    test:assertEquals(xml1, outputs[20]);
}
