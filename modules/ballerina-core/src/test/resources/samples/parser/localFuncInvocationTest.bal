
function process(int a, int b, int c) (int) {
    int x;
    x = 10;
    a = add(a, b);
    a = add(a, c);
    return add(a, x);
}

function add(int x, int y) (int) {
    int z;
    z = x  + y;
    return z;
}

