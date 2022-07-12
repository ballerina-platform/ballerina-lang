public function test() returns int {
    record {|int a;|} | int x = {a:10};
    boolean|int y = false;
    if (x is int) {
        return x;
    }
    if (y is boolean) {
        return y;
    }

    return x;
}
