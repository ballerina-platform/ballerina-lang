package ballerina.jms;

public type Session object {
    public {
        SessionConfiguration config;
    }

    public new (Connection connection, SessionConfiguration config) {
        self.config = config;
        self.initEndpoint(connection);
    }

    public native function initEndpoint(Connection connection);

    public native function createTextMessage (string content) returns (Message);

    public native function unsubscribe (string subscriptionId) returns (Error | ());
};

public type SessionConfiguration {
    string acknowledgementMode = "AUTO_ACKNOWLEDGE";
};
