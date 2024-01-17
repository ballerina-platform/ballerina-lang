function testFunction() {
    int localVar = 0;
    boolean bool = true;
    if bool {
        localVar = 1;
    } else {
        localVar = -1;
    }

    doSomething(localVar);
}

function doSomething(int b) {

}
