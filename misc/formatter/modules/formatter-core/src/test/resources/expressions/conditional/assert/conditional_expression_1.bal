function getValue(int v) returns int|() {
    return v;
}

public function foo() {
    int t = true ? 7 : 0;
    int u = getValue(6) ?: 8;
}
