function func2() returns int {
    int|error myVar = 1;
    myVar. //<cursor>

    if myVar is int {
        return 1;
    }
}
