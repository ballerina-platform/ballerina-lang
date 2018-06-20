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
    outputs[counter] = <string>s[0];
    counter++;
}

@test:Config
function testFunc() {
    // Invoking the main function.
    main();

    string jt1 = "Apple";
    string jt2 = "5.36";
    string jt3 = "true";
    string jt4 = "false";
    string jt5 = "{\"name\":\"apple\",\"color\":\"red\",\"price\":5.36}";
    string jt6 = "[1,false,null,\"foo\",{\"first\":\"John\",\"last\":\"Pala\"}]";
    string jt7 = "{\"fname\":\"John\",\"lname\":\"Stallone\",\"age\":30}";
    string jt8 = "John";
    string jt9 = "Stallone";
    string jt10 = "{\"fname\":\"John\",\"lname\":\"Silva\",\"age\":31}";
    string jt11 = "{\"fname\":\"Peter\",\"lname\":\"Stallone\",\"age\":30,\"address\":{\"line\":\"20 Palm Grove\",\"city\":\"Colombo 03\",\"country\":\"Sri Lanka\"}}";
    string jt12 = "{\"fname\":\"Peter\",\"lname\":\"Stallone\",\"age\":30,\"address\":{\"line\":\"20 Palm Grove\",\"city\":\"Colombo 03\",\"country\":\"Sri Lanka\",\"province\":\"Western\"}}";

    test:assertEquals(jt1, outputs[0]);
    test:assertEquals(jt2, outputs[1]);
    test:assertEquals(jt3, outputs[2]);
    test:assertEquals(jt4, outputs[3]);
    test:assertEquals(jt5, outputs[4]);
    test:assertEquals(jt6, outputs[5]);
    test:assertEquals(jt7, outputs[6]);
    test:assertEquals(jt8, outputs[7]);
    test:assertEquals(jt9, outputs[8]);
    test:assertEquals(jt10, outputs[9]);
    test:assertEquals(jt11, outputs[10]);
    test:assertEquals(jt12, outputs[11]);
}

