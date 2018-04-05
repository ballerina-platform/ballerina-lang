package ballerina.jms;

public type Session object {
    public {
        SessionConnector connector;
        SessionConfiguration config;
    }

    new () {
        session.connector = new ();
    }

    public function init(SessionConfiguration config) {
        self.config = config;
        ConnectionConnector connectionConnector = config.connection.getClient();
        self.initEndpoint(connectionConnector);
    }

    public native function initEndpoint(ConnectionConnector connector);

    public function register (typedesc serviceType) {
    }

    public function start () {
    }

    public function getClient () returns (SessionConnector) {
        return self.connector;
    }

    public function stop () {
    }

    public native function createTextMessage (string content) returns (Message);
}

type SessionConfiguration {
    Connection connection;
    string acknowledgementMode = "AUTO_ACKNOWLEDGE";
}
public type SessionConnector object {
}