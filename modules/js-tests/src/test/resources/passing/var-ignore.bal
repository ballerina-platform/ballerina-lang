function m() (int) {
    int x;
    int y;
    _, x, y = abc();
    return y;
}

function abc() (int, int, int) {
    return 1, 2, 3;
}
