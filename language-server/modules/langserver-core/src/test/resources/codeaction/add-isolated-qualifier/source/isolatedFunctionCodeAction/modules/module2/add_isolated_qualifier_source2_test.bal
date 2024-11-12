function testFunction() {
    return;
}

function testFunctionWithReturn() returns int => 0;

transactional function testFunctionWithInput(int i) returns int => i + 2;
