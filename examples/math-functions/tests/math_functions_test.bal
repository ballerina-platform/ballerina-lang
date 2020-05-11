import ballerina/test;

(any|error)[] outputs = [];

// This is the mock function which will replace the real function
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any|error... s) {
    foreach var entry in s {
        outputs.push(entry);
    }
}

@test:Config {}
function testFunc() {
    // Invoking the main function
    main();

    string out1 = "Value of PI : ";
    string out2 = "Value of E  : ";
    string out3 = "Absolute value of -152.2544 : ";
    string out4 = "Absolute value of -152 : ";
    string out5 = "Arc cosine of 0.027415567780803774 : ";
    string out6 = "Arc sine of 0.027415567780803774 : ";
    string out7 = "Arc tangent of 0.027415567780803774 : ";
    string out8 = "Cube root of -27.0 : ";
    test:assertEquals(outputs[0], out1);
    test:assertEquals(outputs[1], 3.141592653589793);
    test:assertEquals(outputs[2], out2);
    test:assertEquals(outputs[3], 2.718281828459045);
    test:assertEquals(outputs[4], out3);
    test:assertEquals(outputs[5], 152.2544);
    test:assertEquals(outputs[6], out4);
    test:assertEquals(outputs[7], 152);
    test:assertEquals(outputs[8], out5);
    test:assertEquals(outputs[9], 1.5433773235341761);
    test:assertEquals(outputs[10], out6);
    test:assertEquals(outputs[11], 0.02741900326072046);
    test:assertEquals(outputs[12], out7);
    test:assertEquals(outputs[13], 0.0274087022410345);
    test:assertEquals(outputs[14], out8);
    test:assertEquals(outputs[15], -3.0);
}
