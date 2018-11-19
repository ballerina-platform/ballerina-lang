function testNonInitArrayAccess() returns (string){
    string[] fruits = [];
    return fruits[5];
}

function arrayAccessTest(int x, int y) returns (int) {
    int[] arr = [];

    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];

    return arr[0] + arr[1] + arr[2];
}

function arrayReturnTest(int x, int y) returns (int[]) {
    int[] arr = [];

    arr[0] = x;
    arr[1] = y;
    arr[ x + y ] = x + y;

    return arr;
}

function arrayArgTest(int[] arr) returns (int) {
    return arr[0] + arr[1];
}

function arrayIndexOutOfBoundTest() {
    string name = "";
    string[] animals = [];

    animals = ["Lion", "Cat"];
    name = animals[5];
}
