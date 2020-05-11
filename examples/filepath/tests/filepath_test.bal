import ballerina/io;
import ballerina/test;
import ballerina/system;

(any|error)[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any|error... s) {
    outputs[counter] = s[0];
    counter += 1;
}

boolean isWindows = system:getEnv("OS") != "";

@test:Config
function testFunc() {
    // Invoking the main function
    main();

    string absolutePath;
    string filename;
    string parent;
    string normalized;
    string elements;
    string buildPath;
    string extension;
    string relative;

    if (isWindows) {
        absolutePath ="/A/B/C is absolute: false";
        filename ="Filename of /A/B/C: C";
        parent ="Parent of /A/B/C: \\A\\B";
        normalized ="Normalized path of foo/../bar: bar";
        elements ="Path elements of /A/B/C: [\"A\", \"B\", \"C\"]";
        buildPath ="Built path of '/', 'foo', 'bar': \\foo\\bar";
        extension ="Extension of path.bal: bal";
        relative ="Relative path between 'a/b/c' and 'a/c/d': ..\\..\\c\\d";
    } else {
        absolutePath ="/A/B/C is absolute: true";
        filename ="Filename of /A/B/C: C";
        parent ="Parent of /A/B/C: /A/B";
        normalized ="Normalized path of foo/../bar: bar";
        elements ="Path elements of /A/B/C: [\"A\", \"B\", \"C\"]";
        buildPath ="Built path of '/', 'foo', 'bar': /foo/bar";
        extension ="Extension of path.bal: bal";
        relative ="Relative path between 'a/b/c' and 'a/c/d': ../../c/d";
    }
    test:assertEquals(outputs[0], absolutePath);
    test:assertEquals(outputs[1], filename);
    test:assertEquals(outputs[2], parent);
    test:assertEquals(outputs[3], normalized);
    test:assertEquals(outputs[4], elements);
    test:assertEquals(outputs[5], buildPath);
    test:assertEquals(outputs[6], extension);
    test:assertEquals(outputs[7], relative);
}
