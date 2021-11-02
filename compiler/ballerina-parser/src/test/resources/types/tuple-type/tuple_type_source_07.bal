function foo() {
    [(int)[], int] t1 = [[23, 700], 17];
    [(int|byte)[], int] t2 = [[23, 700], 17];
    [(int|byte)[]...] t3 = [[17], [23, 700]];
}
