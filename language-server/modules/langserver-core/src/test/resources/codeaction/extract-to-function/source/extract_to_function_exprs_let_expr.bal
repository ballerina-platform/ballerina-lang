function testFunction(int param) {
    int localVar = let int letVar1 = 1, int letVar2 = letVar1 + 1 in letVar1 + doSomething(letVar2) + 1;
}

function doSomething(int a) returns int {
    return 1;
}
