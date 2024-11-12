isolated class IsolatedClass {
    private final int[][] arr = [[1, 2]];

    function fn(int[] & readonly inputArr) {
        _ = start asyncFn(self.arr[0]);
    }
}

isolated function asyncFn(int[] arr) {

}
