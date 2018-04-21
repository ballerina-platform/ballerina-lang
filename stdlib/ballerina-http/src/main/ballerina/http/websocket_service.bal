
public type WebSocketService object {
    public function getEndpoint() returns WebSocketListener {
        WebSocketListener ep = new;
        return ep;
    }
};
