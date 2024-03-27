isolated class IsolatedClass {
    int|map<string> val;

    function init(int|map<string> & readonly val) {
        self.val = val;
    }
}
