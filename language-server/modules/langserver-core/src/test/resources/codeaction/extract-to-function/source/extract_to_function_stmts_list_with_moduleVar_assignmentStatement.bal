int moduleVar = 6900000;

function testFunction() {
    moduleVar = moduleVar + 1;
    doSomething(1);

    doSomething(moduleVar);
}

function doSomething(int b) {

}
