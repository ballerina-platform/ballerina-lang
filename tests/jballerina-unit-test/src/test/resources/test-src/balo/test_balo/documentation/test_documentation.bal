import testDocOrg/test;


function testDocDummy() returns (boolean) {
    return test:open("testt");
}

@test:Test{}
function testAnnotation() {
}
