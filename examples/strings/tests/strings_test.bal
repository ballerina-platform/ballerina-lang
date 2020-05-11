import ballerina/test;

(string|error)[] outputs = [];
int counter = 0;
// This is the mock function which will replace the real function
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any|error... s) {
       string outstr = "";
       foreach var str in s {
           outstr = outstr + str.toString();
       }
       outputs[counter] = outstr;
       counter += 1;
}

@test:Config {}
function testFunc() {
    // Invoking the main function
    main();
    test:assertEquals(outputs[0], "ToUpper: LION IN TOWN. CATCH THE LION");
    test:assertEquals(outputs[1], "ToLower: lion in town. catch the lion");
    test:assertEquals(outputs[2], "SubString: Lion");
    test:assertEquals(outputs[3], "IndexOf: 2");
    test:assertEquals(outputs[4], "Length: 28");
    test:assertEquals(outputs[5], "Concat: Hello Ballerina!");
    test:assertEquals(outputs[6], "Join: Hello,Ballerina!");
    test:assertEquals(outputs[7], "From bytes: Hello");
    test:assertEquals(outputs[8], "Trim: Ballerina Programming Language");
    test:assertEquals(outputs[9], "HasSuffix: true");
    test:assertEquals(outputs[10], "HasPrefix: true");
    test:assertEquals(outputs[11], "Sprintf: Sam scored 90 for English and has an average of 71.50.");
    test:assertEquals(outputs[12], "Member Access: L");

}
