import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function
@test:Mock {
    packageName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[counter] = s[0];
    counter++;
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();
    test:assertEquals(outputs[1], ());
    test:assertEquals(io:sprintf("%r", outputs[0]),
        "{name:\"\", age:0, addr:{line01:\"61 brandon stree\", line02:\"\", city:\"Santa Clara\", state:\"CA\", zipcode:\"95134\"}, guardian:null}"
    );
}
