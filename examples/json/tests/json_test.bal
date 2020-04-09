import ballerina/test;

(any|error)[] outputs = [];

// This is the mock function that will replace the real function.
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any|error... s) {
    outputs.push(s[0]);
}

@test:Config {}
function testFunc() {
    // Invoking the main function.
    main();
    string jt1 = "Apple";
    float jt2 = 5.36;
    boolean jt3 = true;
    boolean jt4 = false;
    json jt13= {name: "apple", color: "red", price: jt2};
    string jt5 = "{\"name\":\"apple\", \"color\":\"red\", \"price\":5.36}";
    string jt6 = "[1, false, null, \"foo\", {\"first\":\"John\", \"last\":\"Pala\"}]";
    string jt7 = "{\"name\":\"Anne\", \"age\":20, \"marks\":{\"math\":90, \"language\":95, \"physics\":85}}";
    error jt9 = error("{ballerina/lang.value}MergeJsonError", message="Cannot merge JSON values of types 'float' and 'boolean'");

    test:assertEquals(outputs[0], jt1);
    test:assertEquals(outputs[1], jt2);
    test:assertEquals(outputs[2], jt3);
    test:assertEquals(outputs[3], jt4);
    test:assertEquals(outputs[4], jt13);
    test:assertEquals(outputs[5], jt5);
    test:assertEquals(outputs[6], jt6);
    test:assertEquals(outputs[7], jt6);
    test:assertEquals(outputs[8], jt7);
    test:assertEquals(outputs[9], jt3);
    test:assertEquals(outputs[10], jt9);
    test:assertEquals(outputs[11], jt5);
    test:assertEquals(outputs[12], jt3);

}

