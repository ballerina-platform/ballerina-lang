function arrayInitTest() (int) {
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

function arrayReturnTest() (string[]) {
    string[] animals;

    animals = ["Lion", "Cat", "Leopard", "Dog", "Tiger", "Croc"];

    return animals;
}

function testNestedArrayInit() (int[][]) {
    int[][] array = [[1,2,3], [6,7,8,9]];
            
    return array;
}

function testArrayOfMapsInit() (map[]) {
    map[] array = [
                     {address:{city:"Colombo", "country":"SriLanka"}},
                     {address:{city:"Kandy", "country":"SriLanka"}},
                     {address:{city:"Galle", "country":"SriLanka"}}
                  ];
            
    return array;
}

function testAnyAsArray() (any) {
    any array = [1,2,3];
    return array;
}

function floatArrayInitWithInt() (float[]) {
    float[] abc = [2,4,5];
    return abc;
}