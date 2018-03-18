string exFlow = "";

public struct DummyEndpoint {
    string prop1;
    int prop2;
}
public function <DummyEndpoint ep> init (DummyEndpointConfig conf) {
    exFlow = exFlow + "init:DummyEndpoint;";
    ep.prop1 = conf.conf1;
    ep.prop2 = conf.conf3;
}

public function <DummyEndpoint ep> start () {
    exFlow = exFlow + "start:DummyEndpoint;";
}

public function <DummyEndpoint ep> stop () {
    exFlow = exFlow + "stop:DummyEndpoint;";
}

public function <DummyEndpoint ep> register (type ser) {
    exFlow = exFlow + "register:DummyEndpoint;";
}

public function <DummyEndpoint ep> getClient () returns (DummyClient) {
    exFlow = exFlow + "getClient:DummyEndpoint;";
    return {};
}

public struct DummyEndpointConfig {
    string conf1;
    boolean conf2;
    int conf3;
}

public struct DummyClient {
    string conf1;
}

public function <DummyClient c> invoke1 (string a, int b) {
    exFlow = exFlow + "invoke1:DummyClient;";
}

public function <DummyClient c> invoke2 (string a, int b) returns (string) {
    exFlow = exFlow + "invoke2:DummyClient;";
    string result = a + b;
    return result;
}

function test1 () returns (string) {
    exFlow = exFlow + "<test1>";
    return exFlow;
}


endpoint DummyEndpoint ep { conf1 : "test1"};

struct DummyServiceType {
}

function <DummyServiceType s> getEndpoint() returns (DummyEndpoint){
    return null;
}

service<DummyServiceType> foo bind ep {
    getAction (endpoint client, string x, float y){
        client -> invoke1("t", 1);
    }
}