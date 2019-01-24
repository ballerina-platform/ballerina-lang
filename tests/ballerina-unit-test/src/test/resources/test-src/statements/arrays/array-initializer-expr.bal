function arrayInitTest() returns (int) {
    int[] arr = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11];
    int size = 11;
    int index = 0;
    int sum = 0;

    arr[5] = 50;

    while (index < size) {
        sum = sum + arr[index];
        index = index + 1;
    }


    return sum;
}

function arrayReturnTest() returns (string[]) {
    string[] animals;

    animals = ["Lion", "Cat", "Leopard", "Dog", "Tiger", "Croc"];

    return animals;
}

function testNestedArrayInit() returns (int[][]) {
    int[][] array = [[1,2,3], [6,7,8,9]];

    return array;
}

function testArrayOfMapsInit() returns (map<any>[]) {
    map<any> addressOne = {city:"Colombo", "country":"SriLanka"};
    map<any> addressTwo = {city:"Kandy", "country":"SriLanka"};
    map<any> addressThree = {city:"Galle", "country":"SriLanka"};
    map<any>[] array = [
                     {address: addressOne},
                     {address: addressTwo},
                     {address: addressThree}
                  ];
    return array;
}

function floatArrayInitWithInt() returns (float[]) {
    float[] abc = [2, 4, 5];
    return abc;
}

public type FiniteType "Terminating"|"NotTerminating"|"BestEffort"|"NotBestEffort";

function finiteTypeArray() returns FiniteType {
    FiniteType?[]? var1 = ["Terminating"];
    if var1 is FiniteType?[] {
        var temp = var1[0];
        if (temp is FiniteType) {
            return temp;
        }
    }
    error e = error ("FAILED TEST");
    panic e;
}
