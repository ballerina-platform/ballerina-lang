
type DummyEndpoint object {
    public function init (record {} conf) {
}

public function getCallerActions () returns (DummyClient) {
    return new;
}
};

type DummyClient object {
    public function invoke1 (string a) returns error? {
        error e = error("i1");
        return e;
    }
};

public endpoint DummyEndpoint ep {};
