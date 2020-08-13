function getVal(int v) returns any {
    return v;
}

public function foo() {
    boolean f = getVal(8) is float;
}
