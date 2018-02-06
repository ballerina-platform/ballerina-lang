function returnStmtBranch1(int value, int b) (int) {

    if( value > 10) {
        return 100;

    } else if ( value == 10) {
        return 200;

    } else {

        if (b > 10) {
            return 300;

        } else if ( b == 10){
            return 400;
        }

        return 500;
    }

    return 1;
}

function returnStmtBranch2(int value, int b) (int) {
    int a = value + b;
    int c = returnStmtBranch1(9, 10);
    c = c + c + a;

    return c;
}

