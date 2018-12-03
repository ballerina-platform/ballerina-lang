type ClientEndpointConfiguration record {

};

public type ABCClient client object {

    remote function testAction1() returns string;
    remote function testAction2() returns string;

};

remote function ABCClient.testAction1() returns string {
        worker sampleWorker {
            string m = "";
            m = <- default;
            string v = "result from sampleWorker";
            v -> default;
        }

        "xxx" -> sampleWorker;
        string result = "";
        result = <- sampleWorker;
        return result;
}

remote function ABCClient.testAction2() returns string {
        worker sampleWorker {
              "request" -> default;
        }

        string result = "";
        result = <- sampleWorker;
        return result;
}

public type Client object {
    public ABCClient abcClient = new;

    public function init(ClientEndpointConfiguration config);

    public function register(typedesc serviceType) {
    }

    public function start() {
    }

    public function getCallerActions() returns ABCClient {
        return self.abcClient;
    }

    public function stop() {
    }
};

function Client.init(ClientEndpointConfiguration config) {
    self.abcClient = new;
}

function testAction1() returns string {
    ABCClient ep1 = new;
   string x = ep1->testAction1();
   return x;
}

function testAction2() returns string {
    ABCClient ep1 = new;
   string x = ep1->testAction2();
   return x;
}
