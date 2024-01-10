function simpleIntArray() returns int {
    int[] values = from int i in 0 ..< 10 select i;
    int sum = 0;
    foreach int each in values {
        sum += each;
    }
    return sum;
}

function simpleIntIter() returns int {
    int[] values = from int i in 0 ..< 10 select i;
    int sum = 0;
    int len = values.length();
    foreach int i in 0..<len {
        sum += values[i];
    }
    return sum;
}
