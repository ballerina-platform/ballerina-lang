function foo() {
    int|string[] arr = [1, 3, 4, 5, 6];
    _ = from var item in arr
        where item is int 
}
