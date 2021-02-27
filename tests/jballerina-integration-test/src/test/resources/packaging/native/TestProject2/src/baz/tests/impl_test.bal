import ballerina/test;

# Test function

@test:Config {
}
function testFunction() {
    string? petKind = getPetKind();
    test:assertEquals(getPetKind(), "dog");
}
