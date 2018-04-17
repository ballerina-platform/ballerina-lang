import ballerina/io;

@final int val1 = calculateExp5(15, 10);

int val2 = calculateExp5(20, 19);

function main(string... args) {
    int cal = val1 + val2;
    io:println("package init - " + cal);
}

function calculateExp5(int x, int y) returns (int) {
    int z;
    while(x >= y) {
        y = y + 1;
        if(y == 10){
            z = 100;
            break;
        }
        z = z + 10;
    }
    return z;
}
