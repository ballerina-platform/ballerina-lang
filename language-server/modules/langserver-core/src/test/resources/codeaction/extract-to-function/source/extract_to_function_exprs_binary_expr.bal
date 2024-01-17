const int constant = 1;
int moduleVar = 1;

function testFunction(int param) {
    int localVar = 1 + param;
    int total = constant + moduleVar + param + localVar;

    doSomething(total);
}

function doSomething(int a) {

}
