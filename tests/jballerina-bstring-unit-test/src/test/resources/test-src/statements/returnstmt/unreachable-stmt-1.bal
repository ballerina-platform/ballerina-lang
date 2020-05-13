function returnStmtBranch1(int value, int b) returns (int) {

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