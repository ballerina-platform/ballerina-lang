function testFunction() {
    int intVal = 2;
    int localVar = let int letVar1 = 1, int letVar2 = letVar1 + 1 + intVal in letVar1;
}
