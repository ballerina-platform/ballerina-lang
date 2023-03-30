int moduleVar = 6900000;

function testFunction() {
    int localVar = 1;
    localVar = localVar + 1;
    moduleVar = moduleVar + 1;

    doSomething(localVar + moduleVar);
}

function doSomething(int b) {

}
