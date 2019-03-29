import ballerina/io;
import ballerina/test;
import ballerina/system;

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

boolean IS_WINDOWS = system:getEnv("OS") != "";

@test:Config
function testFunc() {
    // Invoking the main function
    main();

    string out1;
    string out2;
    string out3;
    string out4;
    string out5;
    string out6;
    string out7;
    string out8;

    if (IS_WINDOWS) {
        out1 ="/A/B/C is absolute: false";
        out2 ="Filename of /A/B/C: C";
        out3 ="Parent of /A/B/C: \\A\\B";
        out4 ="Normalized path of foo/../bar: bar";
        out5 ="Path elements of /A/B/C: [\"A\", \"B\", \"C\"]";
        out6 ="Built path of '/', 'foo', 'bar': \\foo\\bar";
        out7 ="Extension of path.bal: bal";
        out8 ="Relative path between 'a/b/c' and 'a/c/d': ..\\..\\c\\d";
    } else {
        out1 ="/A/B/C is absolute: true";
        out2 ="Filename of /A/B/C: C";
        out3 ="Parent of /A/B/C: /A/B";
        out4 ="Normalized path of foo/../bar: bar";
        out5 ="Path elements of /A/B/C: [\"A\", \"B\", \"C\"]";
        out6 ="Built path of '/', 'foo', 'bar': /foo/bar";
        out7 ="Extension of path.bal: bal";
        out8 ="Relative path between 'a/b/c' and 'a/c/d': ../../c/d";
    }
    test:assertEquals(outputs[0], out1);
    test:assertEquals(outputs[1], out2);
    test:assertEquals(outputs[2], out3);
    test:assertEquals(outputs[3], out4);
    test:assertEquals(outputs[4], out5);
    test:assertEquals(outputs[5], out6);
    test:assertEquals(outputs[6], out7);
    test:assertEquals(outputs[7], out8);
}
