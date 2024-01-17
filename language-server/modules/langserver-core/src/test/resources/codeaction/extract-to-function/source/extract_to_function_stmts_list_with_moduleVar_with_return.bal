int moduleVar = 6900000;

function testFunction() {
    int localVar = 10;
    localVar = localVar + moduleVar;

    doSomething(localVar);
}

function doSomething(int b) {

}
