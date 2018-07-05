function scopeIfValue(int a, int b, int c) returns int {
    int k;
    if(a > b) {
        k = k + c;
    } else {
        k = 1;
        if (c > b) {
            k = k + a;
        } else {
            k = k + 99999;
        }
    }

    return k;
}

function scopeWhileScope (int a, int b, int c) returns int {
    int k = 5;
    int b1 = b;
    while (a > b1 ) {
        b1 = b1 + 1;
        int i  = 10;

        if (c < a) {
            i = i + 10;
        } else {
            i = i + 20;
        }
        k = k + i;
    }
    return k;
}