function testFunction(int param) {
    int localVar = 0;
    do {
        if param == 0 {
            localVar /= (param + 1);
        } else {
            localVar /= param;
        }
    }

    doSomething(localVar);
}

function doSomething(int a) {

}
