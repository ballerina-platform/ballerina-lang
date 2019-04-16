import ballerina/io;
import ballerina/test;

any[] outputs = [];
int counter = 0;

// This is the mock function that will replace the real function.
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    foreach var arg in s {
        outputs[counter] = arg;
        counter += 1;
    }
}

@test:Config
function testFunc() {
    // Invoking the main function.
    main();

    string output1 = "The update operation - Creating a table:";
    string output2 = "Create student table status: 0";
    string output3 = "\nThe update operation - Inserting data to a table";
    string output4 = "Insert to student table with no parameters status: 1";
    string output5 = "\nThe select operation - Select data from a table";
    string output6 = "\nConvert the table into json";
    string output7 = "JSON: ";
    string output8 = "[{\"ID\":1, \"AGE\":23, \"NAME\":\"john\"}]";
    string output9 = "\nThe update operation - Drop student table";
    string output10 = "Drop table student status: 0";

    test:assertEquals(<string>outputs[0], output1);
    test:assertEquals(<string>outputs[1], output2);
    test:assertEquals(<string>outputs[2], output3);
    test:assertEquals(<string>outputs[3], output4);
    test:assertEquals(<string>outputs[4], output5);
    test:assertEquals(<string>outputs[5], output6);
    test:assertEquals(<string>outputs[6], output7);
    test:assertEquals(<string>outputs[7], output8);
    test:assertEquals(<string>outputs[8], output9);
    test:assertEquals(<string>outputs[9], output10);
}
