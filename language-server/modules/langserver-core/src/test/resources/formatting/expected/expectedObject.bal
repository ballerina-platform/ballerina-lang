import ballerina/io;

type ObjectName1 object {};

type ObjectName2 object {
    private int bd = 0;
    public int a = 0;
    public string s;
    string h;
};

type ObjectName3 object {
    public int a = 0;
    public string s;
    public ObjectName1? b;
    string h;

    function init() {
        self.s = "";
    }

    public function testS() {
        self.a = 1;
    }

    public function testd() returns int {
        return self.a;
    }

    function sd();
};

function close(io:ReadableByteChannel|io:WritableByteChannel ch) {
    object {
        public function close() returns error?;
    } channelResult = ch;
    var cr = channelResult.close();
    if (cr is error) {
        log:printError("Error occured while closing the channel: ", err = cr);
    }
}

public type Client client object {
    public string url;

    # Gets invoked to initialize the client. During initialization, configurations provided through the `config`
    # record is used to determine which type of additional behaviours are added to the endpoint (e.g: caching,
    # security, circuit breaking).
    #
    # + url - URL of the target service
    # + config - The configurations to be used when initializing the client
    public function init(string url) {
        self.url = url;
    }

    # The `post()` function can be used to send HTTP POST requests to HTTP endpoints.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `error` if failed to establish communication with the upstream server
    public remote function post(@untainted string path, RequestMessage message) returns Response|error {

    }

    public remote function get(@untainted string path, RequestMessage message) returns Response|error = external;

    private function getConfig()
        =
        external
    ;
}
