isolated class IsolatedClass {
    private map<string> mp = {"a": "1"};

    function fn() {
        self.mp["a"] = "2";
    }
}
