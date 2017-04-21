function recursive (int number) (int) {
    if (number == 1) {
        return number;
    }
    return recu<caret>rsive(number - 1);
}
