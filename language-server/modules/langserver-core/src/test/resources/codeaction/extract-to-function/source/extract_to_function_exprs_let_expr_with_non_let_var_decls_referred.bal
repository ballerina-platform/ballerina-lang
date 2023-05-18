int moduleVar = 69;
const CONST = 3;

function testFunction(int param) {
    int localVar1 = 10;
    int localVar2 = let int letVar1 = 1, int letVar2 = letVar1 + 1
        in doSomething(letVar2) + moduleVar + CONST + localVar1 + param;
}

function doSomething(int a) returns int {
    return 1;
}
