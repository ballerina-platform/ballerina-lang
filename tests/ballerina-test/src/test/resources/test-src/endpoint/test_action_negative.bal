
type DummyEndpoint object {
    public function init ({} conf) {
    }

    public function getCallerActions () returns (DummyClient) {
        return new;
    }
};

type DummyClient object {

    public function invoke1 (string a) returns map {
        map m;
        return m;
    }

    public function invoke2 (string a, int b) returns (string) {
        return a + b;
    }
};

function test1 () {
    endpoint DummyEndpoint ep {};
    foo(ep -> invoke2("val", 2));

    if(lengthof ep -> invoke1("aa") > 5){
        int i = 10;
    }
}

function foo(string s){
    // Do nothing.
}
