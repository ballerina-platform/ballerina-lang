string exFlow = "";

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

    public function getCallerActions () returns (DummyClient) {
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

type DummyServiceType object {
    function getEndpoint () returns (DummyEndpoint);
};

function test1 () returns (string) {
    exFlow = exFlow + "<test1>";
    return exFlow;
}

service<DummyServiceType> foo bind  { confX : "test1"} {
    getAction (endpoint client, string x, float y){
        client -> invoke1("t", 1);
    }
}