type ClientConfig record {|
    string host;
    int port;
    string scheme;

    int timeout;
|};

public type Error distinct error;

client class Client {

    private ClientConfig config;

    public function init(ClientConfig config) returns error? {
        self.config = config;
    }

    remote function findByName(string name) returns json|Error {
        return {
            id: "123",
            name: "Test"
        };
    }
}

public function main() returns error? {
    Client cl = check new ({});
}
