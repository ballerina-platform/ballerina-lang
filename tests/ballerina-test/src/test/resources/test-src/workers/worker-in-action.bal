type ClientEndpointConfiguration {

};

public type ABCClient object {

    public function testAction1() returns string;
    public function testAction2() returns string;

};

public function ABCClient::testAction1() returns string {
        worker default {
            "xxx" -> sampleWorker;
            string result;
            result <- sampleWorker;
            return result;
        }
        worker sampleWorker {
            string m;
            m <- default;
            string v = "result from sampleWorker";
            v -> default;
        } 
}

public function ABCClient::testAction2() returns string {
        worker default {
            string result;
            result <- sampleWorker;
            return result;
        }
        worker sampleWorker {
              "request" -> default;
        }
}

public type Client object {
    public {
        ABCClient abcClient;
    }

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

public function Client::init(ClientEndpointConfiguration config) {
    self.abcClient = new;
}

function testAction1() returns string {
   endpoint Client ep1 { };
   string x = ep1->testAction1();
   return x;
}

function testAction2() returns string {
   endpoint Client ep1 { };
   string x = ep1->testAction2();
   return x;
}
