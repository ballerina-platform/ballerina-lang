function recursive (int n) {
    if (n == 0) {
        return 1;
    }
    int recurs<caret>ive = 10;
    return recursive + recursive(n - 1);
}
