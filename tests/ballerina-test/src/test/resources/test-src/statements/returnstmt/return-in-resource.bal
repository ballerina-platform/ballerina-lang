service<DummyService> helloWorld {
    sayHello(string x) {
        return x;
    }
}

struct DummyEndpoint {}

function <DummyEndpoint s> init (struct {} conf)  {
}

struct DummyService {}

function <DummyService s> getEndpoint() returns (DummyEndpoint) {
    return null;
}