function recursive (int n) {
    if (n == 0) {
        return 1;
    }
    int recursive = 10;
    return recursive + recurs<caret>ive(n - 1);
}
