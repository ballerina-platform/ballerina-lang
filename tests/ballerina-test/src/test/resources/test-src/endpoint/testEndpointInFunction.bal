string exFlow = "";

function main (string[] args) {
    endpoint DummyEndpoint ep { conf1 : "test1"};
    exFlow = exFlow + "<main>";
    ep -> invoke1("t", 1);
    var result = ep -> invoke2("t", 2);
    exFlow = exFlow + result;
}

type DummyEndpoint object {
    public {
        string prop1;
        int prop2;
    }

    public function init (DummyEndpointConfig conf) {
        exFlow = exFlow + "init:DummyEndpoint;";
        prop1 = conf.conf1;
        prop2 = conf.conf3;
    }

    public function start () {
        exFlow = exFlow + "start:DummyEndpoint;";
    }

    public function stop () {
        exFlow = exFlow + "stop:DummyEndpoint;";
    }

    public function register (typedesc ser) {
        exFlow = exFlow + "register:DummyEndpoint;";
    }

    public function getClient () returns (DummyClient) {
        exFlow = exFlow + "getClient:DummyEndpoint;";
        return new;
    }
};

public type DummyEndpointConfig {
    string conf1;
    boolean conf2;
    int conf3;
};

type DummyClient object {
    public {string conf1; }

    public function invoke1 (string a, int b) {
        exFlow = exFlow + "invoke1:DummyClient;";
    }

    public function invoke2 (string a, int b) returns (string) {
        exFlow = exFlow + "invoke2:DummyClient;";
        string result = a + b;
        return result;
    }
};

function test1 () returns (string) {
    endpoint DummyEndpoint ep { conf1 : "test1"};
    ep -> invoke1("t", 1);
    var result = ep -> invoke2("t", 2);
    exFlow = exFlow + result;
    return exFlow;
}

function test2 () returns (string) {
    endpoint DummyEndpoint ep { conf1 : "test1"};
    exFlow = exFlow + "<test2Caller>";
    test2Caller(ep);
    return exFlow;
}

function test2Caller (DummyEndpoint epVal) {
    endpoint DummyEndpoint ep = epVal;
    ep -> invoke1("t", 1);
    var result = ep -> invoke2("t", 2);
    exFlow = exFlow + result;
}

function test3 () returns (string) {
    endpoint DummyEndpoint ep;
    exFlow = exFlow + "<test3>";
    ep = new;
    ep.init({});
    ep.start();
    ep -> invoke1("t", 1);
    var result = ep -> invoke2("t", 2);
    exFlow = exFlow + result;
    return exFlow;
}

function test4 () returns (string) {
    endpoint DummyEndpoint ep;
    exFlow = exFlow + "<test3>";
    ep -> invoke1("t", 1);
    var result = ep -> invoke2("t", 2);
    exFlow = exFlow + result;
    return exFlow;
}