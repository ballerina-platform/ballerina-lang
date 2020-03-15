import ballerina/test;

any[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    if(s.length()==1){
      outputs[counter]=s[0];
    }else{
    string outstr = "";
    foreach var str in s {
        outstr = outstr + str.toString();
    }
    outputs[counter] = outstr;
    }
    counter += 1;
}

@test:Config {}
function testFunc() {
    // Invoking the main function
    main();
    test:assertEquals(outputs[0], 10);
    test:assertEquals(outputs[1], 100);
    test:assertEquals(outputs[2], 20.0);
    test:assertEquals(outputs[3], "Max float: 22.0");
    test:assertEquals(outputs[4], true);
    test:assertEquals(outputs[5], true);
    test:assertEquals(outputs[6], true);
    test:assertEquals(outputs[7], 27.5d);
    test:assertEquals(outputs[8], 23);
    test:assertEquals(outputs[9], "Ballerina");
    test:assertEquals(outputs[10], true);
}
