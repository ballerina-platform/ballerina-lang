function valueAssignmentAndRetrieval() (int) {
    int[] x = [3];
    int[] y = [4, 5];

    int[][] xx = [x, y];
    return xx[0][0];
}

function arrayInitializationAndRetrieval() (int) {
    int[][] x = [];
    x[0][0] = 1;

    return x[0][0];
}

