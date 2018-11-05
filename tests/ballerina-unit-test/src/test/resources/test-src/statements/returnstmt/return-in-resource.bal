service<DummyService> helloWorld {
    sayHelloWithString(string x) returns string {
        return x;
    }

    sayHelloWithInt(int x) returns int {
        return x;
    }

    sayHelloWithNil1(string x) {
        return;
    }

    sayHelloWithNil2(string x) returns () {
        return;
    }
}

type Config record {
};

type DummyEndpoint object {

    function init (Config conf)  {
    }
};
type DummyService object{

    function getEndpoint() returns (DummyEndpoint) {
        DummyEndpoint ep = new;
        return ep;
    }
};