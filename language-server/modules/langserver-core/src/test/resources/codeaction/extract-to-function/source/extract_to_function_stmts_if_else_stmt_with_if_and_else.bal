function testFunction1(boolean bool) {
    int localVar = 10;

    if bool {
        localVar += 1;
    } else {
        localVar += 2;
    }

    doSomething(localVar);
}

function doSomething(int a) {

}
