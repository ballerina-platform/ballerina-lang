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

type OneToFour 1|2|3|4;

function testArrayAccessWithFiniteType() returns boolean {
    int x = 1;
    boolean y = true;
    string s = "hello world";

    int[] arr = [];
    OneToFour a = 1;
    arr[a] = x;

    boolean accessSuccessful = arr[1] == x;

    boolean[!...] bArr = [false, true, y];
    a = 2;
    accessSuccessful = accessSuccessful && bArr[a] == y;

    string[5] sArr = ["ballerina", "hi", "this is a string", "test string", s];
    a = 4;
    string s2 = sArr[a];
    return accessSuccessful && sArr[a] == s;
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

type OneThree 1|3;

function testArrayIndexOutOfRangeErrorWithFiniteTypeIndex() {
    string name = "";
    string[] animals = [];

    animals = ["Lion", "Cat"];
    OneThree ot = 3;
    name = animals[ot];
}
