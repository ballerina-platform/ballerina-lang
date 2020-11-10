var objWithMethods = @deprecated client object {
    int n;
    public function init() {
        self.n = 1;
    }

    public function inc() {
        self.n = self.n + 1;
    }

    function getN() returns int {
        return self.n;
    }

    @deprecated
    function getIncrementN() returns int {
        self.n = self.n + 1
        return self.n;
    }
};
