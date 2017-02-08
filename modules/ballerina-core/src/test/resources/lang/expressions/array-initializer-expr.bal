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

