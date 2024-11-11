isolated class IsolatedClass {
    private map<int[]> mp = {"a": [1, 2]};

    function fn(int[] inputArr) {
        self.mp["a"] = inputArr;
    }
}
