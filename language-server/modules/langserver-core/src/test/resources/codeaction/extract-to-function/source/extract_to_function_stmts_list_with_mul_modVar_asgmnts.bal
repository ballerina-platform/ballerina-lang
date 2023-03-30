int moduleVar1 = 6900000;
int moduleVar2 = 225;

function testFunction() {
    moduleVar1 = moduleVar1 + 1;
    moduleVar2 = moduleVar2 + 2;

    doSomething(moduleVar1 + moduleVar2);
}

function doSomething(int b) {

}
