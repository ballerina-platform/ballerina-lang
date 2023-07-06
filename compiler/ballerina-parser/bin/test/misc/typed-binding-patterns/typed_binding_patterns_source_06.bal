function foo() {
    int[] & string[] a;
    T1[]|T2[] a;
    T1[a] & T2[b] c = y;
    T1[a] & T2 [b] = y;
    T1[a] & T2[b]|T3[c]|T4 [d] = y;
    T1[a] & T2[b]|T3[c]|T4[d] [e] = y;
}
