package ballerina.jms;

public type Session object {
    public {
        SessionConnector connector;
        SessionConfiguration config;
    }

    public new (SessionConfiguration config) {
        self.config = config;
        Connection connection = config.connection;
        self.initEndpoint(connection);
    }

    public native function initEndpoint(Connection);

    public native function createTextMessage (string content) returns (Message);
}

public type SessionConfiguration {
    Connection connection;
    string acknowledgementMode = "AUTO_ACKNOWLEDGE";
}
public type SessionConnector object {
}