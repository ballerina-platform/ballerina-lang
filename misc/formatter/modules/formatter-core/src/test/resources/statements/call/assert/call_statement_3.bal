function calculate() returns int {
    int result = subtract(add([21, 45, 6, 12, 67, 89], [1, 5, 6, 9]),
            add([11, 12, 13, 14, 15, 16, 17, 18, 19, 20],
                    [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]));
    return result;
}

function add(int[] a, int[] b) returns int {
    int sum = 0;
    foreach int n in a {
        sum += n;
    }
    foreach int n in b {
        sum += n;
    }
    return sum;
}

function subtract(int x, int y) returns int {
    return x - y;
}
