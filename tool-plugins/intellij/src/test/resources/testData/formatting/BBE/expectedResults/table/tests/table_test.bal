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
    string op1 = "{data: [{id:1, name:\"Jane\", salary:300.5}, {id:2, name:\"Anne\", salary:100.5}, {id:3,name:\"John\", salary:400.5}, {id:4, name:\"Peter\", salary:150.0}]}";

    string op2 = "Name: Jane";
    string op3 = "Name: Anne";
    string op4 = "Name: John";
    string op5 = "Name: Peter";
    string op6 = "Average of Low salary:125.25";
    string op7 = "Deleted row count:2";
    string js1 = "[{\"id\":1,\"name\":\"Jane\",\"salary\":300.5},{\"id\":3,\"name\":\"John\",\"salary\":400.5}]";
    xml xml1 = xml `<results><result><id>1</id><name>Jane</name><salary>300.5</salary></result><result><id>3</id><name>
    John</name><salary>400.5</salary></result></results>`;

    //test:assertEquals(op2, <string>outputs[0]);
    test:assertEquals(op2, <string>outputs[1]);
    test:assertEquals(op3, <string>outputs[2]);
    test:assertEquals(op4, <string>outputs[3]);
    test:assertEquals(op5, <string>outputs[4]);
    test:assertEquals(op6, <string>outputs[5]);
    test:assertEquals(op7, <string>outputs[6]);
    test:assertEquals(js1, <string>outputs[8]);
    test:assertEquals(xml1, outputs[9]);
}
