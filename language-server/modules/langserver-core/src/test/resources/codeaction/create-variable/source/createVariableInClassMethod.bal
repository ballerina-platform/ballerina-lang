function createIntWithError() returns int|error {
    return 10;
}

class MyClass {

    function testFunction() {
        createIntWithError()
    }
}
