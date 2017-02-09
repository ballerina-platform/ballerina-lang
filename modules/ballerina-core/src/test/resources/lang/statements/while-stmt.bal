function testWhileStmt(int x, int y) (int) {
    int z;

    while(x >= y) {
        y = y + 1;
        z = z + 10;
    }
    return z;
}

function testWhileScope(int number)(int) {
    int i = number;
    while(i < 4) {
        i = i + 1;
       if(i == 2) {
          int x = 200;
          i = x;
       } else {
          int x = 400;
          i = x;
        }
    }
    return i;
}