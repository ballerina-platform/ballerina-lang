isolated class IsolatedClass {
    private map<int[]> mp = {"a": [1, 2]};

    function fn(int[] & readonly inputArr) {
        self.mp["a"] = inputArr;
    }
}
