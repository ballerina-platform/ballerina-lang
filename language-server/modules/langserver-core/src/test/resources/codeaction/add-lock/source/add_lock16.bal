isolated int[][] b = [[1, 2]];

function name() returns function => isolated function() {
    int[][] arr2 = b;
};
