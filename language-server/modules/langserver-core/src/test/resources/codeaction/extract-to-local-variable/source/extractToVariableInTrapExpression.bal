function testFunction() {
     int|error result = trap foo();
}
    
function foo() returns int {
    return 0;
}
