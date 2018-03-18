connector TestConnector (string param1, string param2, int param3) {

    boolean action2Invoked;

    action action1 (string msg) (string) {
        worker default {
            msg -> sampleWorker;
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

    action action2 (string msg) (string) {
        worker default {
            string result;
            result <- sampleWorker;
            return result;
        }
        worker sampleWorker {
              msg -> default;
        }
    }

}

function testAction1 () (string) {
    endpoint<TestConnector> testConnector {
        create TestConnector("MyParam1", "MyParam2", 5);
    }
    string request = "request";
    string value = testConnector.action1(request);
    return value;
}

function testAction2 () (string) {
    endpoint<TestConnector> testConnector {
        create TestConnector("MyParam1", "MyParam2", 5);
    }
    string request = "request";
    string value = testConnector.action2(request);
    return value;
}

struct testEP {
}

function <testEP ep> init(string name, struct {} config) {
}

function <testEP ep> start() {
}

function <testEP ep> stop() {
}

function <testEP ep> register(type t) {
}

function <testEP ep> getConnector() (TestConnector) {
    return null;
}