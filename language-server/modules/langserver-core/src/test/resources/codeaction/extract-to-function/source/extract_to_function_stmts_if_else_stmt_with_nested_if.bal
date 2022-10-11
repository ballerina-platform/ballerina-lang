function testFunction1() {
    int localVar = 10;

    if true {
        if true {
            localVar += 1;
        }
    }

    doSomething(localVar);
}

function doSomething(int a) {

}
