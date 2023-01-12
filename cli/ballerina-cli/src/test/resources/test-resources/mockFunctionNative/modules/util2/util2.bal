public function intDiv(int a, int b) returns int {
    return (a / b);

}

public function intDivInside(int a, int b) returns int {
    int c = intDiv(a, b);
    return c + 100;
}