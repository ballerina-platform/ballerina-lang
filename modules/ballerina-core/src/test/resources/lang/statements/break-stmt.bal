function calculateExp1(int x, int y) (int) {
    int z;
    while(x >= y) {
        y = y + 1;
        if(y == 10){
            z = 100;
            break;
        } else if( y > 20){
            z = 1000;
            break;
        }
        z = z + 10;
    }
    return z;
}

function nestedBreakStmt(int x, int y) (int) {
    int z = 10;
    while(x >= y) {
        y = y + 1;
        if(y >= 10){
            z = z + 100;
            break;
        }
        z = z + 10;
        while(y < x) {
            z = z + 10;
            y = y + 1;
            if (z >= 40) {
                break;
            }
        }
    }
    return z;
}



