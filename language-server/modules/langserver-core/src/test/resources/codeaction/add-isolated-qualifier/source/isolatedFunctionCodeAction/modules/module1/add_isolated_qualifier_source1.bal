function testFunction() {
    return;
} 

isolated function test() {
    testFunction();
    return;
}
