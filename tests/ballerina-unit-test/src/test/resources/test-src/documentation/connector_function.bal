documentation {Test Connector
F{{url}} url for endpoint
F{{path}} path for endpoint
}
type TestConnector object {
    public string url;
    public string path;

    documentation {Test Connector action testAction R{{value}} whether successful or not}
    public function testAction() returns boolean;

    documentation {Test Connector action testSend P{{ep}} endpoint url R{{value}} whether successful or not}
    public function testSend(string ep) returns boolean;
};

function TestConnector::testAction() returns boolean {
    boolean value;
    return value;
}

function TestConnector::testSend(string ep) returns boolean {
    boolean value;
    return value;
}

