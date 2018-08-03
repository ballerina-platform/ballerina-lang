function dump(int arg1, int arg2) returns int {
    int a = 10;
    int b = 30;

    if a == b  {
        a = b;
        //if (a != b) {
        //    a = a * b;
        //} else if ( a < b ) {
        //    return a;
        //}
    } else if ( a > b ) {
        a = a -b;
    } else {
        a = a / b;
        return a + b;
    }

    return a + b;
}


function main (int arg) returns int {
    int a = 10;
    boolean b = a > 100;
    return a;
}

function genComplex (int arg1, int arg2) returns int {
    int a = 10;
    int b = a + arg1;
    int c = a - b + arg2;
    b = b + c;
    return a + b;
}
