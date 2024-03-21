public function main() {
    
}

public add(int a, int b) returns int {
    if (a < 0) {
        return 0;
    } else (b < 0) {
        return 0;
    }
    return a + b;
}

public sub(int a, int b) returns int {
    int count = 0;
    while true {
        count = count + 1;
    }
    return a - b;
}

public function query() {
    int[] nums = [1, 2, 3, 4];
    int[] numsTimes10 = from var i in nums i * 10;
    int[] numsTimes2 = var i in nums select i * 2;
    int[] numsTimes3 = from var i nums select i * 3;
    int[] numsReversed = from int i in nums by i descending select i;
    int[] numsReversed_ = from int i in nums order i descending select i;

}



