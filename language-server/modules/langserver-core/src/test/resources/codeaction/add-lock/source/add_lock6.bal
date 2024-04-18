isolated class IsolatedClass {
    private final int[][] arr = [[1, 2]];

    function fn(int[] & readonly inputArr) {
        self.arr.push(inputArr);
    }
}
