package ballerina.http;

public type WebSocketClientService object {
    public function getEndpoint() returns WebSocketClient {
        WebSocketClient ep = new;
        return ep;
    }
};
