import ballerina/test;

(any|error)[] outputs = [];
int count = 0;

// This is the mock function which will replace the real function
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any|error... s) {
    if (s.length() == 1) {
        outputs[count] = s[0];
    } else {
        string outstr = "";
        foreach var str in s {
            outstr = outstr + str.toString();
        }
        outputs[count] = outstr;
    }
    count += 1;
}

@test:Config {}
function testFunc() {
    // Invoking the main function
    main();
    test:assertEquals(outputs[0], 0);
    test:assertEquals(outputs[1], 1);
    test:assertEquals(outputs[2], 8);
    test:assertEquals(outputs[3], "Reversed: 8 7 6 5 4 3 2 1");
    test:assertEquals(outputs[4], "Before pop: 1 2 3 4 5 6 7 8");
    test:assertEquals(outputs[5], "Popped value: 8");
    test:assertEquals(outputs[6], "After pop: 1 2 3 4 5 6 7");
    test:assertEquals(outputs[7], "Doubled: 2 4 6 8 10 12 14");
    test:assertEquals(outputs[8], 23);
    test:assertEquals(outputs[9], 1000);
    test:assertEquals(outputs[10], 3);
    test:assertEquals(outputs[11], 3);
    test:assertEquals(outputs[12], 9);
    test:assertEquals(outputs[13], 5);
    test:assertEquals(outputs[14], 4);

}
