function arrayInitTest() returns (int) {
    int[] arr = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11];
    int size;
    int index;
    int sum;

    arr[5] = 50;

    size = 11;

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

function testArrayOfMapsInit() returns (map[]) {
    map addressOne = {city:"Colombo", "country":"SriLanka"};
    map addressTwo = {city:"Kandy", "country":"SriLanka"};
    map addressThree = {city:"Galle", "country":"SriLanka"};
    map[] array = [
                     {address: addressOne},
                     {address: addressTwo},
                     {address: addressThree}
                  ];
    return array;
}

function floatArrayInitWithInt() returns (float[]) {
    float[] abc = [2,4,5];
    return abc;
}