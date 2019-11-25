function testPanic() {
    int a = 10;
    
    if (a == 10) {
        error err = error("Value equals to 10");
        panic err;
    }
}