function recursive (int number) (int) {
    if (number == 1) {
        return number;
    }
    return recursive(number - 1) + recur<caret>sive(number - 2);
}
