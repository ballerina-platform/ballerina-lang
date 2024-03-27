isolated class IsolatedClass {
    private map<int[]> & readonly mp = {"a": [1, 2]};

    function fn() returns int[] {
        return self.mp.get("a");
    }
}
