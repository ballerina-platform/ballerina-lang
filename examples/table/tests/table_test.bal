import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function that will replace the real function.
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[counter] = s[0];
    counter += 1;
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
    string js1 = "[{\"id\":1, \"name\":\"Jane\", \"salary\":300.5}, {\"id\":3, \"name\":\"John\", \"salary\":400.5}]";
    xml xml1 = xml `<results><result><id>1</id><name>Jane</name><salary>300.5</salary></result><
    result><id>3</id><name>John</name><salary>400.5</salary></result></results>`;

    //test:assertEquals(<string>outputs[0], op2);
    test:assertEquals(<string>outputs[1], op2);
    test:assertEquals(<string>outputs[2], op3);
    test:assertEquals(<string>outputs[4], op5);
    test:assertEquals(<string>outputs[5], op6);
    test:assertEquals(<string>outputs[6], op7);
    test:assertEquals(<string>outputs[7], op8);
    test:assertEquals(<string>outputs[8], op9);
    test:assertEquals(<string>outputs[9], op10);
    test:assertEquals(<string>outputs[10], op6);
    test:assertEquals(<string>outputs[11], op7);
    test:assertEquals(<string>outputs[12], op8);
    test:assertEquals(<string>outputs[13], op9);
    test:assertEquals(<string>outputs[14], op11);
    test:assertEquals(<string>outputs[15], op12);
    test:assertEquals(<string>outputs[17], op13);
    test:assertEquals(<string>outputs[19], js1);
    test:assertEquals(outputs[20], xml1);
}
