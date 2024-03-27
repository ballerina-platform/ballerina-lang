isolated class IsolatedClass {
    private int[][] arr = [[1, 2]];

    function fn() returns int[] {
        return self.arr[0];
    }
}
