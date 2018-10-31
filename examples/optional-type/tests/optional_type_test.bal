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
    test:assertEquals(outputs[1], ());
    test:assertEquals(io:sprintf("%s", outputs[0]),
        "{name:\"\", age:0, addr:{line01:\"61 brandon street\", line02:\"\", city:\"Santa Clara\", state:\"CA\", zipcode:\"95134\"}, guardian:null}"
    );
}
