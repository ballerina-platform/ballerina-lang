import ballerina/test;


@test:MockFn {
    functionName : "addFn"
}
test:MockFunction mock_addFn = new();


// Test for Function mocking in different modules
@test:Config {
}
public function multipleModuleTest() {
    test:when(mock_addFn).call("mockFn");
    test:assertEquals(addFn(10, 5), 115);
}

// Mock Function
public function mockFn(int a, int b) returns (int) {
    return a + b + 100;
}
