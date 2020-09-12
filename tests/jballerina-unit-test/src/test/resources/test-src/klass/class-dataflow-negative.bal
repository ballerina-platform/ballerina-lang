class Per {
    int i = 0;

    function f() {
        int i = 0;
        int j;
        j = 3;
        i = j;

        int k;
        i = k;
        k = i;
        i = k;

        int q;
        i = q;
        k = q;
        q = q;
        q = i;
        i = q;
        k = q;
        q = q;
    }
}
