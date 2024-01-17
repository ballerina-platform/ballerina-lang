function testFunction(int m, int n) returns int {
    int a = 1;
    error err = error("new error");
    if n == 0 {
        panic 
    }

    return m/n;
}
