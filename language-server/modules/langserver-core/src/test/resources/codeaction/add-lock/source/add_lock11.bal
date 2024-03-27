isolated class IsolatedClass {
    private final map<int[]> mp = {"a": [1, 2]};

    function fn() returns int[] {
        return self.mp.get("a");
    }
}
