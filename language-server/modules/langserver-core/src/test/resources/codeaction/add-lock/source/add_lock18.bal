isolated int[] arr = [1, 2, 3];

isolated function name() {
    int[] listResult = from var el in arr
        where el > 10
        select el;
}
