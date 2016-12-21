public function foo() (int) {
    int x;
    int y;
    int z;

    x = 10;
    y = 1;
    z = 0;

    while(x != y) {
        y=y+1;
        z=z+10;
    }
    return z;
}