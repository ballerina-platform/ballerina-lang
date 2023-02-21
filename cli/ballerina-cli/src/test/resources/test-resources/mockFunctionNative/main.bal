function intAdd(int a, int b) returns int {
    return (a+b);
    
}

function intAddInside(int a, int b) returns int {
    int c = intAdd(a, b);
    return c + 100;
}