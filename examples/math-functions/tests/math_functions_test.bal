import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function
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
    // Invoking the main function
    main();

    string out1 = "Value of PI : 3.141592653589793";
    string out2 = "Value of E  : 2.718281828459045";
    string out3 = "Absolute value of -152.2544 : 152.2544";
    string out4 = "Absolute value of -152      : 152";
    string out5 = "Arc cosine of 0.027415567780803774  : 1.5433773235341761";
    string out6 = "Arc sine of 0.027415567780803774    : 0.02741900326072046";
    string out7 = "Arc tangent of 0.027415567780803774 : 0.0274087022410345";
    string out8 = "Cube root of 0.027415567780803774   : -3.0";
    test:assertEquals(outputs[0], out1);
    test:assertEquals(outputs[1], out2);
    test:assertEquals(outputs[2], out3);
    test:assertEquals(outputs[3], out4);
    test:assertEquals(outputs[4], out5);
    test:assertEquals(outputs[5], out6);
    test:assertEquals(outputs[6], out7);
    test:assertEquals(outputs[7], out8);
}
