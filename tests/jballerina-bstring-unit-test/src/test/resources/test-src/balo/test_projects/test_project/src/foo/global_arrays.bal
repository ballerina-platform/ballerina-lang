int[] glbArray = [1, 2];

public function getGlbArray() returns int[] {
    return glbArray;
}

int[3] glbSealedArray = [0, 0, 0];

public function getGlbSealedArray() returns int[3] {
    return glbSealedArray;
}

int[*] glbSealedArray2 = [1, 2, 3, 4];

public function getGlbSealedArray2() returns int[4] {
    return glbSealedArray2;
}

int[2][3] glbSealed2DArray = [[0, 0, 0], [0, 0, 0]];

public function getGlbSealed2DArray() returns int[2][3] {
    return glbSealed2DArray;
}

int[*][*] glbSealed2DArray2 = [[1, 2], [3, 4], [5, 6]];

public function getGlbSealed2DArray2() returns int[3][2] {
    return glbSealed2DArray2;
}
