import testdocorg/test_documentation;


function testDocDummy() returns (boolean) {
    return test_documentation:open("testt");
}

@test_documentation:Test{}
function testAnnotation() {
}
