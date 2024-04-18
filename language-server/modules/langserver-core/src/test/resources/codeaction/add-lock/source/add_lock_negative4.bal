isolated int[] arr = [1, 2, 3];
isolated boolean condition = false;

isolated function name() {
    int[] listResult = from var el in arr
        where el > 10 && condition
        select el;
}
