function test() returns int {
    int|string x = 10;

    if x is int {
        return x;
    }

    return x;

}
