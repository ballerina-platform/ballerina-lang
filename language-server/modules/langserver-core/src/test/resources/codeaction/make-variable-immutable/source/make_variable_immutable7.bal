isolated class IsolatedClass {
    int[]|map<string> val;

    function init(int[]|map<string> val) {
        self.val = val.cloneReadOnly();
    }
}
