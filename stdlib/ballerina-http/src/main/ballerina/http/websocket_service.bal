package ballerina.http;

public type WebSocketService object {
    public function getEndpoint() returns WebSocketEndpoint {
        WebSocketEndpoint ep = new;
        return ep;
    }
};
