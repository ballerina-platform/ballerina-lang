import ballerina/io;

final int val1 = calculateExp5(15, 10);

int val2 = calculateExp5(20, 19);

public function main(string... args) {
    int cal = val1 + val2;
    io:println("package init - " + cal);
}

function calculateExp5(int x, int y) returns (int) {
    int z = 0;
    int a = y;
    while(x >= a) {
        a = a + 1;
        if(a == 10){
            z = 100;
            break;
        }
        z = z + 10;
    }
    return z;
}
