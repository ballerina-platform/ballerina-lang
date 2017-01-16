
public function testBreakStatement1(int x, int y) (int) {
    int z;

    while(x >= y) {
        y = y + 1;
        z = z + 10;
        if(y == 5){
            break;
        }
    }
    return z;
}

public function testBreakStatement2(int x, int y) (int) {
    int z;
    int a;
    int b;
    a = 1;
    b = 5;
    while(a<=b){
     a = a +1;
     while(x >= y) {
        if(z == 5){
            break;
        }
        y = y + 1;
        z = z + 1;    
     }
     if(a == 3){
        break;
     }
    }
    
    return z;
}