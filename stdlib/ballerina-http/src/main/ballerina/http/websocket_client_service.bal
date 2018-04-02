package ballerina.http;

public struct WebSocketClientService {
}

function <WebSocketClientService s> getEndpoint() returns WebSocketClient {
    WebSocketClient ep = {};
    return ep;
}
