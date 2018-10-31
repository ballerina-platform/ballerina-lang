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
    outputs[counter] = <string>s[0];
    counter += 1;
}

@test:Config
function testFunc() {
    // Invoking the main function.
    main();

    string jt1 = "Apple";
    string jt2 = "5.36";
    string jt3 = "true";
    string jt4 = "false";
    string jt5 = "{\"name\":\"apple\", \"color\":\"red\", \"price\":5.36}";
    string jt6 = "[1, false, null, \"foo\", {\"first\":\"John\", \"last\":\"Pala\"}]";
    string jt7 = "{\"fname\":\"John\", \"lname\":\"Stallone\", \"age\":30}";
    string jt8 = "John";
    string jt9 = "Stallone";
    string jt10 = "{\"fname\":\"John\", \"lname\":\"Silva\", \"age\":31}";
    string jt11 = "{\"fname\":\"Peter\", \"lname\":\"Stallone\", \"age\":30, \"address\":{\"line\":\"20 Palm Grove\", \"city\":\"Colombo 03\", \"country\":\"Sri Lanka\"}}";
    string jt12 = "{\"fname\":\"Peter\", \"lname\":\"Stallone\", \"age\":30, \"address\":{\"line\":\"20 Palm Grove\", \"city\":\"Colombo 03\", \"country\":\"Sri Lanka\", \"province\":\"Western\"}}";

    test:assertEquals(outputs[0], jt1);
    test:assertEquals(outputs[1], jt2);
    test:assertEquals(outputs[2], jt3);
    test:assertEquals(outputs[3], jt4);
    test:assertEquals(outputs[4], jt5);
    test:assertEquals(outputs[5], jt6);
    test:assertEquals(outputs[6], jt7);
    test:assertEquals(outputs[7], jt8);
    test:assertEquals(outputs[8], jt9);
    test:assertEquals(outputs[9], jt10);
    test:assertEquals(outputs[10], jt11);
    test:assertEquals(outputs[11], jt12);
}

